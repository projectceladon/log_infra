/* Copyright (C) Intel 2013
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <cutils/log.h>

#undef LOG_TAG
#define LOG_TAG "LogReader"

// Uncomment to debug (LOGV)
// #undef LOG_NDEBUG
// #define LOG_NDEBUG 0

#define LOG_V(fmt, ...) ALOGV("%s: " fmt, __FUNCTION__, ##__VA_ARGS__)

#define LOG_D(fmt, ...) ALOGD("%s: " fmt, __FUNCTION__, ##__VA_ARGS__)

#define LOG_I(fmt, ...) ALOGI("%s: " fmt, __FUNCTION__, ##__VA_ARGS__)

#define LOG_W(fmt, ...) ALOGW("%s: " fmt, __FUNCTION__, ##__VA_ARGS__)

#define LOG_E(fmt, ...) ALOGE("%s: " fmt, __FUNCTION__, ##__VA_ARGS__)

