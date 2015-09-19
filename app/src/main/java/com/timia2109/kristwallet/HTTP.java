package com.timia2109.kristwallet;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTP {
    public static String readURL(URL url, String userAgent) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", userAgent);

        //int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    public static String readURL(URL url) throws IOException {
        // Use generic user agent
        return readURL(url, "Mozilla/5.0");
    }

    public static String readURL(String url) throws IOException {
        return readURL(new URL(url));
    }
}