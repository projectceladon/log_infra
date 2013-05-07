package com.intel.crashreport;

import com.intel.crashreport.specific.EventGenerator;

public enum GcmEvent {

	INSTANCE;

	public void registerGcm(String registrationId) {
		CustomizableEventData mEvent = generateGcmRegisterEvent();
		mEvent.setData1("ON");
		mEvent.setData2(registrationId);
		GeneralEventGenerator.INSTANCE.generateEvent(mEvent);
	}

	public void unregisterGcm() {
		CustomizableEventData mEvent = generateGcmRegisterEvent();
		mEvent.setData1("OFF");
		GeneralEventGenerator.INSTANCE.generateEvent(mEvent);
	}

	public void enableGcm() {
		CustomizableEventData mEvent = generateGcmActivationEvent();
		mEvent.setData1("ON");
		GeneralEventGenerator.INSTANCE.generateEvent(mEvent);
	}

	public void disableGcm() {
		CustomizableEventData mEvent = generateGcmActivationEvent();
		mEvent.setData1("OFF");
		GeneralEventGenerator.INSTANCE.generateEvent(mEvent);
	}

	private CustomizableEventData generateGcmRegisterEvent() {
		CustomizableEventData mEvent = EventGenerator.INSTANCE.getEmptyInfoEvent();
		mEvent.setType("GCM");
		mEvent.setData0("REGISTER_ID");
		return mEvent;
	}

	private CustomizableEventData generateGcmActivationEvent() {
		CustomizableEventData mEvent = EventGenerator.INSTANCE.getEmptyInfoEvent();
		mEvent.setType("GCM");
		mEvent.setData0("USER_ACTION");
		return mEvent;
	}

}
