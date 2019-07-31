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

package com.intel.crashtoolserver.bean;

import java.io.Serializable;

public class ModemVariant implements Serializable {

    private static final long serialVersionUID = -8734276160104789948L;

    private Long id;

    private String name;
    
    /**
     * Used by the server in order to retrieve the right modemVariant when
     * rowSwConfig is blank
     */
    private String regexpr;

    private boolean defaultModem;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegexpr() {
        return regexpr;
    }

    public void setRegexpr(String regexpr) {
        this.regexpr = regexpr;
    }

    public boolean isDefaultModem() {
        return defaultModem;
    }

    public void setDefaultModem(boolean defaultModem) {
        this.defaultModem = defaultModem;
    }

    @Override
    public String toString() {
        return "ModemVariant [id=" + id + ", name=" + name + ", regexpr="
                + regexpr + ", defaultModem=" + defaultModem + "]";
    }
}
