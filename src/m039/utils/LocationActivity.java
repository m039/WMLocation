package m039.utils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.text.method.ScrollingMovementMethod;
import android.app.TabActivity;
import android.widget.TabHost;
import android.text.InputFilter;
import android.util.Log;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class LocationActivity extends TabActivity {
    private Locator mLocator = null;

    private class MyHandler implements Handler.Callback {
	public boolean handleMessage (Message msg) {
	    TextView tv = null;

	    switch (msg.arg1) {
	    case Locator.CHANGED_LOG:
		tv = (TextView) findViewById(R.id.log_tv);
		break;
	    case Locator.CHANGED_STATUS:
		tv = (TextView) findViewById(R.id.status_tv);
		break;
	    case Locator.CHANGED_LOCATION:
		tv = (TextView) findViewById(R.id.location_tv);
		break;
	    default:
		return false;
	    }

	    tv.setText(mLocator.getChanges(msg.arg1));

	    return true;
	}
    }

    // Taken from the SO
    private void autoScaleTextViewTextToHeight(TextView tv, String initialString, int requiredWidth) {
	String old = tv.getText().toString();

	tv.setText(initialString);

	float currentWidth = tv.getPaint().measureText(initialString);
	float phoneDensity = this.getResources().getDisplayMetrics().density;

	while(currentWidth > (requiredWidth * phoneDensity)) {
	    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv.getTextSize() - 1.0f);
	    currentWidth = tv.getPaint().measureText(initialString);
	}

	tv.setText(old);
    }


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);

	TextView tv;

	tv = (TextView) findViewById(R.id.log_tv);
	tv.setMovementMethod(new ScrollingMovementMethod());

	tv = (TextView) findViewById(R.id.status_tv);
	tv.setMovementMethod(new ScrollingMovementMethod());

	tv = (TextView) findViewById(R.id.location_tv);
	tv.setMovementMethod(new ScrollingMovementMethod());

	TabHost th = getTabHost();
	th.addTab(th.newTabSpec("0").setIndicator("Location").setContent(R.id.location_tv));
	th.addTab(th.newTabSpec("1").setIndicator("GpsStatus").setContent(R.id.status_tv));
	th.addTab(th.newTabSpec("2").setIndicator("Log").setContent(R.id.log_tv));

	th.setCurrentTab(0);

	mLocator = new Locator(this, new Handler(new MyHandler()));
    }

    @Override
    public void onResume() {
	super.onResume();

	TextView tv;
	
	//
	// Recalculation of the text size
	//
	tv = (TextView) findViewById(R.id.status_tv);
	autoScaleTextViewTextToHeight(tv,
				      "  # | Azimuth | Elevation |  PRN |    SNR\n",
				      getWindowManager().getDefaultDisplay().getWidth());

	tv = (TextView) findViewById(R.id.log_tv);
	autoScaleTextViewTextToHeight(tv,
				      "requestLocationUpdates\t| minTime = " + 3000 + " minDistance = " + 0f + "\n",
				      getWindowManager().getDefaultDisplay().getWidth());
	
	mLocator.start();
    }

    @Override
    public void onPause() {
	super.onPause();
	mLocator.stop();
    }
}
