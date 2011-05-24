package m039.utils;

import android.content.Context;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.location.LocationListener;
import android.location.Location;
import android.util.Log;
import java.util.List;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.GpsSatellite;
import android.os.Handler;
import android.os.Message;

// (setenv "ANDROID_LOG_TAGS" "Locator:V *:S")

/**
 * Here all code for my application. The name of my application is "My
 * Locator" ;)
 *
 * Created: Mon May 23 21:44:12 2011
 *
 * @author <a href="mailto:flam44@gmail.com">Mozgin Dmitry</a>
 * @version 1.0
 */
public class Locator {
    private static final String TAG = "Locator";
    
    public static final int CHANGED_LOG		= 0x0;
    public static final int CHANGED_STATUS	= 0x1;
    public static final int CHANGED_LOCATION	= 0x2;

    private StringBuilder mLog		= new StringBuilder();
    private StringBuilder mStatus	= null;
    private StringBuilder mLocation	= new StringBuilder();

    private Context		mParent;
    private LocationManager	mManager;
    private Handler		mHandler;
    private LocationListener	mLocationListener;
    private GpsStatus.Listener	mStatusListener;

    /**
     * Why it doesn't work? I really don't know. I'll try
     * GpsStatus.Listener, maybe it's better.
     */
    private class MyListener implements LocationListener {

	public MyListener() {
	    mLocation.append("onLocationChanged:\n");
	}

	// Implementation of android.location.LocationListener

	public final void onLocationChanged(final Location location) {
	    mLocation.append(location.toString() + "\n");
	    sendStatus(CHANGED_LOCATION);
	}

	public final void onStatusChanged(final String string, final int n, final Bundle bundle) {
	    mLog.append("StatusChanged [" + string + "] status=" + n + "\n");
	    sendStatus(CHANGED_LOG);
	}

	public final void onProviderEnabled(final String string) {
	    mLog.append("ProviderEnabled [" + string + "] \n");
	    sendStatus(CHANGED_LOG);
	}

	public final void onProviderDisabled(final String string) {
	    mLog.append("ProviderDisabled [" + string + "] \n");
	    sendStatus(CHANGED_LOG);
	}
    }

    private class GPSListener implements Listener {

	// Implementation of android.location.GpsStatus$Listener

	public final void onGpsStatusChanged(final int n) {
	    mLog.append("GpsStatusChanged with " + n + "\n");
	    sendStatus(CHANGED_LOG);

	    if (GpsStatus.GPS_EVENT_SATELLITE_STATUS == n) {
		mStatus = new StringBuilder();

		GpsStatus gs = mManager.getGpsStatus(null);
		for (GpsSatellite satellite: gs.getSatellites()) {
		    mStatus.append("Asimuth " + satellite.getAzimuth() + "\n" +
				   "Elevation " + satellite.getElevation() + "\n" +
				   "Prn " + satellite.getPrn() + "\n");
		}

		sendStatus(CHANGED_STATUS);
	    }
	}
    }

    /**
     * Get a representation of the status.
     *
     * @param status a status to get
     * @return a string representation of the status
     */
    public String getChanges(int status) {
	switch (status) {
	case CHANGED_STATUS:
	    return mStatus.toString();
	case CHANGED_LOG:
	    return mLog.toString();
	case CHANGED_LOCATION:
	    return mLocation.toString();
	default:
	    return null;
	}
    }

    /**
     * Called within onResume()
     *
     */
    public void start() {
	mManager.requestLocationUpdates(mManager.GPS_PROVIDER,
					3000,
					0,
					mLocationListener,
					mParent.getMainLooper());

	mManager.addGpsStatusListener(mStatusListener);
	
	mLog.append("The listener was requested with minTime 3000 (3 sec) and minDistance 0.\n");
	mLog.append("Added GPSListener.\n");

	sendStatus(CHANGED_LOG);
    }

    /**
     * Called within onPause()
     */
    public void stop() {
	mManager.removeUpdates(mLocationListener);
	mManager.removeGpsStatusListener(mStatusListener);
    }

    /**
     * SendStatus to the parent with a message that the status has
     * changed.
     */
    private void sendStatus(int status) {
	Message msg = mHandler.obtainMessage();
	msg.arg1 = status;
	msg.sendToTarget();
    }

    /**
     * Creates a new <code>Locator</code> instance.
     *
     */
    public Locator(Context parent, Handler handler) {
	mParent = parent;
	mHandler = handler;
	mManager = (LocationManager) mParent.getSystemService(mParent.LOCATION_SERVICE);
	mLocationListener = new MyListener();
	mStatusListener = new GPSListener();

	mLog.append("-[Start of a trash]----------\n");

	mLog.append("The location manager was retrieved." +
		    "There are some interesting parameters of this instance.\n" +
		    "\n" +
		    "For an example provider constans:\n" +
		    " GPS " + mManager.GPS_PROVIDER + "\n" +
		    " Network " + mManager.NETWORK_PROVIDER + "\n" +
		    " Passive (wtf?) " + mManager.PASSIVE_PROVIDER + "\n" +
		    "\n");

	mLog.append("But there is getProviders() function. Let's check it!\n" +
		    "getProviders(false):\n");

	for (String s: mManager.getProviders(false)) {
	    mLog.append(" " + s + "\n");
	}

	mLog.append("getProviders(true):\n");

	for (String s: mManager.getProviders(true)) {
	    mLog.append(" " + s + "\n");
	}

	mLog.append("\n" +
		    "WTF! There is getAllProviders() function even!\n" +
		    "getAllProviders():\n");

	for (String s: mManager.getAllProviders()) {
	    mLog.append(" " + s + "\n");
	}

	Location l = null;
	mLog.append("Let's look at getLastKnowLocation(GPS_PROVIDER):\n");

	try {
	    l = mManager.getLastKnownLocation(mManager.GPS_PROVIDER);
	    mLog.append("Location " + l.toString() + "\n");
	} catch (NullPointerException e) {
	    mLog.append("Location is " + l);
	}

	mLog.append("\n-[End of a trash]----------\n");
	sendStatus(CHANGED_LOG);
    }
}
