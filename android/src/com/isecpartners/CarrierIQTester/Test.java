package com.isecpartners.CarrierIQTester;

import android.content.Context;
import android.util.Log;

public class Test {
    public static final String TAG = "Test";

    public static U.LogFunc log = new U.LogFunc() {
        public void log(String s) {
            Log.i(Test.TAG + "-Log:", s);
        }
    };

    public static void detect(Context c, String name, U.DetectFunc f, String arg1, String arg2) {
        boolean res = f.Func(c, log, arg1, arg2);
        Log.i(Test.TAG, name + " " + arg1 + " " + arg2 + " -> " + res);
    }
    public static void detect(Context c, String name, U.DetectFunc f, String arg1) {
        detect(c, name, f, arg1, "");
    }

    public static void all(Context c) {
        detect(c, "pkg", U.findPackage, "com.android.phone");
        detect(c, "pkg", U.findPackage, "com.android.bogus");

        detect(c, "fname", U.matchFilename, "/dev", "mtd0");
        detect(c, "fname", U.matchFilename, "/dev", "mtd.*");
        detect(c, "fname", U.matchFilename, "/dev", "bogus.*");

        detect(c, "file", U.matchFile, "/etc/hosts", ".*localhost.*");
        detect(c, "file", U.matchFile, "/etc/hosts", ".*bogus.*");
        detect(c, "file", U.matchFile, "/etc/.*.xml", ".*monospace.*");
        detect(c, "file", U.matchFile, "/etc/.*.xml", ".*bogus.*");

        detect(c, "dmesg", U.matchDmesg, "yaffs");
        detect(c, "dmesg", U.matchDmesg, "bogus");

        detect(c, "logcat", U.matchLogcat, "GC_CONCURRENT");
        detect(c, "logcat", U.matchLogcat, "bogus");

        detect(c, "service", U.matchService, "SensorServer");
        detect(c, "service", U.matchService, "bogus");

        detect(c, "process", U.matchProcess, "mediaserver");
        detect(c, "process", U.matchProcess, "bogus");

        detect(c, "class", U.matchClass, "android.net.Proxy");
        detect(c, "class", U.matchClass, "com.android.bogus");
    }
}

