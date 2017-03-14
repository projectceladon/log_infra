# INTEL CONFIDENTIAL
# Copyright 2015 Intel Corporation
#
# The source code contained or described herein and all documents
# related to the source code ("Material") are owned by Intel
# Corporation or its suppliers or licensors. Title to the Material
# remains with Intel Corporation or its suppliers and
# licensors. The Material contains trade secrets and proprietary
# and confidential information of Intel or its suppliers and
# licensors. The Material is protected by worldwide copyright and
# trade secret laws and treaty provisions. No part of the Material
# may be used, copied, reproduced, modified, published, uploaded,
# posted, transmitted, distributed, or disclosed in any way without
# Intel's prior express written permission.
#
# No license under any patent, copyright, trade secret or other
# intellectual property right is granted to or conferred upon you
# by disclosure or delivery of the Materials, either expressly, by
# implication, inducement, estoppel or otherwise. Any license under
# such intellectual property rights must be express and approved by
# Intel in writing.

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
LOCAL_PRIVILEGED_MODULE := false
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_STATIC_JAVA_LIBRARIES := libgcmforpd libpd-intelcommons com.google.gson crashparsing libpd-crashtool libpd-core
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, res)
LOCAL_RESOURCE_DIR += $(addprefix $(LOCAL_PATH)/, ../libpd-intelcommons/res)
LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_CERTIFICATE := platform
ifdef TARGET_OUT_VENDOR_APPS_PRIVILEGED
LOCAL_PROPRIETARY_MODULE := true
endif
include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
	libgcmforpd:lib/gcm.jar \

include $(BUILD_MULTI_PREBUILT)
