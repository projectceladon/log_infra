/* Platform Trace Server - cnx source file
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
#include <cutils/sockets.h>
#include <sys/socket.h>
#include <unistd.h>

#include "errors.h"
#include "logs.h"
#include "pts.h"
#include "client_cnx.h"


#define DEFAULT_BACKLOG 5

/**
 * open PTS cnx
 *
 * @param [out] fd cnx file descriptor
 *
 * @return E_PTS_ERR_SUCCESS if successful
 * @return E_PTS_ERR_BAD_PARAMETER if fd is NULL
 * @return E_PTS_ERR_FAILED otherwise
 */
e_pts_errors_t open_cnx(int *fd)
{
    e_pts_errors_t ret = E_PTS_ERR_FAILED;

    CHECK_PARAM(fd, ret, out);

    LOG_DEBUG("configure socket: %s", PTS_SOCKET_NAME);
    *fd = android_get_control_socket(PTS_SOCKET_NAME);

    if (listen(*fd, DEFAULT_BACKLOG) < 0) {
        LOG_ERROR("listen failed (%s)", strerror(errno));
        goto out;
    }
    ret = E_PTS_ERR_SUCCESS;

    out:
    return ret;
}

/**
 * accept PTS cnx connection
 *
 * @param [in] fd cnx file descriptor
 *
 * @return file descriptor
 */
int accept_cnx(int fd)
{
    return accept(fd, NULL, NULL);
}

/**
 * read data from PTS cnx
 *
 * @param [in] fd cnx file descriptor
 * @param [out] data output buffer
 * @param [in,out] len size of data. the value returned is the read size
 *
 * @return E_PTS_ERR_BAD_PARAMETER if data and/or len are NULL
 * @return E_PTS_ERR_FAILED read fails
 * @return E_PTS_ERR_SUCCESS if successful
 */
e_pts_errors_t read_cnx(int fd, void *data, size_t *len)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;
    int err;

    CHECK_PARAM(data, ret, out);
    CHECK_PARAM(len, ret, out);

    memset(data, 0, *len);
    err = recv(fd, data, *len, 0);
    if (err < 0) {
        LOG_ERROR("read fails (%s)", strerror(errno));
        ret = E_PTS_ERR_FAILED;
    } else
        *len = err;

    out:
    return ret;
}

/**
 * write data to PTS cnx
 *
 * @param [in] fd cnx file descriptor
 * @param [in] data data to write
 * @param [in] len data length
 *
 * @return E_PTS_ERR_BAD_PARAMETER if data and/or len is NULL
 * @return E_PTS_ERR_FAILED send fails
 * @return E_PTS_ERR_SUCCESS if successful
 */
e_pts_errors_t write_cnx(int fd, void *data, size_t *len)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;
    int err;

    CHECK_PARAM(data, ret, out);
    CHECK_PARAM(len, ret, out);

    err = send(fd, data, *len, MSG_NOSIGNAL);
    if (err < 0) {
        LOG_ERROR("send fails (%s)", strerror(errno));
        ret = E_PTS_ERR_FAILED;
    }

    out:
    return ret;
}

/**
 * close PTS cnx
 *
 * @param [in,out] fd cnx file descriptor
 *
 * @return E_PTS_ERR_BAD_PARAMETER if fd is NULL
 * @return E_PTS_ERR_SUCCESS if successful
 * @return E_PTS_ERR_FAILED otherwise
 */
e_pts_errors_t close_cnx(int *fd)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;

    CHECK_PARAM(fd, ret, out);

    shutdown(*fd, SHUT_RDWR);
    if (close(*fd) < 0) {
        LOG_ERROR("(fd=%d) reason: (%s)", *fd, strerror(errno));
        ret = E_PTS_ERR_FAILED;
    }
    *fd = CLOSED_FD;

    out:
    return ret;
}
