/* Copyright (C) Intel 2013
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @file logreader.c
 * @brief logreader.c is the core file of liblogreader library.
 */

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <cutils/logd.h>
#include <cutils/logger.h>
#include <cutils/logprint.h>
#include <stdbool.h>

#include "log.h"
#include "logreader.h"
#include "send_event.h"

#define LOG_EVENTS_DEV_PATH "/dev/log/events"

/**
 * @brief Event Tag Map reference.
 *
 * Used to map an event tag as int to a name.
 */
static EventTagMap* g_eventTagMap = NULL;

/**
 * A logger entry
 *
 * @var buf represent a raw entry read from logger driver
 * @var entry read from logger driver in logger_entry form
 * @var next logger entry in the device (log_device_t)
 */
struct queued_entry_t {
    union {
        unsigned char buf[LOGGER_ENTRY_MAX_LEN + 1] __attribute__((aligned(4)));
        struct logger_entry entry __attribute__((aligned(4)));
    };
    struct queued_entry_t* next;
};

static struct queued_entry_t* queued_entry_t_new()
{
    struct queued_entry_t* e = malloc(sizeof *e);
    if (e != NULL)
        e->next = NULL;
    return e;
}

static void queued_entry_t_delete(struct queued_entry_t* e)
{
    free(e);
}

/**
 * A logger device
 */
struct log_device_t {
    char* device; /**< device path name */
    int fd; /**< device file descriptor */
    struct queued_entry_t* queue; /**< the device entry queue */
};

static void skipNextEntry(struct log_device_t* dev)
{
    struct queued_entry_t* entry = dev->queue;
    if (entry != NULL) {
        dev->queue = entry->next;
        queued_entry_t_delete(entry);
    }
}

/**
 * Create a new logger device from its path
 */
static struct log_device_t* log_device_t_new(char* d)
{
    struct log_device_t* dev = malloc(sizeof *dev);
    if (dev == NULL)
        return dev;
    dev->device = d;
    dev->fd = -1;
    dev->queue = NULL;
    return dev;
}

static void log_device_t_delete(struct log_device_t* dev)
{
    if (dev->fd > 0) {
        close(dev->fd);
        dev->fd = -1;
    }
    while(dev->queue != NULL)
        skipNextEntry(dev);
    free(dev);
}

static int queued_entry_t_cmp(struct queued_entry_t* a, struct queued_entry_t* b)
{
    int n = a->entry.sec - b->entry.sec;
    if (n != 0) {
        return n;
    }
    return a->entry.nsec - b->entry.nsec;
}

static void log_device_t_enqueue(struct log_device_t* dev, struct queued_entry_t* entry)
{
    LOG_V("enqueue entry: %08x, sec %d", (unsigned int)entry, entry->entry.sec);
    if (dev->queue == NULL) {
        dev->queue = entry;
    } else {
        struct queued_entry_t** e = &dev->queue;
        while (*e && queued_entry_t_cmp(entry, *e) >= 0) {
            e = &((*e)->next);
        }
        entry->next = *e;
        *e = entry;
    }
}

static bool isInEventTagRange(EventTagRange range, unsigned int tag)
{
    if ((tag >= range.rangeStart) && (tag <= range.rangeEnd))
        return true;
    else
        return false;
}

/*
 * Extract a 4-byte value from a byte stream.
 */
static inline uint32_t get4LE(const uint8_t* src)
{
    return src[0] | (src[1] << 8) | (src[2] << 16) | (src[3] << 24);
}

static EventTagRange* getEntryRange(EventTagRange ranges[], struct logger_entry* entry)
{
    int i;
    const unsigned char* eventData;
    unsigned int tag;

    eventData = (const unsigned char*) entry->msg;
    tag = get4LE(eventData);
    for (i = 0; ranges[i].group; i++)
        if (isInEventTagRange(ranges[i], tag))
            return &ranges[i];

    return NULL;
}

static unsigned int convertEventToAction(EventType event)
{
    if (event == EVENT_INFO)
        return I_ACTION_INFO;
    if (event == EVENT_ERROR)
        return I_ACTION_ERROR;
    return I_ACTION_ERROR;
}

