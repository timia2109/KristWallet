package com.timia2109.kristwallet.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Tim on 15.02.2016.
 */
public class Name extends APIResult {

    public String name, owner, a;
    public Date registered, updated;

    public Name(JSONObject data) {
        try {
            name = data.getString("name");
            owner = data.getString("owner");
            if (data.has("a"))
                a = data.getString("a");
            registered = parseDate( data.getString("registered") );
            updated = parseDate( data.getString("updated") );
        }
        catch (JSONException e) {
            isVaild = false;
            error = e;
        }
    }
}
