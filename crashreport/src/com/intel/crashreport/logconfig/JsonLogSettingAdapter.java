/* INTEL CONFIDENTIAL
 * Copyright 2015 Intel Corporation
 *
 * The source code contained or described herein and all documents
 * related to the source code ("Material") are owned by Intel
 * Corporation or its suppliers or licensors. Title to the Material
 * remains with Intel Corporation or its suppliers and
 * licensors. The Material contains trade secrets and proprietary
 * and confidential information of Intel or its suppliers and
 * licensors. The Material is protected by worldwide copyright and
 * trade secret laws and treaty provisions. No part of the Material
 * may be used, copied, reproduced, modified, published, uploaded,
 * posted, transmitted, distributed, or disclosed in any way without
 * Intel's prior express written permission.
 *
 * No license under any patent, copyright, trade secret or other
 * intellectual property right is granted to or conferred upon you
 * by disclosure or delivery of the Materials, either expressly, by
 * implication, inducement, estoppel or otherwise. Any license under
 * such intellectual property rights must be express and approved by
 * Intel in writing.
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
            return new String("com.intel.crashreport.logconfig.bean.FSLogSetting");
        else if (name.contentEquals("prop"))
            return new String("com.intel.crashreport.logconfig.bean.PropertyLogSetting");
        else if (name.contentEquals("event"))
            return new String("com.intel.crashreport.logconfig.bean.EventTagLogSetting");
        else if (name.contentEquals("intent"))
            return new String("com.intel.crashreport.logconfig.bean.IntentLogSetting");
        return null;
    }

    private static String getIdFromClass(LogSetting c) {
        String fullName = c.getClass().getName();
        if (fullName.endsWith("FSLogSetting"))
            return new String("file");
        else if (fullName.endsWith("PropertyLogSetting"))
            return new String("prop");
        else if (fullName.endsWith("EventTagLogSetting"))
            return new String("event");
        else if (fullName.endsWith("IntentLogSetting"))
            return new String("intent");
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
