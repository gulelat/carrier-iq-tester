package com.isecpartners.CarrierIQTester;

import android.content.Context;
import android.util.Log;

public enum Detect {
    /*
     * Our list of detectors.
     * Credit where its due, this is based heavily on the code at
     * https://github.com/project-voodoo/simple_carrieriq_detector_app
     *
     * Note: do not change this list without good reason...
     * and when you do, you may have to bump the version.
     * Analysis will be affected by the meaning of these tests.
     *
     * A version number is included here and should probably
     * be adjusted when analysis options change.
     */

    // XXX should we add a score to these?
    DETECT0(0, U.matchPackage, "com.carrieriq"),
    DETECT1(1, U.matchPackage, "iqagent"),

    DETECT4(4, U.matchFilename, "/dev/", "sdio_tty_ciq.*"),
    DETECT5(5, U.matchFilename, "/dev/socket/", "iqbrd"),

    // what about workarounds?  perhaps a better pattern is called for?
    DETECT6(6, U.matchFile, "/proc/kallsyms", "_ciq_"),

    DETECT7(7, U.matchDmesg, "iq.logging"),
    DETECT8(8, U.matchDmesg, "iq.service"),
    DETECT9(9, U.matchDmesg, "iq.cadet"),
    DETECT10(10, U.matchDmesg, "iq.bridge"),
    DETECT11(11, U.matchDmesg, "SDIO_CIQ"),
    DETECT12(12, U.matchDmesg, "ttyCIQ"),
    DETECT13(13, U.matchDmesg, "iqagent"),

    DETECT14(14, U.matchLogcat, "AppWatcherCIQ"),
    DETECT15(15, U.matchLogcat, "IQService"),
    DETECT16(16, U.matchLogcat, "IQBridge"),
    DETECT17(17, U.matchLogcat, "IQClient"),
    DETECT18(18, U.matchLogcat, "IQ_METRIC"),
    DETECT19(19, U.matchLogcat, "_CIQ"),
    DETECT20(20, U.matchLogcat, "IQAgent"),
    DETECT21(21, U.matchLogcat, "iqagent"),
    DETECT22(22, U.matchLogcat, "KernelPanicCiqBroadcastReceiver"),
    DETECT23(23, U.matchLogcat, "ToCIQ"),
    DETECT24(24, U.matchLogcat, "submitAL34"),
    DETECT25(25, U.matchLogcat, "AgentService_J"),
    DETECT26(26, U.matchLogcat, "com.carrieriq."),
    DETECT27(27, U.matchLogcat, "com/carrieriq/"),
    DETECT28(28, U.matchLogcat, ".iqd"),

    DETECT29(29, U.matchFile, "/etc/.*.txt", "enableCIQ"),

    DETECT30(30, U.matchFilename, "/system/", "iqmsd"),
    DETECT31(31, U.matchFilename, "/system/", "libiq_.*"),
    DETECT32(32, U.matchFilename, "/system/", "iqbridged"),

    DETECT33(33, U.matchService, "carrieriq"),

    DETECT34(34, U.matchProcess, "iqmsd"),
    DETECT35(35, U.matchProcess, "iqbridged"),
    DETECT36(36, U.matchProcess, "iqd"),

    // XXX should we instead just look for a file for this, like a jar?
    DETECT37(37, U.findClass, "com.carrieriq.iqagent.service.receivers.BootCompletedReceiver"),
    DETECT38(38, U.findClass, "com.carrieriq.iqagent.IQService");


    // ---------------------------------
    public static final long version = 0;
    final int n;
    final U.DetectFunc func;
    final String arg1, arg2;

    Detect(int n, U.DetectFunc func, String arg1, String arg2) {
        this.n = n;
        this.func = func;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }
    Detect(int n, U.DetectFunc func, String arg1) {
        this.n = n;
        this.func = func;
        this.arg1 = arg1;
        this.arg2 = "";
    }

    public long test(Context c, U.LogFunc l) {
        long start = System.currentTimeMillis();
        boolean res = func.Func(c, l, arg1, arg2);
        double sec = (System.currentTimeMillis() - start) / 1000.0;

        Log.i("DETECT", "test " + n + " returned " + res + " in " + sec + "\n");
        if(res)
            l.log("test " + n + " found evidence of CIQ\n");
        else
            l.log("test " + n + " found nothing\n");
        
        return res ? (1L << n) : 0;
    }
}

