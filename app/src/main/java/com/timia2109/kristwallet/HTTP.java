package com.timia2109.kristwallet;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.timia2109.kristwallet.util.PostData;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTP {
    public final static String UPDATE_URL = "http://timia2109.com/kst/kristWallet.php";
    public final static String URL_UPDATE_NOTES = "https://raw.githubusercontent.com/timia2109/KristWallet/master/updateNotes.txt";
    public static boolean IS_ONLINE = true;
    public static final String USER_AGENT = "KristWalletForAndroid/1.2";

    public static final String getUpdateURL(String version) {
        return "https://github.com/timia2109/KristWallet/releases/download/v"+version+"/kristWallet.apk";
    }

    public static String readURL(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);

        //int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        con.disconnect();
        return response.toString();
    }

    public static String readURL(String url) throws IOException {
        return readURL(new URL(url));
    }

    public static String postURL(URL url, PostData postData) throws IOException {
        String postDataStr = postData.toString();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        con.setRequestProperty("Content-Length", Integer.toString(postDataStr.getBytes().length));
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Content-Language", "en-US");
        con.setDoInput(true);
        con.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream (con.getOutputStream());
        wr.writeBytes(postDataStr);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        con.disconnect();
        return response.toString();
    }

    public static String postURL(String url, PostData postData) throws IOException {
        return postURL(new URL(url), postData);
    }

    public static String getUpdateNotes(){
        try {
            return HTTP.readURL(URL_UPDATE_NOTES);
        }
        catch (Exception e) {
            return "Error getting Update Notes!";
        }
    }

    public static boolean isOnline(Activity activity) {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        IS_ONLINE = netInfo != null && netInfo.isConnectedOrConnecting();
        return IS_ONLINE;
    }
}