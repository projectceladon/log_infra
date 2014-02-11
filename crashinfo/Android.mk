# Copyright 2012 INTEL - crashinfo
#
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := crashinfo
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_JAVA_LIBRARIES := com.google.gson crashparsing
LOCAL_CERTIFICATE := platform

include $(BUILD_JAVA_LIBRARY)
