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

package com.intel.crashreport.logconfig;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.intel.crashreport.logconfig.bean.LogSetting;

@SuppressWarnings("rawtypes")
class JsonLogSettingAdapter implements JsonDeserializer, JsonSerializer {

    private JsonElement getElem(final JsonObject container, String name) {
        final JsonElement element = container.get(name);
        if (element == null)
            throw new JsonParseException(name + "not found in container");
        return element;
    }

    private Type classFromName(final JsonElement element) {
        try {
            return Class.forName(getClassFromId(element.getAsString()));
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e);
        }
    }

    private static String getClassFromId(String name) {
        if (name.contentEquals("file"))
            return "com.intel.crashreport.logconfig.bean.FSLogSetting";
        else if (name.contentEquals("prop"))
            return "com.intel.crashreport.logconfig.bean.PropertyLogSetting";
        else if (name.contentEquals("event"))
            return "com.intel.crashreport.logconfig.bean.EventTagLogSetting";
        else if (name.contentEquals("intent"))
            return "com.intel.crashreport.logconfig.bean.IntentLogSetting";
        return null;
    }

    private static String getIdFromClass(LogSetting c) {
        String fullName = c.getClass().getName();
        if (fullName.endsWith("FSLogSetting"))
            return "file";
        else if (fullName.endsWith("PropertyLogSetting"))
            return "prop";
        else if (fullName.endsWith("EventTagLogSetting"))
            return "event";
        else if (fullName.endsWith("IntentLogSetting"))
            return "intent";
        return null;
    }

    public LogSetting deserialize(JsonElement element, Type t,
                                  JsonDeserializationContext ctx) throws JsonParseException {
        final JsonObject container = (JsonObject) element;
        final JsonElement className = getElem(container, "type");
        final JsonElement content = getElem(container, "data");
        final Type myClass = classFromName(className);
        return ctx.deserialize(content, myClass);
    }

    public JsonElement serialize(Object obj, Type t, JsonSerializationContext ctx) {
        final JsonObject container = new JsonObject();
        container.addProperty("type", getIdFromClass((LogSetting) obj));
        container.add("data", ctx.serialize(obj));
        return container;
    }

}
