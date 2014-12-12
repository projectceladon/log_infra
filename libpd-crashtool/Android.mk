# Copyright 2015 INTEL - crashtoolserver
#
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := libpd-crashtool
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-subdir-java-files)

include $(BUILD_STATIC_JAVA_LIBRARY)
