#############################################
# PTS interface file copy
#############################################
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_COPY_HEADERS := \
    pts_cli.h \
    pts.h

LOCAL_COPY_HEADERS_TO := pts
include $(BUILD_COPY_HEADERS)

