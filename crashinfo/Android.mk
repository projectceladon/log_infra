# Copyright 2012 INTEL - crashinfo
#
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := crashinfo
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_OWNER := intel
LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_JAVA_LIBRARIES := com.google.gson crashparsing
LOCAL_CERTIFICATE := platform
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := crashinfo-cmd
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_OWNER := intel
LOCAL_SRC_FILES := crashinfo
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE_PATH := $(TARGET_OUT_EXECUTABLES)
LOCAL_MODULE_STEM := crashinfo
LOCAL_REQUIRED_MODULES := crashinfo
include $(BUILD_PREBUILT)
