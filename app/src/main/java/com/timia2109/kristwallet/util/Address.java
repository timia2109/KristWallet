package com.timia2109.kristwallet.util;

import com.timia2109.kristwallet.KristAPI;

import org.json.JSONException;
import org.json.JSONObject;


import java.util.Date;

// This class stores the address, balance, and last seen date of a given address.
public class Address extends APIResult {
    private String address, userName;
    private long balance, totalIn, totalOut;
    private Date firstSeen;

    public Address(JSONObject object) {
        try {
            address = object.getString("address");
            balance = object.getLong("balance");
            firstSeen = parseDate(object.getString("firstseen"));
            totalIn = object.getLong("totalin");
            totalOut = object.getLong("totalout");
        }
        catch (JSONException e) {
            isVaild = false;
            error = e;
        }
    }

    public Address(String address, String userName) {
        this.address = address;
        this.userName = userName;
    }

    public String getAddress() {
        return address;
    }

    public long getBalance() {
        return balance;
    }

    public long getTotalIn() {return totalIn;}

    public long getTotalOut() {return totalOut;}

    public Date getFirstSeen() {return firstSeen;}

    public String getUserName() {return userName;}
    public void setUserName(String pUsername) {userName = pUsername;}

    public KristAPI toAPI() {
        return new KristAPI(getAddress(), true);
    }
}