package com.isecpartners.CarrierIQTester;

import android.app.Activity;
import android.os.Bundle;

public class CarrierIQTesterActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Detectors d = Detectors.DETECT1;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}
