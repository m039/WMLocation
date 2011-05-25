/*
* Copyright (C) 2011 Mozgin Dmitry
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

// (setenv "ANDROID_LOG_TAGS" "Locator:V *:S")
// (setenv "ANDROID_LOG_TAGS" "")

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

    private CompactLog    mLog		= new CompactLog();
    private StringBuilder mStatus	= null;
    private StringBuilder mLocation	= new StringBuilder();

    private Context		mParent;
    private LocationManager	mManager;
    private Handler		mHandler;
    private LocationListener	mLocationListener;
    private GpsStatus.Listener	mStatusListener;
   
    /**
     * Why it doesn't work? I really don't know. I'll try
     * GpsStatus.Listener, maybe it's better. Hmm.. GpsStatus.Listener
     * actually is another thing.
     */
    private class MyListener implements LocationListener {

	// Implementation of android.location.LocationListener

	public final void onLocationChanged(final Location l) {
	    mLocation = new StringBuilder();
	    
	    mLocation.append("onLocationChanged:\n" +
			     "  Provider  " + l.getProvider() + "\n" +
			     "  Time      " + l.getTime() + "\n" +
			     "  Latitude  " + l.getLatitude() + "\n" +
			     "  Longitude " + l.getLongitude() + "\n" +
			     "  Altitude  " + l.getAltitude() + "\n" +
			     "  Speed     " + l.getSpeed() + "\n" +
			     "  Bearing   " + l.getBearing() + "\n" +
			     "  Accuracy  " + l.getAccuracy() + "\n" +
			     "  Extras    " + l.getExtras() + "\n");	    

	    sendStatus(CHANGED_LOCATION);
	}

	public final void onStatusChanged(final String string, final int status, final Bundle bundle) {
	    mLog.append("onStatusChanged\t|  provider = " + string + " status = " + status + "\n");
	    sendStatus(CHANGED_LOG);
	}

	public final void onProviderEnabled(final String string) {
	    mLog.append("onProviderEnabled\t| provider = " + string + " \n");
	    sendStatus(CHANGED_LOG);
	}

	public final void onProviderDisabled(final String string) {
	    mLog.append("onProviderDisabled\t| provider = " + string + " \n");
	    sendStatus(CHANGED_LOG);
	}
    }

    private class GPSListener implements Listener {

	// Implementation of android.location.GpsStatus$Listener

	public final void onGpsStatusChanged(final int event) {
	    mLog.append(String.format("onGpsStatusChanged\t| event = %1d\n", event));
	    sendStatus(CHANGED_LOG);

	    if (GpsStatus.GPS_EVENT_SATELLITE_STATUS == event) {
		mStatus = new StringBuilder("  # | Azimuth | Elevation |  PRN |    SNR\n");

		GpsStatus gs = mManager.getGpsStatus(null);
		    
		int i = 1;
		for (GpsSatellite satellite: gs.getSatellites()) {
		    mStatus.append(String.format("%3d | %7.2f | %9.2f | %4d | %6.2f\n",
						 i++,
						 satellite.getAzimuth(),
						 satellite.getElevation(),
						 satellite.getPrn(),
						 satellite.getSnr()));
		}

		sendStatus(CHANGED_STATUS);
	    }
	}
    }

    /**
     * Get a representation of a status.
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
     */
    public void start() {
	mManager.requestLocationUpdates(mManager.GPS_PROVIDER,
					3000,
					0,
					mLocationListener,
					mParent.getMainLooper());

	mManager.addGpsStatusListener(mStatusListener);
	
	mLog.append("requestLocationUpdates\t| minTime = " + 3000 + " minDistance = " + 0f + "\n");
	mLog.append("addGpsStatusListener\n");

	sendStatus(CHANGED_LOG);
    }

    /**
     * Called within onPause()
     */
    public void stop() {
	mManager.removeUpdates(mLocationListener);
	mManager.removeGpsStatusListener(mStatusListener);

	mLog.append("removeUpdates\n");
	mLog.append("removeGpsStatusListener\n");

	sendStatus(CHANGED_LOG);
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
     */
    public Locator(Context parent, Handler handler) {
	mParent = parent;
	mHandler = handler;
	mManager = (LocationManager) mParent.getSystemService(mParent.LOCATION_SERVICE);
	mLocationListener = new MyListener();
	mStatusListener = new GPSListener();

	Location l = mManager.getLastKnownLocation(mManager.GPS_PROVIDER);
	if (l == null) {
	    mLocation.append("Location: " + l);
	} else {
	    mLocation.append("LastKnownLocation:\n" +
			     "  Provider  " + l.getProvider() + "\n" +
			     "  Time      " + l.getTime() + "\n" +
			     "  Latitude  " + l.getLatitude() + "\n" +
			     "  Longitude " + l.getLongitude() + "\n" +
			     "  Altitude  " + l.getAltitude() + "\n" +
			     "  Speed     " + l.getSpeed() + "\n" +
			     "  Bearing   " + l.getBearing() + "\n" +
			     "  Accuracy  " + l.getAccuracy() + "\n" +
			     "  Extras    " + l.getExtras() + "\n");
	}
	    
	sendStatus(CHANGED_LOCATION);	    
    }
}
