package com.isecpartners.CarrierIQTester;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.util.Log;
import android.os.Build;

public class CarrierIQTesterActivity extends Activity {
    public static final String TAG = "CarrierIQTesterActivity";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Context c = getApplicationContext();        
        //Test.all(c);

        // XXX move details of this to detect class
        long v = 0;
        for(Detect d : Detect.values()) {
            v |= d.test(c);
        }
        Log.i(TAG, "computed flag: " + Long.toHexString(v));
        
        // XXX
        // send in a report
        Report r = new Report(Detect.version, v);
        r.send(c);
    }
}
