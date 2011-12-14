package com.isecpartners.CarrierIQTester;

import java.util.LinkedList;

import android.app.Activity;

import android.os.Bundle;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;

public class CarrierIQTesterActivity extends Activity implements U.LogFunc {
    public static final String TAG = "CarrierIQTesterActivity";

    boolean analysisDone = false;
    long analysisResult;
    AnalysisTask aTask = null;
    ReportTask rTask = null;
    TextView txt = null;
    TextView log = null;
    Button analyzeButton = null;
    Button reportButton = null;
   
    class AnalysisTask extends AsyncTask<Void,Integer,Long> implements U.LogFunc {
    	LinkedList<String> tmpLog = new LinkedList<String>();
    	
    	public synchronized void log(String s) {
    		tmpLog.add(s);
    	}
    	
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
                v |= d.test(c, this);
                publishProgress((int)(prog++ * pfact));
            }
            Log.i(TAG, "computed flag: " + Long.toHexString(v));
            return v;
    	}
    	
    	protected synchronized void onProgressUpdate(Integer... progress) {
    		for(String s : tmpLog)
    			CarrierIQTesterActivity.this.log(s);
    		tmpLog.clear();
    	}

    	protected void onPostExecute(Long res) {
    		analysisResult = res;
    		analysisDone = true;
    		analyzeButton.setEnabled(true);
    		reportButton.setEnabled(true);
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

    	protected void onPostExecute(Void res) {
    		reportButton.setEnabled(true);
    		toast("Report sent. Thank you!");
    		
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
 
   		analyzeButton.setEnabled(false);
    	txt.setText("Analyzing...");
    	log.setText("");
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
        reportButton.setEnabled(false);
        stopAnalysis();
        startAnalysis();
    }

    void reportResults() {
        if(!analysisDone)
            return;
        reportButton.setEnabled(false);
        rTask = new ReportTask();
        rTask.execute(analysisResult);
    }

    public void log(String s) {
    	log.append(s);
    }
    
    public void toast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        txt = (TextView)findViewById(R.id.txt);
        log = (TextView)findViewById(R.id.log);
        analyzeButton = (Button)findViewById(R.id.analyzeButton);
        reportButton = (Button)findViewById(R.id.reportButton);

        analyzeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				restartAnalysis();
			}
		});
        reportButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				reportResults();
			}
		});
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
