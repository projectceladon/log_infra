# Copyright 2012 INTEL - crashparsing
#
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := crashparsing
LOCAL_MODULE_OWNER := intel
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-subdir-java-files)
include $(BUILD_STATIC_JAVA_LIBRARY)
