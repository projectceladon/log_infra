/* Platform Trace Server - recovered client traces source file
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

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <pthread.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <termios.h>
#include <poll.h>

#include "errors.h"
#include "logs.h"
#include "pts_def.h"
#include "client.h"
#include "events_manager.h"
#include "client_trace.h"

/* Length of the full PATH in a file expressed in bytes */
#define PATH_LEN 256

/* FALLOC_FL_KEEP_SIZE is not exported by bionic yet, so defining it here until bionic exports it */
#define FALLOC_FL_KEEP_SIZE 0x01

static void * client_trace_thread(client_t *client);
static e_pts_errors_t create_client_trace_thread(client_t *client);
static e_pts_errors_t route_trace_fs(client_t *client);
static void init_file(client_t *client);
static int rotateFile(client_t *client, int fs_fd);
static char * get_names(client_t *client);
static int init_new_file(char *p_input, client_t *client);
static void log_traces(client_t *client);
static e_pts_errors_t route_trace_ld(client_t *client);
static int get_ldisc_id(const char *ldisc_name);

e_pts_errors_t trace_manager(pts_data_t *pts) {

    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;
    int i;
    bool state = true;

    CHECK_PARAM(pts, ret, out);

    if (pts->clients.connected > 0) {
        /* At least one client is connected */
        /* Process client list to check if thread needs to be created */
        for (i = 0; i < pts->clients.list_size; i++) {
            /* Check if client is registered */
            ret = is_registered(&pts->clients.list[i], &state);
            if ((ret == E_PTS_ERR_SUCCESS) &&
                    (state == true) &&
                    !(pts->clients.list[i].cnx & E_CNX_TRACE_READY)) {
                /* Client has sent its trace parameters but thread is not yet created */
                ret = create_client_trace_thread(&pts->clients.list[i]);
                if (ret == E_PTS_ERR_FAILED) {
                    /* Disconnect the client */
                    ret = remove_client(&pts->clients, &pts->clients.list[i]);
                }
            }
        }
    }

    out:
    return ret;
}

static e_pts_errors_t create_client_trace_thread(client_t *client)
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;
    int err;

    CHECK_PARAM(client, ret, out);

    /* Pipe creation for communication with route_trace thread */
    if (pipe(client->fd_pipe) < 0) {
        LOG_ERROR("(client=%s) failed to create pipe (%s)", client->name, strerror(errno));
        ret = E_PTS_ERR_FAILED;
    } else {
        /* Create thread for polling client traces */
        err = pthread_create(&client->thr_id, NULL, (void *)client_trace_thread, client);
        if (err != 0) {
            LOG_ERROR("(fd=%d client=%s) failed to create the router trace thread. "
                    "Disconnect the client", client->fd,
                    client->name);

            ret = E_PTS_ERR_FAILED;
        } else {
            /* Thread creation succeed */
            client->cnx |= E_CNX_TRACE_READY;
        }
    }

    out:
    return ret;
}

static void * client_trace_thread(client_t *client) {

    e_err_pts_cli_t ret = E_ERR_PTS_CLI_FAILED;

    CHECK_PARAM(client, ret, out);

    switch (client->trace_mode) {
    case E_TRACE_MODE_FILE_SYSTEM:
        /* Traces routed to File System */
        ret = route_trace_fs(client);
        break;

    case E_TRACE_MODE_LINE_DISCS:
        /* Traces routed via line disciplines */
        ret = route_trace_ld(client);
        break;

    default:
        /* Unknown trace mode
         * We should never go here as trace mode parameter is checked
         * before client connection phase (pts_cli_set_trace_parameters)
         */
        break;
    }

    out:
    pthread_exit(&ret);

    return NULL;
}

static e_pts_errors_t route_trace_fs(client_t *client) {

    e_err_pts_cli_t ret = E_ERR_PTS_CLI_SUCCEED;

    CHECK_PARAM(client, ret, out);

    /* Initialize output file */
    init_file(client);
    /* Polling client traces */
    log_traces(client);

    out:
    return ret;
}

