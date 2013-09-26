/*
 * mts.c (Modem Trace Server):- Server application that routes
 *                              data coming from a tty location to
 *                              another location like a socket.
 *
 * ---------------------------------------------------------------------------
 * Copyright (c) 2011, Intel Corporation
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the Intel Corporation nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES,
 * (INCLUDING BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ---------------------------------------------------------------------------
 */

/*
 *This program is unit tested only for following use cases in TEST mode:
 *
 *  1) File System (-f) option
 *      - Used a bin file in place of ttyGSM18
 *      - program was able to read and generate same size log file
 *  2) RNDIS / Socket (-p) optind
 *      - Used a bin file in place of ttyGSM18
 *      - Program waits for usb0 interface to become available
 *      - Open socket
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include <getopt.h>
#include <sys/socket.h>
#include <net/if.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/ioctl.h>
#include <sys/poll.h>
#include <sys/stat.h>
#include <linux/tty.h>
#include <fcntl.h>
#include <termios.h>
#include <unistd.h>
#include <ctype.h>
#include <errno.h>
#include <signal.h>
#include <sys/types.h>
#include <cutils/sockets.h>
#include <cutils/log.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <cutils/properties.h>
#include <string.h>
#include <stdbool.h>
#include <sys/inotify.h>
#include <time.h>
#include "mmgr_cli.h"

/* For using PTS library */
#define USE_PTS

#ifdef USE_PTS
#include "pts_cli.h"
#endif

/* Usage text of mts displayed with -h toggle */
#define USAGE_TXT "Usage:\n" \
    "mts -i <input> -t <out_type> -o <output>" \
    "[-r <kbytes>] [-n <count>]]\n" \
    "\n" \
    "\t-i <input>      Input file, usually a tty (/dev/gsmtty18)\n" \
    "\t-t <out_type>   Output type, f for file, p for socket and k for PTI\n" \
    "\t-o <output>     Output target.\n" \
    "\t\t\t type p: port number\n" \
    "\t\t\t type k: tty supporting kernel routing\n" \
    "\t\t\t type f: file name\n" \
    "\t-r <kbytes>     Rotate log every kbytes (10MB if " \
    "unspecified). Requires type f\n" \
    "\t-n <count>      Sets max number of rotated logs to " \
    "<count>, default 4\n"

/* Log Size in KBytes : 10Megs */
#define DEFAULT_LOG_SIZE 10*(1<<10)
/* Number of Allowed file rotations will create bplog and bplog.{1-N} where N is DEFAULT_MAX_ROTATED_NR */
#define DEFAULT_MAX_ROTATED_NR 4
/* Check Interval used in parameters of sleep in various loops */
#define CHECK_INTERVAL 2
/* aplog TAG */
#undef LOG_TAG
#define LOG_TAG "BPLOG"
/* Interface name used when listening on a socket */
#define IFNAME "usb0"
/* Buffer size of receive function in logs */
#define MAXDATA 8192
/* Signal used to un-configure the ldiscs */
#define MY_SIG_UNCONF_PTI SIGUSR1
/* Inotify node to watch */
#define FILE_NAME_SZ 256
#define INOT_USB_DEV_NODE "/dev/bus/usb/001"
#define INOT_EVENT_QUEUE 512
#define INOT_EVENT_SZ sizeof(struct inotify_event)
#define INOT_BUF_SZ INOT_EVENT_QUEUE * (INOT_EVENT_SZ + FILE_NAME_SZ)
/* USB log port */
#define USB_LOG_DEV "/dev/ttyACM1"
#ifndef USE_PTS
/* Poll FDs */
#define POLL_FD_NBR 3
#define POLL_FD_LOG 0
#define POLL_FD_COM 1
#define POLL_FD_NOT 2
#else
#define POLL_FD_NBR 1
#define POLL_FD_COM 0
#define PTS_READY "PTS_RD\n"
#endif
#define MDM_UP "MDM_UP\n"
#define MDM_DW "MDM_DW\n"
#define MDM_MSG_SZ 7

#define ATTEMPTI(fun, predicate, msg, ret) do { \
    if ((*ret = fun)predicate) { \
        LOGI("%s:%d " msg " FAILED when calling %s : returned %d!", \
        __FUNCTION__, __LINE__, #fun, *ret); \
    }}while(0)

#define ATTEMPTE(fun, predicate, msg, ret, label) do { \
    if ((*ret = fun)predicate) { \
        LOGE("%s:%d " msg " FAILED when calling %s : returned %d!", \
        __FUNCTION__, __LINE__, #fun, *ret); \
        goto label; \
    }}while(0)

/* Length of the full PATH in a file expressed in bytes */
#define PATH_LEN 256

/* FALLOC_FL_KEEP_SIZE is not exported by bionic yet, so defin'ing it here until bionic exports it */
#define FALLOC_FL_KEEP_SIZE 0x01
#define PROP_HEAD "persist.service.mts."

#define MAX_WAIT_MMGR_CONNECT_SECONDS  5
#define MMGR_CONNECT_RETRY_TIME_MS     200

typedef struct args_s
{
    char *name;
    char *key;
    char *type_conv;
    void *storage;
} args;

typedef struct comm_mdm_s
{
    pthread_cond_t modem_online;
    pthread_mutex_t cond_mtx;
    int intercom[2];            /* 0 read - 1 write */
    bool modem_available;
    bool usb_logging;
    int ttyfd;
    int inotfd;
    int inotwd;
    char *p_input;
    mmgr_cli_handle_t *mmgr_hdl;
#ifdef USE_PTS
    pts_cli_handle_t *pts_hdl;
    pthread_mutex_t cond_pts_ready_mtx;
    bool pts_available;
#endif
} comm_mdm;



#ifdef USE_PTS
int pts_down(pts_cli_event_t * ev) {

    printf("HANDLER Event PTS_DOWN\n");
    return 0;
}

int pts_up(pts_cli_event_t * ev) {

    printf("HANDLER Event PTS_UP\n");
    return 0;
}

int pts_ready(pts_cli_event_t * ev) {

    comm_mdm *ctx = (comm_mdm *) ev->context;
    LOGD ("%s:%d Received PTS_READY", __FUNCTION__, __LINE__);

    if (pthread_mutex_lock (&ctx->cond_pts_ready_mtx) != 0)
    {
        LOGE ("%s:%d PTHREAD_COND_MUTEX Error taking mutex: %s. CRITICAL: PTHREAD_COND not sent!",
              __FUNCTION__, __LINE__, strerror (errno));
        goto out;
    }
    /* PTS up - start tracing */
    ctx->pts_available = true;
    if (write (ctx->intercom[1], PTS_READY, MDM_MSG_SZ * sizeof (char))
        != (MDM_MSG_SZ * sizeof (char)))
        LOGE ("%s:%d PTS event READY msg not properly sent. MTS may missbehave.",
             __FUNCTION__, __LINE__);

    if (pthread_mutex_unlock (&ctx->cond_pts_ready_mtx) != 0)
    {
        LOGE ("%s:%d PTHREAD_COND_MUTEX Error releasing mutex: %s.\
              CRITICAL: MTS not expected to work anymore. PTHREAD_COND not sent!",
              __FUNCTION__, __LINE__, strerror (errno));
        goto out;
    }

out:
    return 0;
}
#endif

