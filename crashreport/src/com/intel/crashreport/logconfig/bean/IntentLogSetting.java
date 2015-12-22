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

    public String getType() {
        return "Intent";
    }

    @Override
    public String toString() {
        return "Intent => Action: " + action;
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
                return "string";
            else if (fullName.endsWith("Integer"))
                return "int";
            else if (fullName.endsWith("Boolean"))
                return "bool";
            else
                throw new JsonParseException("Class type not supported : " + fullName);
        }

        private String getClassFromName(String name) {
            if (name.contentEquals("string"))
                return "java.lang.String";
            else if (name.contentEquals("int"))
                return "java.lang.Integer";
            else if (name.contentEquals("bool"))
                return "java.lang.Boolean";
            else
                throw new JsonParseException("Class name not supported : " + name);
        }
    };

}