static e_pts_errors_t route_trace_ld(client_t *client) {

    e_err_pts_cli_t ret = E_ERR_PTS_CLI_SUCCEED;
    fd_set rfds;
    int select_result;
    char buffer[10];
    int data_size = 10;
    ssize_t retsize;
    int fs_router = -1;
    int fs_sink = -1;
    int ldisc_id_sink, ldisc_id_router;
    int retio;

    CHECK_PARAM(client, ret, out);

    /* Inform client trace are ready to be polled */
    ret = inform_client(client, E_PTS_EVENT_TRACE_READY, NULL);

    /* Starting thread loop */
    for(;;) {

        FD_ZERO(&rfds);
        if (client->fd_pipe[0] > 0)
            FD_SET(client->fd_pipe[0], &rfds);

        /* Block thread until reception of request REQUEST_TRACE_START/STOP
         * or incoming trace message
         */
        select_result = select(client->fd_pipe[0] + 1, &rfds, NULL, NULL, NULL);
        if ((select_result == -1) && (errno == EINTR)) {
            LOG_ERROR("(fd=%d client=%s) erreur select: EINTR (%s)", client->fd,
                    client->name, strerror(errno));
            continue;
        } else if (select_result == -1) {
            LOG_ERROR("(fd=%d client=%s) erreur select (%d: %s)", client->fd,
                    client->name, errno, strerror(errno));
            break;
        } else {

            /* REQUEST_TRACE_START/STOP reception */
            if (FD_ISSET(client->fd_pipe[0], &rfds)) {
                memset((void *)&buffer, 0, sizeof(buffer));
                retsize = read(client->fd_pipe[0], buffer, data_size);
                if (retsize == 1) {
                    switch (buffer[0]) {
                    case '1':
                        /* REQUEST_TRACE_START: trace polling activation */
                        LOG_INFO("(fd=%d client=%s) starting thread", client->fd, client->name);

                        LOG_INFO("(fd=%d client=%s) opening devices", client->fd, client->name);
                        fs_router = open(client->input_path, O_RDWR);
                        if (fs_router < 0) {
                            LOG_ERROR("(fd=%d client=%s) erreur open %s (%d: %s)",
                                    client->fd, client->name,
                                    client->input_path, errno, strerror(errno));
                            goto out;
                        }

                        fs_sink = open(client->output_path, O_RDWR);
                        if (fs_sink < 0) {
                            LOG_ERROR("(fd=%d client=%s) erreur open %s (%d: %s)",
                                    client->fd, client->name,
                                    client->output_path, errno, strerror(errno));
                            close(fs_router);
                            goto out;
                        }

                        /* Get lines disciplines Id */
                        LOG_INFO("(fd=%d client=%s) getting ldiscs", client->fd, client->name);
                        ldisc_id_sink = get_ldisc_id("n_tracesink");
                        if (ldisc_id_sink == -1) {
                            LOG_ERROR("(fd=%d client=%s) getting sink ldisc id (%d: %s)",
                                    client->fd, client->name, errno, strerror(errno));
                            close(fs_sink);
                            close(fs_router);
                            goto out;
                        }

                        ldisc_id_router = get_ldisc_id("n_tracerouter");
                        if (ldisc_id_sink == -1) {
                            LOG_ERROR("(fd=%d client=%s) getting router ldisc id (%d: %s)",
                                    client->fd, client->name, errno, strerror(errno));
                            close(fs_sink);
                            close(fs_router);
                            goto out;
                        }

                        /* Set lines disciplines to the devices */
                        LOG_INFO("(fd=%d client=%s) setting ldiscs", client->fd, client->name);
                        retio = ioctl(fs_router, TIOCSETD, &ldisc_id_router);
                        if (retio < 0) {
                            LOG_ERROR("(fd=%d client=%s) setting router ldisc (%d: %s)",
                                    client->fd, client->name, errno, strerror(errno));
                            close(fs_sink);
                            close(fs_router);
                            goto out;
                        }

                        retio = ioctl(fs_sink, TIOCSETD, &ldisc_id_sink);
                        if (retio < 0) {
                            LOG_ERROR("(fd=%d client=%s) setting sink ldisc (%d: %s)",
                                    client->fd, client->name, errno, strerror(errno));
                            /* Deconfigure ldisc */
                            ioctl(fs_router, TIOCSETD, (int[]) {0});
                            close(fs_sink);
                            close(fs_router);
                            goto out;
                        }
                        break;

                    case '0':
                        /* REQUEST_TRACE_STOP: trace polling inhibition */
                        LOG_INFO("(fd=%d client=%s) stopping thread",
                                client->fd, client->name);

                        /* Deconfigure ldisc */
                        LOG_INFO("(fd=%d client=%s) deconfiguring ldiscs",
                                client->fd, client->name);
                        ioctl(fs_sink, TIOCSETD, (int[]) {0});
                        ioctl(fs_router, TIOCSETD, (int[]) {0});
                        close(fs_sink);
                        close(fs_router);
                        break;

                    default:
                        LOG_ERROR("(fd=%d client=%s) unknown request", client->fd, client->name);
                        break;
                    }
                } else {
                    LOG_ERROR("(fd=%d client=%s) unknown request", client->fd, client->name);
                }
            }
        }
    }

    out:
    return ret;
}

