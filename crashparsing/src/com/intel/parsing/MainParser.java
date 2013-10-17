package com.intel.parsing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class MainParser{

	public static final String PATH_UUID = "/logs/uuid.txt";

	private final static String[] LEGACY_BOARD_FABRIC = {"redhookbay","victoriabay"};
	private String sOutput = null;
	private String sTag = "";
	private String sCrashID = "";
	private String sUptime = "";
	private String sBuild = "";
	private String sBoard = "";
	private String sDate = "";
	private String sImei = "";
	private Writer myOutput = null;
	private int iDataReady = 1;

	public MainParser(String aOutput, String aTag, String aCrashID, String aUptime,
			String aBuild, String aBoard, String aDate, String aImei){
		this(aOutput,aTag,aCrashID,aUptime,aBuild,aBoard,aDate,aImei,1);
	}

	public MainParser(String aOutput, String aTag, String aCrashID, String aUptime,
			String aBuild, String aBoard, String aDate, String aImei, int aData_Ready){
		sOutput = aOutput;
		sTag = aTag;
		sCrashID = aCrashID;
		sUptime = aUptime;
		sBuild = aBuild;
		sBoard = aBoard;
		sDate = aDate;
		sImei = aImei;
		iDataReady = aData_Ready;
	}

	public int execParsing(){
		String sCrashfilename= sOutput + "/crashfile";
		String sWdt = "";
		String sDropbox = "";

		if (sTag.equals("SWWDT_RESET")) {
			sTag = "WDT";
			sWdt = "KernelWatchdog";
		}

		if (sTag.equals( "HWWDT_RESET" )) {
			sTag="WDT";
			sWdt="SecureWatchdog";
		}

		if (sTag.equals( "LOST_DROPBOX_JAVACRASH" )) {
			sTag="JAVACRASH";
			sDropbox="full";
		}

		if (sTag.equals( "LOST_DROPBOX_ANR" )) {
			sTag="ANR";
			sDropbox="full";
		}


		if (sTag.equals( "LOST_DROPBOX_UIWDT")) {
			sTag="UIWDT";
			sDropbox="full";
		}

		if (sTag.equals( "LOST_DROPBOX_WTF")) {
			sTag="WTF";
			sDropbox="full";
		}

		File fOutput = new File(sOutput);

		if (fOutput.isDirectory()){
			if (prepare_crashfile( sTag, sCrashfilename,  sCrashID, sUptime, sBuild,  sBoard, sDate, sImei)) {
				if (sDropbox.equals("full" )){
					if (!fulldropbox()){
						closeOutput();
						return -1;
					}
				}

				if (sTag.equals("WDT")){
					if (!iwdt(sWdt)){
						closeOutput();
						return -1;
					}
				}
				if (sTag.equals("IPANIC") || sTag.equals("IPANIC_SWWDT") || sTag.equals("IPANIC_HWWDT") || sTag.equals("IPANIC_FAKE" )) {
					if (!ipanic(sOutput)){
						closeOutput();
						return -1;
					}
				}

				if (sTag.equals("JAVACRASH")){
					if (!javacrash(sOutput)){
						closeOutput();
						return -1;
					}
				}

				if  (sTag.equals("ANR" )){
					if (!anr(sOutput)){
						closeOutput();
						return -1;
					}
				}

				if  (sTag.equals("UIWDT" )){
					if (!uiwdt(sOutput)){
						closeOutput();
						return -1;
					}
				}

				if  (sTag.equals("WTF" )){
					if (!wtf(sOutput)){
						closeOutput();
						return -1;
					}
				}

				if (sTag.equals("TOMBSTONE")) {
					if (!tombstone(sOutput)){
						closeOutput();
						return -1;
					}
				}

				if (sTag.equals("FABRICERR") || sTag.equals("MEMERR") ||
						sTag.equals("INSTERR") || sTag.equals("SRAMECCERR") ||
						sTag.equals("HWWDTLOGERR")|| sTag.equals("FABRIC_FAKE") ||
						sTag.equals("FIRMWARE")) {
					boolean bUseNewFabric = true;
					for (String sBoardNew : LEGACY_BOARD_FABRIC){
						if (sBoardNew.equals(sBoard)){
							bUseNewFabric = false;
							break;
						}
					}
					if (bUseNewFabric){
						if (!newFabricerr(sOutput)){
							closeOutput();
							return -1;
						}
					}else{
						if (!fabricerr(sOutput)){
							closeOutput();
							return -1;
						}
					}
				}

				if (sTag.equals("MPANIC")) {
					if (!modemcrash(sOutput)){
						closeOutput();
						return -1;
					}
				}

				if (sTag.equals("APIMR")) {
					if (!genericCrash(sOutput)){
						closeOutput();
						return -1;
					}
				}
				//add generic parsing for unknown tag?

				finish_crashfile(sOutput);
				//Close the output only at the end of the process
				closeOutput();
			}else{
				return -1;
			}
		}
		return 0;
	}


	private boolean prepare_crashfile(String aTag, String aCrashfilename, String aCrashid, String aUptime,
			String aBuild, String aBoard, String aDate, String aImei) {
		boolean bResult = true;
		try{
			BufferedReader uuid = new BufferedReader(new FileReader(PATH_UUID));
			String sUuid = uuid.readLine();
			uuid.close();
			//Output object creation mandatory here
			myOutput = new BufferedWriter(new FileWriter(aCrashfilename));

			// TO DO LATER : manage other EVENTNAME than crash
			// with this mechanism only crash event is supported
			bResult &= appendToCrashfile( "EVENT=CRASH");
			bResult &= appendToCrashfile( "ID=" + aCrashid);
			bResult &= appendToCrashfile( "SN=" + sUuid);
			bResult &= appendToCrashfile( "DATE=" + aDate);
			bResult &= appendToCrashfile( "UPTIME=" + aUptime);
			bResult &= appendToCrashfile( "BUILD=" + aBuild);
			bResult &= appendToCrashfile( "BOARD=" + aBoard);
			bResult &= appendToCrashfile( "IMEI=" + aImei);
			bResult &= appendToCrashfile( "TYPE=" + aTag);
			bResult &= appendToCrashfile( "DATA_READY=" + iDataReady);

		} catch (Exception e) {
			System.err.println( "prepare_crashfile : " + e);
			e.printStackTrace();
			return false;
		}
		return bResult;
	}

	private void closeOutput(){
		if (myOutput != null){
			try{
				myOutput.close();
			}catch (Exception e) {
				System.out.println("error on crashfile close");
				e.printStackTrace();
			}
		}
	}

	private boolean fulldropbox(){
		return appendToCrashfile("DATA0=full dropbox");
	}

	private boolean finish_crashfile(String aFolder){
		boolean bResult = true;

		bResult &= appendToCrashfile("_END");
		Pattern patternSD = java.util.regex.Pattern.compile(".*mnt.*sdcard.*");
		Matcher matcherFile = patternSD.matcher(aFolder);
		if (!matcherFile.find()){
			try {
				Runtime rt = Runtime.getRuntime ();
				rt.exec("chown system.log " + aFolder);
			}catch (Exception e){
				System.err.println( "chown system.log failed : " + e);
				e.printStackTrace();
			}
		}
		return bResult;
	}

	private boolean iwdt(String aWdt){
		return appendToCrashfile("DATA0=" + aWdt);
	}

	private boolean modemcrash(String aFolder){
		boolean bResult = true;

		String sData0="";
		String sModemFile = fileGrepSearch(".*mpanic.*", aFolder);
		if (sModemFile != ""){
			try{
				BufferedReader bufModemFile = new BufferedReader(new FileReader(sModemFile));
				String sCurLine;
				while ((sCurLine = bufModemFile.readLine()) != null) {
					sData0 += sCurLine;
				}
				bResult &= appendToCrashfile("DATA0=" + sData0);
				bufModemFile.close();
			}
			catch(Exception e) {
				System.err.println( "modemcrash : " + e);
				e.printStackTrace();
				return false;
			}
		}else{
			//using default parsing method
			return genericCrash(aFolder);
		}
		return bResult;
	}

	private boolean genericCrash(String aFolder){
		boolean bResult = true;
		boolean bData0Found = false;
		boolean bData1Found = false;
		boolean bData2Found = false;
		boolean bData3Found = false;
		boolean bData4Found = false;
		boolean bData5Found = false;

		String sData0="";
		String sData1="";
		String sData2="";
		String sData3="";
		String sData4="";
		String sData5="";
		String sGenFile = fileGrepSearch(".*_crashdata", aFolder);
		if (sGenFile != ""){
			try{
				Pattern patternData0 = java.util.regex.Pattern.compile("DATA0=.*");
				Pattern patternData1 = java.util.regex.Pattern.compile("DATA1=.*");
				Pattern patternData2 = java.util.regex.Pattern.compile("DATA2=.*");
				Pattern patternData3 = java.util.regex.Pattern.compile("DATA3=.*");
				Pattern patternData4 = java.util.regex.Pattern.compile("DATA4=.*");
				Pattern patternData5 = java.util.regex.Pattern.compile("DATA5=.*");
				BufferedReader bufGenFile = new BufferedReader(new FileReader(sGenFile));
				String sCurLine;
				while ((sCurLine = bufGenFile.readLine()) != null) {
					String sTmp;
					if (!bData0Found){
						sTmp = simpleGrepAwk(patternData0, sCurLine, "=", 1, true);
						if (sTmp != null){
							sData0 = sTmp;
							bData0Found = true;
						}
					}
					if (!bData1Found){
						sTmp = simpleGrepAwk(patternData1, sCurLine, "=", 1, true);
						if (sTmp != null){
							sData1 = sTmp;
							bData1Found = true;
						}
					}
					if (!bData2Found){
						sTmp = simpleGrepAwk(patternData2, sCurLine, "=", 1, true);
						if (sTmp != null){
							sData2 = sTmp;
							bData2Found = true;
						}
					}
					if (!bData3Found){
						sTmp = simpleGrepAwk(patternData3, sCurLine, "=", 1, true);
						if (sTmp != null){
							sData3 = sTmp;
							bData3Found = true;
						}
					}
					if (!bData4Found){
						sTmp = simpleGrepAwk(patternData4, sCurLine, "=", 1, true);
						if (sTmp != null){
							sData4 = sTmp;
							bData4Found = true;
						}
					}
					if (!bData5Found){
						sTmp = simpleGrepAwk(patternData5, sCurLine, "=", 1, true);
						if (sTmp != null){
							sData5 = sTmp;
							bData5Found = true;
						}
					}
				}
				bResult &= appendToCrashfile("DATA0=" + sData0);
				bResult &= appendToCrashfile("DATA1=" + sData1);
				bResult &= appendToCrashfile("DATA2=" + sData2);
				bResult &= appendToCrashfile("DATA3=" + sData3);
				bResult &= appendToCrashfile("DATA4=" + sData4);
				bResult &= appendToCrashfile("DATA5=" + sData5);
				bufGenFile.close();
			}
			catch(Exception e) {
				System.err.println( "modemcrash : " + e);
				e.printStackTrace();
				return false;
			}
		}
		return bResult;
	}




	private boolean ipanic(String aFolder){
		boolean bResult = true;

		String sIPanicFile = fileGrepSearch(".*ipanic_console.*", aFolder);
		if (sIPanicFile != ""){
			String sData="";
			String sComm = "";
			String sPanic= "";
			boolean bDataFound = false;
			boolean bCommFound = false;
			boolean bCandidateCommFound = false;
			boolean bPanicFound = false;

			try{
				BufferedReader bufPanicFile = new BufferedReader(new FileReader(sIPanicFile));
				Pattern patternData = java.util.regex.Pattern.compile("EIP:.*SS:ESP");
				Pattern patternComm = java.util.regex.Pattern.compile("(c|C)omm: .*");
				Pattern patternPanic = java.util.regex.Pattern.compile("Kernel panic - not syncing: .*");

				String sCurLine;
				while ((sCurLine = bufPanicFile.readLine()) != null) {
					String sTmp;
					if (!bDataFound){
						sTmp = simpleGrepAwk(patternData, sCurLine, " ", 2);
						if (sTmp != null){
							sData = sTmp;
							bDataFound = true;
							if (bCandidateCommFound){
								//we use last "comm" found
								bCommFound = true;
							}
						}
					}

					if (!bCommFound){
						sTmp = simpleGrepAwk(patternComm, sCurLine, " ", 1);
						if (sTmp != null){
							sComm = sTmp;
							//considered found when data "SS:EP" is found
							//Panicfound is also a condition to store "comm"
							//if it is not found, we keep value but continue seeking pattern
							if (bDataFound || bPanicFound){
								bCommFound = true;
							}else{
								bCandidateCommFound = true;
							}
						}
					}

					if (!bPanicFound){
						sTmp = simpleGrepAwk(patternPanic, sCurLine, ":", 1);
						if (sTmp != null){
							sPanic = sTmp;
							bPanicFound = true;
						}
					}
				}

				//filter step
				// 1 - remove number after "/" pattern
				if (sComm.contains("/")){
					String sFilteredValue = simpleAwk(sComm, "/",0);
					if (sFilteredValue != null && !sFilteredValue.isEmpty()){
						sComm =  sFilteredValue;
					}
				}

				// 2 - remove thread name if "Fatal exception in interrupt"
				if (sPanic.contains("Fatal exception in interrupt")){
					sComm = "";
				}

				bResult &= appendToCrashfile("DATA0=" + sData);
				bResult &= appendToCrashfile("DATA1=" + sComm );
				bResult &= appendToCrashfile("DATA2=" + sPanic );
				if (sIPanicFile.contains("emmc_ipanic_console")){
					bResult &= appendToCrashfile("DATA3=emmc");
				} else {
					if (sIPanicFile.contains("ram_ipanic_console")){
						bResult &= appendToCrashfile("DATA3=ram");
					}
				}
				bufPanicFile.close();
			}
			catch(Exception e) {
				System.err.println( "iPanic : " + e);
				e.printStackTrace();
				return false;
			}
		}
		return bResult;
	}

	private boolean fabricerr(String aFolder){
		boolean bResult = true;

		String sFabricFile = fileGrepSearch(".*ipanic_fabric_err.*", aFolder);
		if (sFabricFile != ""){
			String sData0 = "";
			String sData1 = "";
			String sData2 = "";
			boolean bData0Found = false;
			boolean bData1Found = false;
			boolean bData2Found = false;
			boolean bForcedFabric = false;
			ArrayList<String> ldata1_2 = new ArrayList<String>();

			try{
				BufferedReader bufFabricFile = new BufferedReader(new FileReader(sFabricFile));
				Pattern patternForcedFabric = java.util.regex.Pattern.compile(".*HW WDT expired.*");
				//suspicious regex repeating r has no effect
				//   data0=`grep "DW0:" $1/ipanic_fabric_err*`
				//   data1=`grep "DW1:" $1/ipanic_fabric_err*`
				//   data2=`grep "DW11:" $1/ipanic_fabric_err*`
				Pattern patternData0 = java.util.regex.Pattern.compile(".*DW0:.*");
				Pattern patternData1 = java.util.regex.Pattern.compile(".*DW1:.*");
				Pattern patternData2 = java.util.regex.Pattern.compile(".*DW11:.*");
				Pattern patternData0_1_2 = java.util.regex.Pattern.compile(".*[erroir|:].*");
				Pattern patternInvertData0_1_2 = java.util.regex.Pattern.compile(".*(Fabric Error|summary|Additional|Decoded).*");

				String sCurLine;
				//First loop for checking force_fabric
				while ((sCurLine = bufFabricFile.readLine()) != null) {
					String sTmp;
					sTmp = simpleGrepAwk(patternForcedFabric, sCurLine, "", 0);
					if (sTmp != null){
						bForcedFabric = true;
						break;
					}
				}
				bufFabricFile.close();
				//No proper reinit method, need to recreate stream
				bufFabricFile = new BufferedReader(new FileReader(sFabricFile));
				while ((sCurLine = bufFabricFile.readLine()) != null) {
					//data0=`grep "[erroir|:]" $1/ipanic_fabric_err* | grep -v -E 'summary|Additional|Decoded' | grep -m1 ".*" | awk -F"[" '{print $1}'`
					String sTmp;
					if (bForcedFabric){
						if (!bData0Found){
							sTmp = simpleGrepAwk(patternData0, sCurLine, "", 0);
							if (sTmp != null){
								sData0 = sTmp;
								bData0Found = true;
							}
						}
						if (!bData1Found){
							sTmp = simpleGrepAwk(patternData1, sCurLine, "", 0);
							if (sTmp != null){
								sData1 = sTmp;
								bData1Found = true;
							}
						}
						if (!bData2Found){
							sTmp = simpleGrepAwk(patternData2, sCurLine, "", 0);
							if (sTmp != null){
								sData2 = sTmp;
								bData2Found = true;
							}
						}
					}else{
						if (checkGrepInvertGrep(patternData0_1_2,patternInvertData0_1_2,sCurLine) )	{
							ldata1_2.add(sCurLine);
							if (!bData0Found){
								sTmp = simpleAwk(sCurLine,"\\[", 0);
								if (sTmp != null){
									sData0 = sTmp;
									bData0Found = true;
								}
							}
						}
					}
				}
				if (!bForcedFabric){
					//	data1=`grep "[erroir|:]" $1/ipanic_fabric_err* | grep -v -E 'summary|Additional|Decoded' | grep -m2 ".*" | tail -1 | awk -F"(" '{print $1}'`
					if (ldata1_2.size()>= 2){
						sData1 = ldata1_2.get(1);
					}else if (ldata1_2.size() == 1){
						sData1 = ldata1_2.get(0);
					}
					sData1 = simpleAwk(sData1,"\\(", 0);
					//	data2=`grep "[erroir|:]" $1/ipanic_fabric_err* | grep -v -E 'summary|Additional|Decoded' | tail -n +3 | grep -m4 ".*" | sed  -e "N;s/\n/ /" | sed -e "N;s/\n/ /"`
					int iDebData2 = 2;
					int iendData2 = java.lang.Math.min(ldata1_2.size(),iDebData2+4);
					for (int i = iDebData2; i < iendData2; i++) {
						if (i == iDebData2){
							sData2 = ldata1_2.get(i);
						}else {
							sData2 += " " + ldata1_2.get(i);
						}
					}
				}
				bResult &= appendToCrashfile("DATA0=" + sData0);
				bResult &= appendToCrashfile("DATA1=" + sData1);
				bResult &= appendToCrashfile("DATA2=" + sData2);
				bufFabricFile.close();
			}
			catch(Exception e) {
				System.err.println( "fabricerr : " + e);
				e.printStackTrace();
				return false;
			}
		}
		return bResult;
	}

	private boolean isDataLine(String sTestString){
		//ignore "* " line
		if (sTestString.startsWith("* ")){
			return false;
		}
		//ignore "---------" line
		if (sTestString.startsWith("---------")){
			return false;
		}
		//ignore blank line
		if (sTestString.trim().length() == 0){
			return false;
		}
		return true;
	}

	private boolean newFabricerr(String aFolder){
		boolean bResult = true;

		String sFabricFile = fileGrepSearch(".*ipanic_fabric_err.*", aFolder);
		if (sFabricFile != ""){
			String sData0 = "";
			String sData1 = "";
			String sData2 = "";
			String sDataHole = "";
			boolean bData0_1Found = false;
			boolean bPatternStart0_1Found = false;
			boolean bData2Found = false;
			int iSubData0Count=0;
			int iSubData1Count=0;
			boolean bSubData2Found = false;
			int iSubData2Count=0;
			boolean bDataHoleFound = false;
			boolean bSubDataHoleFound = false;
			int iSubDataHoleCount=0;

			try{
				Pattern patternData0_1 = java.util.regex.Pattern.compile("Summary of Fabric Error detail:");
				Pattern patternData2 = java.util.regex.Pattern.compile(".*ERROR LOG.*");
				Pattern patternHole = java.util.regex.Pattern.compile(".*Address Hole.*");
				String sCurLine;

				BufferedReader bufFabricFile = new BufferedReader(new FileReader(sFabricFile));
				while ((sCurLine = bufFabricFile.readLine()) != null) {

					if (!bData0_1Found){
						//check sub data first
						if (bPatternStart0_1Found){
							//search should stop if pattern Data2 has been found
							if (bSubData2Found){
								bData0_1Found = true;
							}else{
								//need to check that line is eligible for data content
								if (isDataLine(sCurLine)){
									//just concat the 2 following lines
									if (iSubData0Count < 2){
										if (!sData0.equals("")){
											sData0 += " / ";
										}
										sData0 += sCurLine;
										iSubData0Count++;
									}else if (iSubData1Count < 2){
										if (!sData1.equals("")){
											sData1 += " / ";
										}
										sData1 += sCurLine;
										iSubData1Count++;
									}
									if (iSubData1Count >= 2){
										bData0_1Found = true;
									}
								}
							}
						}else{
							String sTmpData0;
							sTmpData0 = simpleGrepAwk(patternData0_1, sCurLine, "", 0);
							if (sTmpData0 != null){
								iSubData0Count = 0;
								iSubData1Count = 0;
								bPatternStart0_1Found = true;
								sData0 = "";
								sData1 = "";
							}
						}
					}

					if (!bDataHoleFound){
						//check sub data first
						if (bSubDataHoleFound){
							if (isDataLine(sCurLine)){
								//just concat 4 following lines
								if (iSubDataHoleCount < 4){
									if (!sDataHole.equals("")){
										sDataHole += " / ";
									}else{
										sDataHole = "Address hole : ";
									}
									sDataHole += sCurLine;
									iSubDataHoleCount++;
								}else{
									bDataHoleFound = true;
								}
							}
						}else{
							String sTmpDataHole;
							sTmpDataHole = simpleGrepAwk(patternHole, sCurLine, "", 0);
							if (sTmpDataHole != null){
								iSubDataHoleCount = 0;
								bSubDataHoleFound = true;
								sDataHole = "";
							}
						}
					}

					if (!bData2Found){
						//check sub data first
						if (bSubData2Found){
							if (isDataLine(sCurLine)){
								//just concat 4 following lines
								if (iSubData2Count < 4){
									if (!sData2.equals("")){
										sData2 += " / ";
									}
									sData2 += sCurLine;
									iSubData2Count++;
								}else{
									bData2Found = true;
								}
							}
						}else{
							String sTmpData2;
							sTmpData2 = simpleGrepAwk(patternData2, sCurLine, "", 0);
							if (sTmpData2 != null){
								iSubData2Count = 0;
								bSubData2Found = true;
								sData2 = "";
							}
						}
					}
				}

				//if present, dataHole should replace data2
				if (bDataHoleFound) {
					sData2 = sDataHole;
				}

				bResult &= appendToCrashfile("DATA0=" + sData0);
				bResult &= appendToCrashfile("DATA1=" + sData1);
				bResult &= appendToCrashfile("DATA2=" + sData2);
				bufFabricFile.close();
			}
			catch(Exception e) {
				System.err.println( "newfabricerr : " + e);
				e.printStackTrace();
				return false;
			}
		}
		return bResult;
	}

	private boolean tombstone(String aFolder){
		boolean bResult = true;
		String sTombstoneFile = fileGrepSearch("tombstone_.*", aFolder);
		if (sTombstoneFile != ""){
			String sProcess= "";
			String sSignal= "";
			String sStackSymbol = "";
			String sStackLibs = "";
			String sFaultAddress = "";
			String sFaultAddrSeparator = "fault addr ";
			String sHexCharactersPattern = "[0-9A-Fa-f]+";
			boolean bProcessFound = false;
			boolean bSignalFound = false;
			boolean bSubSignalFound = false;
			boolean bDisplaySymbols = false;
			int iSubSignalCount = 0;
			int iStackCount = 0;
			boolean bSubStackFound = false;
			int iSubStackCount = 0;

			/*Defines patterns expected to be found in the tombstone file to extract relevant crash data*/
			Pattern patternProcess = java.util.regex.Pattern.compile(".*>>>.*");
			Pattern patternSignalStack = java.util.regex.Pattern.compile(".*Build fingerprint.*");
			Pattern patternSubSignal = java.util.regex.Pattern.compile(".*signal.*");
			Pattern patternSubStack = java.util.regex.Pattern.compile(".*#0[0-7].*");
			String sCurLine;
			try {
				BufferedReader bufTombstoneFile = new BufferedReader(new FileReader(sTombstoneFile));
				while ((sCurLine = bufTombstoneFile.readLine()) != null) {
					String sTmp;
					if (!bProcessFound){
						sTmp = simpleGrepAwk(patternProcess, sCurLine, ">>>", 1);
						sTmp = simpleAwk(sTmp,"<", 0);
						if (sTmp != null){
							sProcess = sTmp;
							bProcessFound = true;
						}
					}
					if (!bSignalFound){
						String sTmpSignal;
						sTmpSignal = simpleGrepAwk(patternSignalStack, sCurLine, "", 0);
						if (sTmpSignal != null){
							iSubSignalCount = 0;
							bSubSignalFound = true;
						}
						if (bSubSignalFound){
							//Search SubSignal and FaultAddress patterns only in the 4 lines following the line
							//containing SignalStack pattern
							if (iSubSignalCount < 4){
								sTmp = simpleGrepAwk(patternSubSignal, sCurLine, "\\(", 1);
								sTmp = simpleAwk(sTmp,"\\)", 0);
								if (sTmp != null){
									sSignal = sTmp;
									bSignalFound = true;
									//signal has been found : it is assumed the fault address is always in the same line
									//and shall necessary be an 8 hex characters long string
									sTmp = simpleAwk(sCurLine,sFaultAddrSeparator, 1);
									if (sTmp != null){
										sTmp = sTmp.substring(0, 8);
										if (sTmp.matches(sHexCharactersPattern)){
											sFaultAddress = sTmp;
										}
									}
								}
								iSubSignalCount++;
							}else{
								bSubSignalFound = false;
							}
						}
					}
					if (iStackCount<8){
						String sTmpStack;
						sTmpStack = simpleGrepAwk(patternSignalStack, sCurLine, "", 0);
						if (sTmpStack != null){
							iSubStackCount = 0;
							bSubStackFound = true;
						}

						if (bSubStackFound){
							if (iSubStackCount < 15){
								//required for managing line with matching subschema
								String sTmpSubStack;
								sTmpSubStack = simpleGrepAwk(patternSubStack, sCurLine,"" , 0);
								sTmp = simpleAwk(sTmpSubStack,"\\(", 1);
								sTmp = simpleAwk(sTmp,"\\)", 0);
								if (sTmp != null){
									bDisplaySymbols = true;
									if (iStackCount == 0){
										sStackSymbol = sTmp;
									} else{
										sStackSymbol += " " +  sTmp;
									}
									iStackCount++;
								}else if (sTmpSubStack != null){
									// required to reproduce exactly number of white space
									if (iStackCount == 0){
										sStackSymbol = "";
									}else{
										sStackSymbol += " " ;
									}
									sTmp = advancedAwk(sTmpSubStack," ", 3);
									if (sTmp != null){ //case without symbols
										if (iStackCount == 0){
											sStackLibs = sTmp;
										}else{
											sStackLibs += " " +  sTmp;
										}
									}else{
										//in order to also reproduce white space mechanism
										if (iStackCount == 0){
											sStackLibs = "";
										}else{
											sStackLibs += " ";
										}
									}
									iStackCount++;
								}
								iSubStackCount++;
							}else{
								bSubStackFound = false;
							}
						}
					}
				}

				bResult &= appendToCrashfile("DATA0=" + sProcess);
				bResult &= appendToCrashfile("DATA1=" + sSignal);
				if (bDisplaySymbols){
					bResult &= appendToCrashfile("DATA2=" + sStackSymbol);
				}else{
					bResult &= appendToCrashfile("DATA2=" + sStackLibs);
				}
				bResult &= appendToCrashfile("DATA3=" + sFaultAddress);

				bufTombstoneFile.close();
			}
			catch(Exception e) {
				System.err.println( "tombstone : " + e);
				e.printStackTrace();
				return false;
			}
		}
		return bResult;
	}

	private boolean uiwdt(String aFolder){
		boolean bResult = true;

		String sUIWDTFileGZ = fileGrepSearch("system_server_watchdog.*txt.gz", aFolder);
		try {
			if (sUIWDTFileGZ != ""){
				GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(sUIWDTFileGZ));
				BufferedReader uiwdtReader = new BufferedReader(new InputStreamReader (gzipInputStream));
				bResult = extractUIWDTData(uiwdtReader);
			}else{
				String sUIWDTFile = fileGrepSearch("system_server_watchdog.*txt" , aFolder);
				if (sUIWDTFile != ""){
					BufferedReader uiwdtReader = new BufferedReader(new FileReader(sUIWDTFile));
					bResult = extractUIWDTData(uiwdtReader);
				}
			}
		}catch(Exception e) {
			System.err.println( "UIWDT : " + e);
			e.printStackTrace();
			return false;
		}
		return bResult;
	}

	private boolean wtf(String aFolder){
		boolean bResult = true;

		String sWTFFileGZ = fileGrepSearch("wtf.*.gz", aFolder);
		try {
			if (sWTFFileGZ != ""){
				GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(sWTFFileGZ));
				BufferedReader wtfReader = new BufferedReader(new InputStreamReader (gzipInputStream));
				bResult = extractWTFData(wtfReader);
			}else{
				String sWTFFile = fileGrepSearch("wtf.*.txt" , aFolder);
				if (sWTFFile != ""){
					BufferedReader wtfReader = new BufferedReader(new FileReader(sWTFFile));
					bResult = extractWTFData(wtfReader);
				}
			}
		}catch(Exception e) {
			System.err.println( "WTF : " + e);
			e.printStackTrace();
			return false;
		}
		return bResult;
	}

	private boolean anr(String aFolder){
		boolean bResult = true;

		String sSysANRGZ = fileGrepSearch(".*_app_anr.*txt.gz", aFolder);
		try {
			if (sSysANRGZ != ""){
				GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(sSysANRGZ));
				BufferedReader sysANRReader = new BufferedReader(new InputStreamReader (gzipInputStream));
				bResult = extractAnrData(sysANRReader);
			}else{
				String sSysANR = fileGrepSearch(".*_app_anr.*txt" , aFolder);
				if (sSysANR != ""){
					BufferedReader sysANRReader = new BufferedReader(new FileReader(sSysANR));
					bResult = extractAnrData(sysANRReader);
				}
			}
		}catch(Exception e) {
			System.err.println( "anr - general AppANR : " + e);
			e.printStackTrace();
			return false;
		}
		return bResult;
	}


	private boolean javacrash(String aFolder){
		boolean bResult = true;
		bResult &= parseJavaCrashFile(".*_app_crash.*.txt.gz",".*_app_crash.*.txt",aFolder);
		bResult &= parseJavaCrashFile("system_server_crash.*.txt.gz","system_server_crash.*.txt",aFolder);
		bResult &= parseJavaCrashFile(".*_app_native_crash.*.txt.gz",".*_app_native_crash.*.txt",aFolder, true);
		return bResult;
	}

	private boolean parseJavaCrashFile(String aFileZip, String aFileNormal, String aFolder){
		return parseJavaCrashFile(aFileZip, aFileNormal, aFolder, false);
	}

	private boolean parseJavaCrashFile(String aFileZip, String aFileNormal, String aFolder, boolean nativ){
		boolean bResult = true;
		String sSysAppGZ = fileGrepSearch(aFileZip, aFolder);
		try {
			if (sSysAppGZ != ""){
				GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(sSysAppGZ));
				BufferedReader sysAppReader = new BufferedReader(new InputStreamReader (gzipInputStream));
				bResult = extractJavaCrashData(sysAppReader, nativ);
			}else{
				String sSysApp = fileGrepSearch(aFileNormal, aFolder);
				if (sSysApp != ""){
					BufferedReader sysAppReader = new BufferedReader(new FileReader(sSysApp));
					bResult = extractJavaCrashData(sysAppReader, nativ);
				}
			}
		}catch(Exception e) {
			System.err.println( "javacrash - parseJavaCrashFile : " + e);
			e.printStackTrace();
			return false;
		}
		return bResult;
	}

	private boolean extractUIWDTData(BufferedReader aReader){
		boolean bResult = true;

		String sPID= "";
		String sType= "";
		String sStack = "";
		boolean bPIDFound = false;
		boolean bTypeFound = false;
		int iStackCount = 0;

		Pattern patternPID = java.util.regex.Pattern.compile(".*Process:.*");
		Pattern patternType = java.util.regex.Pattern.compile(".*Subject:.*");
		Pattern patternStack = java.util.regex.Pattern.compile("^  at.*");

		String sCurLine;
		try {
			while ((sCurLine = aReader.readLine()) != null) {
				String sTmp;
				if (!bPIDFound){
					sTmp = simpleGrepAwk(patternPID, sCurLine, ":", 1);
					if (sTmp != null){
						sPID = sTmp;
						bPIDFound = true;
					}
				}
				if (!bTypeFound){
					sTmp = simpleGrepAwk(patternType, sCurLine, ":", 1);
					if (sTmp != null){
						sType = sTmp;
						bTypeFound = true;
					}
				}

				if (iStackCount<8){
					sTmp = simpleGrepAwk(patternStack, sCurLine, "at ", 1);
					sTmp = simpleAwk(sTmp,"\\(", 0);
					if (sTmp != null){
						if (iStackCount == 0){
							sStack = sTmp;
						}
						else{
							sStack += " " +  sTmp;
						}
						iStackCount++;
					}
				}
			}
			bResult &= appendToCrashfile("DATA0=" + sPID);
			bResult &= appendToCrashfile("DATA1=" + sType);
			bResult &= appendToCrashfile("DATA2=" + sStack);
			aReader.close();
		}catch (Exception e) {
			System.err.println( "extractUIWDTData : " + e);
			e.printStackTrace();
			return false;
		}
		return bResult;
	}


	private boolean extractWTFData(BufferedReader aReader){
		boolean bResult = true;

		String sPID= "";
		String sType= "";
		boolean bPIDFound = false;
		boolean bTypeFound = false;

		Pattern patternPID = java.util.regex.Pattern.compile(".*Process:.*");
		Pattern patternType = java.util.regex.Pattern.compile(".*Subject:.*");

		String sCurLine;
		try {
			while ((sCurLine = aReader.readLine()) != null) {
				String sTmp;
				if (!bPIDFound){
					sTmp = simpleGrepAwk(patternPID, sCurLine, ":", 1);
					if (sTmp != null){
						sPID = sTmp;
						bPIDFound = true;
					}
				}
				if (!bTypeFound){
					sTmp = simpleGrepAwk(patternType, sCurLine, ":", 1);
					if (sTmp != null){
						sType = sTmp;
						bTypeFound = true;
					}
				}
			}
			bResult &= appendToCrashfile("DATA0=" + sPID);
			bResult &= appendToCrashfile("DATA1=" + sType);
			aReader.close();
		}catch (Exception e) {
			System.err.println( "extractWTFData : " + e);
			e.printStackTrace();
			return false;
		}
		return bResult;
	}


	private boolean extractAnrData(BufferedReader aReader){
		boolean bResult = true;

		String sPID= "";
		String sType= "";
		String sStack = "";
		String sCPU = "";
		boolean bPIDFound = false;
		boolean bTypeFound = false;
		boolean bCPUFound = false;
		int iStackCount = 0;

		Pattern patternPID = java.util.regex.Pattern.compile(".*Process:.*");
		Pattern patternType = java.util.regex.Pattern.compile(".*Subject:.*");
		Pattern patternStack = java.util.regex.Pattern.compile("^  at.*");
		Pattern patternCPU = java.util.regex.Pattern.compile(".*TOTAL.*");
		String sCurLine;
		try {
			while ((sCurLine = aReader.readLine()) != null) {
				String sTmp;
				if (!bPIDFound){
					sTmp = simpleGrepAwk(patternPID, sCurLine, ":", 1);
					if (sTmp != null){
						sPID = sTmp;
						bPIDFound = true;
					}
				}
				if (!bTypeFound){
					sTmp = simpleGrepAwk(patternType, sCurLine, ":", 1);
					if (sTmp != null){
						sType = sTmp;
						bTypeFound = true;
					}
				}
				if (!bCPUFound){
					sTmp = simpleGrepAwk(patternCPU, sCurLine, "TOTAL", 0);
					if (sTmp != null){
						sCPU = sTmp;
						bCPUFound = true;
					}
				}

				if (iStackCount<8){
					sTmp = simpleGrepAwk(patternStack, sCurLine, "at ", 1);
					sTmp = simpleAwk(sTmp,"\\(", 0);
					if (sTmp != null){
						if (iStackCount == 0){
							sStack = sTmp;
						}
						else{
							sStack += " " +  sTmp;
						}
						iStackCount++;
					}
				}
			}

			bResult &= appendToCrashfile("DATA0=" + sPID);
			bResult &= appendToCrashfile("DATA1=" + sType);
			bResult &= appendToCrashfile("DATA2=" + sStack);
			bResult &= appendToCrashfile("DATA3=cpu:" + sCPU);
			aReader.close();
		}catch (Exception e) {
			System.err.println( "extractAnrData : " + e);
			e.printStackTrace();
			return false;
		}
		return bResult;
	}

	private boolean extractJavaCrashData(BufferedReader aReader, boolean nativ){
		boolean bResult = true;
		String sPID= "";
		String sException= "";
		String sStack = "";
		boolean bPIDFound = false;
		boolean bExceptionFound = false;
		int iStackCount = 0;

		Pattern patternPID = java.util.regex.Pattern.compile(".*Process:.*");
		Pattern patternJavaLang = java.util.regex.Pattern.compile("java\\.lang.*");
		Pattern patternStack = java.util.regex.Pattern.compile(".*at .*");
		String sCurLine;
		try {
			while ((sCurLine = aReader.readLine()) != null) {
				String sTmp;
				if (!bPIDFound){
					sTmp = simpleGrepAwk(patternPID, sCurLine, ":", 1);
					if (sTmp != null){
						sPID = sTmp;
						bPIDFound = true;
					}
				}
				if (!bExceptionFound){
					sTmp = simpleGrepAwk(patternJavaLang, sCurLine, "", 0);
					if (sTmp != null){
						sException = sTmp;
						bExceptionFound = true;
					}
				}
				if (iStackCount<4){
					sTmp = simpleGrepAwk(patternStack, sCurLine, "at ", 1);
					sTmp = simpleAwk(sTmp,"\\(", 0);
					if (sTmp != null){
						if (iStackCount == 0){
							sStack = sTmp;
						}
						else{
							sStack += " " +  sTmp;
						}
						iStackCount++;
					}
				}
			}

			bResult &= appendToCrashfile("DATA0=" + sPID);
			bResult &= appendToCrashfile("DATA1=" + (nativ?"app_native_crash":"") + sException);
			bResult &= appendToCrashfile("DATA2=" + sStack);
			aReader.close();
		}catch (Exception e) {
			System.err.println( "extractJavaCrashData : " + e);
			e.printStackTrace();
			return false;
		}
		return bResult;
	}

	private boolean appendToCrashfile(String aStr){
		try{
			myOutput.write(aStr + "\n");
		} catch (Exception e) {
			System.err.println( "appendToCrashfile : " + e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean checkGrepInvertGrep(Pattern aPattern,Pattern aInvertPattern, String aLine)
	{
		String sTestGrep = simpleGrepAwk(aPattern, aLine, "", 0);
		if (sTestGrep != null){
			String sTestInvertGrep = simpleGrepAwk(aInvertPattern, aLine, "", 0);
			if (sTestInvertGrep == null){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}

	private String simpleGrepAwk(Pattern aPattern, String aLine, String sSeparator, int iReturnIndex){
		return simpleGrepAwk(aPattern, aLine, sSeparator, iReturnIndex, false);
	}

	private String simpleAwk(String aString, String sSeparator, int iReturnIndex){
		return simpleAwk(aString, sSeparator, iReturnIndex, false);
	}

	private String advancedAwk(String aString, String sSeparator, int iReturnIndex){
		String sResult = null;
		if (aString != null){
			String[] splitString = aString.split("(" + sSeparator + ")+" );
			//to manage beginning separator
			if (splitString[0].equals("")){
				iReturnIndex++;
			}
			if (splitString.length > iReturnIndex ){
				sResult = splitString[iReturnIndex];
			}
		}
		return sResult;
	}

	private String fileGrepSearch(String aPattern, String aFolder){
		Pattern patternFile = java.util.regex.Pattern.compile(aPattern);

		File searchFolder = new File(aFolder );
		File[] files = searchFolder.listFiles();
		String sFileResult = "";
		if(files!=null) {
			for(File f: files) {
				Matcher matcherFile = patternFile.matcher(f.getName());
				if (matcherFile.find()){
					sFileResult = aFolder + "/" + f.getName();
					break;
				}
			}
		}
		return sFileResult;
	}

	private String simpleGrepAwk(Pattern aPattern, String aLine, String sSeparator, int iReturnIndex, boolean left){
		Matcher simpleMatcher = aPattern.matcher(aLine);
		String sResult = null;
		if (simpleMatcher.find()){
			String sGroup = simpleMatcher.group();
			if (sSeparator.equals("")){
				sResult = sGroup;
			}else {
				sResult = simpleAwk(sGroup,sSeparator,iReturnIndex, left);
			}
		}
		return sResult;
	}

	private String simpleAwk(String aString, String sSeparator, int iReturnIndex, boolean left){
		String sResult = null;
		if (aString != null){
			String[] splitString = left?aString.split(sSeparator, iReturnIndex + 1):aString.split(sSeparator);
			if (splitString.length > iReturnIndex ){
				sResult = splitString[iReturnIndex];
			}
		}
		return sResult;
	}

}
