
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
