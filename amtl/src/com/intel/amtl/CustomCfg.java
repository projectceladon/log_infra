/* Android Modem Traces and Logs
 *
 * Copyright (C) Intel 2012
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.intel.amtl;

public class CustomCfg {

    /* Trace locations */
    public static final int TRACE_LOC_NONE = -1;
    public static final int TRACE_LOC_EMMC = 0;
    public static final int TRACE_LOC_SDCARD = 1;
    public static final int TRACE_LOC_COREDUMP = 2;
    public static final int TRACE_LOC_USB_APE = 3;
    public static final int TRACE_LOC_USB_MODEM = 4;

    /* Maximum logs size */
    public static final int LOG_SIZE_NONE = -1;
    public static final int LOG_SIZE_100_MB = 0;
    public static final int LOG_SIZE_800_MB = 1;

    /* HSI frequencies */
    public static final int HSI_FREQ_NONE = -1;
    public static final int HSI_FREQ_78_MHZ = 0;
    public static final int HSI_FREQ_156_MHZ = 1;

    /* MUX trace state */
    public static final int MUX_TRACE_OFF = 0;
    public static final int MUX_TRACE_ON = 1;

    /* Modem traces levels */
    public static final int TRACE_LEVEL_NONE = 0;
    public static final int TRACE_LEVEL_BB = 1;
    public static final int TRACE_LEVEL_BB_3G = 2;
    public static final int TRACE_LEVEL_BB_3G_DIGRF = 3;

    public int traceLocation = TRACE_LOC_EMMC;
    public int traceLevel = TRACE_LEVEL_BB_3G;
    public int traceFileSize = LOG_SIZE_800_MB;
    public int hsiFrequency = HSI_FREQ_78_MHZ;
    public int muxTrace = MUX_TRACE_OFF;
}
