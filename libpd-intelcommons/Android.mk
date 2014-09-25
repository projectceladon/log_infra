# Copyright 2014 INTEL - intelcommons
#
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := libpd-intelcommons
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-subdir-java-files)

include $(BUILD_STATIC_JAVA_LIBRARY)
