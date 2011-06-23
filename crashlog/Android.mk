LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= crashlogd.c

LOCAL_MODULE_TAGS := eng
LOCAL_MODULE:= crashlogd

LOCAL_UNSTRIPPED_PATH := $(TARGET_ROOT_OUT_UNSTRIPPED)

LOCAL_STATIC_LIBRARIES:= libc libcutils

include $(BUILD_EXECUTABLE)

PRODUCT_COPY_FILES += \
        $(LOCAL_PATH)/del_hist.sh:system/bin/del_hist.sh \
        $(LOCAL_PATH)/del_log.sh:system/bin/del_log.sh