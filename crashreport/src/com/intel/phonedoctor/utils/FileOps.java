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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

/**
 * This class is intended to store helper methods on files (File Operations)
 */
public class FileOps {

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
}
