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

package com.intel.crashreport;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.telephony.TelephonyManager;

import com.intel.crashreport.bugzilla.BZFile;
import com.intel.crashreport.specific.Build;
import com.intel.crashreport.specific.Event;
import com.intel.crashreport.database.EventDB;
import com.intel.crashreport.specific.PDStatus;
import com.intel.crashreport.specific.PDStatus.PDSTATUS_TIME;
import com.intel.phonedoctor.utils.FileOps;

public enum GeneralEventGenerator {
	INSTANCE;

	private Context mContext=null;

	public void setContext(Context aContext){
		mContext = aContext;
	}

	public boolean generateEvent(CustomizableEventData aEventData,boolean toNotify) {
		boolean bResult = true;
		if (mContext != null){
			EventDB db = new EventDB(mContext);
			try {
				db.open();
				Event event = new Event();
				Date date= new Date();
				String sDateTime = (new Long(date.getTime())).toString();
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
				event.setTestCase(event.fillCurrentTestInfo());
				event.setType(aEventData.getType());
				event.setData0(aEventData.getData0());
				event.setData1(aEventData.getData1());
				event.setData2(aEventData.getData2());
				event.setData3(aEventData.getData3());
				event.setData4(aEventData.getData4());
				event.setData5(aEventData.getData5());
				event.setCrashDir(aEventData.getCrashDir());
				Build myBuild = new Build(mContext);
				myBuild.fillBuildWithSystem();

				event.setBuildId(myBuild.toString());
				event.readDeviceId();
				event.setImei(event.readImeiFromSystem());

				String SHA1String = event.getBuildId() + event.getDeviceId() + event.getEventName() +
						event.getType() + sDateTime + event.getData0() +
						event.getData1() + event.getData2() + event.getData3() +
						event.getData4() + event.getData5() + event.getCrashDir();

				event.setEventId(sha1Hash(SHA1String));
				event.setVariant(Build.getVariant());
				event.setIngredients(Build.getIngredients());
				event.setUniqueKeyComponent(Build.getUniqueKeyComponent());
				PDStatus.INSTANCE.setContext(mContext);
				event.setPdStatus(PDStatus.INSTANCE.computePDStatus(event, PDSTATUS_TIME.INSERTION_TIME));

				db.addEvent(event);
				CrashReport ap = (CrashReport)mContext;
				db.updateDeviceInformation(event.getDeviceId(), event.getImei(),
						Event.getSSN(), ap.getTokenGCM(), Event.getSpid());

				if(event.getEventName().equals("BZ")){
					BZFile bzDescription = new BZFile(event.getCrashDir());
					bzDescription.setEventId(event.getEventId());
					bzDescription.setCreationDate(event.getDate());
					db.addBZ(bzDescription);
				}
				db.close();
				if (toNotify) {
					Intent intent = new Intent("com.intel.crashreport.intent.START_CRASHREPORT");
					mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
				}
				Intent DBIntent = new Intent("com.intel.crashreport.database_changed");
				mContext.sendBroadcastAsUser(DBIntent, UserHandle.CURRENT);
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

		if (mContext == null)
			return "";

		TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm == null)
			return "";

		deviceId = tm.getDeviceId();
		if(deviceId == null)
			deviceId = "";
		return deviceId;
	}
}
