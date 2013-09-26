#############################################
# PLATFORM TRACE SERVER daemon
#############################################
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := pts
LOCAL_CFLAGS += -Wall -Wvla

LOCAL_C_INCLUDES += $(TARGET_OUT_HEADERS)/pts

LOCAL_SRC_FILES:= \
    client_cnx.c \
    client_events.c \
    client_trace.c \
    client.c \
    data_to_msg.c \
    events_manager.c \
    msg_to_data.c \
    pts_manager.c

LOCAL_MODULE_TAGS := optional
LOCAL_SHARED_LIBRARIES := libcutils

#uncomment this to enable gcov
#LOCAL_CFLAGS += -fprofile-arcs -ftest-coverage
#LOCAL_LDFLAGS += -fprofile-arcs -ftest-coverage -lgcov
include $(BUILD_EXECUTABLE)
