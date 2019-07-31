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

package com.intel.crashreport.logconfig.bean;

public class FSLogSetting implements LogSetting {

    private String path;
    private String value;
    private Boolean append;

    public FSLogSetting() {
    }

    public FSLogSetting(String path, String value, Boolean append) {
        this.path = path;
        this.value = value;
        this.append = append;
    }

    public String toString() {
        return "File => Path: " + path + " Value: " +
                value + " Append: " + append;
    }

    public String getType() {
        return "FS";
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getAppend() {
        return append;
    }

    public void setAppend(Boolean append) {
        this.append = append;
    }

}