#ifndef USE_PTS
static int
open_gsmtty (char *p_portname)
{
    struct termios t_termios;
    int tty_fd;
    int ret;
    LOGI ("%s:%d Opening %s", __FUNCTION__, __LINE__, p_portname);
    ATTEMPTE (open (p_portname, O_RDONLY | O_NONBLOCK), <0, "Opening tty",
              &tty_fd, out);
    /*
     * Setting up for tty raw mode, which is based
     * of what ttyIFX0 diagnostic apps developed.
     * I don't think we should quit in the event of an
     * error, but we should inform.
     */
    ATTEMPTI (tcgetattr (tty_fd, &t_termios), !=0, "getting attr's", &ret);
    if (ret == 0)
    {
        cfmakeraw (&t_termios);
        ATTEMPTI (tcsetattr (tty_fd, TCSANOW, &t_termios), !=0,
                  "setting attr's", &ret);
    }
    LOGI ("%s:%d %s opened", __FUNCTION__, __LINE__, p_portname);
out:
    return tty_fd;
}

static int
iface_up (char *ip_addr_if)
{
    int fd;
    int if_status = 0;
    struct ifreq ifr;

    fd = socket (AF_INET, SOCK_DGRAM, 0);
    int flags = fcntl (fd, F_GETFL, 0);
    if (fcntl (fd, F_SETFL, flags | O_NONBLOCK) != 0)
        LOGI ("%s:%d Couldn't set O_NONBLOCK on %s socket ... proceeding anyway",
              __FUNCTION__, __LINE__, IFNAME);

    memset (&ifr, 0, sizeof (struct ifreq));
    strncpy (ifr.ifr_name, IFNAME, sizeof (IFNAME));
    ifr.ifr_addr.sa_family = AF_INET;

    /* Read Flag for interface usb0, Bit#0 indicates Interface Up/Down */
    if (ioctl (fd, SIOCGIFFLAGS, &ifr) < 0)
        if_status = 0;
    /* if usb0 is up get its ip address */
    else if (ifr.ifr_flags & 0x01)
    {
        if_status = 1;
        ioctl (fd, SIOCGIFADDR, &ifr);
        strncpy (ip_addr_if,
                 (char
                  *)(inet_ntoa (((struct sockaddr_in *) &ifr.ifr_addr)->
                                sin_addr)), 16);
        LOGI ("%s:%d IPAddress: %s", __FUNCTION__, __LINE__, ip_addr_if);
    }
    close (fd);
    return if_status;
}

static int
init_new_file (char *p_input, int rotate_size)
{
    struct stat st;
    int tfd = -1;
    int ret;
    ret = stat (p_input, &st);
    if (ret == -1 && errno == ENOENT)
    {
        if ((tfd = open (p_input, O_WRONLY | O_CREAT, S_IRUSR | S_IWUSR)) > 0)
            if (fallocate (tfd, FALLOC_FL_KEEP_SIZE, 0, rotate_size * 1024) <
                0)
                LOGE ("%s:%d Error allocating %d KBytes: %s", __FUNCTION__,
                      __LINE__, rotate_size, strerror (errno));
    }
    else
    {
        LOGD ("%s:%d file %s exists, skipping creation", __FUNCTION__,
              __LINE__, p_input);
        tfd = open (p_input, O_WRONLY);
    }

    return tfd;
}

static char *
get_names (char *p_input, int rotate_size, int rotate_num)
{
    static char *fnames = NULL;
    if (rotate_num != 0 && fnames == NULL)
    {
        /* Initialize fnames and allocates .x files if they don't exists */
        fnames = malloc (rotate_num * (PATH_LEN + 1));
        if (fnames == NULL)
            return NULL;
        memset (fnames, '\0', rotate_num * PATH_LEN + 1);
        strncpy (&fnames[0], p_input, PATH_LEN);
        close (init_new_file (&fnames[0], rotate_size));
        int i;
        for (i = 1; i < rotate_num; i++)
        {
            snprintf (&fnames[i * PATH_LEN], PATH_LEN, "%s.%d.istp", p_input,
                      i);
            LOGD ("%s:%d \t %s", __FUNCTION__, __LINE__,
                  &fnames[i * PATH_LEN]);
            close (init_new_file (&fnames[i * PATH_LEN], rotate_size));
        }
    }
    return fnames;
}

static int
rotateFile (char *p_input, int fs_fd, int rotate_size, int rotate_num)
{
    int err;
    int i;
    char *fnames = get_names (p_input, rotate_size, rotate_num);

    if (p_input == NULL)
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

    for (i = rotate_num - 2; i >= 0; i--)
    {
        err = rename (&fnames[i * PATH_LEN], &fnames[(i + 1) * PATH_LEN]);
        LOGD ("%s:%d renaming log file %s to %s", __FUNCTION__, __LINE__,
              &fnames[i * PATH_LEN], &fnames[(i + 1) * PATH_LEN]);
        if (err < 0 && errno != ENOENT)
            LOGE ("%s:%d \t error: %s", __FUNCTION__, __LINE__,
                  strerror (errno));
    }

out:
    return init_new_file (p_input, rotate_size);
out_err:
    return -1;
}

static int
get_ldisc_id (const char *ldisc_name)
{
    int val;
    int ret = -1;
    char name[255] = { 0 };
    FILE *fldisc = fopen ("/proc/tty/ldiscs", "r");

    if (fldisc == NULL)
    {
        goto out_noclose;
    }

    while (fscanf (fldisc, "%254s %d", name, &val) == 2)
    {
        /*early return if the ldisc_name is found */
        if (strncmp (ldisc_name, name, strlen (ldisc_name)) == 0)
        {
            ret = val;
            goto out;
        }
    }
out:
    fclose (fldisc);
out_noclose:
    return ret;
}

