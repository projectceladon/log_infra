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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;


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
                for (File c : f.listFiles())
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
        FileOutputStream out = new FileOutputStream(dst);
        int count;
        byte[] data = new byte[1024];
        try {
            while ((count = in.read(data)) != -1) {
                out.write(data, 0, count);
            }
            out.flush();
        } finally {
            try {
                out.close();
            } finally {
                in.close();
            }
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
	 * for the give file name
	 * @param path the full path of the file to parse.
	 * @return an <code>InputStreamReader</code> instance
	 * 		or <code>null</code> if operation failed.
	 */
    public static InputStreamReader getInputStreamReader(String path) {
        try {
            InputStream is = new FileInputStream(path);
            InputStreamReader isr = new InputStreamReader(is);
            return isr;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
