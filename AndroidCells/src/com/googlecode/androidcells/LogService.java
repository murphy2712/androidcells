package com.googlecode.androidcells;

import com.googlecode.androidcells.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.util.Log;

public class LogService extends Service {

	private static final String TAG = "AndroidCells.LogService";
	private static boolean recording = false;
	private static final int RECORDING_NOTIFICATION_ID = R.layout.main;
	private static NotificationManager mNotificationManager;
	private static Notification n;
	private LocationManager mLocationManager;
	private gpsLocationListener mGpsLocationListener;
	private DeviceInformation di;
	

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	private final LogServiceInterface.Stub mBinder = new LogServiceInterface.Stub() {
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
	
	public static boolean isRecording() {
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
				getString(R.string.search_signals), System.currentTimeMillis());
		Intent notificationIntent = new Intent(this, Android_Cells.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		n.setLatestEventInfo(this,
						getString(R.string.app_name), getString(R.string.search_signals),
						contentIntent);
		n.flags = n.flags | Notification.FLAG_ONGOING_EVENT;	// | Notification.FLAG_NO_CLEAR;
		
	}
	
	public static void startRecording() {
		recording = true;
		mNotificationManager.notify(RECORDING_NOTIFICATION_ID, n);		
	}
	
	public static void stopRecording() {
		recording = false;
		// stops the notification icon
		if (mNotificationManager != null)
			mNotificationManager.cancel(RECORDING_NOTIFICATION_ID);
	}
	
	private class gpsLocationListener implements LocationListener {
		public void onLocationChanged(final Location loc) {
			LogService.this.onLocationChanged(loc);
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
	
	private void onLocationChanged(Location currentLocation) {
		/*LogServiceInterfaceResponse lsir;
		try {
			lsir.onLocationChanged(currentLocation.getTime());
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		if (recording) {
			try {
				di.updateDB(currentLocation);
				//Log.v(TAG, di.getStringInfos(currentLocation));
			} catch (Exception e) {
				Log.e(TAG, "onLocationChanged Exception: " + e.toString());
			}
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