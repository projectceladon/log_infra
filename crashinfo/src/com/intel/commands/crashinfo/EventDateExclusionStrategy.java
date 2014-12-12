package com.intel.commands.crashinfo;

import java.util.Date;

import com.intel.crashtoolserver.bean.Event;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class EventDateExclusionStrategy implements ExclusionStrategy {

	@Override
	public boolean shouldSkipField(FieldAttributes f) {
		return f.getDeclaringClass() == Event.class && f.getDeclaredClass() == Date.class && "date".equals(f.getName());
	}

	@Override
	public boolean shouldSkipClass(Class<?> clazz) {
		return false;
	}

}


