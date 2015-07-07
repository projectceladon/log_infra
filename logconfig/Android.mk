LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := logconfig.c
LOCAL_MODULE := logconfig
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_OWNER := intel
LOCAL_SHARED_LIBRARIES := libcutils liblog
LOCAL_PROPRIETARY_MODULE := true
include $(BUILD_EXECUTABLE)
