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

package com.intel.crashreport.specific;

import com.intel.crashreport.CustomizableEventData;
import com.intel.crashreport.GeneralEventGenerator;

import android.content.Context;


public enum EventGenerator {
	INSTANCE;

	private Context mContext=null;

	public void setContext(Context aContext){
		mContext = aContext;
	}

	public CustomizableEventData getEmptyInfoEvent(){
		CustomizableEventData result = new CustomizableEventData();
		result.setEventName("INFO");
		return result;
	}

	public CustomizableEventData getEmptyErrorEvent(){
		CustomizableEventData result = new CustomizableEventData();
		result.setEventName("ERROR");
		return result;
	}

	public CustomizableEventData getEmptyStatsEvent(){
		CustomizableEventData result = new CustomizableEventData();
		result.setEventName("STATS");
		return result;
	}

	public CustomizableEventData getEmptyRainEvent(){
		CustomizableEventData result = new CustomizableEventData();
		result.setEventName("RAIN");
		return result;
	}

	/**
	 * @brief Generate an event of RAIN type and add it to the events db
	 *
	 * @param signature is the input signature of the RAIN event to generate
	 * @param occurences is the number of crashes contained by the rain event
	 * @return true is the generation is successful. False otherwise.
	 */
	public boolean generateEventRain(RainSignature signature, int occurences) {
		CustomizableEventData eventRain = getEmptyRainEvent();
		eventRain.setType(signature.getType());
		eventRain.setData0(signature.getData0());
		eventRain.setData1(signature.getData1());
		eventRain.setData2(signature.getData2());
		eventRain.setData3(signature.getData3());
		eventRain.setData4(""+occurences);
		GeneralEventGenerator.INSTANCE.setContext(mContext);
		return GeneralEventGenerator.INSTANCE.generateEvent(eventRain,false);
	}
}
