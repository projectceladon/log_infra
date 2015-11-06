/*
 * Copyright (c) Intel 2012
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.phonedoctor.utils;

import com.intel.crashreport.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.LinkedList;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.GZIPOutputStream;

import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

/**
 * This class is intended to store helper methods on files (File Operations)
 */
public class FileOps {
	private static long GZIP_THRESHOLD_IN_BYTES = 1 * 1024 * 1024; /* 1 MB */

	/**
	 * Delete a file/folder recursively
	 *
	 * @param f file/folder to delete
	 * @return true delete is successful, else false
	 */
	public static boolean delete(File f){
		File[] files = null;
		if(f == null) {
			return false;
		}
		if (f.isDirectory()) {
			files = f.listFiles();
			if(files != null) {
				for (File c : files)
					delete(c);
			}
		}
		if (!f.delete())
			return false;
		return true;
	}

	/**
	 * Creates a new file, given a path
	 *
	 * @param directory in which we want to create the file
	 * @param name of the file we are supposed to create
	 * @return true if file exists at the end of the operation, else false
	 * note: it does not create the path to the file if it does not exist!
	 */
	public static boolean createNewFile(File directory, String name)
			throws IOException, FileNotFoundException {
		File file = new File(directory, name);

		if (!file.exists())
			return file.createNewFile();

		return true;
	}

	/**
	 * Calculates the size in bytes for a given path
	 *
	 * @param path to a file or directory
	 * @return value representing the size in bytes for the given path
	 */
	public static long getPathSize(String path) {
		File file = new File(path);
		long result = 0;

		if (file == null || !file.exists())
			return 0;

		if (!file.isDirectory())
			return file.length();

		List<File> list = new LinkedList<File>();
		list.add(file);

		while (!list.isEmpty()) {
			File entry = list.remove(0);

			if (!entry.exists())
				continue;

			File[] files = entry.listFiles();
			if (files == null)
				continue;

			for (File f : files) {
				if (f.isDirectory())
					list.add(f);
				else
					result += f.length();
			}
		}

		return result;
	}

