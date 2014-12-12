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
