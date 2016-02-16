package com.example.antoine.testapp;

/**
 * Created by Antoine on 16/02/2016.
 */
import android.content.Context;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


public class Utils {

    /**
     * Downloads a file given URL to specified destination
     * @param url
     * @param destFile
     * @return
     */
    //public static boolean downloadFile(Context context, String url, String destFile) {
    public static boolean downloadFile(Context context, String url, File destFile) {
        //Log.v(TAG, "@downloadFile()");
        //Log.d(TAG, "Downloading " + url);

        boolean ret = false;

        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        InputStream is = null;

        try {
            URL myUrl = new URL(url);
            URLConnection connection = myUrl.openConnection();

            is = connection.getInputStream();
            bis = new BufferedInputStream(is);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            //We create an array of bytes
            byte[] data = new byte[50];
            int current = 0;

            while((current = bis.read(data,0,data.length)) != -1){
                buffer.write(data,0,current);
            }

            fos = new FileOutputStream(destFile);
            fos.write(buffer.toByteArray());
            fos.close();
            ret = true;
        }
        catch(Exception e) {
            //Log.e(TAG, "Error while downloading and saving file !", e);
        }
        finally {
            try {
                if ( fos != null ) fos.close();
            } catch( IOException e ) {}
            try {
                if ( bis != null ) bis.close();
            } catch( IOException e ) {}
            try {
                if ( is != null ) is.close();
            } catch( IOException e ) {}
        }

        return ret;
    }

}