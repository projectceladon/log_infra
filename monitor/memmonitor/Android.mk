# memmonitor
LOCAL_PATH := $(call my-dir)

# $1 is the *.sh file to copy

define script_to_copy
include $(CLEAR_VARS)
LOCAL_MODULE := $(1)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_OWNER := intel
LOCAL_SRC_FILES := $(1)
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_PROPRIETARY_MODULE := true
include $(BUILD_PREBUILT)

endef

script_list := do_gather.sh memmonitor_hours.sh memmonitor_seconds.sh memmonitor.sh

$(foreach script,$(script_list),$(eval $(call script_to_copy,$(script))))