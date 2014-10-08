LOCAL_PATH:= $(call my-dir)

# gson lib
include $(CLEAR_VARS)
LOCAL_MODULE := com.google.gson
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_OWNER := intel
LOCAL_CERTIFICATE := platform
LOCAL_SRC_FILES := $(call all-java-files-under, lib/gson-2.2.1-sources)
include $(BUILD_JAVA_LIBRARY)

# gson lib permissions
include $(CLEAR_VARS)
LOCAL_MODULE := com.google.gson.xml
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_OWNER := intel
LOCAL_MODULE_CLASS := ETC
# This will install the file in /system/etc/permissions
LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/permissions
LOCAL_SRC_FILES := lib/$(LOCAL_MODULE)
include $(BUILD_PREBUILT)

# CrashReport
include $(CLEAR_VARS)
LOCAL_PACKAGE_NAME := CrashReport
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_OWNER := intel
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_STATIC_JAVA_LIBRARIES := libgcmforpd
LOCAL_JAVA_LIBRARIES := com.google.gson crashparsing
LOCAL_CERTIFICATE := platform
include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
	libgcmforpd:lib/gcm.jar \

include $(BUILD_MULTI_PREBUILT)
