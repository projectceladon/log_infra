
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
class JsonLogSettingAdapter implements JsonSerializer, JsonDeserializer {

    public JsonElement serialize(Object object, Type interfaceType, JsonSerializationContext context) {
        final JsonObject wrapper = new JsonObject();
        wrapper.addProperty("type", getNameFromClass((LogSetting) object));
        wrapper.add("data", context.serialize(object));
        return wrapper;
    }

    public LogSetting deserialize(JsonElement elem, Type interfaceType,
            JsonDeserializationContext context) throws JsonParseException {
        final JsonObject wrapper = (JsonObject) elem;
        final JsonElement typeName = get(wrapper, "type");
        final JsonElement data = get(wrapper, "data");
        final Type actualType = typeForName(typeName);
        return context.deserialize(data, actualType);
    }

    private Type typeForName(final JsonElement typeElem) {
        try {
            return Class.forName(getClassFromName(typeElem.getAsString()));
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e);
        }
    }

    private JsonElement get(final JsonObject wrapper, String memberName) {
        final JsonElement elem = wrapper.get(memberName);
        if (elem == null)
            throw new JsonParseException("no '" + memberName
                    + "' member found in what was expected to be an interface wrapper");
        return elem;
    }

    private static String getNameFromClass(LogSetting c) {
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

    private static String getClassFromName(String name) {
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
}
