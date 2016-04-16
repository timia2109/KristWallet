package com.timia2109.kristwallet;

/**
 * @author: Apemanzlla & timia2109
 * @version: 2.0
 *
 * KristAPI orginal by apemanzilla.
 * V2 by me to use the new JSON ready API!
 * Feel free to use!
 */

import com.timia2109.kristwallet.util.Address;
import com.timia2109.kristwallet.util.Name;
import com.timia2109.kristwallet.util.PostData;
import com.timia2109.kristwallet.util.Transactions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;

public class KristAPI implements Serializable {
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    static final String donateAddress = "k840a8mqze";
    static String URL = "http://krist.ceriat.net";
    static final String ADDR_URL = URL+"/addresses/";
    static final String syncNode = "https://raw.githubusercontent.com/BTCTaras/kristwallet/master/staticapi/syncNode";
    public static final String currency = " KST";


    private String key, address;
    private long totalin=0, totalout=0, lastBalance;
    private String alias;

    public KristAPI(String key) {
        this.key = key;
        this.address = makeAddressV2(getKey());
    }

    public KristAPI(JSONObject sData) {
        try {
            key = sData.getString("key");
            address = sData.getString("address");
            alias = sData.getString("alias");
        } catch (Exception ignored) {}
    }

    public KristAPI(String address, boolean viewUser) {
        this.address = address;
    }

    public JSONObject toJSON() {
        JSONObject data = new JSONObject();
        try {
            data.put("key", key);
            data.put("address", address);
            data.put("alias", alias);
        } catch (Exception ignored) {}
        return data;
    }

    public void makeViewAddress(String address) {
        this.key = null;
        this.address = address;
    }

    public long getBalance() throws IOException, JSONException, APIBadResult {
        JSONObject node = getAddressNode(null);
        node = node.getJSONObject("address");
        totalin = node.getLong("totalin");
        totalout = node.getLong("totalout");
        lastBalance = node.getLong("balance");
        return lastBalance;
    }

    public long getTotalin() {return totalin;}
    public long getTotalout() {return totalout;}
    public long getCachedBalance() {return lastBalance;}

    public JSONObject getAddressNode(String plusParameter) throws IOException, JSONException, APIBadResult {
        StringBuilder uUrl = new StringBuilder(ADDR_URL).append(address);
        if (plusParameter != null)
            uUrl.append("/").append(plusParameter);
        JSONObject data = new JSONObject(HTTP.readURL( uUrl.toString() ));
        if (data.getBoolean("ok")) {
            return data;
        }
        else {
            throw new APIBadResult(data.getString("error")+"\n"+uUrl.toString());
        }
    }

    public Transactions[] getTransactions() throws APIBadResult, JSONException, IOException {
        return getTransactions(50, 0);
    }


    /**
     * Gives the transactions as JSONArray back
     * @param limit length off array
     * @param offset Startvalue
     * @return An Array with Transactions Objects.(See KristDocs)
     */
    public Transactions[] getTransactions(int limit, int offset) throws APIBadResult, JSONException, IOException {
        JSONObject node = getAddressNode("transactions?limit="+limit); //+"&offset="+offset);
        JSONArray transactions = node.getJSONArray("transactions");
        if (transactions != null) {
            int size = transactions.length();
            Transactions[] rtn = new Transactions[size];
            for (int i=0; i<size; i++) {
                rtn[i] = new Transactions(transactions.getJSONObject(i));
            }
            return rtn;
        }
        else
            throw new APIBadResult("transactions = null");
    }

    /**
     * Gives you the names of the Acc.
     * @return Array with names of the Krist Account (See KristDocs)
     * @throws APIBadResult Error on Server Site
     * @throws JSONException Error on parsing JSON
     * @throws IOException Error with Webrequest
     */
    public Name[] getNames() throws APIBadResult, JSONException, IOException {
        JSONObject node = getAddressNode("names");
        JSONArray names = node.getJSONArray("names");
        if (names == null)
            throw new APIBadResult("names = null");
        int size = names.length();
        Name[] namesArr = new Name[size];
        for (int i=0; i<size; i++) {
            namesArr[i] = new Name( names.getJSONObject(i) );
        }
        return namesArr;
    }

