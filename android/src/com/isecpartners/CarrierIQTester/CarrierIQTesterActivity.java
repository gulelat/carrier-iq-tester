package com.isecpartners.CarrierIQTester;

import java.util.concurrent.ExecutionException;

import android.app.Activity;

import android.os.Bundle;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.os.AsyncTask;

public class CarrierIQTesterActivity extends Activity {
    public static final String TAG = "CarrierIQTesterActivity";

    boolean done = false;
    Analysis aTask = null;
    TextView txt = null;
    
    class Analysis extends AsyncTask<Void,Integer,Long> {
    	protected Long doInBackground(Void... args) {
            Context c = getApplicationContext();        

            double pfact = 100.0 / (Detect.values().length + 1);
            
            // run all detectors
            long v = 0;
            int prog = 0;
            for(Detect d : Detect.values()) {
            	if(isCancelled())
            		return null;
                v |= d.test(c);
                publishProgress((int)(prog++ * pfact));
            }
            Log.i(TAG, "computed flag: " + Long.toHexString(v));
            
            // report results
            // XXX this should be user initiated after analysis completes!
            if(isCancelled())
            	return null;
            Report r = new Report(c, Detect.version, v);
            r.send(c);
            publishProgress((int)(prog++ * pfact));
			return v;
    	}
    	
    	protected void onProgressUpdate(Integer... progress) {
    	
    	}
    	protected void onPostExecute(Long res) {
    		done = true;
    		showResult();
    	}
    }
    
    void showResult() {
    	if(done && aTask != null) {
    		try {
				txt.setText("Detection done.  Result: " + aTask.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
    	}
    }
    
    /* start or restart the analysis if necessary, else just show the prior result. */
    void startAnalysis() {
    	if(done) {
    		showResult();
    		return;
    	}
    	
    	if(aTask != null)
    		return;
    	txt.setText("Analyzing...");
    	aTask = new Analysis();
    	aTask.execute();
    }
    
    /* stop the analysis if started and not yet finished. */
    void stopAnalysis() {
    	if(aTask != null && !done) {
    		aTask.cancel(true);
    		aTask = null;
    	}
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        txt = (TextView)findViewById(R.id.txt);
    }

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		startAnalysis();
		super.onStart();
	}

	@Override
	protected void onStop() {
		stopAnalysis();
		super.onStop();
	}
	
	@Override
	protected void onPause() {
		stopAnalysis();
		super.onPause();
	}

	@Override
	protected void onResume() {
		startAnalysis();
		super.onResume();
	}
}
