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
        public boolean Func(Context c, LogFunc l, String arg1, String arg2);
    };

    public interface LogFunc {
        public void log(String s);
    };

    public static LinkedList<File> allFiles(String dir, String pat, LinkedList<File> l) {
        try {
            for(File f : new File(dir).listFiles()) {
                if(f.isDirectory()) {
                    allFiles(f.getAbsolutePath(), pat, l);
                } else {
                    if(f.getName().matches(pat))
                        l.add(f);
                }
            }
        } catch(Exception e) { // XXX more specific
            Log.e(U.TAG, "Error listing path: " + dir);
        }
        return l;
    }
    public static LinkedList<File> allFiles(String dir, String pat) {
        return allFiles(dir, pat, new LinkedList<File>());
    }

    public static String grep(String fn, String pat) {
        try {
            BufferedReader f = new BufferedReader(new InputStreamReader(new FileInputStream(fn)));
            String line;
            while((line = f.readLine()) != null) {
                if(line.matches(pat))
                    return line;
            }
        } catch(Exception e) { // XXX more specific
            Log.e(U.TAG, "Error reading file: " + fn);
        }
        return null;
    }

    public static String grepCmd(String cmd, String pat) {
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader f = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while((line = f.readLine()) != null) {
                if(line.matches(pat))
                    return line;
            }
        } catch (Exception e) { // XXX more specific
            Log.e(U.TAG, "Error running cmd: " + cmd);
        }
        return null;
    }

    public static final DetectFunc matchFile = new DetectFunc() {
        public boolean Func(Context c, LogFunc l, String fpat, String pat) {
            String pat2 = ".*" + pat + ".*";
            String found = null;
            String fn = null;

            if(fpat.contains(".*") && fpat.contains("/")) {
                int idx = fpat.lastIndexOf("/");
                String dir = fpat.substring(0, idx);
                String fpat2 = fpat.substring(idx+1);
                for(File f : allFiles(dir, fpat2)) {
                    fn = f.getAbsolutePath();
                    found = grep(fn, pat2);
                    if(found != null)
                        break;
                }
            } else {
                // no pattern, open file directly...
                fn = fpat;
                found = grep(fn, pat2);
            }
            if(found != null) 
                Log.i(U.TAG, "Found match for " + pat + " in " + fn + ": " + found);
            return (found != null);
        }
    };

    public static final DetectFunc matchFilename = new DetectFunc() {
        public boolean Func(Context c, LogFunc l, String dir, String pat) {
            for(File f : allFiles(dir, pat)) {
                Log.i(U.TAG, "Found filename matching " + pat + ": " + f.getAbsolutePath());
                return true;
            }
            return false;
        }
    };

    public static final DetectFunc findPackage = new DetectFunc() {
        public boolean Func(Context c, LogFunc l, String pkg, String unused) {
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
        String pat2 = ".*" + pat + ".*";
        String m = grepCmd(cmd, pat2);
        if(m != null) {
            Log.i(U.TAG, "Found " + logmsg + " matching " + pat + ": " + m);
            return true;
        }
        return false;
    }

    public static final DetectFunc matchProcess = new DetectFunc() {
        public boolean Func(Context c, LogFunc l, String pat, String unused) {
            return grepCmdAndLog("ps", pat, "process");
        }
    };


    public static final DetectFunc matchDmesg = new DetectFunc() {
        public boolean Func(Context c, LogFunc l, String pat, String unused) {
            return grepCmdAndLog("dmesg", pat, "dmesg");
        }
    };

    public static final DetectFunc matchLogcat = new DetectFunc() {
        public boolean Func(Context c, LogFunc l, String pat, String unused) {
            return grepCmdAndLog("logcat -d", pat, "logcat");
        }
    };

    public static final DetectFunc matchService = new DetectFunc() {
        public boolean Func(Context c, LogFunc l, String pat, String unused) {
            return grepCmdAndLog("service list", pat, "service");
        }
    };

    public static final DetectFunc matchClass = new DetectFunc() {
        public boolean Func(Context c, LogFunc l, String klass, String unused) {
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