    public Address[] getRichList() throws APIBadResult, JSONException, IOException {
        String uUrl = ADDR_URL+"rich";
        JSONObject data = new JSONObject(HTTP.readURL( uUrl ));
        if (data.getBoolean("ok")) {
            JSONArray addO = data.getJSONArray("addresses");
            if (addO == null)
                throw new APIBadResult("addresses = null");
            int size = addO.length();
            Address[] addresses = new Address[size];
            for (int i=0; i<size; i++) {
                addresses[i] = new Address(addO.getJSONObject(i));
            }
            return addresses;
        }
        else {
            throw new APIBadResult(data.getString("error"));
        }
    }

    public void sendKrist(long amount, String receiver, String metadata) throws IOException, SendKristException {
        PostData pd = new PostData();
        pd.put( "privatekey", getKey() )
                .put( "to", receiver)
                .put("amount", Long.toString(amount));
        if (metadata != null)
            pd.put("metadata", metadata);
        String result = HTTP.postURL(URL+"/transactions", pd);
        try {
            JSONObject res = new JSONObject(result);
            if (!res.getBoolean("ok")) {
                throw new SendKristException(res.getString("error")+"\nURL:"+URL+"/transactions"+"\nPD:"+pd.toString());
            }
        }
        catch (JSONException e) {
            throw new SendKristException("JSON is not parsable");
        }
    }

    public void sendKrist(long amount, String to) throws IOException, SendKristException {
       sendKrist(amount, to, null);
    }

    public void buyName(String name) throws IOException, JSONException, APIBadResult {
        PostData pd = new PostData();
        pd.put("privatekey", getKey());
        JSONObject result = new JSONObject( HTTP.postURL(URL+"/names/"+name, pd) );
        if (!result.getBoolean("ok")) {
            throw new APIBadResult(result.getString("error"));
        }
    }

    public void transfereName(String name, String to) throws IOException, JSONException, APIBadResult {
        PostData pd = new PostData();
        pd.put("address", to)
                .put("privatekey", getKey());
        JSONObject result = new JSONObject( HTTP.postURL(URL+"/names/"+name+"/transfer", pd) );
        if (!result.getBoolean("ok"))
            throw new APIBadResult( result.getString("error") );
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        if (alias != null)
            return alias;
        return address;
    }

    public String getKey() {return SHA256.hash256("KRISTWALLET"+key)+"-000";}

    public String getRawKey() {return key; }

    public boolean isFullAPI() {
        return key != null;
    }

    public static char numtochar(int inp) {
        for (int i = 6; i <= 251; i += 7)
        {
            if (inp <= i)
            {
                if (i <= 69)
                {
                    return (char) ('0' + (i - 6) / 7);
                }
                return (char) ('a' + ((i - 76) / 7));
            }
        }
        return 'e';
    }

    public static String makeAddressV2(String key) {
        String[] protein = {"", "", "", "", "", "", "", "", ""};
        int link;
        String v2 = "k";
        String stick = SHA256.hash256(SHA256.hash256(key));
        for (int i = 0; i <= 9; i++) {
            if (i < 9) {
                protein[i] = stick.substring(0, 2);
                stick = SHA256.hash256(SHA256.hash256(stick));
            }
        }
        int i = 0;
        while (i <= 8) {
            link = Integer.parseInt(stick.substring(2*i,2+(2*i)),16) % 9;
            if (protein[link].equals("")) {
                stick =SHA256.hash256(stick);
            } else {
                v2 = v2 + numtochar(Integer.parseInt(protein[link],16));
                protein[link] = "";
                i++;
            }
        }
        return v2;
    }

    private String getPage(String query) throws IOException{
        return HTTP.readURL(URL + "?" + query);
    }

    public String getLastBlock() {
        try {
            return getPage("lastblock");
        }
        catch (Exception e) {
            return "";
        }
    }

    public String getWork() {
        try {
            return getPage("getwork");
        }
        catch (Exception e) {
            return "";
        }
    }

    public boolean submitBlock(String nonce) {
        try {
            getPage("submitblock&address=" + this.address + "&nonce=" + nonce);
            return true;
        }
        catch (Exception e) {return false;}
    }

    public static void refreshURL() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String a;
                try {
                   a = HTTP.readURL(syncNode);
                } catch (Exception e) {a = URL;}
                URL = a;
            }
        });
        t.start();
    }

    public void setAlias() {this.alias=null;}
    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {return alias;}

    public static class APIBadResult extends Exception {
        public APIBadResult(String message) { super(message); }
    }

    public static class SendKristException extends Exception {
        public SendKristException(String message) {super(message);}
    }
}