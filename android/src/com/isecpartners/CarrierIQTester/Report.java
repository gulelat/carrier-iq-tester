package com.isecpartners.CarrierIQTester;

import java.util.LinkedList;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;

import android.util.Log;

public class Report {
	static final String TAG = "Report";
    static final String reportUrl = "https://carrieriqtester.appspot.com/report"; // XXX resource
    //static final String reportUrl = "http://10.200.200.110:8080/report"; // XXX testing
    static final String secret = "no cheating, please"; // XXX resource

    public long version;
    public String os;
    public String phone;
    public String carrier;
    public long features;
    public String auth;

    public Report() {
    }

    static String hex(byte[] bs) { // XXX move to utils
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        for(byte b : bs)
            f.format("%02x", b & 0xff);
        return new String(sb);
    }

    void setAuth() {
        String s = String.format("secret=%s:version=%d:os=\"%s\":phone=\"%s\":carrier=\"%s\":features=%d", secret, version, os, phone, carrier, features);
        try {
        	MessageDigest dig = MessageDigest.getInstance("SHA-256");
        	dig.update(s.getBytes());
        	auth = hex(dig.digest());
        } catch(NoSuchAlgorithmException e) {
        	Log.e(TAG, "Couldn't make sh256 hash");
        }
    }

    static void addPair(LinkedList<NameValuePair> l, String n, String v) {
        l.add(new BasicNameValuePair(n, v));
    }

    public boolean send() {
        setAuth();

        LinkedList<NameValuePair> ps = new LinkedList<NameValuePair>();
        addPair(ps, "version", "" + version);
        addPair(ps, "os", os);
        addPair(ps, "phone", phone);
        addPair(ps, "carrier", carrier);
        addPair(ps, "features", "" + features);
        addPair(ps, "auth", auth);

        HttpPost post = new HttpPost(reportUrl);

        try {        
        	post.setEntity(new UrlEncodedFormEntity(ps));
            HttpResponse r = new DefaultHttpClient().execute(post);
            Log.i(TAG, "POST submitted to " + reportUrl + ": " + r.getStatusLine().getStatusCode());
            return r.getStatusLine().getStatusCode() == 200;
        } catch(ClientProtocolException e) {
            Log.e(TAG, "POST protocol error " + e);
        } catch(IOException e) {
            Log.e(TAG, "POST IO error " + e);
        }
        return false;
    }
}