static int get_ldisc_id(const char *ldisc_name) {

    int val;
    int ret = -1;
    char name[255] = { 0 };
    FILE *fldisc = fopen("/proc/tty/ldiscs", "r");

    if (fldisc == NULL) {
        goto out_noclose;
    }

    while (fscanf(fldisc, "%254s %d", name, &val) == 2) {
        /* early return if the ldisc_name is found */
        if (strncmp(ldisc_name, name, strlen(ldisc_name)) == 0) {
            ret = val;
            goto out;
        }
    }

    out:
    fclose(fldisc);
    out_noclose:
    return ret;
}

static void init_file(client_t *client) {

    int ret;
    struct stat st;

    client->outfd = -1;

    ret = stat(client->output_path, &st);
    if ((ret == 0) && (S_ISREG(st.st_mode))) {
        /* Logs exists so we rotate them so that we start with a fresh bplog */
        LOG_INFO("%s:%d file %s exists, rotating...", __FUNCTION__, __LINE__, client->output_path);
        rotateFile(client, client->outfd);

    } else if ((ret == 0) && (S_ISCHR(st.st_mode))) {
        /* We output on a character device, just disable file rotation */
        LOG_INFO("%s:%d file %s is a Character device, disabling file rotation...",
                __FUNCTION__, __LINE__, client->output_path);
        client->outfd = open(client->output_path, O_WRONLY);
        client->rotate_size = 0;

    } else if ((ret == -1) && (errno == ENOENT)) {
        /* Something in the path (a directory) does not exist or empty path */
        char *end_dir = strrchr(client->output_path, '/');
        /* Search for the filename */
        if (end_dir != NULL) {
            *end_dir = '\0';
            if ((stat(client->output_path, &st) == -1) && (errno == ENOENT)) {
                *end_dir = '/';
                /* Creates the missing directories */
                LOG_INFO("%s:%d Path for file %s does not exists",
                        __FUNCTION__, __LINE__, client->output_path);
                char path[PATH_LEN];
                char *p = path;
                strncpy (path, client->output_path, PATH_LEN - 1);
                path[strnlen(client->output_path, PATH_LEN)] = '\0';
                /* Skips first / to avoid trying to create nothing */
                if (p[0] == '/')
                    p++;
                while ((p = strchr (p, '/')) != NULL) {
                    *p = '\0';
                    ret = stat (path, &st);
                    if (ret != 0 && errno == ENOENT) {
                        ret = mkdir (path, 0755);
                        if (ret != 0) {
                            LOG_ERROR("%s:%d Can't create path %s : %s",
                                    __FUNCTION__,
                                    __LINE__,
                                    path,
                                    strerror(errno));
                            break;
                        }
                        LOG_INFO("%s:%d Path %s ... created", __FUNCTION__, __LINE__, path);
                    }
                    *p = '/';
                    p++;
                }
            }
            *end_dir = '/';
        }

        /* Creation of output trace file */
        init_new_file(client->output_path, client);
        if (client->outfd < 0) {
            LOG_ERROR("%s:%d Couldn't open %s : %s",
                    __FUNCTION__, __LINE__, client->output_path, strerror(errno));
        } else {
            if ((client->rotate_num > 0) && (get_names(client) == NULL)) {
                LOG_ERROR("%s:%d can't init file list", __FUNCTION__, __LINE__);
            }
        }

    } else {
        LOG_ERROR("%s:%d Won't be able to log on %s : %s",
                __FUNCTION__, __LINE__, client->output_path, strerror(errno));
    }
}

