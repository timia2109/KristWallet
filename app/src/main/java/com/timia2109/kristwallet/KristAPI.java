package com.timia2109.kristwallet;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.text.ParseException;
import io.github.apemanzilla.kwallet.*;

public class KristAPI implements Serializable {
    static String donateAddress = "k840a8mqze";

    private String remoteAPI, key, address;
    private long cacheTime=0, lastCache=0,cacheTimeTransactions=0;
    private Transaction[] tCache;

    public KristAPI(String remoteAPI,String address) {
        this.remoteAPI = remoteAPI;
        this.key = address;
        this.address = makeAddressV2(getKey());
    }

    public void makeViewAddress(String address) {
        this.key = null;
        this.address = address;
    }

    public long getBalance() throws NumberFormatException, IOException {
        if (lastCache+300 < (System.currentTimeMillis() / 1000L)) {
            lastCache = getBalance(address);
            cacheTime = System.currentTimeMillis() / 1000L;
        }
        return lastCache;
    }

    public long getBalance(String address) throws NumberFormatException, IOException {
        return Long.parseLong(HTTP.readURL(remoteAPI + "?getbalance=" + address));
    }

    public Transaction[] getTransactions(String address) throws MalformedURLException, IOException {
        String transactionData = HTTP.readURL(remoteAPI + "?listtx=" + address);
        transactionData = transactionData.substring(0, transactionData.length() - 3).replace("\n", "").replace("\r", "");
        if (transactionData.length() == 0) {
            return new Transaction[0];
        } else if ((transactionData.length() % 31) == 0) {
            Transaction[] transactions = new Transaction[transactionData.length() / 31];
            for (int i = 0; i < transactionData.length() / 31; i++) {
                transactions[i] = new Transaction(transactionData.substring(i * 31, (i + 1) * 31), address);
            }
            return transactions;
        } else {
            return new Transaction[0];
        }
    }

    public Transaction[] getTransactions() throws IOException {
        if (cacheTimeTransactions+300 < (System.currentTimeMillis() / 1000L)) {
            tCache = getTransactions(address);
            cacheTimeTransactions = System.currentTimeMillis() / 1000L;
        }
        return tCache;
    }

    public Address[] getRichList() throws IOException, ParseException {
        // Extra HTML tags seem to get caught without this regex
        String richList = HTTP.readURL(remoteAPI+"?richapi").replaceAll("<[^>]*>", "");
        if (richList.length() == 0) {
            return new Address[0];
        } else if ((richList.length() % 29) == 0) {
            Address[] result = new Address[richList.length() / 29];
            for (int i = 0; i < richList.length() / 29; i++) {
                result[i] = new Address(richList.substring(i * 29, (i + 1) * 29));

            }
            return result;
        } else {
            return new Address[0];
        }
    }

    public enum TransferResults {
        Success,
        InsufficientFunds,
        NotEnoughKST,
        BadValue,
        InvalidRecipient,
        SelfSend,
        Unknown,
        WebConnectFail,
        NoLong,
    }

    public TransferResults sendKrist(long amount, String recipient) throws IOException {
        if (address == recipient)
            return TransferResults.SelfSend;
        switch (HTTP.readURL(remoteAPI+"?pushtx2&q=" + recipient + "&pkey=" + getKey() + "&amt=" + amount)) {
            case "Success": {
                return TransferResults.Success;
            }
            case "Error1": {
                return TransferResults.InsufficientFunds;
            }
            case "Error2": {
                return TransferResults.NotEnoughKST;
            }
            case "Error3": {
                return TransferResults.BadValue;
            }
            case "Error4": {
                return TransferResults.InvalidRecipient;
            }
            default: {
                return TransferResults.Unknown;
            }
        }
    }

    public String getAddress() {
        return address;
    }
    public String getKey() {return SHA256.hash256("KRISTWALLET"+key)+"-000";}
    public String getRawKey() {return key; }

    private char numtochar(int inp) {
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

    private String makeAddressV2(String key) {
        String[] protein = {"", "", "", "", "", "", "", "", ""};
        int link = 0;
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

}