/* Platform Trace Server - data to message source file
 **
 ** Copyright (C) Intel 2012
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **     http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 **
 */

#include <arpa/inet.h>
#include <string.h>

#include "errors.h"
#include "logs.h"
#include "client_cnx.h"
#include "msg_to_data.h"

static void deserialize_uint32(char **buffer, uint32_t *value);

/**
 * deserialize uint32 data from buffer
 *
 * @param [in,out] buffer data received. the address is shifted of read size
 * @param [out] value data extracted
 *
 * @return none
 */
static void deserialize_uint32(char **buffer, uint32_t *value)
{
    memcpy(value, *buffer, sizeof(uint32_t));
    *value = ntohl(*value);
    *buffer += sizeof(uint32_t);
}

/**
 * read data from cnx and extract header
 *
 * @param [in] fd cnx file descriptor
 * @param [out] hdr message header
 *
 * @return E_PTS_ERR_BAD_PARAMETER if hdr is NULL
 * @return E_PTS_ERR_SUCCESS if successful
 * @return E_PTS_ERR_DISCONNECTED if client is disconnected
 * @return E_PTS_ERR_FAILED otherwise
 */
e_pts_errors_t get_header(int fd, msg_hdr_t *hdr)
{
    e_pts_errors_t ret = E_PTS_ERR_FAILED;
    char buffer[SIZE_HEADER];
    size_t len = SIZE_HEADER;
    char *p = buffer;

    CHECK_PARAM(hdr, ret, out);

    if ((ret = read_cnx(fd, buffer, &len)) != E_PTS_ERR_SUCCESS)
        goto out;

    if (len == 0) {
        ret = E_PTS_ERR_DISCONNECTED;
        LOG_DEBUG("client disconnected");
        goto out;
    } else if (len < SIZE_HEADER) {
        ret = E_PTS_ERR_FAILED;
        LOG_ERROR("Invalid message. Header is missing");
        goto out;
    }

    /* extract request id */
    deserialize_uint32(&p, &hdr->id);

    /* extract timestamp */
    deserialize_uint32(&p, &hdr->ts);

    /* extract data len */
    deserialize_uint32(&p, &hdr->len);

    if (hdr->len < (len - SIZE_HEADER)) {
        LOG_ERROR("Invalid message. Bad buffer len");
        goto out;
    } else {
        ret = E_PTS_ERR_SUCCESS;
    }

    out:
    return ret;
}

/**
 * set client structure for empty messages
 *
 * @param [in] event data to send to client
 * @param [out] msg data to send
 *
 * @return E_PTS_ERR_BAD_PARAMETER if event or/and msg is/are invalid
 * @return E_PTS_ERR_SUCCESS if successful
 * @return E_PTS_ERR_FAILED otherwise
 */
e_pts_errors_t set_data_empty(msg_t *msg, pts_cli_event_t *event)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;

    CHECK_PARAM(msg, ret, out);
    CHECK_PARAM(event, ret, out);

    event->data = NULL;
    event->len = 0;

    out:
    return ret;
}

/**
 * free client structure for message empty data message
 *
 * @param [in] event unused param
 *
 * @return E_PTS_ERR_BAD_PARAMETER if event or/and msg is/are invalid
 * @return E_PTS_ERR_SUCCESS if successful
 * @return E_PTS_ERR_FAILED otherwise
 */
e_pts_errors_t free_data_empty(pts_cli_event_t *event)
{
    (void)event;                /* unused */
    return E_PTS_ERR_SUCCESS;
}