static int rotateFile(client_t *client, int fs_fd) {

    int err;
    int i;
    char *fnames = get_names(client);

    if (client->output_path == NULL)
        goto out_err;
    if (fnames == NULL)
        goto out;

    /* close fs_fd in a thread to avoid waiting for a flush on filp_close */
    pthread_t thr_close;
    pthread_attr_t attr;
    pthread_attr_init (&attr);
    pthread_attr_setdetachstate (&attr, PTHREAD_CREATE_DETACHED);
    pthread_create (&thr_close, &attr, (void *) close, (void *) fs_fd);
    pthread_attr_destroy (&attr);

    for (i = client->rotate_num - 2; i >= 0; i--) {
        err = rename(&fnames[i * PATH_LEN], &fnames[(i + 1) * PATH_LEN]);
        LOG_DEBUG("%s:%d renaming log file %s to %s", __FUNCTION__,
                __LINE__,
                &fnames[i * PATH_LEN],
                &fnames[(i + 1) * PATH_LEN]);

        if (err < 0 && errno != ENOENT)
            LOG_ERROR("%s:%d \t error: %s", __FUNCTION__,
                    __LINE__,
                    strerror(errno));
    }

    out:
    return init_new_file(client->output_path, client);
    out_err:
    return -1;
}

static char * get_names(client_t *client) {

    char *fnames = NULL;
    uint32_t i;

    if (client->rotate_num != 0) {
        /* Initialize fnames and allocates .x files if they don't exists */
        fnames = malloc(client->rotate_num * (PATH_LEN + 1));
        if (fnames == NULL)
            return NULL;

        memset(fnames, 0, client->rotate_num * PATH_LEN + 1);
        strncpy(&fnames[0], client->output_path, PATH_LEN);
        close(init_new_file(&fnames[0], client));

        for (i = 1; i < client->rotate_num; i++) {
            snprintf(&fnames[i * PATH_LEN], PATH_LEN, "%s.%d.istp", client->output_path, i);

            LOG_DEBUG("%s:%d \t %s", __FUNCTION__, __LINE__, &fnames[i * PATH_LEN]);
            close (init_new_file(&fnames[i * PATH_LEN], client));
        }
    }

    return fnames;
}

static int init_new_file(char *p_input, client_t *client) {

    struct stat st;
    int ret;

    client->outfd = -1;

    ret = stat(p_input, &st);
    if ((ret == -1) && (errno == ENOENT)) {
        /* File does not exist so create it */

        if ((client->outfd = open(p_input, O_WRONLY | O_CREAT, S_IRUSR | S_IWUSR)) > 0) {
            LOG_DEBUG("%s:%d file %s is created", __FUNCTION__, __LINE__, p_input);

            /* File creation in write only mode (user can read and write) */
            /* Allocate and initialize to zero the disk space */
            if (fallocate(client->outfd, FALLOC_FL_KEEP_SIZE, 0, client->rotate_size) < 0)
                LOG_ERROR("%s:%d Error allocating %d KBytes: %s", __FUNCTION__,
                        __LINE__,
                        client->rotate_size,
                        strerror(errno));
        }
    } else {
        LOG_DEBUG("%s:%d file %s exists, skipping creation", __FUNCTION__, __LINE__, p_input);
        client->outfd = open(p_input, O_WRONLY);
    }

    return client->outfd;
}

