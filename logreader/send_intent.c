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
 * @file send_intent.c
 * @brief send_intent provide an API to send broadcast intents
 */

#include <stdlib.h>
#include <string.h>

#include "log.h"
#include "send_intent.h"

#define INTENT_MAX_SIZE 1024
#define AM_BROADCAST_INTENT_START "am broadcast"
#define AM_BROADCAST_SEPARATOR " "
#define AM_BROADCAST_STRING_SEPARATOR "\""

#define AM_KEYWORD(symbol, nargs, command)      \
    [ AM_##symbol ] = { nargs, command, },

struct {
    unsigned char nargs;
    const char* cmd;
} am_elms[AM_COUNT] = {
    AM_KEYWORD(ACTION, 1, "-a")
    AM_KEYWORD(EXTRA_STRING, 2, "--es")
};
#undef AM_KEYWORD

#define am_nargs(am_e) (am_elms[am_e].nargs)
#define am_cmd(am_e) (am_elms[am_e].cmd)

struct intent_elm {
    unsigned int key;
    const char* value;
    struct intent_elm* next;
};

intent_t* new_intent_elm(unsigned int key, const char* value) {
    intent_t* i = malloc(sizeof *i);
    if (i == NULL)
        return i;
    i->key = key;
    i->value = value;
    i->next = NULL;
    return i;
}

intent_t* new_intent(unsigned int action) {
    return new_intent_elm(action, NULL);
}

void delete_intent(intent_t* intent) {
    if (intent != NULL) {
        if (intent->next != NULL)
            delete_intent(intent->next);
        free(intent);
    }
}

void intent_add_elm(intent_t* intent, intent_t* elm) {
    if (intent != NULL && elm != NULL) {
        if (intent->next != NULL)
            intent_add_elm(intent->next, elm);
        else
            intent->next = elm;
    }
}

void intent_put_extra(intent_t* i, unsigned int key, const char* value) {
    if (i != NULL) {
        intent_t* elm = new_intent_elm(key, value);
        if (elm != NULL)
            intent_add_elm(i, elm);
    }
}

void am_cmd_append(char* cmd, const char* src, int secure_string) {
    if ((INTENT_MAX_SIZE - strlen(cmd)) > (strlen(src) + 1)) {
        strcat(cmd, AM_BROADCAST_SEPARATOR);
        if (secure_string)
            strcat(cmd, AM_BROADCAST_STRING_SEPARATOR);
        strcat(cmd, src);
        if (secure_string)
            strcat(cmd, AM_BROADCAST_STRING_SEPARATOR);
    }
}

void send_intent(const struct intent_elements* elms, intent_t* intent) {
    char cmd[INTENT_MAX_SIZE] = AM_BROADCAST_INTENT_START;
    char tmp_cmd[INTENT_MAX_SIZE];
    intent_t* i;
    int ret;

    if (elms == NULL || intent == NULL) {
        LOG_E("NULL args elms:%08x intent:%08x",
              (unsigned int)elms, (unsigned int)intent);
        return;
    }

    i = intent;
    do {
        unsigned int am_type = ie_am_type(elms, i->key);
        tmp_cmd[0] = '\0';
        am_cmd_append(tmp_cmd, am_cmd(am_type), 0);
        if (am_nargs(am_type) >= 1)
            am_cmd_append(tmp_cmd, ie_data(elms, i->key), 0);
        if (am_nargs(am_type) >= 2 && i->value != NULL)
            am_cmd_append(tmp_cmd, i->value, 1);
        am_cmd_append(cmd, tmp_cmd, 0);
    } while (i->next != NULL, i = i->next);

    LOG_V("am cmd: %s", cmd);
    if (system(cmd) < 0)
        LOG_E("am cmd error: %s", cmd);
}
