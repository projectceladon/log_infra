1. Overview
================================
This README file is intended for providing explanations about the interfaces
given by PTS to its clients.
All clients shall use the PTS client library to communicate with PTS server.
It is mandatory for any processes accessing PTS to make use of this interface.
Data exchange between PTS and clients is based on a Unix domain socket (IPC socket).


2. PTS interface
================================
PTS provides a single socket endpoint accessible to radio group members and system user only.
The goal of this socket is to provide a communication medium
for a client to send PTS its own trace parameters (e.g. ON/OFF, input path, output path, etc...).
This socket is also able to inform all clients of PTS status.

For your information, the name of the socket is defined in pts.h header file
(PTS_SOCKET_NAME constant definition).
In this header file are also defined different constants such as length of file name
(input and output trace paths) and default values used for handling of traces on file system.

Socket name and constant values are subject to change.
Please include the unique pts_cli.h header file in your project instead of redefining the values.

In order to be connected with PTS server, a client shall first create a PTS client library handler
by declaring its name.
The name size is CLIENT_NAME_LEN characters maximum.

Another prerequisite of PTS server connection is to set trace parameters
using PTS library interfaces.
Some configuration parameters are manadatory such as input path,
output path and way of trace data routing.
If a client wants to route trace data to File System, it could set optional parameters
file rotate size and file rotate number.

A client could also provide its events subscription mask. This mask is 32-bit long.
To subscribe to an event, the bit matching the event id must be set to 1.

A client is considered connected to PTS server if it has sent:
  - its name (while library handler creation)
  - its mandatory trace parameters
  - the way client wants to route its data trace


3. API definition
================================

pts_cli_create_handle
---------------------
This interface shall be called by a client that wants to use PTS.
Consequently, a PTS client library handle is created.
This interface shall be called before any other one.

pts_cli_subscribe_event
-----------------------
This interface allows a client to subscribe to a PTS event.
As all requests sent by a client are ALWAYS acknowledged by PTS, E_PTS_ACK and E_PTS_NACK
events are automatically subscribed by default.

pts_cli_set_trace_parameters
----------------------------
This interface allows a client to set a trace parameter.
Trace parameters could be:
  - input path,
  - output path,
  - rotate file size (in case of selecting routing trace data towards file system),
  - rotate file number (in case of selecting routing trace data towards file system)
This interface shall also be called by a client to set the way its trace data are routed.

pts_cli_send_configuration
--------------------------
This interface shall be called by a client to initialize communication with PTS server.
If succeed, client is considered connected to PTS.
If a client wants to send a new configuration, it shall first remove the current configuration
(invoking pts_cli_remove_configuration).

pts_cli_send_request
--------------------
This interface allows a connected client to send PTS a request.
IMPORTANT REMARK: This interface shall not be called under client's callback.
Client's callback must be used only for a short processing time, otherwise responsiveness
is not guaranteed.
A mechanism is set to avoid the request sending under callback. Otherwise a deadlock
happens when a client tries to send a message under its callback.

pts_cli_remove_configuration
----------------------------
This interface disconnects the client from PTS server.

pts_cli_delete_handle
---------------------
This interface shall be called by a client that wants to delete its PTS client library handle
(typically after calling pts_cli_remove_configuration).


4. PTS events
================================

PTS events are broadcasted by PTS to its clients thanks to the socket.
PTS events are used to provide the PTS status.
Once a client get connected to PTS, PTS systematically sends back its current status
(if the client is registered to this event).
Afterwards, PTS will send its own status whenever it changes.

The following status can be sent:
  - E_PTS_EVENT_TRACE_READY: this status indicates PTS has initialized all its internal resources
for catching client traces and routing them to the desired location.
Thus this status indicates PTS is ready to be used. When this status is received,
client can activate trace by sending E_PTS_REQUEST_TRACE_START request.

NOTE: THIS EVENT IS MANDATORY FOR GOOD WORKING OF TRACE DATA READING.
CLIENT SHALL SUBSCRIBE THIS EVENT.


5. Client requests
================================

