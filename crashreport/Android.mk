#
# Copyright (C) 2019 Intel Corporation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

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
LOCAL_STATIC_JAVA_LIBRARIES := libgcmforpd com.google.gson crashparsing libpd-crashtool libpd-core
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, res)
LOCAL_RESOURCE_DIR += $(addprefix $(LOCAL_PATH)/, themes)
LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_CERTIFICATE := platform
LOCAL_PRIVATE_PLATFORM_APIS := true
include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
	libgcmforpd:lib/gcm.jar \

include $(BUILD_MULTI_PREBUILT)
