package com.googlecode.androidcells;

import com.googlecode.androidcells.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.util.Log;

public class LogService extends Service {

	private static final String TAG = "AndroidCells.LogService";
	private LogServiceInterfaceResponse callback;
	private boolean recording = false;
	private static final int RECORDING_NOTIFICATION_ID = R.layout.main;
	private static NotificationManager mNotificationManager;
	private static Notification n;
	private LocationManager mLocationManager;
	private gpsLocationListener mGpsLocationListener;
	private DeviceInformation di;
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	private final LogServiceInterface.Stub mBinder = new LogServiceInterface.Stub() {
		@Override
		public void setCallback(LogServiceInterfaceResponse callback_)
				throws RemoteException {
			callback = callback_;
		}
		
		@Override
		public boolean isRecording() throws RemoteException {
			return recording;
		}
		
		@Override
		public void startRecording() throws RemoteException {
			recording = true;
			mNotificationManager.notify(RECORDING_NOTIFICATION_ID, n);
		}

		@Override
		public void stopRecording() throws RemoteException {
			recording = false;
			// stops the notification icon
			if (mNotificationManager != null)
				mNotificationManager.cancel(RECORDING_NOTIFICATION_ID);
		}
		
		@Override
		public String getProviderInfos() throws RemoteException {
			return di.getProviderInfos();
		}

		@Override
		public int nbGpsLocations() throws RemoteException {
			return di.nbGpsLocations();
		}
		
		@Override
		public int nbCellLocations() throws RemoteException {
			return di.nbCellLocations();
		}
		
		@Override
		public int nbNeighborsLocations() throws RemoteException {
			return di.nbNeighborsLocations();
		}
		
		@Override
		public int nbWifiLocations() throws RemoteException {
			return di.nbWifiLocations();
		}
	};
	
	public boolean isRecording() {
		return recording;
	}
	
	@Override
	public void onCreate() {
		Log.v(TAG, "onCreate()");
		super.onCreate();

		// init the notification bar
		if (mNotificationManager == null)
			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (mLocationManager == null)
			mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// activate GPS listener
		if (mLocationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
			mGpsLocationListener = new gpsLocationListener();
			mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0,
					mGpsLocationListener);
		}
		// start getting informations
		di = new DeviceInformation(LogService.this, getBaseContext());
		// Preparing Notification
		n = new Notification(R.drawable.icon,
				getString(R.string.recording_signals), System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, Android_Cells.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		n.setLatestEventInfo(this,
						getString(R.string.app_name), getString(R.string.recording_signals),
						contentIntent);
		n.flags = n.flags | Notification.FLAG_ONGOING_EVENT;	// | Notification.FLAG_NO_CLEAR;
	}
	
	private class gpsLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(final Location loc) {
			if (recording) {
				try {
					di.updateDB(loc);
					callback.nbGpsLocation(di.nbGpsLocations());
					//Log.v(TAG, di.getStringInfos(currentLocation));
				} catch (Exception e) {
					Log.e(TAG, "onLocationChanged Exception: " + e.toString());
				}
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
		}
	}
	
	@Override
	public void onDestroy() {
		Log.v(TAG, "onDestroy()");
		super.onDestroy();
		// stops the rssi listener
		di.mTelephonyManager.listen(di.signalListener, PhoneStateListener.LISTEN_NONE);
		// stops the gps listener
		mLocationManager.removeUpdates(mGpsLocationListener);
		// close the db
		di.closeDB();
	}
}