static int
log_to_pti (char *gsmtty, char *sink, comm_mdm * ctx)
{
    int ldisc_id_sink;
    int ldisc_id_router;
    int ret = -1;
    int rcvd = 0;
    int fs_gsmtty, fs_sink;
    sigset_t sig_mask;

    /* Wait modem ready to start traces */
    if (pthread_mutex_lock (&ctx->cond_mtx) != 0)
    {
        LOGE ("%s:%d PTHREAD_COND_MUTEX Error taking mutex: %s. Exiting wait loop and MTS.",
              __FUNCTION__, __LINE__, strerror (errno));
        goto out;
    }

    while (!ctx->modem_available)
        if (pthread_cond_wait (&ctx->modem_online, &ctx->cond_mtx) != 0)
        {
            LOGE ("%s:%d PTHREAD_COND_WAIT Error: %s. Exiting.", __FUNCTION__,
                  __LINE__, strerror (errno));
            if (pthread_mutex_unlock (&ctx->cond_mtx) != 0)
                LOGE ("%s:%d PTHREAD_COND_MUTEX Error releasing mutex: %s.\
                      Exiting wait loop and MTS.",
                      __FUNCTION__, __LINE__, strerror (errno));
            goto out;
        }

    if (pthread_mutex_unlock (&ctx->cond_mtx) != 0)
    {
        LOGE ("%s:%d PTHREAD_COND_MUTEX Error releasing mutex: %s. Exiting wait loop and MTS.",
              __FUNCTION__, __LINE__, strerror (errno));
        goto out;
    }

    LOGI ("%s:%d opening devices", __FUNCTION__, __LINE__);
    ATTEMPTE (open (gsmtty, O_RDWR), <0, "Opening tty", &fs_gsmtty, out);
    ATTEMPTE (open (sink, O_RDWR), <0, "Opening pti", &fs_sink, out_close_tty);

    LOGI ("%s:%d getting ldiscs", __FUNCTION__, __LINE__);
    /*get ldisc ids */
    ATTEMPTE (get_ldisc_id ("n_tracesink"), == -1, "Getting sink ldisc id",
              &ldisc_id_sink, out_close);
    ATTEMPTE (get_ldisc_id ("n_tracerouter"), == -1, "Getting router ldisc id",
              &ldisc_id_router, out_close);

    LOGI ("%s:%d setting ldiscs", __FUNCTION__, __LINE__);
    /*finally set the ldisc to the devices */
    ATTEMPTE (ioctl (fs_gsmtty, TIOCSETD, &ldisc_id_router), <0,
              "Setting router ldisc", &ret, out_close);
    ATTEMPTE (ioctl (fs_sink, TIOCSETD, &ldisc_id_sink), <0,
              "Setting sink ldisc", &ret, out_rm_ldisc_tty);

    LOGI ("%s:%d waiting for MY_SIG_UNCONF_PTI", __FUNCTION__, __LINE__);
    /*blocks the program, waiting for MY_SIG_UNCONF_PTI signal to happen */
    sigemptyset (&sig_mask);
    sigaddset (&sig_mask, MY_SIG_UNCONF_PTI);
    sigprocmask (SIG_BLOCK, &sig_mask, NULL);
    if (sigwait (&sig_mask, &rcvd) == -1)
        LOGD ("%s:%d ERROR waiting for signal", __FUNCTION__, __LINE__);
    LOGI ("%s:%d Unconfiguring PTI", __FUNCTION__, __LINE__);

    /*deconfigure ldiscs */

    ioctl (fs_sink, TIOCSETD, (int[])
           {
               0
           });
out_rm_ldisc_tty:
    ioctl (fs_gsmtty, TIOCSETD, (int[])
           {
               0
           });
out_close:
    close (fs_sink);
out_close_tty:
    close (fs_gsmtty);
out:
    return ret;
}

static int
init_file (char *p_input, int *rotate_size, int rotate_num)
{
    int ret;
    int fs_fd = -2;
    struct stat st;

    ret = stat (p_input, &st);
    if (ret == 0 && S_ISREG (st.st_mode))
    {
        /* logs exists so we rotate them so that we start with a fresh bplog */
        LOGI ("%s:%d file %s exists, rotating...", __FUNCTION__, __LINE__,
              p_input);
        fs_fd = rotateFile (p_input, fs_fd, *rotate_size, rotate_num);
    }
    else if (ret == 0 && S_ISCHR (st.st_mode))
    {
        /* We output on a character device, just disable file rotation */
        LOGI ("%s:%d file %s is a Characater device, disabling file rotation...",
              __FUNCTION__, __LINE__, p_input);
        fs_fd = open (p_input, O_WRONLY);
        *rotate_size = 0;
    }
    else if (ret == -1 && errno == ENOENT)
    {
        char *end_dir = strrchr (p_input, '/');
        if (end_dir != NULL)
        {
            *end_dir = '\0';
            if (stat (p_input, &st) == -1 && errno == ENOENT)
            {
                *end_dir = '/';
                /* Creates the missing directories */
                LOGI ("%s:%d Path for file %s does not exists",
                      __FUNCTION__, __LINE__, p_input);
                char path[PATH_LEN];
                char *p = path;
                strncpy (path, p_input, PATH_LEN - 1);
                path[strnlen (p_input, PATH_LEN)] = '\0';
                /* Skips first / to avoid trying to create nothing */
                if (p[0] == '/')
                    p++;
                while ((p = strchr (p, '/')) != NULL)
                {
                    *p = '\0';
                    ret = stat (path, &st);
                    if (ret != 0 && errno == ENOENT)
                    {
                        ret = mkdir (path, 0755);
                        if (ret != 0)
                        {
                            LOGE ("%s:%d Can't create path %s : %s",
                                  __FUNCTION__, __LINE__, path,
                                  strerror (errno));
                            goto out_error;
                        }
                        LOGI ("%s:%d Path %s ... created", __FUNCTION__,
                              __LINE__, path);
                    }
                    *p = '/';
                    p++;
                }
            }
            *end_dir = '/';
        }
        fs_fd = init_new_file (p_input, *rotate_size);
    }
    else
    {
        LOGI ("%s:%d Won't be able to log on %s : %s", __FUNCTION__, __LINE__,
              p_input, strerror (errno));
        goto out_error;
    }

    if (rotate_num > 0
        && get_names (p_input, *rotate_size, rotate_num) == NULL)
    {
        LOGE ("%s:%d can't init file list", __FUNCTION__, __LINE__);
        goto out_error;
    }

    if (fs_fd < 0)
    {
        LOGE ("%s:%d Couldn't open %s : %s", __FUNCTION__, __LINE__, p_input,
              strerror (errno));
        goto out_error;
    }

    return fs_fd;
out_error:
    return -1;
}

