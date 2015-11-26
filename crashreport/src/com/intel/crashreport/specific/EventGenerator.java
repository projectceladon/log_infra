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
