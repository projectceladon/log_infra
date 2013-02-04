package com.intel.phonemonitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.BufferedReader;

import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Util {
    private static final String TAG = Util.class.getSimpleName();
    private static final int BUFFER_SIZE_BYTES = 2048;

    public static boolean copyFile(String from, String to) {
        File fromFile = new File(from);
        File toFile = new File(to);

        try {
            InputStream inStream = null;
            OutputStream outStream = null;

            if (!fromFile.exists()) {
                Log.e(TAG, "Cannot see file "+ from);
                return false;
            }

            inStream = new FileInputStream(fromFile);
            outStream = new FileOutputStream(toFile);

            byte[] buffer = new byte[BUFFER_SIZE_BYTES];
            int length;
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }
            inStream.close();
            outStream.close();
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }

        toFile.setReadable(true, false);
        toFile.setWritable(true, false);
        toFile.setExecutable(true, false);
        return true;
    }

    public static boolean zipFiles (String zipFileName, String[] inputFileNames) {
        try {
            byte[] buffer = new byte[BUFFER_SIZE_BYTES];
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
            out.setLevel(Deflater.BEST_COMPRESSION);

            for (String fname :inputFileNames) {
                File f = new File(fname);
                FileInputStream in = new FileInputStream(f);
                out.putNextEntry(new ZipEntry(f.getName()));
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                out.closeEntry();
                in.close();
            }
            out.close();
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /* Pretty inefficient... but this is meant to work on relatively small files, so
       we probably do not care */
    public static String stringFromFile(String fname) {
        try {
            FileInputStream fstream = new FileInputStream(fname);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            String str = "";

            while ((strLine = br.readLine()) != null) {
                str = str + strLine + "\n";
            }
            in.close();
            return str;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
