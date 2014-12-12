LOCAL_PATH:= $(call my-dir)

# gson lib
include $(CLEAR_VARS)
LOCAL_MODULE := com.google.gson
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_OWNER := intel
LOCAL_CERTIFICATE := platform
LOCAL_SRC_FILES := $(call all-java-files-under, lib/gson-2.2.1-sources)
include $(BUILD_STATIC_JAVA_LIBRARY)

# CrashReport
include $(CLEAR_VARS)
LOCAL_PACKAGE_NAME := CrashReport
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_OWNER := intel
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_STATIC_JAVA_LIBRARIES := libgcmforpd libpd-intelcommons com.google.gson crashparsing libpd-crashtool
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, res)
LOCAL_RESOURCE_DIR += $(addprefix $(LOCAL_PATH)/, ../libpd-intelcommons/res)
LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_CERTIFICATE := platform
LOCAL_PROPRIETARY_MODULE := true
include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
	libgcmforpd:lib/gcm.jar \

include $(BUILD_MULTI_PREBUILT)
