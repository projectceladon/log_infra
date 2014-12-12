# Copyright 2012 INTEL - crashinfo
#
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := crashinfo
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_OWNER := intel
LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_STATIC_JAVA_LIBRARIES := com.google.gson crashparsing libpd-crashtool
LOCAL_CERTIFICATE := platform
LOCAL_PROPRIETARY_MODULE := true
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := crashinfo-cmd
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_OWNER := intel
LOCAL_SRC_FILES := crashinfo
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE_STEM := crashinfo
LOCAL_REQUIRED_MODULES := crashinfo
LOCAL_PROPRIETARY_MODULE := true
include $(BUILD_PREBUILT)
