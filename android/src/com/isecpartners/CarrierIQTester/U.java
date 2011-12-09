package com.isecpartners.CarrierIQTester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

/* static utils */
public class U {
    public static String TAG = "CarrierIQTester.U";

    // The pains we must endure to use function pointers in java...
    public interface DetectFunc {
        public boolean Func(Context c, String arg1, String arg2);
    };

    public static LinkedList<String> allFiles(String dir, String pat, LinkedList<String> l) {
        try {
            for(File f : new File(dir).listFiles()) {
                if(f.isDirectory()) {
                    allFiles(f.getAbsolutePath(), pat, l);
                } else {
                    if(f.getName().matches(pat))
                        l.add(f.getAbsolutePath());
                }
            }
        } catch(Exception e) { // XXX more specific
            Log.e(U.TAG, "Error listing path: " + dir);
        }
        return l;
    }
    public static LinkedList<String> allFiles(String dir, String pat) {
        return allFiles(dir, pat, new LinkedList<String>());
    }

    public static boolean grep(String fn, String pat) {
        try {
            BufferedReader f = new BufferedReader(new InputStreamReader(new FileInputStream(fn)));
            String line;
            while((line = f.readLine()) != null) {
                if(line.matches(pat))
                    return true;
            }
        } catch(Exception e) { // XXX more specific
            Log.e(U.TAG, "Error reading file: " + fn);
        }
        return false;
    }

    public static boolean grepCmd(String cmd, String pat) {
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader f = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while((line = f.readLine()) != null) {
                if(line.matches(pat))
                    return true;
            }
        } catch (Exception e) { // XXX more specific
            Log.e(U.TAG, "Error runnin cmd: " + cmd);
        }
        return false;
    }

    public static final DetectFunc matchFile = new DetectFunc() {
        public boolean Func(Context c, String fpat, String pat) {
            boolean found = false;

            if(fpat.matches("\\.\\*") && fpat.matches("/")) {
Log.i(U.TAG, "split filename: " + fpat);
                int idx = fpat.lastIndexOf("/");
                String dir = fpat.substring(0, idx);
                String fpat2 = fpat.substring(idx+1);
                for(String fn : allFiles(dir, fpat2)) {
                    found = grep(fn, pat);
                    if(found) {
                        Log.i(U.TAG, "Found " + pat + " in " + fn);
                        break;
                    }
                }
            } else {
Log.i(U.TAG, "dont split filename: " + fpat);
                // no pattern, open file directly...
                found = grep(fpat, pat);
                if(found)
                    Log.i(U.TAG, "Found " + pat + " in " + fpat);
            }
            return found;
        }
    };

    public static final DetectFunc matchFilename = new DetectFunc() {
        public boolean Func(Context c, String dir, String pat) {
            for(File f : new File(dir).listFiles()) {
                if (f.getName().matches(pat)) {
                    Log.i(U.TAG, "Found filename " + dir + f.getName());
                } 
            }
            return false;
        }
    };

    public static final DetectFunc matchProcess = new DetectFunc() {
        public boolean Func(Context c, String pat, String unused) {
            if(grepCmd("ps", pat)) {
                Log.i(U.TAG, "Found process " + pat);
                return true;
            }
            return false;
        }
    };

    public static final DetectFunc findPackage = new DetectFunc() {
        public boolean Func(Context c, String pkg, String unused) {
            try {
                c.getPackageManager().getApplicationInfo(pkg, 0);
                Log.i(U.TAG, "Found package " + pkg);
                return true;
            } catch(NameNotFoundException e) {
                return false;
            }
        }
    };

    public static final boolean grepCmdAndLog(String cmd, String pat, String logmsg) {
        if(grepCmd(cmd, pat)) {
            Log.i(U.TAG, logmsg + pat);
            return true;
        }
        return false;
    }

    public static final DetectFunc matchDmesg = new DetectFunc() {
        public boolean Func(Context c, String pat, String unused) {
            return grepCmdAndLog("dmesg", pat, "Found in dmesg: ");
        }
    };

    public static final DetectFunc matchLogcat = new DetectFunc() {
        public boolean Func(Context c, String pat, String unused) {
            return grepCmdAndLog("logcat -d", pat, "Found in logcat: ");
        }
    };

    public static final DetectFunc matchService = new DetectFunc() {
        public boolean Func(Context c, String pat, String unused) {
            return grepCmdAndLog("service list", pat, "Found service: ");
        }
    };

    public static final DetectFunc matchClass = new DetectFunc() {
        public boolean Func(Context c, String klass, String unused) {
            try {
                Class.forName(klass);
                Log.i(U.TAG, "Found class: " + klass);
                return true;
            } catch (Exception e) { // XXX more specific
                return false;
            }
        }
    };
}

