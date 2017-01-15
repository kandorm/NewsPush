package com.ihandy.a2014011290.mode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ebc5 on 2016/8/31.
 */
public class JsonURL {
    public static String getJsonContent(String urlStr)
    {
        if(urlStr.equals("") || urlStr==null)return"";
        try
        {
            URL url = new URL(urlStr);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setConnectTimeout(3000);
            httpConn.setReadTimeout(6000);
            httpConn.setDoInput(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            int respCode = httpConn.getResponseCode();
            if (respCode == 200)
            {
                return ConvertStream2Json(httpConn.getInputStream());
            }
        }
        catch (MalformedURLException e){e.printStackTrace();}
        catch (IOException e) {e.printStackTrace();}
        return "";
    }
    private static String ConvertStream2Json(InputStream inputStream)
    {
        String jsonStr = "";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len = 0;
        try
        {
            while ((len = inputStream.read(buffer, 0, buffer.length)) != -1)
            {
                out.write(buffer, 0, len);
            }
            jsonStr = new String(out.toByteArray());
        }
        catch (IOException e) {e.printStackTrace();}
        return jsonStr;
    }

}
