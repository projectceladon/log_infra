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
#include "data_to_msg.h"

/**
 * serialize uint32_t data to buffer
 *
 * @param [out] buffer output buffer
 * @param [in] value size_t value to serialize
 *
 * @return none
 */
static void serialize_uint32(char **buffer, uint32_t value)
{
    value = htonl(value);
    memcpy(*buffer, &value, sizeof(uint32_t));
    *buffer += sizeof(uint32_t);
}

/**
 * set header
 *
 * @param [in,out] msg received message
 *
 * @return E_PTS_ERR_BAD_PARAMETER if msg is NULL
 * @return E_PTS_ERR_SUCCESS if successful
 */
static e_pts_errors_t set_header(msg_t *msg)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;
    struct timeval ts;
    uint32_t tmp;
    char *msg_data = msg->data;

    CHECK_PARAM(msg, ret, out);

    /* setting id */
    serialize_uint32(&msg_data, msg->hdr.id);

    /* setting timestamp */
    gettimeofday(&ts, NULL);
    memcpy(&tmp, &ts.tv_sec, sizeof(ts.tv_sec));
    serialize_uint32(&msg_data, tmp);

    /* setting size */
    serialize_uint32(&msg_data, msg->hdr.len);

    out:
    return ret;
}

/**
 * free message data
 *
 * @param [in] msg data to send
 *
 * @return E_PTS_ERR_BAD_PARAMETER if request or/and msg is/are invalid
 * @return E_PTS_ERR_SUCCESS if successful
 * @return E_PTS_ERR_FAILED otherwise
 */
e_pts_errors_t delete_msg(msg_t *msg)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;

    CHECK_PARAM(msg, ret, out);

    if (msg->data != NULL)
        free(msg->data);
    else
        ret = E_PTS_ERR_FAILED;

    out:
    return ret;
}

/**
 * handle message allocation memory and set message header
 *
 * @private
 *
 * @param [in,out] msg data to send
 * @param [in] msg_data data to send
 * @param [in] id message id
 * @param [in] size data size
 *
 * @return E_PTS_ERR_BAD_PARAMETER if request or/and msg is/are invalid
 * @return E_PTS_ERR_SUCCESS if successful
 * @return E_PTS_ERR_FAILED otherwise
 */
static e_pts_errors_t prepare_msg(msg_t *msg, char **msg_data,
        e_pts_events_t id, size_t *size)
{
    e_pts_errors_t ret = E_PTS_ERR_FAILED;
    size_t len;

    CHECK_PARAM(msg, ret, out);
    CHECK_PARAM(msg_data, ret, out);

    len = SIZE_HEADER + *size;
    msg->data = calloc(len, sizeof(char));
    if (msg->data == NULL) {
        LOG_ERROR("memory allocation fails");
        goto out;
    }
    memcpy(&msg->hdr.id, &id, sizeof(id));
    memcpy(&msg->hdr.len, size, sizeof(size_t));
    ret = set_header(msg);
    *size = len;
    *msg_data = msg->data + SIZE_HEADER;

    out:
    return ret;
}

/**
 * handle SET_NAME message allocation
 *
 * @param [out] msg data to send
 * @param [in] request request to send
 *
 * @return E_PTS_ERR_BAD_PARAMETER if request or/and msg is/are invalid
 * @return E_PTS_ERR_SUCCESS if successful
 * @return E_PTS_ERR_FAILED otherwise
 */
e_pts_errors_t set_msg_name(msg_t *msg, pts_cli_event_t *request)
{
    e_pts_errors_t ret = E_PTS_ERR_FAILED;
    size_t size;
    char *msg_data = NULL;

    CHECK_PARAM(msg, ret, out);
    CHECK_PARAM(request, ret, out);

    if (request->len <= 0) {
        LOG_ERROR("name is empty");
        goto out;
    }

    /* msg.hdr is used to store the string length */
    size = request->len;
    ret = prepare_msg(msg, &msg_data, request->id, &size);
    if (ret != E_PTS_ERR_SUCCESS)
        goto out;

    /* set name */
    memcpy(msg->data + SIZE_HEADER, request->data, sizeof(char) * request->len);
    ret = E_PTS_ERR_SUCCESS;

    out:
    return ret;
}

/**
 * handle SET_INPUT/OUTPUT_PATH_TRACE_CFG message allocation
 *
 * @param [out] msg data to send
 * @param [in] request request to send
 *
 * @return E_PTS_ERR_BAD_PARAMETER if request or/and msg is/are invalid
 * @return E_PTS_ERR_SUCCESS if successful
 * @return E_PTS_ERR_FAILED otherwise
 */