static int
init_socket (char *ip_addr_if, int port_no)
{
    int connect_fd = -1;
    int sock_stream_fd = -1;
    int ret;
    socklen_t clilen;
    struct sockaddr_in serv_addr, cli_addr;

    LOGI ("%s:%d Open Stream Socket", __FUNCTION__, __LINE__);
    ATTEMPTE (socket (AF_INET, SOCK_STREAM, 0), <0, "Opening stream socket",
              &sock_stream_fd, out);

    memset (&serv_addr, 0, sizeof (serv_addr));
    serv_addr.sin_family = AF_INET;
    inet_aton (ip_addr_if, &serv_addr.sin_addr);
    serv_addr.sin_port = htons (port_no);

    LOGI ("%s:%d Bind to port %d", __FUNCTION__, __LINE__, port_no);
    ATTEMPTE (bind (sock_stream_fd, (struct sockaddr *) &serv_addr,
              sizeof (serv_addr)), <0, "Binding to port", &ret, out);

    LOGI ("%s:%d Listening()...", __FUNCTION__, __LINE__);
    ATTEMPTE (listen (sock_stream_fd, 5), <0, "Listening on sock", &ret, out);

    clilen = sizeof (cli_addr);

    LOGI ("%s:%d Wait for client to connect...", __FUNCTION__, __LINE__);
    ATTEMPTE (accept (sock_stream_fd, (struct sockaddr *) &cli_addr, &clilen),
              <0, "Accepting socket", &connect_fd, out);
    LOGI ("%s:%d Socket %d accepted...", __FUNCTION__, __LINE__, port_no);
out:
    return connect_fd;
}
#endif

#ifdef USE_PTS
static void
log_pts (comm_mdm * ctx)
{
    int ret = 0;
    struct pollfd *mtspoll = NULL;
    bool exit_cond = false;
    char intercom_msg_buf[MDM_MSG_SZ];
    int n;
    bool pts_connected = false;
    bool pts_start = false;
    uint32_t status_flag = 0;
    pts_cli_requests_t request;
    e_err_pts_cli_t pts_ret;
    uint32_t iMaxTryConnect = MAX_WAIT_MMGR_CONNECT_SECONDS * 1000 / MMGR_CONNECT_RETRY_TIME_MS;

    if ((mtspoll = calloc (POLL_FD_NBR, sizeof (struct pollfd))) == NULL) {
        LOGE ("%s:%d POLL struct calloc failed: %s. Exiting wait loop and MTS.",
              __FUNCTION__, __LINE__, strerror (errno));
        return;
    }

    /* Initialize poll event field */
    mtspoll[POLL_FD_COM].events = POLLHUP | POLLIN | POLLERR;

    /* FD pipe read to receive modem and PTS events */
    mtspoll[POLL_FD_COM].fd = ctx->intercom[0];

    /* Trace until critical error */
    while (!exit_cond) {

        /* Poll FDs */
        ret = poll (mtspoll, POLL_FD_NBR, -1);
        if ((ret < 0) && (ret != EINVAL)) {
            LOGD ("%s:%d Interrupted poll - reason: %s. MTS will continue.",
                    __FUNCTION__, __LINE__, strerror (errno));
            continue;
        } else if ((ret < 0) && (ret == EINVAL)) {
            LOGE ("%s:%d Error in poll - reason: %s. MTS will exit.",
                    __FUNCTION__, __LINE__, strerror (errno));
            goto out;
        } else {
            if (mtspoll[POLL_FD_COM].revents & POLLIN) {

                LOGD ("%s:%d Get modem or PTS event notification while polling.", __FUNCTION__, __LINE__);
                memset (intercom_msg_buf, 0, sizeof (intercom_msg_buf));
                n = 0;

                ATTEMPTE (read(mtspoll[POLL_FD_COM].fd, intercom_msg_buf,
                        MDM_MSG_SZ * sizeof (char)), <0,
                        "Reading modem event data", &n, out);

                if (strncmp (intercom_msg_buf, MDM_UP, MDM_MSG_SZ * sizeof (char)) == 0) {
                    LOGD ("%s:%d Get modem event notification - MODEM UP.",
                            __FUNCTION__, __LINE__);

                    /* Set flag MODEM */
                    status_flag |= 0x01;
                    LOGE ("%s:%d Status: 0x%.2X", __FUNCTION__, __LINE__, status_flag);
                }

                if (strncmp (intercom_msg_buf, MDM_DW, MDM_MSG_SZ * sizeof (char)) == 0) {
                    LOGD ("%s:%d Get modem event notification - MODEM DOWN.",
                            __FUNCTION__, __LINE__);

                    /* Reset flag MODEM */
                    status_flag &= ~0x00;
                    LOGE ("%s:%d Status: 0x%.2X", __FUNCTION__, __LINE__, status_flag);
                }

                if (strncmp (intercom_msg_buf, PTS_READY, MDM_MSG_SZ * sizeof (char)) == 0) {
                    LOGD ("%s:%d Get PTS event notification - PTS READY.",
                            __FUNCTION__, __LINE__);

                    /* Set flag PTS */
                    status_flag |= 0x10;
                    LOGE ("%s:%d Status: 0x%.2X", __FUNCTION__, __LINE__, status_flag);
                }
            }

            if (mtspoll[POLL_FD_COM].revents & POLLERR) {

                LOGE ("%s:%d Error: POLLERR event captured on fd. Exiting MTS.",
                        __FUNCTION__, __LINE__);
                goto out;
            }

            if (mtspoll[POLL_FD_COM].revents & POLLHUP) {

                LOGD ("%s:%d Warning: POLLERR event captured on fd. Exiting MTS.",
                        __FUNCTION__, __LINE__);
                goto out;
            }

            if ((!pts_connected) && (status_flag == 0x01)) {
                /* MODEM ready */
                while (iMaxTryConnect-- != 0) {
                    /* Try to connect */
                    pts_ret = pts_cli_send_configuration(ctx->pts_hdl);
                    if (pts_ret == E_ERR_PTS_CLI_SUCCEED) {
                        break;
                    } else {
                        pts_connected = true;
                    }

                    ALOGE("Delaying pts_cli_send_configuration %d", pts_ret);

                    /* Wait */
                    usleep(MMGR_CONNECT_RETRY_TIME_MS * 1000);
                }
            }

            if ((!pts_start) && (status_flag == 0x11)) {
                /* PTS and MODEM ready */
                LOGD ("%s:%d send request E_PTS_REQUEST_TRACE_START", __FUNCTION__, __LINE__);

                request.id = E_PTS_REQUEST_TRACE_START;
                request.len = 0;
                pts_ret = pts_cli_send_request(ctx->pts_hdl, &request);

                pts_start = true;
            }

            /* Clean up revents fields - everything is processed */
            mtspoll[POLL_FD_COM].revents = 0;
        }
    }

    out:
    LOGD ("%s:%d End of function", __FUNCTION__, __LINE__);
    return;
}