The goal of these requests is to give the possibility for PTS clients to ask for specific operations.

The following request can be sent (the seven first requests are automatically sent while client
sends its configuration by calling pts_cli_send_configuration):
  - E_PTS_SET_NAME: this request is used to provide PTS with client's name
  (transparent for the client).
  - E_PTS_SET_EVENTS: this request is used to provide PTS with client's subscription mask
  (transparent for the client).
  - E_PTS_SET_INPUT_PATH_TRACE_CFG: this request is used to provide PTS with client's input
  data trace file path (transparent for the client).
  - E_PTS_SET_OUTPUT_PATH_TRACE_CFG: this request is used to provide PTS with client's
  data trace route file path (transparent for the client).
  - E_PTS_SET_ROTATE_SIZE_TRACE_CFG: this request is used to provide PTS with client's
  output file rotation size (transparent for the client).
  - E_PTS_SET_ROTATE_NUM_TRACE_CFG: this request is used to provide PTS with client's
  output file rotation number (transparent for the client).
  - E_PTS_SET_TRACE_MODE: this request is used to provide PTS with client's
  data trace route used (transparent for the client).
  - E_PTS_REQUEST_TRACE_START: this request is used to inform PTS that it could poll client's
  input interface to catch trace data.
  - E_PTS_REQUEST_TRACE_STOP: this request is used to inform PTS that it shall stop polling
  client's input interface.


6. PTS client implementation
================================

To use the PTS client library, you should include the pts_cli.h header file and
statically link the library to your binary.

    Android.mk file extract:
    ~~~~~~~~~~~~~~~~~~~~~~~~
    LOCAL_C_INCLUDES += $(TARGET_OUT_HEADERS)/pts
    LOCAL_SHARED_LIBRARIES += libptscli

    Code client:
    ~~~~~~~~~~~~

    #include "pts_cli.h"

    [...]

    char name[CLIENT_NAME_LEN] = "my_app_name";
    pts_cli_handle_t * pts_hdl;
    char input[FILE_NAME_LEN] = "/dev/name"; /* Example: /dev/ptd_bt, /dev/ptd_wlan, /dev/ptd_gnss,
    or /dev/oct (TBC) */
    char output[FILE_NAME_LEN] = "/logs/name";
    e_trace_mode_t trace_mode = E_TRACE_MODE_FILE_SYSTEM;

    int pts_up(pts_cli_event_t * ev) {

        /* Do what you need */
    }

    int pts_down(pts_cli_event_t * ev) {

        /* Do what you need */
    }

    int pts_ready(pts_cli_event_t * ev) {

        /* Do what you need (for example set a flag to activate a request sending) */
    }


    int main(...) {

        pts_hdl = NULL;
        pts_cli_create_handle(&pts_hdl, name, NULL);
        pts_cli_subscribe_event(pts_hdl, pts_up, E_PTS_EVENT_PTS_UP);
        pts_cli_subscribe_event(pts_hdl, pts_down, E_PTS_EVENT_PTS_DOWN);
        pts_cli_subscribe_event(pts_hdl, pts_ready, E_PTS_EVENT_TRACE_READY);

        pts_cli_set_trace_parameters(pts_hdl, E_PTS_PARAM_INPUT_PATH, input);
        pts_cli_set_trace_parameters(pts_hdl, E_PTS_PARAM_OUTPUT_PATH, output);
        pts_cli_set_trace_parameters(pts_hdl, E_PTS_PARAM_TRACE_MODE, &trace_mode);

        pts_cli_send_configuration(pts_hdl);

        [...]

        /* If flag of pts_ready is set, you can send a request to start trace routing */
        pts_cli_requests_t request;
        fd = open(pc_input, O_WRONLY | O_NONBLOCK);
        request.id = E_PTS_REQUEST_TRACE_START;
        request.len = 0;
        pts_cli_send_request(pts_hdl, &request);

        [...]

        pts_cli_remove_configuration(pts_hdl);
        pts_cli_delete_handle(pts_hdl);

        [...]

        return 0;
    }