e_pts_errors_t set_msg_path_cfg(msg_t *msg, pts_cli_event_t *request)
{
    e_pts_errors_t ret = E_PTS_ERR_FAILED;
    size_t size;
    char *msg_data = NULL;

    CHECK_PARAM(msg, ret, out);
    CHECK_PARAM(request, ret, out);

    if (request->len <= 0) {
        LOG_ERROR("trace configuration is empty");
        goto out;
    }

    /* msg.hdr is used to store the string length */
    size = request->len;
    ret = prepare_msg(msg, &msg_data, request->id, &size);
    if (ret != E_PTS_ERR_SUCCESS)
        goto out;

    /* set configuration */
    memcpy(msg->data + SIZE_HEADER, request->data, sizeof(char) * request->len);
    ret = E_PTS_ERR_SUCCESS;

    out:
    return ret;
}

/**
 * handle SET_ROTATE_SIZE/NUM_TRACE_CFG message allocation
 *
 * @param [out] msg data to send
 * @param [in] request request to send
 *
 * @return E_PTS_ERR_BAD_PARAMETER if request or/and msg is/are invalid
 * @return E_PTS_ERR_SUCCESS if successful
 * @return E_PTS_ERR_FAILED otherwise
 */
e_pts_errors_t set_msg_rotate_cfg(msg_t *msg, pts_cli_event_t *request)
{
    e_pts_errors_t ret = E_PTS_ERR_FAILED;
    uint32_t tmp;
    size_t size;
    char *msg_data = NULL;

    CHECK_PARAM(msg, ret, out);
    CHECK_PARAM(request, ret, out);

    if (request->len <= 0) {
        LOG_ERROR("rotate parameter (size or num) is empty");
        goto out;
    }

    size = sizeof(uint32_t);
    ret = prepare_msg(msg, &msg_data, request->id, &size);
    if (ret != E_PTS_ERR_SUCCESS)
        goto out;

    /* set filter */
    memcpy(&tmp, request->data, sizeof(uint32_t));
    serialize_uint32(&msg_data, tmp);
    ret = E_PTS_ERR_SUCCESS;

    out:
    return ret;
}

/**
 * handle SET_TRACE_MODE message allocation
 *
 * @param [out] msg data to send
 * @param [in] request request to send
 *
 * @return E_PTS_ERR_BAD_PARAMETER if request or/and msg is/are invalid
 * @return E_PTS_ERR_SUCCESS if successful
 * @return E_PTS_ERR_FAILED otherwise
 */
e_pts_errors_t set_msg_trace_mode(msg_t *msg, pts_cli_event_t *request)
{
    e_pts_errors_t ret = E_PTS_ERR_FAILED;
    uint32_t tmp;
    size_t size;
    char *msg_data = NULL;

    CHECK_PARAM(msg, ret, out);
    CHECK_PARAM(request, ret, out);

    if (request->len <= 0) {
        LOG_ERROR("trace configuration is empty");
        goto out;
    }

    size = sizeof(uint32_t);
    ret = prepare_msg(msg, &msg_data, request->id, &size);
    if (ret != E_PTS_ERR_SUCCESS)
        goto out;

    /* set filter */
    memcpy(&tmp, request->data, sizeof(uint32_t));
    serialize_uint32(&msg_data, tmp);
    ret = E_PTS_ERR_SUCCESS;

    out:
    return ret;
}

/**
 * handle SET_EVENTS message allocation
 *
 * @param [out] msg data to send
 * @param [in] request request to send
 *
 * @return E_PTS_ERR_BAD_PARAMETER if request or/and msg is/are invalid
 * @return E_PTS_ERR_SUCCESS if successful
 * @return E_PTS_ERR_FAILED otherwise
 */
e_pts_errors_t set_msg_filter(msg_t *msg, pts_cli_event_t *request)
{
    e_pts_errors_t ret = E_PTS_ERR_FAILED;
    uint32_t tmp;
    size_t size;
    char *msg_data = NULL;

    CHECK_PARAM(msg, ret, out);
    CHECK_PARAM(request, ret, out);

    size = sizeof(uint32_t);
    ret = prepare_msg(msg, &msg_data, request->id, &size);
    if (ret != E_PTS_ERR_SUCCESS)
        goto out;

    /* set filter */
    memcpy(&tmp, request->data, sizeof(uint32_t));
    serialize_uint32(&msg_data, tmp);
    ret = E_PTS_ERR_SUCCESS;

    out:
    return ret;
}

/**
 * set buffer to send empty message
 *
 * @param [out] msg data to send
 * @param [in] request request to send
 *
 * @return E_PTS_ERR_BAD_PARAMETER if request or/and msg is/are invalid
 * @return E_PTS_ERR_SUCCESS if successful
 * @return E_PTS_ERR_FAILED otherwise
 */
e_pts_errors_t set_msg_empty(msg_t *msg, pts_cli_event_t *request)
{
    e_pts_errors_t ret = E_PTS_ERR_FAILED;
    size_t size;
    char *msg_data = NULL;

    CHECK_PARAM(msg, ret, out);
    CHECK_PARAM(request, ret, out);

    size = 0;
    ret = prepare_msg(msg, &msg_data, request->id, &size);
    if (ret != E_PTS_ERR_SUCCESS)
        goto out;

    ret = E_PTS_ERR_SUCCESS;

    out:
    return ret;
}
