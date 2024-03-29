package com.isecpartners.CarrierIQTester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
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

    public static class LogCapture implements LogFunc {
        LinkedList<String> strings;

        LogCapture() {
            strings = new LinkedList<String>();
        }

        public void log(String s) {
            strings.add(s);
        }

        public LinkedList<String> get() {
            LinkedList<String> ss = strings;
            strings = new LinkedList<String>();
            return ss;
        }
    }

    public static String vecToString(long v) {
        StringBuilder flags = new StringBuilder();
        for(long i = 0; i < 64; i ++) {
        	if((v & (1L << i)) != 0) {
        		if(flags.length() > 0)
        			flags.append(",");
        		flags.append("" + i);
        	}
        }
        if(flags.length() == 0)
        	flags.append("none");
        return flags.toString();
    }
    
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

    public static boolean grep(LogFunc l, String fn, String pat) {
    	int cnt = 0;
        try {
            BufferedReader f = new BufferedReader(new InputStreamReader(new FileInputStream(fn)));
            String line;
            while((line = f.readLine()) != null) {
                if(line.matches(pat)) {
                	cnt ++;
                	l.log(line);
                }
            }
        } catch(Exception e) { // XXX more specific
            Log.e(U.TAG, "Error reading file: " + fn);
        }
        return cnt > 0;
    }

    public static boolean grepCmd(LogFunc l, String cmd, String pat) {
        int cnt = 0;
    	try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader f = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while((line = f.readLine()) != null) {
                if(line.matches(pat)) {
                	cnt ++;
                    l.log(line);
                }
            }
        } catch (Exception e) { // XXX more specific
            Log.e(U.TAG, "Error running cmd: " + cmd);
        }
        return cnt > 0;
    }

    public static final DetectFunc matchFile = new DetectFunc() {
        public boolean Func(Context c, LogFunc l, String fpat, String pat) {
            String pat2 = ".*" + pat + ".*";
            LogCapture matched = new LogCapture();

            LinkedList<File> files;
            if(fpat.contains(".*") && fpat.contains("/")) {
                int idx = fpat.lastIndexOf("/");
                String dir = fpat.substring(0, idx);
                String fpat2 = fpat.substring(idx+1);
                files = allFiles(dir, fpat2);
            } else {
            	files = new LinkedList<File>();
            	files.add(new File(fpat));
            }
            
            int cnt = 0;
            for(File f : files) {
                String fn = f.getAbsolutePath();
                grep(matched, fn, pat2);
                for(String m : matched.get()) {
                	l.log("  Found match for " + pat + " in " + fn + ": " + m + "\n");
                  	cnt++;
               }
            }
            return cnt > 0;
        }
    };

    public static final DetectFunc matchFilename = new DetectFunc() {
        public boolean Func(Context c, LogFunc l, String dir, String pat) {
            for(File f : allFiles(dir, pat)) {
                l.log("  Found filename matching " + pat + ": " + f.getAbsolutePath() + "\n");
                return true;
            }
            return false;
        }
    };

    public static final DetectFunc matchPackage = new DetectFunc() {
        public boolean Func(Context c, LogFunc l, String pat, String unused) {
        	String pat2 = ".*" + pat + ".*";
        	boolean found = false;
        	for(PackageInfo p : c.getPackageManager().getInstalledPackages(0)) {
        		if(p.packageName.matches(pat2)) {
        			found = true;
        			l.log("  Found package matching " + pat + ": " + p.packageName + "\n");
        		}
        	}
        	return found;
        }
    };

    public static final boolean grepCmdAndLog(LogFunc l, String cmd, String pat, String logmsg) {
        String pat2 = ".*" + pat + ".*";
        LogCapture matched = new LogCapture();
        if(grepCmd(matched, cmd, pat2)) {
            for(String m : matched.get())
                l.log("  Found " + logmsg + " matching " + pat + ": " + m + "\n");
            return true;
        }
        return false;
    }

    public static final DetectFunc matchProcess = new DetectFunc() {
        public boolean Func(Context c, LogFunc l, String pat, String unused) {
            return grepCmdAndLog(l, "ps", pat, "process");
        }
    };

    public static final DetectFunc matchDmesg = new DetectFunc() {
        public boolean Func(Context c, LogFunc l, String pat, String unused) {
            return grepCmdAndLog(l, "dmesg", pat, "dmesg");
        }
    };

    public static final DetectFunc matchLogcat = new DetectFunc() {
        public boolean Func(Context c, LogFunc l, String pat, String unused) {
            return grepCmdAndLog(l, "logcat -d", pat, "logcat");
        }
    };

    public static final DetectFunc matchService = new DetectFunc() {
        public boolean Func(Context c, LogFunc l, String pat, String unused) {
        	boolean found = false;
        	String pat2 = ".*" + pat + ".*";
            for(PackageInfo p : c.getPackageManager().getInstalledPackages(PackageManager.GET_SERVICES)) {
            	if(p.services != null)
           		for(ServiceInfo s : p.services) {
           			if(s != null && s.name.matches(pat2)) {
           				l.log("  Found service matching " + pat + ": " + s.name + "\n");
           				found = true;
           			}
           		}
            }
            return found;
        }
    };

    public static final DetectFunc findClass = new DetectFunc() {
        public boolean Func(Context c, LogFunc l, String klass, String unused) {
            try {
                Class.forName(klass);
                l.log("  Found class: " + klass + "\n");
                return true;
            } catch (Exception e) { // XXX more specific
                return false;
            }
        }
    };
}

