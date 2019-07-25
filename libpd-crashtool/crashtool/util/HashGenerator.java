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

package com.intel.crashtool.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import com.intel.crashtoolserver.bean.Event;

public class HashGenerator {
	
	
	public static String sha1Hash(final String toHash ){ 
		
		String hash = null; 
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update( toHash.getBytes(), 0, toHash.length() ); 
			hash = new BigInteger( 1, digest.digest() ).toString( 16 ).substring(0, 20);
		} 
		catch (NoSuchAlgorithmException e) {
		} 
		return hash;
	}
	
	public static String getUniqueEventId (Event e) {
	       String sHA1String = e.getBuild().getBuildId() + e.getDevice().getDeviceId() + e.getEvent() + e.getType() + e.getDate();
	       return HashGenerator.sha1Hash(sHA1String);
	}
}
