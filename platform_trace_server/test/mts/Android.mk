LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_C_INCLUDES += $(LOCAL_PATH) $(TARGET_OUT_HEADERS)/IFX-modem $(TARGET_OUT_HEADERS)/pts
LOCAL_SRC_FILES := mts.c
LOCAL_MODULE := mtspts
LOCAL_STATIC_LIBRARIES := libcutils liblog
LOCAL_SHARED_LIBRARIES := libmmgrcli libptscli
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

