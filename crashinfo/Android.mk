# Copyright 2012 INTEL - crashinfo
#
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := crashinfo
LOCAL_MODULE_TAGS := eng debug
LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_JAVA_LIBRARIES := crashparsing
LOCAL_CERTIFICATE := platform

include $(BUILD_JAVA_LIBRARY)