static void log_traces(client_t *client) {

    fd_set rfds;
    int fd_input = -1;
    int fdmax = 0;
    char buffer[8192];
    int data_size = 8192;
    ssize_t retsize;
    int select_result;
    int activation = 0;
    ssize_t retwrite;
    uint32_t cnt_written = 0;

    /* Open input trace client file */
    fd_input = open(client->input_path, O_RDONLY | O_NONBLOCK);
    if (fd_input < 0) {
        LOG_ERROR("%s:%d Error opening input trace interface %s", __FUNCTION__,
                __LINE__,
                client->input_path);
        goto out;
    }

    /* Open output trace file */
    client->outfd = open(client->output_path, O_WRONLY);
    if (client->outfd < 0) {
        LOG_ERROR("%s:%d Error opening trace output %s", __FUNCTION__,
                __LINE__,
                client->output_path);
        goto out;
    }

    /* Inform client trace are ready to be polled */
    if (inform_client(client, E_PTS_EVENT_TRACE_READY, NULL) != E_PTS_ERR_SUCCESS)
        goto out;

    /* Starting thread loop */
    for(;;) {

        FD_ZERO(&rfds);
        if (client->fd_pipe[0] > 0) {
            FD_SET(client->fd_pipe[0], &rfds);
            if (client->fd_pipe[0] > fdmax)
                fdmax = client->fd_pipe[0];
        }

        if ((fd_input > 0) && (activation == 1)) {
            FD_SET(fd_input, &rfds);
            if (fd_input > fdmax)
                fdmax = fd_input;
        }

        /* Block thread until reception of request REQUEST_TRACE_START/STOP
         * or incoming trace message
         */
        select_result = select(fdmax + 1, &rfds, NULL, NULL, NULL);
        if ((select_result == -1) && (errno == EINTR)) {
            LOG_ERROR("(fd=%d client=%s) error select: EINTR (%s)",
                    client->fd, client->name, strerror(errno));
            continue;
        } else if (select_result == -1) {
            LOG_ERROR("(fd=%d client=%s) error select (%d: %s)",
                    client->fd, client->name, errno, strerror(errno));
            break;
        } else {

            /* REQUEST_TRACE_START/STOP reception */
            if (FD_ISSET(client->fd_pipe[0], &rfds)) {
                memset((void *)&buffer, 0, sizeof(buffer));
                retsize = read(client->fd_pipe[0], buffer, data_size);
                if (retsize == 1) {
                    switch (buffer[0]) {
                    case '1':
                        /* REQUEST_TRACE_START: trace polling activation */
                        LOG_INFO("(fd=%d client=%s) starting thread", client->fd, client->name);
                        activation = 1;
                        break;

                    case '0':
                        /* REQUEST_TRACE_STOP: trace polling inhibition */
                        LOG_INFO("(fd=%d client=%s) stopping thread", client->fd, client->name);
                        activation = 0;
                        break;

                    case 'k':
                        /* Exit thread */
                        goto out;
                        break;

                    default:
                        LOG_ERROR("(fd=%d client=%s) unknown request", client->fd, client->name);
                        activation = 0;
                        break;
                    }
                } else {
                    LOG_ERROR("(fd=%d client=%s) unknown request", client->fd, client->name);
                }
            }

            /* Trace reception */
            if (FD_ISSET(fd_input, &rfds)) {
                memset((void *)&buffer, 0, sizeof(buffer));
                retsize = read(fd_input, buffer, data_size);
                if (retsize > 0) {

                    /* Write trace on File System output file */
                    retwrite = write(client->outfd, buffer, retsize);
                    if (retwrite < 0) {
                        LOG_ERROR("(fd=%d client=%s outfd=%d) error write (%s)",
                                client->fd, client->name, client->outfd, strerror(errno));
                    }

                    cnt_written += retsize;
                    if ((client->rotate_size > 0) && (cnt_written >= client->rotate_size)) {
                        rotateFile(client, client->outfd);
                        cnt_written = 0;
                        LOG_INFO("(fd=%d client=%s) Logs rotated", client->fd, client->name);
                    }

                } else if (retsize == 0) {
                    LOG_ERROR("(fd=%d client=%s) readsize: 0 (EOF)", client->fd, client->name);

                } else {
                    LOG_ERROR("(fd=%d client=%s) error read %d (%d: %s)",
                            client->fd, client->name, retsize, errno, strerror(errno));
                    break;
                }
            }
        }
    }

    out:
    if (client->outfd != -1)
        close(client->outfd);
    if (fd_input != -1)
        close(fd_input);
    LOG_INFO("End of log traces");
}
