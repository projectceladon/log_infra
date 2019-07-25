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

include $(CLEAR_VARS)
LOCAL_MODULE := crashinfo
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_OWNER := intel
LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_STATIC_JAVA_LIBRARIES := com.google.gson crashparsing libpd-crashtool libpd-core
LOCAL_CERTIFICATE := platform
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := crashinfo-cmd
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_OWNER := intel
LOCAL_SRC_FILES := crashinfo
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_MODULE_STEM := crashinfo
LOCAL_REQUIRED_MODULES := crashinfo
include $(BUILD_PREBUILT)
