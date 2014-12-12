package com.intel.crashtoolserver.bean;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

/**
 * Log File class that allow to manipulate a crashtool logfile
 * @author glivonx
 *
 */
public class CtZipFile extends ZipFile {


	/** Path Name */
	private String pathname;

	/**
	 * Construct a logfile from it path
	 * @param pathname
	 */
	public CtZipFile(String pathname) throws IOException {
		super(pathname);
		this.pathname = pathname;
	}

	/**
	 * Gives the logfile's parent path
	 * @return
	 */
	public String getLogFileParentPath() {
		File file = new File(pathname);
		return file.getParent();
	}

	/**
	 * @return the pathname
	 */
	public String getPathname() {
		return pathname;
	}
}
