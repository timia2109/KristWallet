package com.timia2109.kristwallet.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Tim on 14.02.2016.
 */
public class PostData {
    StringSetNode head,tail;
    public int length;
    public PostData() {
        length = 0;
    }

    public PostData put(String key, String value) {
        if (head == null) {
            head = new StringSetNode(key, value);
            tail = head;
        }
        else {
            StringSetNode ssn = new StringSetNode(key,value);
            tail.setNext(ssn);
            tail = ssn;
        }
        length++;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        StringSetNode c = head;
        do {
            c.string(s);
        } while ((c=c.getNext()) != null);
        return s.substring(1);
    }

    private class StringSetNode {
        StringSetNode next;
        String key, value;
        public StringSetNode(String key, String value) {
            try {
                this.key = URLEncoder.encode(key,"UTF-8");
                this.value = URLEncoder.encode(value,"UTF-8");
            }
            catch (UnsupportedEncodingException ignored) {}
        }

        public void string(StringBuilder s) {
            s.append("&")
                    .append(key)
                    .append("=")
                    .append(value);
        }

        public void setNext(StringSetNode pNext) {next=pNext;}
        public StringSetNode getNext() {return next;}
    }
}