#else
static void
log_traces (int outfd, char *p_input, char *p_output,
            int rotate_size, int rotate_num, comm_mdm * ctx)
{
    off_t cnt_written = 0;
    int ret = 0;
    bool exit_cond = false;
    int i = 0;
    char trace_buffer[MAXDATA];
    char intercom_msg_buf[MDM_MSG_SZ];
    char inot_event_buf[INOT_BUF_SZ];
    struct pollfd *mtspoll = NULL;

    if ((mtspoll = calloc (POLL_FD_NBR, sizeof (struct pollfd))) == NULL)
    {
        LOGE ("%s:%d POLL struct calloc failed: %s. Exiting wait loop and MTS.",
              __FUNCTION__, __LINE__, strerror (errno));
        return;
    }

    /* Initialize poll event field */
    for (i = 0; i < POLL_FD_NBR; i++)
        mtspoll[i].events = POLLHUP | POLLIN | POLLERR;

    /* Clean any log fd */
    ctx->ttyfd = -1;

    /* Trace until critical error */
    while (!exit_cond)
    {
        /* Wait modem ready to start traces */
        if (pthread_mutex_lock (&ctx->cond_mtx) != 0)
        {
            LOGE ("%s:%d PTHREAD_COND_MUTEX Error taking mutex: %s. Exiting wait loop and MTS.",
                  __FUNCTION__, __LINE__, strerror (errno));
            goto out;
        }

        while (!ctx->modem_available)
        {
            /* Ensure fd is closed - due to previous iteration */
            if (ctx->ttyfd != -1)
                close (ctx->ttyfd);
            if (pthread_cond_wait (&ctx->modem_online, &ctx->cond_mtx) != 0)
            {
                LOGE ("%s:%d PTHREAD_COND_WAIT Error: %s. Exiting.",
                      __FUNCTION__, __LINE__, strerror (errno));
                if (pthread_mutex_unlock (&ctx->cond_mtx) != 0)
                    LOGE ("%s:%d PTHREAD_COND_MUTEX Error releasing mutex: %s.\
                          Exiting wait loop and MTS.",
                          __FUNCTION__, __LINE__, strerror (errno));
                goto out;
            }
        }

        if (pthread_mutex_unlock (&ctx->cond_mtx) != 0)
        {
            LOGE ("%s:%d PTHREAD_COND_MUTEX Error releasing mutex: %s.\
                  Exiting wait loop and MTS.",
                  __FUNCTION__, __LINE__, strerror (errno));
            goto out;
        }

        /* So far the modem ready event is received - so open must succeed */
        /* Debug trace */
        if (ctx->ttyfd == -1)
            LOGD ("%s:%d Got MODEM_UP, gsmtty to be opened: %s", __FUNCTION__,
                  __LINE__, ctx->p_input);

        if ((ctx->ttyfd == -1)
            && ((ctx->ttyfd = open_gsmtty (ctx->p_input)) < 0))
        {
            LOGE ("%s:%d GSMTTY open failure - but modem is notified UP.\
                  MTS needs to wait for USB enumeration.",
                  __FUNCTION__, __LINE__);
            if (!(ctx->usb_logging))
            {
                LOGE ("%s:%d HSI log port open failure. MTS will exit now.",
                      __FUNCTION__, __LINE__);
                /* For HSI we exit as this is an critical case */
                /* For USB we will get node creation by INOTIFY */
                goto out;
            }
        }

        /* First FD is always the modem log port */
        mtspoll[POLL_FD_LOG].fd = ctx->ttyfd;
        /* 2nd FD pipe read to receive modem event */
        mtspoll[POLL_FD_COM].fd = ctx->intercom[0];
        /* If 3rd FD, this is the USB INOTIFIER */
        if (ctx->usb_logging)
            mtspoll[POLL_FD_NOT].fd = ctx->inotfd;
        else
            mtspoll[POLL_FD_NOT].fd = -1;

        /* Poll FDs */
        ret = poll (mtspoll, POLL_FD_NBR, -1);
        if (ret < 0 && ret != EINVAL)
        {
            LOGD ("%s:%d Interrupted poll - reason: %s. MTS will continue.",
                  __FUNCTION__, __LINE__, strerror (errno));
            continue;
        }
        if (ret < 0 && ret == EINVAL)
        {
            LOGE ("%s:%d Error in poll - reason: %s. MTS will exit.",
                  __FUNCTION__, __LINE__, strerror (errno));
            goto out;
        }

        /* process poll data */
        for (i = 0; i < POLL_FD_NBR; i++)
        {

            if (mtspoll[i].revents & POLLIN)
            {
                int n, n1;
                switch (i)
                {

                /* CASE POLL_FD_LOG */

                case POLL_FD_LOG:

                    memset (trace_buffer, 0, sizeof (trace_buffer));
                    n = n1 = 0;

                    ATTEMPTE (read (mtspoll[POLL_FD_LOG].fd, trace_buffer,
                              MAXDATA), <0, "Reading input data", &n,
                              out);

                    /* Workaround dlp_trace bug */
                    if (n == 0)
                    {
                        if (ctx->usb_logging)
                        {
                            LOGD ("%s:%d Warning: READ 0 bytes on an invalid fd.\
                                  Will close and reopen it.",
                                  __FUNCTION__, __LINE__);
                            close (ctx->ttyfd);
                            ctx->ttyfd = -1;
                        }
                        break;
                    }

                    ATTEMPTE (write (outfd, trace_buffer, n),
                              <=0
                              && errno != ENOSPC, "Writing data to output",
                              &n1, out);
                    if (n1 <= 0)
                    {
                        /* if we end up with no space to write on device, then we have a problem.
                           we suppose that pre-allocation didn't work (not implemented on FS for
                           example) mts can't just exit because android will restart the service
                           and chaos will ensue.
                           Cleaner solution is to divide rotate_size by 1.5 and
                           force a file rotation in the hope to free enough space to keep logging.
                         */
                        if (rotate_size > 1024)
                        {
                            rotate_size /= 1.5;
                            LOGD ("%s:%d Rotate Size adjusted to: %d",
                                  __FUNCTION__, __LINE__, rotate_size);
                            ATTEMPTE (rotateFile(p_output, outfd, rotate_size,
                                      rotate_num), <0, "Rotating logs",
                                      &outfd, out);
                            cnt_written = 0;
                            break;
                        }
                        /* if we end up there, there's not much we can do ...
                           just pause the logging :(
                         */
                        else
                        {
                            LOGE ("%s:%d No more space on device!",
                                  __FUNCTION__, __LINE__);
                            pause ();
                        }
                    }

                    cnt_written += n;
                    if (rotate_size > 0
                        && (cnt_written / 1024) >= rotate_size)
                    {
                        ATTEMPTE (rotateFile(p_output, outfd, rotate_size,
                                  rotate_num), <0, "Rotating logs",
                                  &outfd, out);
                        cnt_written = 0;
                        LOGI ("%s:%d Logs rotated", __FUNCTION__,
                              __LINE__);
                    }
                    break;

                /* CASE POLL_FD_COM */

                case POLL_FD_COM:

                    LOGD ("%s:%d Get modem event notification while polling.",
                          __FUNCTION__, __LINE__);
                    memset (intercom_msg_buf, 0,
                            sizeof (intercom_msg_buf));
                    n = n1 = 0;

                    ATTEMPTE (read(mtspoll[POLL_FD_COM].fd, intercom_msg_buf,
                              MDM_MSG_SZ * sizeof (char)), <0,
                              "Reading modem event data", &n, out);

                    if (strncmp(intercom_msg_buf, MDM_UP,
                        MDM_MSG_SZ * sizeof (char)) == 0)
                    {
                        LOGD ("%s:%d Get modem event notification - MODEM UP.",
                              __FUNCTION__, __LINE__);
                    }
                    if (strncmp(intercom_msg_buf, MDM_DW,
                        MDM_MSG_SZ * sizeof (char)) == 0)
                    {
                        LOGD ("%s:%d Get modem event notification - MODEM DOWN.",
                              __FUNCTION__, __LINE__);
                        /* Close tty */
                        if (ctx->ttyfd != -1)
                        {
                            LOGD ("%s:%d Log FD closed.", __FUNCTION__,
                                  __LINE__);
                            close (ctx->ttyfd);
                            ctx->ttyfd = -1;
                        }
                    }

                    break;

                /* CASE POLL_FD_NOT */

                case POLL_FD_NOT:

                    memset (inot_event_buf, 0, sizeof (inot_event_buf));
                    n = n1 = 0;

                    ATTEMPTE (read(mtspoll[POLL_FD_NOT].fd, inot_event_buf,
                              INOT_BUF_SZ), <0, "Reading inotify data",
                              &n, out);

                    if (n == 0)
                    {
                        LOGD ("%s:%d Warning: READ 0 bytes for inotify event.",
                              __FUNCTION__, __LINE__);
                        break;
                    }

                    while (n1 < n)
                    {
                        struct inotify_event *evt =
                            (struct inotify_event *) &inot_event_buf[n1];
                        struct timespec ptim;
                        ptim.tv_sec = 0;
                        ptim.tv_nsec = 50000000L;       /* 50 ms sleep */
                        if (evt->len)
                        {
                            if ((evt->mask & IN_CREATE)
                                && (!(evt->mask & IN_ISDIR)))
                            {
                                int attempt = 5;
                                bool try = true;
                                LOGD ("%s:%d USB node %s CREATED.",
                                      __FUNCTION__, __LINE__,
                                      evt->name);
                                /* Give time for ttyACM1 node creation */
                                nanosleep (&ptim, NULL);
                                if (ctx->ttyfd == -1)
                                {
                                    LOGD ("%s:%d MTS will loop until we connect on USB.",
                                          __FUNCTION__, __LINE__);
                                    while ((ctx->ttyfd =
                                                open_gsmtty (ctx->
                                                             p_input)) < 0 && try)
                                    {
                                        if (!attempt)
                                        {
                                            LOGE ("%s:%d Too many open tentative. \
                                            MTS will give up until next USB event.",
                                            __FUNCTION__, __LINE__);
                                            n1 += INOT_EVENT_SZ + evt->len;
                                            try = false;
                                        }
                                        nanosleep (&ptim, NULL);
                                        attempt--;
                                    }
                                }
                                n1 += INOT_EVENT_SZ + evt->len;
                            }
                            if ((evt->mask & IN_DELETE)
                                && (!(evt->mask & IN_ISDIR)))
                            {
                                LOGD ("%s:%d USB node %s DELETED.",
                                      __FUNCTION__, __LINE__,
                                      evt->name);
                                if (ctx->ttyfd != -1)
                                {
                                    LOGD ("%s:%d USB node FD closed.",
                                          __FUNCTION__, __LINE__);
                                    close (ctx->ttyfd);
                                    ctx->ttyfd = -1;
                                }
                                n1 += INOT_EVENT_SZ + evt->len;
                            }
                        }
                    }
                    break;
                }
            }
            if (mtspoll[i].revents & POLLERR)
            {
                switch (i)
                {
                case POLL_FD_LOG:
                    LOGE ("%s:%d Error: POLLERR event captured on fd. Closing LOG fd.",
                      __FUNCTION__, __LINE__);
                    if (ctx->ttyfd != -1)
                    {
                        close (ctx->ttyfd);
                        ctx->ttyfd = -1;
                    }
                    break;
                case POLL_FD_COM:
                case POLL_FD_NOT:
                    LOGE ("%s:%d Error: POLLERR event captured on fd. Exiting MTS.",
                      __FUNCTION__, __LINE__);
                    goto out;
                    break;
                }
            }
            if (mtspoll[i].revents & POLLHUP)
            {
                switch (i)
                {
                case POLL_FD_LOG:
                    LOGD ("%s:%d Warning: POLLHUP event captured on fd. Closing LOG fd.",
                      __FUNCTION__, __LINE__);

                    if (ctx->ttyfd != -1)
                    {
                        close (ctx->ttyfd);
                        ctx->ttyfd = -1;
                    }
                    break;
                case POLL_FD_COM:
                case POLL_FD_NOT:
                    LOGD ("%s:%d Warning: POLLERR event captured on fd. Exiting MTS.",
                      __FUNCTION__, __LINE__);
                    goto out;
                    break;
                }
            }
        }

        /* Clean up revents fields - everything is processed */
        for (i = 0; i < POLL_FD_NBR; i++)
            mtspoll[i].revents = 0;

    }

out:
    if (ctx->ttyfd != -1)
    {
        close (ctx->ttyfd);
        ctx->ttyfd = -1;
    }
    close (outfd);
    if (mtspoll != NULL)
        free (mtspoll);
    LOGD ("%s:%d Closed input: %s and output: %s", __FUNCTION__, __LINE__,
          p_input, p_output);
    return;
}
#endif

