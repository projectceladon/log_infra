package com.intel.crashreport;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class HistoryEventFile {

	private File histFile;
	private Scanner scanner;

	public HistoryEventFile() throws FileNotFoundException {
		histFile = openFile();
		scanner = new Scanner(histFile);
	}

	public Boolean hasNext() {
		return scanner.hasNext();
	}

	public String getNextEvent() {
		String line;

		while(scanner.hasNext()){
			line = scanner.nextLine();
			if (line.charAt(0) != '#') {
				return line;
			}
		}

		return "";
	}

	private File openFile() {
		String path = new String("/data/" + "logs/history_event");
		File histFile = new File(path);

		if (!histFile.canRead())
			Log.w("HistoryEventFile: can't read file : " + path);

		return histFile;
	}

}
