/* Copyright (C) 2019 Intel Corporation
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

package com.intel.crashreport.common;

import java.util.List;
import java.util.Arrays;

import android.os.SystemProperties;

public class Constants {
	public static final int BEGIN_FIBONACCI = 13;
	public static final int BEGIN_FIBONACCI_BEFORE = 8;

	public static final String LOGS_DIR = SystemProperties.get("persist.vendor.crashlogd.root", "/logs");
	public static final String PRODUCT_PROPERTY_NAME = "ro.build.product";
}
