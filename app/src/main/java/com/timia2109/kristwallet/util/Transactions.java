package com.timia2109.kristwallet.util;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

public class Transactions extends APIResult implements Serializable {
    private long id;
    private Date time;
    private String toAddr;
    private String fromAddr, name, metadata;
    private long amount;
    private boolean isVaild;
    private Exception error;

    //Server time zone isCET

    public Transactions(JSONObject object) {
        try {
            id = object.getLong("id");
            fromAddr = object.getString("from");
            toAddr = object.getString("to");
            amount = object.getLong("value");
            name = object.getString("name");
            time = parseDate(object.getString("time"));
            metadata = object.getString("metadata");
        }
        catch (JSONException e) {
            isVaild = false;
            error = e;
        }
    }

    public Date getTime() {
        return time;
    }

    public String getToAddr() {
        return toAddr;
    }

    public String getFromAddr() {
        if (fromAddr.equals("a"))
            return name;
        return fromAddr;
    }

    public boolean isMined() {
        return fromAddr == null || fromAddr.equals("") || fromAddr.equals("null");
    }

    public long getAmount() {
        return amount;
    }

    public String getMetadata() {
        if (!metadata.equals("null"))
            return metadata;
        else
            return null;
    }
}

