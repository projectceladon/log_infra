LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := crash_package
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_OWNER := intel

LOCAL_REQUIRED_MODULES :=  \
    CrashReport \
    crashinfo-cmd \
    logconfig

include $(BUILD_PHONY_PACKAGE)
include $(call first-makefiles-under,$(LOCAL_PATH))
