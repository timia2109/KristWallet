package com.timia2109.kristwallet;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Tim on 09.02.2016.
 * Controlls and saves all AppData. All things will saved here!
 */
public class Saver implements Serializable{

    private static transient Saver saver;
    public KristAPI[] apis;
    public boolean isSaved;
    public boolean hasUpdate;
    public long lastUpdate;
    public int lastVCode;
    public String[] webApps, webAppNames;
    public boolean allowStatics;
    public String lastWebApp, dateFormat;

    public Saver() {
        saver=this;
        isSaved = false;
        apis = new KristAPI[0];
        webAppNames = new String[0];
        webApps = new String[0];
        lastWebApp = "http://timia2109.com/kst";
        dateFormat = "dd.MM.yyyy HH:mm:ss";
    }

    public Saver(JSONObject data) {
        saver = this;
        try {
            JSONArray apisJ = data.getJSONArray("apis");
            apis = new KristAPI[apisJ.length()];
            for (int i = 0; i < apis.length; i++) {
                apis[i] = new KristAPI(apisJ.getJSONObject(i));
            }
            JSONArray webAPP = data.getJSONArray("webApps");
            JSONArray webAPPN = data.getJSONArray("webAppNames");
            webApps = new String[webAPP.length()];
            webAppNames = new String[webAPPN.length()];
            for (int i=0; i<webApps.length; i++) {
                webApps[i] = webAPP.getString(i);
                webAppNames[i] = webAPPN.getString(i);
            }
            hasUpdate = data.getBoolean("hasUpdate");
            lastUpdate = data.getLong("lastUpdate");
            lastVCode = data.getInt("lastVCode");
            allowStatics = data.getBoolean("allowStatics");
            lastWebApp = data.getString("lastWebApp");
            dateFormat = data.getString("dateFormat");

        } catch (Exception ignored) {}
    }

    public void appendAPI(KristAPI api) {
    	ArrayList<KristAPI> h = new ArrayList<KristAPI>( Arrays.asList(apis) );
    	h.add( api );
    	apis = h.toArray(new KristAPI[h.size()]);
        isSaved = false;
    }

    public void removeAPI(String address) {
        ArrayList<KristAPI> h = new ArrayList<KristAPI>( Arrays.asList(apis) );
        for (int i=0; i<h.size();i++) {
        	if (h.get(i).getAddress().equals(address)) {
        		h.remove(i);
        		break;
        	}
        }
    	apis = h.toArray(new KristAPI[h.size()]);
        isSaved = false;
    }

    public void save(Context context) {
        try {
            JSONObject data = new JSONObject();
            data.put("lastUpdate", lastUpdate);
            data.put("lastVCode", lastVCode);
            data.put("hasUpdate", hasUpdate);
            data.put("allowStatics", allowStatics);
            data.put("lastWebApp", lastWebApp);
            data.put("dateFormat", dateFormat);
            JSONArray apisJ = new JSONArray();
            for (int i=0; i<apis.length; i++) {
                apisJ.put(i, apis[i].toJSON());
            }
            JSONArray webAPP = new JSONArray();
            JSONArray webAPPN = new JSONArray();
            for (int i=0; i<webApps.length; i++) {
                webAPP.put(webApps[i]);
                webAPPN.put(webAppNames[i]);
            }
            data.put("apis", apisJ);
            data.put("webApps", webAPP);
            data.put("webAppNames", webAPPN);
            File file = getFile(context);
            FileWriter writer = new FileWriter(file);
            writer.write(data.toString());
            writer.close();
            isSaved = true;
        }
        catch(Exception ex){
            System.out.println(ex.toString());
            Toast.makeText(context, R.string.errorLoadData, Toast.LENGTH_SHORT).show();
        }
    }

    public void setWebApps(JSONArray apps) {
        String[] newA = new String[apps.length()];
        String[] newAN = new String[apps.length()];
        JSONObject c;
        for (int i=0; i<apps.length(); i++) {
            try {
                c = apps.getJSONObject(i);
                newA[i] = c.getString("url");
                newAN[i] = c.getString("name");
            }
            catch (JSONException ignored) {}
        }
        webApps = newA;
        webAppNames = newAN;
    }

    public void nosave() {isSaved = false;}

    //Static's

    public static Saver load() {
        if (saver == null)
            return null;
        return saver;
    }

    public static Saver load(Context context) {
        if (saver == null) {
            File file = getFile(context);
            if (!file.exists()) {
                //No data, nothing to load... Handled by the MainActivity
                return null;
            }
            else {
                try {
                    FileReader r = new FileReader(file);
                    BufferedReader br = new BufferedReader(r);
                    StringBuilder s = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null)
                        s.append(line);
                    r.close();
                    saver = new Saver(new JSONObject(s.toString()));
                    saver.isSaved = true;
                }
                catch(Exception ex){
                    Toast.makeText(context, R.string.errorLoadData, Toast.LENGTH_SHORT).show();
                    return null;
                }
            }
        }
        return saver;
    }

    public static boolean hasSaver(Context context) {
        return getFile(context).exists();
    }

    public static File getFile(Context context) {
        return new File(context.getFilesDir(), "saver.json");
    }

    public static String stringifyDate(Date date, String format, Context context) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.US);
            return simpleDateFormat.format(date);
        } catch (IllegalArgumentException ignored) {
            return context.getString(R.string.failDateFormat);
        }
    }

    public static String stringifyDate(Date date, Saver saver, Context context) {
        return stringifyDate(date, saver.dateFormat, context);
    }

}
