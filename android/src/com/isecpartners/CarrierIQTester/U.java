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
            String pat2 = ".*" + pat + ".*";
            boolean found = false;

            if(fpat.contains(".*") && fpat.contains("/")) {
                int idx = fpat.lastIndexOf("/");
                String dir = fpat.substring(0, idx);
                String fpat2 = fpat.substring(idx+1);
                for(File f : allFiles(dir, fpat2)) {
                    String fn = f.getAbsolutePath();
                    found = grep(fn, pat2);
                    if(found) {
                        Log.i(U.TAG, "Found " + pat2 + " in " + fn);
                        break;
                    }
                }
            } else {
                // no pattern, open file directly...
                found = grep(fpat, pat2);
                if(found)
                    Log.i(U.TAG, "Found " + pat2 + " in " + fpat);
            }
            return found;
        }
    };

    public static final DetectFunc matchFilename = new DetectFunc() {
        public boolean Func(Context c, String dir, String pat) {
            for(File f : allFiles(dir, pat)) {
                Log.i(U.TAG, "Found filename " + dir + f.getAbsolutePath());
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
        String pat2 = ".*" + pat + ".*";
        if(grepCmd(cmd, pat2)) {
            Log.i(U.TAG, logmsg + pat);
            return true;
        }
        return false;
    }

    public static final DetectFunc matchProcess = new DetectFunc() {
        public boolean Func(Context c, String pat, String unused) {
            return grepCmdAndLog("ps", pat, "Found process: ");
        }
    };


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

