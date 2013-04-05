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

import java.util.ArrayList;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipException;
import java.util.zip.GZIPOutputStream;

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
                Log.e(TAG, "Cannot see file " + from);
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

    public static boolean zipFiles (String zipFileName, ArrayList<String> inputFileNames) {
        boolean zipGood = true;
        ZipOutputStream out = null;
        FileInputStream in = null;

        try {
            byte[] buffer = new byte[BUFFER_SIZE_BYTES];
            out = new ZipOutputStream(new FileOutputStream(zipFileName));
            out.setLevel(Deflater.BEST_COMPRESSION);

            for (String fname :inputFileNames) {
                File f = new File(fname);
                in = new FileInputStream(f);
                out.putNextEntry(new ZipEntry(f.getName()));
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                out.closeEntry();
                in.close();
            }
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
            zipGood = false;
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            zipGood = false;
        }
        catch (IOException e) {
            e.printStackTrace();
            zipGood = false;
        }
        finally {
            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
                zipGood = false;
            }
        }
        return zipGood;
    }

    /* Pretty inefficient... but this is meant to work on relatively small files, so
       we probably do not care */
    public static String stringFromFile(String fname) {
        String str = "";
        DataInputStream in = null;

        try {
            FileInputStream fstream = new FileInputStream(fname);
            in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            while ((strLine = br.readLine()) != null) {
                str = str + strLine + "\n";
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
            /* Crude way of stripping the last \n.
               First test could also be done by catching
               StringIndexOutOfBoundsException */
            if (str.length() > 0) {
                return str.substring(0, str.length()-1);
            }
            else {
                return "";
            }
        }
    }

    public static String gzipFile(String fname) {
        byte[] buffer = new byte[1024];
        boolean gz_success = true;
        final String GZIP_PATH = fname + ".gz";
        GZIPOutputStream gzos = null;
        FileInputStream in = null;

        try {
            gzos = new GZIPOutputStream(new FileOutputStream(GZIP_PATH));
            in   = new FileInputStream(fname);

            int len;
            while ((len = in.read(buffer)) > 0) {
                gzos.write(buffer, 0, len);
            }

            gzos.finish();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            gz_success = false;
        } catch (IOException e) {
            e.printStackTrace();
            gz_success = false;
        } finally {
            try {
                if (in != null)
                    in.close();
                if (gzos != null)
                    gzos.close();
            } catch (IOException e) {
                e.printStackTrace();
                gz_success = false;
            }
        }

        if (gz_success)
            return GZIP_PATH;

        return null;
    }
}
