package com.isecpartners.CarrierIQTester;

public enum Detectors {
    /*
     * Our list of detectors.
     *
     * Note: do not change this list without good reason...
     * and when you do, you may have to bump the version.
     * The ordering of this list is the order in which the
     * bits in the features flag is defined.  Changing ordering
     * will alter the meaning of features!
     * Changing tests may require discarding of old data in
     * the database, too. 
     *
     * A version number is included here and should probably
     * be adjusted when analysis options change.
     */

    // XXX should we add a score to these?
    Detectors(findPackage, "com.carrieriq.iqagent");
    Detectors(findPackage, "com.htc.android.iqagent");
    Detectors(findPackage, "com.carrieriq.attrom");
    Detectors(findPackage, "com.carrieriq.tmobile");

    Detectors(matchFilename, "/dev/", "sdio_tty_ciq.*");
    Detectors(matchFilename, "/dev/socket/", "iqbrd");

    // what about workarounds?  perhaps a better pattern is called for?
    Detectors(matchFile, "/proc/kallsyms", "_ciq_");

    Detectors(matchDmesg, "iq.logging");
    Detectors(matchDmesg, "iq.service");
    Detectors(matchDmesg, "iq.cadet");
    Detectors(matchDmesg, "iq.bridge");
    Detectors(matchDmesg, "SDIO_CIQ");
    Detectors(matchDmesg, "ttyCIQ");
    Detectors(matchDmesg, "iqagent");

    Detectors(matchLogcat, "AppWatcherCIQ");
    Detectors(matchLogcat, "IQService");
    Detectors(matchLogcat, "IQBridge");
    Detectors(matchLogcat, "IQClient");
    Detectors(matchLogcat, "IQ_METRIC");
    Detectors(matchLogcat, "_CIQ");
    Detectors(matchLogcat, "IQAgent");
    Detectors(matchLogcat, "iqagent");
    Detectors(matchLogcat, "KernelPanicCiqBroadcastReceiver");
    Detectors(matchLogcat, "ToCIQ");
    Detectors(matchLogcat, "submitAL34");
    Detectors(matchLogcat, "AgentService_J");
    Detectors(matchLogcat, "com.carrieriq.");
    Detectors(matchLogcat, "com/carrieriq/");
    Detectors(matchLogcat, ".iqd");

    Detectors(matchFile, "/etc/.*.txt", "enableCIQ");

    Detectors(matchFilename, "/system/", "iqmsd");
    Detectors(matchFilename, "/system/", "libiq_.*");
    Detectors(matchFilename, "/system/", "iqbridged");

    Detectors(matchServices, "carrieriq");

    Detectors(matchProcess, "iqmsd");
    Detectors(matchProcess, "iqbridged");
    Detectors(matchProcess, "iqd");

    // XXX should we instead just look for a file for this, like a jar?
    Detectors(matchClass, "com.carrieriq.iqagent.service.receivers.BootCompletedReceiver");
    Detectors(matchClass, "com.carrieriq.iqagent.IQService");


    public static final long version = 0;
    static long nextFlag = 1;
    public final long flag;
    public final DetectFunc func;
    public final String arg1, arg2;

    Detectors(DetectFunc func, String arg1, String arg2) {
        this.flag = nextFlag;
        nextFlag = nextFlag << 1;
        this.func = func;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }
    Detectors(DetectFunc func, String arg1) {
        this.flag = nextFlag;
        nextFlag = nextFlag << 1;
        this.func = func;
        this.arg1 = arg1;
        this.arg2 = "";
    }

    // The pains we must endure to use function pointers in java...
    public Interface DetectFunc {
        public bool Func(Context c, String arg1, String arg2);
    }

    static final DetectFunc matchFile = new DetectFunc() {
        public bool Func(Context c, String fpat, String pat) {
            // XXX split fpat into dir and pattern
            // iterate over matching files, read file, match lines against pat
        }
    };

    static final DetectFunc matchFilename = new DetectFunc() {
        public bool Func(Context c, String dir, String pat) {
            for(File f : new File(dir).listFiles()) {
                if (f.getName().matches(pat)) {
                    Log.i(TAG, "Found filename " + dir + f.getname());
                } 
            }
            return false;
        }
    };

    static final DetectFunc matchProcess = new DetectFunc() {
        public bool Func(Context c, String fpat, String unused) {
            // XXX list processes, match
        }
    };

    static final DetectFunc findPackage = new DetectFunc() {
        public bool Func(Context c, String pkg, String unused) {
            try {
                c.getPackageManager().getApplicationInfo(pkg, 0);
                Log.i(TAG, "Found package " + pkg);
                return true;
            } catch(NameNotFoundException e) {
                return false;
            }
        }
    };

    static final DetectFunc matchDmesg = new DetectFunc() {
        public bool Func(Context c, String fpat, String unused) {
            // XXX run dmesg, match lines...
        }
    };

    static final DetectFunc matchLogcat = new DetectFunc() {
        public bool Func(Context c, String fpat, String unused) {
            // XXX 
        }
    };

    static final DetectFunc matchService = new DetectFunc() {
        public bool Func(Context c, String fpat, String unused) {
            // XXX 
        }
    };

    static final DetectFunc matchClass = new DetectFunc() {
        public bool Func(Context c, String fpat, String unused) {
            // XXX try to load class, report exceptions
        }
    };
}

