LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SRC_FILES:= \
    logreader.c \
    send_intent.c \
    send_event.c
LOCAL_MODULE_TAGS := eng debug
LOCAL_MODULE:= liblogreader
LOCAL_SHARED_LIBRARIES:= liblog libc libcutils
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_COPY_HEADERS := logreader.h
LOCAL_COPY_HEADERS_TO := logreader
include $(BUILD_COPY_HEADERS)
