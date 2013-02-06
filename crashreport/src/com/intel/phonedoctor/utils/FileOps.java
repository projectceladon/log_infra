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
import java.io.FileOutputStream;
import java.io.IOException;

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
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
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
        while ((count = in.read(data)) != -1)
            out.write(data, 0, count);
        out.flush();
        out.close();
        in.close();
    }

}
