# Copyright 2012 INTEL - crashinfo
#
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := crashinfo
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-subdir-java-files)

include $(BUILD_JAVA_LIBRARY)

PRODUCT_COPY_FILES += \
	$(LOCAL_PATH)/crashinfo:system/bin/crashinfo
