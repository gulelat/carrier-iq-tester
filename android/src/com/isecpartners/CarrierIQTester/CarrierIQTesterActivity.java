package com.isecpartners.CarrierIQTester;

import android.app.Activity;

import android.os.Bundle;
import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.os.AsyncTask;

public class CarrierIQTesterActivity extends Activity {
    public static final String TAG = "CarrierIQTesterActivity";

    boolean analysisDone = false;
    long analysisResult;
    AnalysisTask aTask = null;
    ReportTask rTask = null;
    TextView txt = null;
    
    class AnalysisTask extends AsyncTask<Void,Integer,Long> {
    	protected Long doInBackground(Void... args) {
            Context c = getApplicationContext();        

            double pfact = 100.0 / Detect.values().length;
            
            // run all detectors
            long v = 0;
            int prog = 0;
            publishProgress((int)(prog++ * pfact));
            for(Detect d : Detect.values()) {
            	if(isCancelled())
            		return null;
                v |= d.test(c);
                publishProgress((int)(prog++ * pfact));
            }
            Log.i(TAG, "computed flag: " + Long.toHexString(v));
            return v;
    	}
    	
    	protected void onProgressUpdate(Integer... progress) {
    	}

    	protected void onPostExecute(Long res) {
    		analysisDone = true;
    		Button b1 = (Button) findViewById(R.id.button1);
    		Button b2 = (Button) findViewById(R.id.button2);
    		b1.setEnabled(true);
    		b2.setEnabled(true);
    		showResult();
    	}
    }
    
    class ReportTask extends AsyncTask<Long,Integer,Void> {
    	protected Void doInBackground(Long... vs) {
            Context c = getApplicationContext();        

            publishProgress(0);
            Report r = new Report(c, Detect.version, vs[0]);
            // XXX some sort of indication of success or failure?
            r.send(c);
            publishProgress(100);
			return null;
    	}
    	
    	protected void onProgressUpdate(Integer... progress) {
    	}

    	protected void onPostExecute(Long res) {
            // XXX show a popup saying it was sent?
            // XXX we should have an indicator saying how many reports
            // have been submitted from this app.
    	}
    }
    
    boolean showResult() {
    	if(analysisDone) {
            txt.setText("Detection done.  Result: " + analysisResult);
            return true;
        }
        return false;
    }
    
    /* start or restart the analysis if necessary, else just show the prior result. */
    void startAnalysis() {
        if(showResult())
            return;
    	
    	if(aTask != null)
    		return;
    	txt.setText("Analyzing...");
    	aTask = new AnalysisTask();
    	aTask.execute();
    }
    
    /* stop the analysis if started and not yet finished. */
    void stopAnalysis() {
    	if(aTask != null && !analysisDone) {
    		aTask.cancel(true);
    		aTask = null;
    	}
    }

    void restartAnalysis() {
        if(!analysisDone)
            return;
        analysisDone = false;
        stopAnalysis();
        startAnalysis();
    }

    void reportResults() {
        if(!analysisDone)
            return;
        rTask = new ReportTask();
        rTask.execute(analysisResult);
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