int
mdm_up (mmgr_cli_event_t * ev)
{
    comm_mdm *ctx = (comm_mdm *) ev->context;
    LOGD ("%s:%d Received MODEM_UP", __FUNCTION__, __LINE__);

    if (pthread_mutex_lock (&ctx->cond_mtx) != 0)
    {
        LOGE ("%s:%d PTHREAD_COND_MUTEX Error taking mutex: %s. CRITICAL: PTHREAD_COND not sent!",
              __FUNCTION__, __LINE__, strerror (errno));
        goto out;
    }
    /* Modem up - start tracing */
    ctx->modem_available = true;
    if (write (ctx->intercom[1], MDM_UP, MDM_MSG_SZ * sizeof (char))
        != (MDM_MSG_SZ * sizeof (char)))
        LOGE ("%s:%d Modem event msg not properly sent. MTS may missbehave.",
             __FUNCTION__, __LINE__);

    if (pthread_mutex_unlock (&ctx->cond_mtx) != 0)
    {
        LOGE ("%s:%d PTHREAD_COND_MUTEX Error releasing mutex: %s.\
              CRITICAL: MTS not expected to work anymore. PTHREAD_COND not sent!",
              __FUNCTION__, __LINE__, strerror (errno));
        goto out;
    }
    /* Send signal to start tracing */
    if (pthread_cond_signal (&ctx->modem_online) != 0)
        LOGE ("%s:%d PTHREAD_COND_SIGNAL Error: %s.\
               CRITICAL: MTS not expected to work anymore. PTHREAD_COND not sent!",
              __FUNCTION__, __LINE__, strerror (errno));
out:
    return 0;
}

