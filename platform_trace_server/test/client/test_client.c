#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <pthread.h>
#include <errno.h>
#include <getopt.h>
#include <fcntl.h>

#include "pts_cli.h"

#define TRACE_TEMPLATE "%s:Trace number %d"

/* Usage text of mts displayed with -h toggle */
#define USAGE_TXT "Usage:\n" \
    "ptscli_test -N <name> -i <input> -o <output> [-r <Mbytes>] [-n <count>]]\n" \
    "\n" \
    "\t-N <name>       Client name.\n" \
    "\t-i <input>      Input file, usually a ptd interface (/dev/ptd_bt).\n" \
    "\t-o <output>     Output target (file name).\n" \
    "\t-r <Mbytes>     Rotate log every X Mbytes (10MB if unspecified).\n" \
    "\t-n <count>      Sets max number of rotated logs to <count>, default 4\n"

pts_cli_handle_t * pts_hdl = NULL;
int fd = -1;
int active = 0;
int count = 1;
int loop = 1;

char pc_name[CLIENT_NAME_LEN] = {0};
char pc_input[FILE_NAME_LEN] = {0};
char pc_output[FILE_NAME_LEN] = {0};
uint32_t rotate_size = 10*(1<<20);
uint32_t rotate_num = 4;

void * write_trace(void *);
void pts_start_trace(void);
void pts_stop_trace(void);

int pts_down(pts_cli_event_t * ev) {

    printf("HANDLER Event PTS_DOWN\n");
    if (fd != -1)
        close(fd);

    loop = 0;
    count = 1;
    active = 0;

    return 0;
}

int pts_up(pts_cli_event_t * ev) {

    printf("HANDLER Event PTS_UP\n");
    return 0;
}

int pts_ready(pts_cli_event_t * ev) {

    printf("HANDLER Event PTS_READY\n");
    active = 1;
    count = 1;

    return 0;
}

void pts_start_trace(void) {

    pts_cli_requests_t request;

    fd = open(pc_input, O_WRONLY | O_NONBLOCK);

    request.id = E_PTS_REQUEST_TRACE_START;
    request.len = 0;
    pts_cli_send_request(pts_hdl, &request);

}

void pts_stop_trace(void) {

    pts_cli_requests_t request;

    if (fd != -1)
        close(fd);

    request.id = E_PTS_REQUEST_TRACE_STOP;
    request.len = 0;
    pts_cli_send_request(pts_hdl, &request);

}

int main (int argc, char **argv)
{
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_SUCCEED;
    pthread_t thr_id;
    int err;
    uint32_t iMaxTryConnect = 5;
    e_trace_mode_t trace_mode = E_TRACE_MODE_FILE_SYSTEM;

    int param_lu;
    char *options = "N:i:o:n:r:";
    struct option long_options[] = {
            {"name", 1, 0, "N"},
            {"input", 1, 0, "i"},
            {"output", 1, 0, "o"},
            {"rotate", 1, 0, "r"},
            {"number", 1, 0, "n"},
            {0, 0, 0, 0},
    };

    do {
        param_lu = getopt_long(argc, argv, options, long_options, NULL);
        switch (param_lu) {
        case 'N':
            strncpy(pc_name, optarg, CLIENT_NAME_LEN);
            break;
        case 'i':
            strncpy(pc_input, optarg, FILE_NAME_LEN);
            break;
        case 'o':
            strncpy(pc_output, optarg, FILE_NAME_LEN);
            break;
        case 'r':
            rotate_size = atoi(optarg)*(1<<20);
            break;
        case 'n':
            rotate_num = atoi(optarg);
            break;
        case -1:
            param_lu = 0;
            break;
        case 'h':
        case '?':
        default:
            puts(USAGE_TXT);
            goto out;
            break;
        }
    } while (param_lu > 0);

    if ((!pc_name[0]) || (!pc_input[0]) || (!pc_output[0])) {
        puts(USAGE_TXT);
        goto out;
    }

    printf("Parameters: name %s input: %s output: %s rotate num: %d rotate size: %d\n",
            pc_name, pc_input, pc_output, rotate_num, rotate_size);

    pts_cli_create_handle(&pts_hdl, pc_name, NULL);
    pts_cli_subscribe_event(pts_hdl, pts_down, E_PTS_EVENT_PTS_DOWN);
    pts_cli_subscribe_event(pts_hdl, pts_up, E_PTS_EVENT_PTS_UP);
    pts_cli_subscribe_event(pts_hdl, pts_ready, E_PTS_EVENT_TRACE_READY);

    ret = pts_cli_set_trace_parameters(pts_hdl, E_PTS_PARAM_INPUT_PATH_TRACE, pc_input);
    ret = pts_cli_set_trace_parameters(pts_hdl, E_PTS_PARAM_OUTPUT_PATH_TRACE, pc_output);
    ret = pts_cli_set_trace_parameters(pts_hdl, E_PTS_PARAM_ROTATE_SIZE, &rotate_size);
    ret = pts_cli_set_trace_parameters(pts_hdl, E_PTS_PARAM_ROTATE_NUM, &rotate_num);
    ret = pts_cli_set_trace_parameters(pts_hdl, E_PTS_PARAM_TRACE_MODE, &trace_mode);

    while (iMaxTryConnect-- != 0) {
        /* Try to connect */
        ret = pts_cli_send_configuration(pts_hdl);
        if (ret == E_ERR_PTS_CLI_SUCCEED) {
            break;
        }

        /* Wait */
        usleep(2 * 1000000);
    }

    system("echo \"ptd_bt 1\" > /sys/module/ptd/parameters/log_to_fs");
    system("echo \"ptd_wlan 1\" > /sys/module/ptd/parameters/log_to_fs");
    system("echo \"ptd_gnss 1\" > /sys/module/ptd/parameters/log_to_fs");

    err = pthread_create(&thr_id, NULL, (void *)write_trace, NULL);
    if (err != 0)
        printf("failed to create the thread write_trace.\n");

    pthread_join(thr_id, NULL);

    out:
    return 0;
}

void * write_trace(void * pv) {

    char mes[512];
    e_err_pts_cli_t ret = E_ERR_PTS_CLI_SUCCEED;
    pts_cli_requests_t request;

    while (loop) {

        if (active == 1) {

            if (count == 1)
                sleep(4);

            if (count == 1)
                pts_start_trace();

//            if (count == 1000)
//                pts_start_trace();

//            if (count == 1500)
//                pts_stop_trace();

//            if (count == 2500)
//                pts_start_trace();

//            if (count == 70000) {
//                printf("fin du client !\n");
//                exit(0);
//            }

            if (count == 0x5FFFFFFF)
                pts_stop_trace();

            memset((void *)&mes, 0, sizeof(mes));
            sprintf(mes, TRACE_TEMPLATE, pc_name, count);
            if (fd != -1)
                write(fd, mes, strlen(mes));

            count++;
        }

        usleep(200);
    }

    if (fd != -1)
        close(fd);

    return NULL;
}
