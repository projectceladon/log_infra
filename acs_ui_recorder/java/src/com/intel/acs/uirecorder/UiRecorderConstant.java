/* ACS UI Recorder
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
 * Author: Julien Reynaud <julienx.reynaud@intel.com>
 */


package com.intel.acs.uirecorder;

/**
 * This class implements all <i>Constants</i> used in framework.
 */
public class UiRecorderConstant {

    public static final int STATUS_READY = 0;
    public static final int STATUS_RECORDING = 1;
    public static final int STATUS_REPLAYING = 2;
    public static final int STATUS_UNKNOWN = 42;

    /**
     * Shared preferences to used to store persistent variable values.
     */
    public static final String ACS_UI_REC_SHARED_PREFERENCES = "acs_ui_recorder_settings";
    /**
     * Persistent option Disclaimer accepted after board reboot persistent variable.
     */
    public static final String DISCLAIMER_ACCEPTED = "disclaimer_accepted";

}