static void processEntry(EventTagRange ranges[], struct logger_entry* lEntry)
{
    EventTagRange* entryRange;
    int err;
    unsigned int action;
    AndroidLogEntry entry;
    // TODO stack vs heap (malloc)
    // binaryMsgBuf size should be close to LOGGER_ENTRY_MAX_LEN (5x1024)
    // use malloc if entry is not discarded to not allocate memory for nothing
    // profiling/analysis should be done to determine what to do
    // current size come from logcat.cpp
    char binaryMsgBuf[1024];

    entryRange = getEntryRange(ranges, lEntry);

    // Discard entry if not in range map
    if (entryRange == NULL)
        return;

    err = android_log_processBinaryLogBuffer(lEntry, &entry, g_eventTagMap,
                                             binaryMsgBuf, sizeof(binaryMsgBuf));
    if (err < 0) {
        LOG_E("Error processing log record");
        return;
    }

    action = convertEventToAction(entryRange->event);
    send_event(action, entryRange->group, entry.tag, entry.message, NULL, NULL, NULL, NULL, NULL);
}

static void processNextEntries(struct log_device_t* dev, EventTagRange ranges[])
{
    while (dev->queue != NULL) {
        processEntry(ranges, &dev->queue->entry);
        skipNextEntry(dev);
    }
}

static void readLogLines(struct log_device_t* dev)
{
    struct queued_entry_t* entry;
    int ret = 0, error = 0;
    LOG_V("entry <");

    while (!error) {
        entry = queued_entry_t_new();
        if (entry == NULL) {
            LOG_E("queued_entry_t_new malloc failed");
            error = 1;
            break;
        }

        ret = read(dev->fd, entry->buf, LOGGER_ENTRY_MAX_LEN);
        LOG_V("read");
        if (ret < 0) {
            if (errno == EINTR || errno == EAGAIN) {
                // EAGAIN : no more entry to read
                LOG_V("EINTR | EAGAIN");
                error = 1;
                break;
            }
            LOG_E("Read error %s", strerror(errno));
            error = 1;
            break;
        } else if (!ret) {
            LOG_W("Unexpected EOF!");
            error = 1;
            break;
        } else if (entry->entry.len != ret - sizeof(struct logger_entry)) {
            LOG_E("Unexpected length. Expected %d, got %d",
                 entry->entry.len, ret - sizeof(struct logger_entry));
            error = 1;
            break;
        }

        entry->entry.msg[entry->entry.len] = '\0';
        log_device_t_enqueue(dev, entry);

    }

    if (error) {
        queued_entry_t_delete(entry);
    }
    LOG_V("out >");
}

/**
 * Events logger device reference
 */
static struct log_device_t* events_dev = NULL;

void logreader_init()
{
    LOG_I("Init...");
    if (events_dev != NULL)
        log_device_t_delete(events_dev);
    events_dev = log_device_t_new(LOG_EVENTS_DEV_PATH);
    if (events_dev == NULL) {
        LOG_E("log_device_t_new malloc failed '%s'\nInit...Failed",
              LOG_EVENTS_DEV_PATH);
        return;
    }
    events_dev->fd = open(events_dev->device, O_RDONLY|O_NONBLOCK);
    if (events_dev->fd < 0) {
        LOG_E("Unable to open log device '%s': %s\nInit...Failed",
             events_dev->device, strerror(errno));
        return;
    }
    g_eventTagMap = android_openEventTagMap(EVENT_TAG_MAP_FILE);
    LOG_I("Init...OK");
}

void logreader_exit()
{
    LOG_I("Close...");
    if (events_dev != NULL)
        log_device_t_delete(events_dev);
    LOG_I("Close...OK");
}

int logreader_get_fd()
{
    if (events_dev != NULL)
        return events_dev->fd;
    else
        return -1;
}

void logreader_handle()
{
    if (events_dev != NULL) {
        readLogLines(events_dev);
        processNextEntries(events_dev, g_eventTagRangeMap);
    }
}