	/**
	 * Copy file from source to destination
	 *
	 * @param src source file
	 * @param dst destination file, should be created before
	 * @throws IOException when copy fails
	 */
	public static void copy(File src, File dst) throws IOException {
		FileInputStream in = new FileInputStream(src);
		try{
			FileOutputStream out = new FileOutputStream(dst);
			int count;
			byte[] data = new byte[1024];
			try {
				while ((count = in.read(data)) != -1) {
					out.write(data, 0, count);
				}
				out.flush();
			} finally {
				out.close();
			}
		}finally {
			in.close();
		}
	}
	/**
	 * Check if the input file is a valid zip file.
	 *
	 * @param file to check
	 * @return true is the file is a valid Zip file. False otherwise.
	 * @throws IOException when zip integrity check fails.
	 */
	public static boolean isValidZipFile(final File file) throws IOException {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file);
			zipFile.close();
			return true;
		} catch (ZipException e) {
			return false;
		}
	}

	/**
	 * Returns an <code>InputStreamReader</code> instance
	 * for the given file.
	 * @param file the file object that we want to read.
	 * @return an <code>InputStreamReader</code> instance
	 *	 	or <code>null</code> if operation failed.
	 */
	public static InputStreamReader getInputStreamReader(final File file) {
		InputStreamReader isr = null;
		if(null != file && file.exists()) {
			try {
				InputStream is = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(is);
				isr = new InputStreamReader(bis);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return isr;
	}

	/**
	 * Returns an <code>InputStreamReader</code> instance
	 * for the given file path.
	 * @param path the path of the file that we want to read.
	 * @return an <code>InputStreamReader</code> instance
	 *	 	or <code>null</code> if operation failed.
	 */
	public static InputStreamReader getInputStreamReader(final String path) {
		InputStreamReader isr = null;
		if(null != path) {
			isr = getInputStreamReader(new File(path));
		}
		return isr;
	}

	/**
	 * Returns a <code>Bitmap</code> from the given file path.
	 * @param path to image file on disk
	 * @param maxWidth to which an image can be resized to
	 * @param maxHeight to which an image can be resized to
	 * @return an <code>Bitmap</code> from the given file path if path ok
	 *	 	or null on error
	 */
	public static Bitmap loadScaledImageFromFile(String path, int maxWidth, int maxHeight){
		int scale = 1;
		Bitmap b = null;
		BitmapFactory.Options bfo = null;
		File f = null;
		FileInputStream stream = null;

		if (path == null || path.isEmpty() || maxWidth <=0 || maxHeight <= 0)
			return null;

		bfo = new BitmapFactory.Options();
		bfo.inJustDecodeBounds = true;

		f = new File(path);
		if (!f.exists() || !f.isFile())
			return null;

		try { stream = new FileInputStream(f); }
		catch (FileNotFoundException e) { return null; }

		BitmapFactory.decodeStream(stream, null, bfo);
		try { stream.close(); } catch (IOException e) { return null; }

		while ( bfo.outWidth/(2*scale) >= maxWidth &&
			bfo.outHeight/(2*scale) >= maxHeight ) {
			scale *= 2;
		}

		bfo = new BitmapFactory.Options();
		bfo.inSampleSize = scale;

		try { stream = new FileInputStream(f); }
		catch (FileNotFoundException e) { return null; }

		b = BitmapFactory.decodeStream(stream, null, bfo);
		try { stream.close(); } catch (IOException e) {}

		return b;
	}

	/**
	 * @param path indicates the directory whose files are desired
	 * to be compressed and are above a defined threshold:
	 * GZIP_THRESHOLD_IN_BYTES. Content will be placed in the same
	 * path with the extension gz. and original files will be removed.
	 */
	public static void compressFolderContent(String path) {
		File folder = null;
		File[] files = null;

		if (path == null || path.isEmpty())
			return;

		folder = new File(path);
		if (!folder.exists() || !folder.isDirectory())
			return;

		files = folder.listFiles();

		if(files == null)
			return;

		for(File f: files) {
			if (isGz(f.getName()))
				continue;

			try {
				if (f.length() < GZIP_THRESHOLD_IN_BYTES)
					continue;
			} catch (SecurityException se) {
				Log.e("No permission to access file: " + f.getAbsolutePath());
				continue;
			}

			String source = f.getAbsolutePath();
			String destination = source + ".gz";
			if (compressFile(source, destination)) {
				String result = f.getAbsolutePath() + " (" +
						Long.toString(f.length()) + " bytes) -> " +
						f.getAbsolutePath() + ".gz";
				File f2 =new File(destination);
				if(f2.exists()) {
					result +=  " (" + Long.toString(f2.length()) + " bytes)";
					f.delete();
				}

				Log.d("File compressed: " + result);
			}
		}
	}

	/**
	 * Returns an <code>boolean</code> value indicating wether the
	 * passed file has the extension gz
	 * @param path to the file to check
	 * @return an <code>boolean</code> value indicating wether the
	 * passed file has the extension gz
	 */
	public static boolean isGz(String file) {
		int offset;

		if (file == null || file.isEmpty())
			return false;

		offset = file.lastIndexOf(".");
		if (offset == -1 || offset == file.length())
			return false;

		String extension = file.substring(offset + 1, file.length());
		return extension.equals("gz");
	}

	/**
	 * Returns an <code>boolean</code> value indicating wether the
	 * passed file was compressed ok.
	 * @param source - path to the file to compress
	 * @param destination - output location
	 * @return an <code>boolean</code> value indicating wether the
	 * passed file was compressed ok.
	 */
	public static boolean compressFile(String source, String destination) {
		int length;
		byte[] buffer = new byte[1024];
		FileInputStream in = null;
		GZIPOutputStream out = null;

		if (source == null || source.isEmpty() || destination == null || destination.isEmpty())
			return false;

		try {
			out = new GZIPOutputStream(new FileOutputStream(destination));
			in = new FileInputStream(source);

			while ((length = in.read(buffer)) > 0)
				out.write(buffer, 0, length);

			in.close();
			out.finish();
			out.close();
		} catch (IOException ex) {
			try { if (in != null) in.close(); } catch (IOException e) {}
			try { if (out != null) out.close(); } catch (IOException e) {}

			Log.e("File could not be compressed: " + source);
			return false;
		}

		return true;
	}

	/**
	 * Writes to passed file the given content.
	 * @param path - path to the file to write
	 * @param value - content that should be written to the file.
	 */
	public static void fileWriteString(String path, String value) throws
						FileNotFoundException, IOException {
		FileOutputStream f = null;

		if (path == null || path.isEmpty() || value == null)
			throw new IllegalArgumentException("Invalid parameters passed");

		try {
			f = new FileOutputStream(path);
			BufferedOutputStream write = new BufferedOutputStream(f);
			write.write(value.getBytes());
			write.close();
		}
		finally {
			if (f != null)
				f.close();
		}
	}
}
