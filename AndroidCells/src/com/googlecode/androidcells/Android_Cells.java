package com.googlecode.androidcells;

import java.util.Iterator;

import com.googlecode.androidcells.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class Android_Cells extends Activity implements android.view.View.OnClickListener	 {
	private static String TAG = "AndroidCells";

	private LocationManager mLocationManager;
	private LocationListener mGpsLocationListener;
	private GpsStatus.Listener gps_listener;
	private TextView gps_state_text;
	private TextView nb_sat_text;
	private TextView accuracy_text;
	private TextView recording_state_text;
	private TextView nbGpsLocations_text;
	private TextView nbCellLocations_text;
	private TextView nbNeighborsLocations_text;
	private TextView nbWifiLocations_text;
	private Button startstop_button;
	private Button refresh_button;
	private Intent svc;
	private LogServiceInterface lsInterface;

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			lsInterface = LogServiceInterface.Stub.asInterface((IBinder)service);
			updateInfosUI(); // initialize DB infos on screen
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			 lsInterface = null;		
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		gps_state_text = (TextView) findViewById(R.id.gps_state);
		nb_sat_text = (TextView) findViewById(R.id.nb_sat_text);
		accuracy_text = (TextView) findViewById(R.id.accuracy_text);
		recording_state_text = (TextView) findViewById(R.id.recording_state_text);
		startstop_button = (Button) findViewById(R.id.start_stop_button);
		startstop_button.setOnClickListener(this);
		nbGpsLocations_text = (TextView) findViewById(R.id.nbGpsLocations_text);
		nbCellLocations_text = (TextView) findViewById(R.id.nbCellLocations_text);
		nbNeighborsLocations_text = (TextView) findViewById(R.id.nbNeighborsLocations_text);
		nbWifiLocations_text = (TextView) findViewById(R.id.nbWifiLocations_text);
		refresh_button = (Button) findViewById(R.id.refresh_button);
		refresh_button.setOnClickListener(this);
		svc = new Intent(this, LogService.class);
		
		// Activate GPS for UI
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Checks if GPS is enabled
		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
			gps_state_text.setText(R.string.activated);
			// activate GPS listener on satellite status change
			gps_listener = new GpsStatus.Listener() {
			    public void onGpsStatusChanged(int event) {
			        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
			            GpsStatus status = mLocationManager.getGpsStatus(null);
			            Iterable<GpsSatellite> sats = status.getSatellites();
			            // Check number of satellites in list to determine fix state
			            Iterator<GpsSatellite> it = sats.iterator();
			            int nbSat=0;
			            int fixSat = 0;
			            while (it.hasNext()) {
			            	nbSat++;
			            	GpsSatellite oSat = (GpsSatellite) it.next() ;
			            	if (oSat.usedInFix()) fixSat++;
			            	//Log.v(TAG,"Android_Cells - onGpsStatusChange - Satellites: " + oSat.getSnr());
			            }
			            updateGpsSat(fixSat, nbSat); // update screen sat info on GPS status change
			        }
			    }
			};
			mLocationManager.addGpsStatusListener(gps_listener);

			// activate GPS listener on location change
			mGpsLocationListener = new LocationListener() {
				@Override
				public void onLocationChanged(Location location) {
					updateInfosUI(); // update screen UI info
					accuracy_text.setText(", +/- "+location.getAccuracy()+"m");
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
				public void onStatusChanged(String provider, int status,
						Bundle extras) {
					// TODO Auto-generated method stub
					
				}
				
			};
			mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0,
					mGpsLocationListener);
			
			// start & initialize the service
			startService();
		} else { // GPS disabled
			gps_state_text.setText(R.string.disabled);
			startstop_button.setEnabled(false);
		}
	}

	private void startService() {
		startService(svc);
		bindService(svc, mConnection, BIND_AUTO_CREATE);
		
		// initialize screen infos following service state
		setUIRecordingState(LogService.isRecording());
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start_stop_button: // invert recording state
			if (LogService.isRecording()) {
				LogService.stopRecording();
				setUIRecordingState(false);
			} else {
				LogService.startRecording();
				setUIRecordingState(true);
			}	
			break;

		case R.id.refresh_button:
			updateInfosUI();
			break;
		default:
			break;
		}
	}
	
	private void setUIRecordingState(boolean currently_recording) {
		if (currently_recording) {
			recording_state_text.setText(R.string.recording);
			startstop_button.setText(R.string.stop);
		} else {
			recording_state_text.setText(R.string.stopped);
			startstop_button.setText(R.string.start);
		}
	}
	
	private void resetUI() {
		// stop service
		stopService(svc);
		// update buttons
		recording_state_text.setText(R.string.disabled);
		startstop_button.setText(R.string.start);
	}
	
	private void updateGpsSat(int fixSat, int nbSat){
		String sat_info = " ("+fixSat+"/"+nbSat+" "+getText(R.string.satellites)+")";
		nb_sat_text.setTextKeepState(sat_info);
	}
	
	private void updateInfosUI() {
		try {
			nbGpsLocations_text.setText(""+lsInterface.nbGpsLocations());
			nbCellLocations_text.setText(""+lsInterface.nbCellLocations());
			nbNeighborsLocations_text.setText(""+lsInterface.nbNeighborsLocations());
			nbWifiLocations_text.setText(""+lsInterface.nbWifiLocations());
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
	
	private void enableGpsListeners() {
		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
			mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0,
					mGpsLocationListener);
			mLocationManager.addGpsStatusListener(gps_listener);
		}
	}
	
	private void disableGpsListeners() {
		// disable the gps listeners (for the ui)
		if (gps_listener != null) mLocationManager.removeGpsStatusListener(gps_listener);
		if (mGpsLocationListener != null) mLocationManager.removeUpdates(mGpsLocationListener);		
	}
	
	private void unbindService() {
		// unbind the connection to the service
		try {
			unbindService(mConnection);	
		} catch (Exception e) {	}
	}
	
	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu) {
		MenuItem prefsMenuItem = pMenu.add(0, Menu.FIRST, Menu.NONE,
				R.string.preferences);
		prefsMenuItem.setIcon(android.R.drawable.ic_menu_preferences);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST:
			Intent intent = new Intent(this,
					com.googlecode.androidcells.Preferences.class);
			startActivity(intent);
			return true;
		}
		return false;
	}
		
	@Override
	protected void onPause() {
		Log.i(TAG, "OnPause()");
		super.onPause();
		disableGpsListeners();
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "OnResume()");
		super.onResume();
		enableGpsListeners();
	}
	
	@Override
	public void onStop() {
		Log.i(TAG, "OnStop()");
		super.onStop();
		unbindService();
		if (!LogService.isRecording()) {
			Log.v(TAG, "Not recording, Stopping service...");
			stopService(svc);
			resetUI();
		}
	}
	
	@Override
	public void onDestroy() {
		Log.i(TAG, "OnDestroy()");
		super.onDestroy();	
	}
}