int
mdm_dwn (mmgr_cli_event_t * ev)
{
    comm_mdm *ctx = (comm_mdm *) ev->context;
    LOGD ("%s:%d Received MODEM_DOWN", __FUNCTION__, __LINE__);

    if (pthread_mutex_lock (&ctx->cond_mtx) != 0)
    {
        LOGE ("%s:%d PTHREAD_COND_MUTEX Error taking mutex: %s. \
              CRITICAL: MTS not expected to work anymore. Trouble on mdm_down notification !",
              __FUNCTION__, __LINE__, strerror (errno));
        goto out;
    }
    /* Modem down - stop tracing */
    ctx->modem_available = false;
    if (write (ctx->intercom[1], MDM_DW, MDM_MSG_SZ * sizeof (char))
        != (MDM_MSG_SZ * sizeof (char)))
        LOGE ("%s:%d Modem event msg not properly sent. MTS may missbehave.",
             __FUNCTION__, __LINE__);

    if (pthread_mutex_unlock (&ctx->cond_mtx) != 0)
        LOGE ("%s:%d PTHREAD_COND_MUTEX Error releasing mutex: %s. \
              CRITICAL: MTS not expected to work anymore. Trouble on mdm_down notification t!",
              __FUNCTION__, __LINE__, strerror (errno));

out:
    return 0;
}

