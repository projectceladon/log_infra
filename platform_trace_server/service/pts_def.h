
#ifndef __PTS_DEF_H__
#define __PTS_DEF_H__

#include "client.h"
#include "client_cnx.h"
#include "pts.h"


#define EVENTS \
        X(NEW_CLIENT), \
        X(CLIENT), \
        X(TIMEOUT),\
        X(NUM)

#define PTS_STATE \
        X(OFF), \
        X(RESET), \
        X(UP), \
        X(NUM)

typedef enum e_events_type {
#undef X
#define X(a) E_EVENT_##a
    EVENTS
} e_events_type_t;

typedef enum e_pts_state {
#undef X
#define X(a) E_PTS_STATE_##a
    PTS_STATE
} e_pts_state_t;

typedef struct pts_events {
    int nfds;
    struct epoll_event *ev;
    int cur_ev;
    e_events_type_t state;
} pts_events_t;

typedef struct current_request {
    msg_t msg;
    client_t *client;
} current_request_t;

struct pts_data;
typedef e_pts_errors_t (*event_hdler_t) (struct pts_data * pts);

typedef struct pts_data {
    int epollfd;
    int fd_cnx;
    e_pts_state_t state;
    client_list_t clients;
    pts_events_t events;
    current_request_t request;
    /* functions handlers: */
    event_hdler_t hdler_events[E_EVENT_NUM];
    event_hdler_t hdler_client[E_PTS_STATE_NUM][E_PTS_NUM_REQUESTS];
} pts_data_t;

#endif                          /* __PTS_DEF_H__ */
