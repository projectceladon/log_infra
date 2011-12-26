package com.intel.crashreport;

public class Logger {

	private String log;

	public Logger() {
		log = new String("");
	}

	public void addMsg(String msg) {
		log = log.concat(msg + "\n");
	}

	public void clearLog() {
		log = "";
	}

	public String getLog() {
		return log;
	}

}
