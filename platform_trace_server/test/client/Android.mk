LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_C_INCLUDES += $(LOCAL_PATH) $(TARGET_OUT_HEADERS)/pts
LOCAL_SRC_FILES := test_client.c
LOCAL_MODULE := ptscli_test
LOCAL_SHARED_LIBRARIES := libcutils libptscli
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