int
main (int argc, char **argv)
{
    char ip_addr_if[16] = "";
    int ret = 0;
    struct comm_mdm_s ctx;
    int outfd = -1;

    char p_output[PATH_LEN] = { 0 };
    char p_out_type = '\0';
    char p_input[PATH_LEN] = { 0 };
    int p_rotate_size = DEFAULT_LOG_SIZE;
    int p_rotate_num = DEFAULT_MAX_ROTATED_NR;
    unsigned int i, j;
#ifdef USE_PTS
    e_err_pts_cli_t pts_ret;
    e_trace_mode_t trace_mode;
#endif

    ctx.ttyfd = -1;
    ctx.inotfd = -1;
    ctx.inotwd = -1;
    ctx.modem_available = false;
    ctx.usb_logging = false;
    struct args_s list[] = {{ .name = PROP_HEAD "output",.key =
                                "-o",.type_conv = "%s",.storage = p_output},
                          { .name = PROP_HEAD "output_type",.key = "-t",.type_conv = "%c",.storage =
                                &p_out_type},
                          { .name = PROP_HEAD "input",.key = "-i",.type_conv = "%s",.storage =
                                p_input},
                          { .name = PROP_HEAD "rotate_size",.key = "-r",.type_conv = "%d",.storage =
                                &p_rotate_size},
                          { .name = PROP_HEAD "rotate_num",.key = "-n",.type_conv = "%d",.storage =
                                &p_rotate_num}};
    char key_array[sizeof (list) / sizeof (*list)] = { 0 };

    LOGI ("%s Version:%s%s", argv[0], __DATE__, __TIME__);

    if (pthread_cond_init (&ctx.modem_online, NULL) != 0)
    {
        LOGE ("%s:%d modem_online PTHREAD_COND cannot be initialized: error %s. Exiting.",
              __FUNCTION__, __LINE__, strerror (errno));
        goto cond_failure;
    }

    if (pthread_mutex_init (&ctx.cond_mtx, NULL) != 0)
    {
        LOGE ("%s:%d PTHREAD_COND_MUTEX cannot be initialized: error %s. Exiting.",
              __FUNCTION__, __LINE__, strerror (errno));
        goto mtx_failure;
    }

#ifdef USE_PTS

    ctx.pts_available = false;

    if (pthread_mutex_init (&ctx.cond_pts_ready_mtx, NULL) != 0)
    {
        LOGE ("%s:%d PTHREAD_COND_PTS_READY_MUTEX cannot be initialized: error %s. Exiting.",
              __FUNCTION__, __LINE__, strerror (errno));
        goto mtx_failure;
    }
#endif

    if (pipe (ctx.intercom) == -1)
    {
        LOGE ("%s:%d PIPE for intercom cannot be initialized: error %s. Exiting.",
              __FUNCTION__, __LINE__, strerror (errno));
        goto init_failure;
    }

    if (argc > 1)
    {
        for (i = 1; i < (unsigned) argc; i++)
        {
            for (j = 0; j < sizeof (list) / sizeof (*list); j++)
            {
                if (key_array[j] == 1)
                    continue;
                if (strncmp (list[j].key, argv[i], 2) == 0)
                {
                    sscanf (argv[++i], list[j].type_conv, list[j].storage);
                    key_array[j] = 1;
                    break;
                }
            }
        }

    }
    else
    {
        char result[80];
        for (i = 0; i < sizeof (list) / sizeof (*list); i++)
        {
            property_get (list[i].name, result, "");
            if (result[0] == '\0')
                continue;
            sscanf (result, list[i].type_conv, list[i].storage);
        }
    }
    LOGD ("%s:%d Parameters: out_type: %c output: %s input: %s rotate_num: %d rotate_size: %d",
          __FUNCTION__, __LINE__, p_out_type, p_output, p_input, p_rotate_num,
          p_rotate_size);
    if (!p_input[0] || !p_output[0])
    {
        puts (USAGE_TXT);
        goto init_failure;
    }

    if (strncmp (p_input, USB_LOG_DEV, sizeof (USB_LOG_DEV)) == 0)
    {
        LOGD ("%s:%d: USB logging enabled - activate INOTIFY watch.",
              __FUNCTION__, __LINE__);
        ctx.usb_logging = true;
        /* Create INOTIFY instance */
        if ((ctx.inotfd = inotify_init ()) < 0)
        {
            LOGE ("%s:%d  INOTIFY cannot be initialized: error %s. Exiting.",
                  __FUNCTION__, __LINE__, strerror (errno));
            goto init_failure;
        }

        /* Add watch for USB device */
        if ((ctx.inotwd =
                 inotify_add_watch (ctx.inotfd, INOT_USB_DEV_NODE,
                                    IN_CREATE | IN_DELETE)) < 0)
        {
            LOGE ("%s:%d  INOTIFY WATCH cannot be initialized: error %s. Exiting.",
                  __FUNCTION__, __LINE__, strerror (errno));
            goto inotwatch_failure;
        }
    }

    ctx.mmgr_hdl = NULL;
    mmgr_cli_create_handle (&ctx.mmgr_hdl, "mts", &ctx);
    mmgr_cli_subscribe_event (ctx.mmgr_hdl, mdm_up, E_MMGR_EVENT_MODEM_UP);
    mmgr_cli_subscribe_event (ctx.mmgr_hdl, mdm_dwn, E_MMGR_EVENT_MODEM_DOWN);
    ctx.p_input = p_input;

    uint32_t iMaxTryConnect = MAX_WAIT_MMGR_CONNECT_SECONDS * 1000 / MMGR_CONNECT_RETRY_TIME_MS;

    while (iMaxTryConnect-- != 0) {

        /* Try to connect */
        ret = mmgr_cli_connect (ctx.mmgr_hdl);

        if (ret == E_ERR_CLI_SUCCEED) {

            break;
        }

        ALOGE("Delaying mmgr_cli_connect %d", ret);

        /* Wait */
        usleep(MMGR_CONNECT_RETRY_TIME_MS * 1000);
    }
    /* Check for unsuccessfull connection */
    if (ret != E_ERR_CLI_SUCCEED) {

        ALOGE("Failed to connect to mmgr %d", ret);

    }

#ifdef USE_PTS
    ctx.pts_hdl = NULL;
    pts_ret = pts_cli_create_handle(&ctx.pts_hdl, "mts", &ctx);
    pts_ret = pts_cli_subscribe_event(ctx.pts_hdl, pts_down, E_PTS_EVENT_PTS_DOWN);
    pts_ret = pts_cli_subscribe_event(ctx.pts_hdl, pts_up, E_PTS_EVENT_PTS_UP);
    pts_ret = pts_cli_subscribe_event(ctx.pts_hdl, pts_ready, E_PTS_EVENT_TRACE_READY);

    pts_ret = pts_cli_set_trace_parameters(ctx.pts_hdl, E_PTS_PARAM_INPUT_PATH_TRACE, p_input);
    pts_ret = pts_cli_set_trace_parameters(ctx.pts_hdl, E_PTS_PARAM_OUTPUT_PATH_TRACE, p_output);
    pts_ret = pts_cli_set_trace_parameters(ctx.pts_hdl, E_PTS_PARAM_ROTATE_SIZE, &p_rotate_size);
    pts_ret = pts_cli_set_trace_parameters(ctx.pts_hdl, E_PTS_PARAM_ROTATE_NUM, &p_rotate_num);

    LOGD ("%s:%d Parameters to PTS: output: %s input: %s rotate_num: %d rotate_size: %d",
          __FUNCTION__, __LINE__, p_output, p_input, p_rotate_num,
          p_rotate_size);
#endif

    switch (p_out_type)
    {
    case 'f':
#ifndef USE_PTS
        /* disables rotation if one or the other param is <=0 */
        if (p_rotate_num <= 0 || p_rotate_size <= 0)
            p_rotate_size = p_rotate_num = 0;
        outfd = init_file (p_output, &p_rotate_size, p_rotate_num);
        log_traces (outfd, p_input, p_output, p_rotate_size, p_rotate_num,
                    &ctx);
#else
        /* logs routed towards PTS */
        trace_mode = E_TRACE_MODE_FILE_SYSTEM;
        pts_ret = pts_cli_set_trace_parameters(ctx.pts_hdl, E_PTS_PARAM_TRACE_MODE, &trace_mode);
        log_pts (&ctx);
#endif
        break;
    case 'p':
#ifndef USE_PTS
        while (!iface_up (ip_addr_if))
        {
            sleep (CHECK_INTERVAL);
            LOGI ("%s:%d wait iface_up(%s)", __FUNCTION__, __LINE__, IFNAME);
        }
        LOGI ("%s:%d %s is up, do init_socket()", __FUNCTION__, __LINE__,
              IFNAME);
        outfd = init_socket (ip_addr_if, atoi (p_output));
        log_traces (outfd, p_input, NULL, 0, 0, &ctx);
        break;
#endif
    case 'k':
#ifndef USE_PTS
        /* log_to_pti will sets pti sink/sources and block until
           a signal MY_SIG_UNCONF_PTI is received
           then it will clean and close all fds
         */
        log_to_pti (p_input, p_output, &ctx);
#else
        /* logs routed towards PTS */
        trace_mode = E_TRACE_MODE_LINE_DISCS;
        pts_ret = pts_cli_set_trace_parameters(ctx.pts_hdl, E_PTS_PARAM_TRACE_MODE, &trace_mode);
        log_pts (&ctx);
        break;
#endif
    default:
        puts (USAGE_TXT);
        break;
    }

    mmgr_cli_disconnect (ctx.mmgr_hdl);
    mmgr_cli_delete_handle (ctx.mmgr_hdl);
    if (ctx.usb_logging)
        inotify_rm_watch (ctx.inotfd, ctx.inotwd);

#ifdef USE_PTS
    pts_cli_remove_configuration(ctx.pts_hdl);
    pts_cli_delete_handle(ctx.pts_hdl);
#endif
inotwatch_failure:
    if (ctx.usb_logging)
        close (ctx.inotfd);
    close (ctx.intercom[0]);
    close (ctx.intercom[1]);

init_failure:
#ifdef USE_PTS
    pthread_mutex_destroy (&ctx.cond_pts_ready_mtx);
#endif
    pthread_mutex_destroy (&ctx.cond_mtx);
mtx_failure:
    pthread_cond_destroy (&ctx.modem_online);
cond_failure:
    LOGI ("%s:%d Exiting mts", __FUNCTION__, __LINE__);
    return 0;
}
