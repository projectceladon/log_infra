/* Phone Doctor (CLOTA)
 *
 * Copyright (C) Intel 2012
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Nicolas Benoit <nicolasx.benoit@intel.com>
 */

package com.intel.crashreport;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import com.intel.phonedoctor.utils.FileOps;
import com.intel.crashreport.PDStatus.PDSTATUS_TIME;

import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;


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
		return generateEvent(eventRain,false);
	}

	public boolean generateEvent(CustomizableEventData aEventData,boolean toNotify) {
		boolean bResult = true;
		if (mContext != null){
			EventDB db = new EventDB(mContext);
			try {
				db.open();
				Event event = new Event();
				Date date= new Date();
				SimpleDateFormat EVENT_DF_GEN = new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss");
				String displayDate = EVENT_DF_GEN.format(date);
				try {
					EVENT_DF_GEN.setTimeZone(TimeZone.getTimeZone("GMT"));
					date = EVENT_DF_GEN.parse(displayDate);
				} catch (ParseException e) {
					date = new Date();
				}
				event.setDate(date);
				event.setEventName(aEventData.getEventName());
				event.setType(aEventData.getType());
				event.setData0(aEventData.getData0());
				event.setData1(aEventData.getData1());
				event.setData2(aEventData.getData2());
				event.setData3(aEventData.getData3());
				event.setData4(aEventData.getData4());
				event.setData5(aEventData.getData5());
				event.setCrashDir(aEventData.getCrashDir());
				Build myBuild = new Build();
				myBuild.fillBuildWithSystem();

				event.setBuildId(myBuild.getBuildId());
				event.readDeviceIdFromFile();
				event.setImei(event.readImeiFromSystem());

				String SHA1String = event.getBuildId() + event.getDeviceId() + event.getEventName() +
						event.getType() + event.getDateAsString();

				event.setEventId(sha1Hash(SHA1String));
				PDStatus.INSTANCE.setContext(mContext);
				event.setPdStatus(PDStatus.INSTANCE.computePDStatus(event, PDSTATUS_TIME.INSERTION_TIME));

				db.addEvent(event);
				db.close();
				if (toNotify) {
					Intent intent = new Intent("com.intel.crashreport.intent.START_CRASHREPORT");
					mContext.sendBroadcast(intent);
				}
			} catch (Exception e) {
				Log.w("generateEvent: Exception : ", e);
				bResult = false;
			}
			return bResult;
		}else{
			Log.e("generateEvent/mContext is null");
			return false;
		}
	}

	public boolean generateEvent(CustomizableEventData aEventData){
		return generateEvent(aEventData,true);
	}

	private String sha1Hash( String toHash ){
		String hash = null;
		try
		{
			MessageDigest digest = MessageDigest.getInstance( "SHA-1" );
			digest.update( toHash.getBytes(), 0, toHash.length() );
			hash = new BigInteger( 1, digest.digest() ).toString( 16 ).substring(0, 20);
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return hash;
	}

	/**
	 * Provide an empty directory, ready to receive event data files.
	 *
	 * @return the directory
	 * @throws FileNotFoundException if it's not possible to provide a directory
	 */
	public File getNewEventDirectory() throws FileNotFoundException {
		if (mContext != null) {
			File dir = new File(new ApplicationPreferences(mContext).getNewEventDirectoryName());
			if (dir.exists())
				FileOps.delete(dir);
			dir.mkdirs();
			return dir;
		}
		throw new FileNotFoundException();
	}

	public String getImei() {
		String deviceId;
		try {
			TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
			deviceId = tm.getDeviceId();
		}catch(NullPointerException e){
			return "";
		}
		if(deviceId == null)
			deviceId = "";
		return deviceId;
	}
}
