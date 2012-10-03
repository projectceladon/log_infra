
package com.intel.crashreport.logconfig.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;

import android.content.Intent;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class IntentLogSetting implements LogSetting {

    private String action;
    private List<IntentExtra> extras;

    public IntentLogSetting() {
    }

    public IntentLogSetting(String action) {
        this.setAction(action);
    }

    public IntentLogSetting(String action, List<IntentExtra> extras) {
        this.setAction(action);
        this.setExtras(extras);
    }

    @Override
    public String getType() {
        return "Intent";
    }

    @Override
    public String toString() {
        return new String("Intent => Action: " + action);
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<IntentExtra> getExtras() {
        return extras;
    }

    public void setExtras(List<IntentExtra> extras) {
        this.extras = extras;
    }

    public static void addExtraToIntent(IntentExtra e, Intent i) {
        String className = e.getValue().getClass().getName();
        if (className.endsWith("String"))
            i.putExtra(e.getKey(), (String) e.getValue());
        else if (className.endsWith("Integer"))
            i.putExtra(e.getKey(), (Integer) e.getValue());
        else if (className.endsWith("Boolean"))
            i.putExtra(e.getKey(), ((Boolean) e.getValue()).booleanValue());
        else
            throw new IllegalArgumentException(className + " not supported as Intent Extra");
    }

    public class IntentExtra {

        private String key;
        private Object value;

        public IntentExtra() {
        }

        public IntentExtra(String key, Object value) {
            this.setKey(key);
            this.setValue(value);
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

    }

    public class JsonIntentExtraAdapter implements JsonSerializer, JsonDeserializer {

        public JsonIntentExtraAdapter() {
        }

        public JsonElement serialize(Object object, Type interfaceType,
                JsonSerializationContext context) {
            final JsonObject wrapper = new JsonObject();
            final IntentExtra mIntentExtra = (IntentExtra) object;
            wrapper.addProperty("type", getNameFromClass(mIntentExtra.getValue()));
            wrapper.addProperty("key", mIntentExtra.getKey());
            wrapper.addProperty("value", mIntentExtra.getValue().toString());
            return wrapper;
        }

        public IntentExtra deserialize(JsonElement elem, Type interfaceType,
                JsonDeserializationContext context) throws JsonParseException {
            final JsonObject wrapper = (JsonObject) elem;
            final JsonElement typeName = get(wrapper, "type");
            final JsonElement key = get(wrapper, "key");
            final JsonElement value = get(wrapper, "value");
            final Type actualType = typeForName(typeName);
            try {
                final Constructor<? extends Object> valueConstructor = ((Class<? extends Object>) actualType)
                        .getConstructor(new Class[] { String.class });
                final Object valueObject = valueConstructor.newInstance(value.getAsString());
                return new IntentExtra(key.getAsString(), valueObject);
            } catch (SecurityException e) {
                throw new JsonParseException(e);
            } catch (NoSuchMethodException e) {
                throw new JsonParseException(e);
            } catch (IllegalArgumentException e) {
                throw new JsonParseException(e);
            } catch (InstantiationException e) {
                throw new JsonParseException(e);
            } catch (IllegalAccessException e) {
                throw new JsonParseException(e);
            } catch (InvocationTargetException e) {
                throw new JsonParseException(e);
            }
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

        private String getNameFromClass(Object c) {
            String fullName = c.getClass().getName();
            if (fullName.endsWith("String"))
                return new String("string");
            else if (fullName.endsWith("Integer"))
                return new String("int");
            else if (fullName.endsWith("Boolean"))
                return new String("bool");
            else
                throw new JsonParseException("Class type not supported : " + fullName);
        }

        private String getClassFromName(String name) {
            if (name.contentEquals("string"))
                return new String("java.lang.String");
            else if (name.contentEquals("int"))
                return new String("java.lang.Integer");
            else if (name.contentEquals("bool"))
                return new String("java.lang.Boolean");
            else
                throw new JsonParseException("Class name not supported : " + name);
        }
    };

}
