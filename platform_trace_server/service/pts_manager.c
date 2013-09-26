/* Platform Trace Server - main source file
 **
 ** Copyright (C) Intel 2010
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
#include <sys/types.h>
#include <sys/stat.h>
#include <signal.h>

#include "errors.h"
#include "logs.h"
#include "pts_def.h"
#include "events_manager.h"


/* global values used to cleanup */
pts_data_t *g_pts_data = NULL;

/**
 * Clean PTS before exit
 */
static void cleanup(void)
{
    events_cleanup(g_pts_data);
    LOG_VERBOSE("Exiting");
}

/**
 * Handle catched signals
 *
 * @param [in] sig signal handler id
 */
static void sig_handler(int sig)
{
    switch (sig) {
    case SIGUSR1:
        pthread_exit(NULL);
        break;
    case SIGHUP:
    case SIGTERM:
    case SIGINT:
        /* nothing to do as cleanup will be called by exit */
        break;
    default:
        break;
    }

    exit(0);
}

/**
 * Set the handler needed to exit a thread
 *
 * @return E_PTS_ERR_SUCCESS if successful
 * @return E_PTS_ERR_FAILED if failed
 */
static e_pts_errors_t set_signal_handler(void)
{
    struct sigaction sigact;
    e_pts_errors_t err = E_PTS_ERR_FAILED;

    memset(&sigact, 0, sizeof(struct sigaction));
    /* Signal handler */
    if (sigemptyset(&sigact.sa_mask) == -1) {
        goto end_set_signal_handler;
    }
    sigact.sa_flags = 0;
    sigact.sa_handler = sig_handler;

    if (sigaction(SIGUSR1, &sigact, NULL) == -1) {
        goto end_set_signal_handler;
    }
    if (sigaction(SIGHUP, &sigact, NULL) == -1) {
        goto end_set_signal_handler;
    }
    if (sigaction(SIGTERM, &sigact, NULL) == -1) {
        goto end_set_signal_handler;
    }
    if (sigaction(SIGINT, &sigact, NULL) == -1) {
        goto end_set_signal_handler;
    }

    /* configuration successful */
    err = E_PTS_ERR_SUCCESS;

    end_set_signal_handler:
    return err;
}

/**
 * Platform Trace Server main function
 *
 * @param [in] argc number of arguments
 * @param [in] argv list of arguments
 *
 * @return EXIT_FAILURE if failed
 * @return EXIT_SUCCESS if successful
 */
int main(int argc, char *argv[])
{
    e_pts_errors_t ret = E_PTS_ERR_SUCCESS;
    pts_data_t pts;

    /* Initialize the PTS structure */
    memset((void *)&pts, 0, sizeof(pts_data_t));
    g_pts_data = &pts;

    if (set_signal_handler() == E_PTS_ERR_FAILED) {
        LOG_ERROR("Error during sigaction initialization. Exit");
        ret = EXIT_FAILURE;
        goto out;
    }

    if (atexit(cleanup) != 0) {
        LOG_ERROR("Exit configuration failed. Exit");
        ret = EXIT_FAILURE;
        goto out;
    }

    if (events_init(&pts) != E_PTS_ERR_SUCCESS) {
        LOG_ERROR("Events configuration failed. Exit");
        ret = EXIT_FAILURE;
        goto out;
    }

    /* Start loop for managing cnx events */
    ret = events_manager(&pts);

    out:
    exit(ret);
}
