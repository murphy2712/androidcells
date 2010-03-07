package com.googlecode.androidcells;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.format.Time;
import android.util.Log;

public class DeviceInformation implements Constants {

	private static String TAG = "AndroidCells.DeviceInformation";
	
	private Service ls; // LogService

	protected TelephonyManager mTelephonyManager;

	private int signalStrengthdBm = 0;
	private int signalStrengthAsu = NeighboringCellInfo.UNKNOWN_RSSI;
	
	private int signalStrengthCdmadBm = 0;
	private int signalStrengthCdmaEcio = 0;
	private int signalStrengthEvdodBm = 0;
	private int signalStrengthEvdoEcio = 0;
	private int signalStrengthSnr = 0;
	private int signalStrengthBitErrorRate = 0;
	private int signalStrengthGsm = 0;
	private boolean signalStrengthIsGsm = false;

	//private LocationManager myLocationManager;

	protected PhoneStateListener signalListener;
	protected WifiManager wm;
	
	private AndroidCellsDB acDB;
	
	private SharedPreferences pref;

	public DeviceInformation(Service logService, Context ctx) {
		// initialize database
		acDB = new AndroidCellsDB();
		//acDB.getNbProviders();
		pref = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		// register signal listener
		this.ls = logService;
		mTelephonyManager = (TelephonyManager) ls.getSystemService(Context.TELEPHONY_SERVICE);

		//myLocationManager = (LocationManager) ls.getSystemService(Context.LOCATION_SERVICE);
		signalListener = new PhoneStateListener() {
			@Override
			public void onSignalStrengthsChanged(SignalStrength signalStrength) {
				signalStrengthCdmadBm = signalStrength.getCdmaDbm();
				signalStrengthCdmaEcio = signalStrength.getCdmaEcio();
				signalStrengthEvdodBm = signalStrength.getEvdoDbm();
				signalStrengthEvdoEcio = signalStrength.getEvdoEcio();
				signalStrengthSnr = signalStrength.getEvdoSnr();
				signalStrengthBitErrorRate = signalStrength.getGsmBitErrorRate();
				signalStrengthAsu = signalStrength.getGsmSignalStrength();
				signalStrengthdBm = -113 + 2 * signalStrengthAsu; // conversion asu en dBm
				signalStrengthIsGsm = signalStrength.isGsm();
			}
			
			@Override
			public void onCellLocationChanged(CellLocation location) {
				
			}
			
			@Override
			public void onServiceStateChanged (ServiceState serviceState) {
				//serviceState.getOperatorAlphaLong();
				//serviceState.getOperatorNumeric();
				//serviceState.getRoaming();
			}
		};
		mTelephonyManager.listen(signalListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

		// Register Wifi
		wm = (WifiManager) ls.getSystemService(Context.WIFI_SERVICE);
	}
	
	private boolean isRecordingCell() {
		return pref.getBoolean("cell_checkbox", true);
	}
	
	private boolean isRecordingWifi() {
		return pref.getBoolean("wifi_checkbox", true);
	}
	
	private int prefCellDistanceFilter() {
		return pref.getInt("CellFilter", 10);
	}
	
	private int prefWifiDistanceFilter() {
		return pref.getInt("WifiFilter", 5);
	}

	protected void closeDB() {
		acDB.closeDB();
	}
	
	private int getGpsAccuracy(int distanceFilter) {
		int gpsAccuracy = GPS_ACCURACY;
		if (distanceFilter < GPS_ACCURACY/2) {
			gpsAccuracy /= 2;
		}
		return gpsAccuracy;
	}
	
	// add signals into DataBase
	public void updateDB(Location currentLoc) {
		boolean updateGPS = false;
		// Checks if the GPS info is accurate (< GPS_ACCURACY or GPS_ACCURACY/2 meters)
		if (currentLoc.getAccuracy() < getGpsAccuracy(prefCellDistanceFilter())) {
			Log.v(TAG,"GPS_ACCURACY<"+getGpsAccuracy(prefCellDistanceFilter())+"m : "+currentLoc.getAccuracy());
			// Checks if there is not a nearby cell position in DB
			if (isRecordingCell()
				&& !acDB.cellLocationNearBD(currentLoc, prefCellDistanceFilter(),
						mTelephonyManager.getNetworkOperator(), mTelephonyManager.getNetworkType())) {
				GsmCellLocation mLocation = (GsmCellLocation) mTelephonyManager.getCellLocation();
				if (isValidCell(mLocation)) {
					Log.v(TAG, "Recording Cell in DB...");
					updateGPS = true;
					updateCellDB(currentLoc, mLocation);
					updateNeighborsDB(currentLoc);
				}
			}
		} else Log.v(TAG,"GPS_ACCURACY>"+getGpsAccuracy(prefCellDistanceFilter())+"m : "+currentLoc.getAccuracy());
		if (currentLoc.getAccuracy() < getGpsAccuracy(prefWifiDistanceFilter())) {
			//Log.v(TAG,"GPS_ACCURACY<"+getGpsAccuracy(prefWifiDistanceFilter())+"m : "+currentLoc.getAccuracy());
			if (isRecordingWifi() && wm.isWifiEnabled()) {
				// Checks if there is not a nearby Wifi position in DB
				if (!acDB.wifiLocationNearBD(currentLoc, prefWifiDistanceFilter())) {
					if (updateWifiDB(wm, currentLoc)) {
						updateGPS = true;
					}
				}
			}
		} else Log.v(TAG,"GPS_ACCURACY>"+getGpsAccuracy(prefWifiDistanceFilter())+"m : "+currentLoc.getAccuracy());
		if (updateGPS) {
			updateGpsDB(currentLoc);
		}
		/*
		if (isValidGps(currentLoc)) {
			GsmCellLocation mLocation = (GsmCellLocation) mTelephonyManager.getCellLocation();
			if (isValidCell(mLocation)) {
				updateGpsDB(currentLoc);
				updateCellDB(currentLoc, mLocation);
				updateNeighborsDB(currentLoc);
				updateWifiDB(wm, currentLoc);
			} else if (updateWifiDB(wm, currentLoc)) {
				updateGpsDB(currentLoc);
			}
		}*/
	}
	/*
	private boolean isValidGps(Location currentLoc) {
		boolean valid = true;
		// checks if the GPS info is accurate (> GPS_ACCURACY meters)
		if (currentLoc.getAccuracy() > GPS_ACCURACY) {
			valid = false;
		} else if (acDB.gpsLocationNearBD(currentLoc, DECIMAL_FILTER, DISTANCE_FILTER)) {
			// Checks if there is a nearby position in DB
			valid = false;
		}
		return valid;
	}*/

	// checks if the cell info is valid
	private boolean isValidCell(GsmCellLocation mLoc) {
		boolean valid = false;
		if (this.signalStrengthAsu != NeighboringCellInfo.UNKNOWN_RSSI) {
			if ((mLoc.getCid() != NeighboringCellInfo.UNKNOWN_CID)
					&& (mLoc.getLac() != NeighboringCellInfo.UNKNOWN_CID)
					&& (mTelephonyManager.getNetworkType() != TelephonyManager.NETWORK_TYPE_UNKNOWN)) {
				valid = true;
			}
		}
		return valid;
	}
	
	// add gps signal
	private void updateGpsDB(Location currentLoc) {
		Gps gps = new Gps();
		gps.gpsdate = gpsTime2String(currentLoc.getTime());
		gps.lat = currentLoc.getLatitude();
		gps.lon = currentLoc.getLongitude();
		gps.alt = currentLoc.getAltitude();
		gps.speed = currentLoc.getSpeed();
		gps.accuracy = currentLoc.getAccuracy();
		gps.bearing = currentLoc.getBearing();
		acDB.insertGps(gps);
	}

	// add cell signal
	private void updateCellDB(Location currentLoc, GsmCellLocation mLocation) {
		Cell cell = new Cell();
		cell.gpsdate = gpsTime2String(currentLoc.getTime());
		cell.operator_name = mTelephonyManager.getNetworkOperatorName();
		cell.operator = mTelephonyManager.getNetworkOperator();
		cell.type = mTelephonyManager.getNetworkType();
		cell.cid = mLocation.getCid();
		cell.lac = mLocation.getLac();
		//cell.psc = mLocation.Cid();
		cell.strenght_asu = signalStrengthAsu;
		acDB.insertCell(cell);
	}

	// add neighbors signals
	private void updateNeighborsDB(Location currentLoc) {
		ArrayList<NeighboringCellInfo> mNeighboringCellInfo;
		mNeighboringCellInfo = new ArrayList<NeighboringCellInfo>();
		mNeighboringCellInfo = (ArrayList<NeighboringCellInfo>) mTelephonyManager
				.getNeighboringCellInfo();
		if (mNeighboringCellInfo.size() != 0) {
			for (int i = 0; i < mNeighboringCellInfo.size(); ++i) {
				// checks if cell info is valid
				if ((signalStrengthAsu != NeighboringCellInfo.UNKNOWN_RSSI)
						&& mNeighboringCellInfo.get(i).getLac() != 0) {
					Neighbor n = new Neighbor();
					n.gpsdate = gpsTime2String(currentLoc.getTime());
					n.type = mNeighboringCellInfo.get(i).getNetworkType();
					n.cid = mNeighboringCellInfo.get(i).getCid();
					n.lac = mNeighboringCellInfo.get(i).getLac();
					n.psc = mNeighboringCellInfo.get(i).getPsc();
					n.strenght_asu = mNeighboringCellInfo.get(i).getRssi();
					acDB.insertNeighbor(n);	
				}
			}
		}
	}
	
	public boolean updateWifiDB(WifiManager wm, Location currentLoc){
		boolean updateWifi = false;
		if (wm.isWifiEnabled()) {
			wm.startScan();
			List<ScanResult> scanlist = wm.getScanResults();
			if (scanlist!=null) {
				int idx=0;
				while(scanlist.size() > idx) {
					updateWifi = true;
					Wifi w = new Wifi();
					w.gpsdate = gpsTime2String(currentLoc.getTime());
					w.BSSID = scanlist.get(idx).BSSID;
					w.SSID = scanlist.get(idx).SSID;
					w.capabilities = scanlist.get(idx).capabilities;
					w.frequency = scanlist.get(idx).frequency;
					w.level= scanlist.get(idx).level;
					acDB.insertWifi(w);
					idx++;
					Log.v(TAG, "Wifi: "+w.BSSID+"/"+w.SSID+"/"+w.capabilities+"/"+w.frequency+"/"+w.level);
				} 
				scanlist.iterator();
			} else {
				Log.v(TAG, "No Wifi signal in scan");
			}
		}
		return updateWifi;
	}
	
	public int nbGpsLocations() {
		return acDB.nbGpsLocations();
	}
	
	public int nbCellLocations() {
		return acDB.nbCellLocations();
	}
	
	public int nbNeighborsLocations() {
		return acDB.nbNeighborsLocations();
	}
	
	public int nbWifiLocations() {
		return acDB.nbWifiLocations();
	}
	
	public String getStringInfos(Location currentLocation) {
		String Build = getBuildInfos();
		String Provider = getProviderInfos();
		String Cell = getCellInfos();
		String Neighbours = getNeighboursInfos();
		String GPS = getGPSInfos(currentLocation);
		String dis = getDeviceInfo();
		return Provider + "\n" + Cell + "\n" + Neighbours + "\n" + GPS;
	}

	public String getBuildInfos() {
		String text;
		text = "Build: manufacturer=" + Build.MODEL + " model=" + Build.DEVICE
				+ "/" + Build.PRODUCT + "/" + Build.BOARD + " revision="
				+ mTelephonyManager.getDeviceSoftwareVersion() + " swid="
				+ Build.ID + " swver=" + Build.VERSION.RELEASE;
		text = "Board=" + Build.BOARD + " ; Brand=" + Build.BRAND
				+ " ; Device=" + Build.DEVICE + " ; Display=" + Build.DISPLAY
				+ "\nFingerPrint=" + Build.FINGERPRINT + "\nHost=" + Build.HOST
				+ "\nId=" + Build.ID + " ; Model=" + Build.MODEL
				+ " ; Product=" + Build.PRODUCT + "\nTags=" + Build.TAGS
				+ "\nTime=" + Build.TIME + "\nType=" + Build.TYPE + " ; User="
				+ Build.USER + "\nVersion.Incremental="
				+ Build.VERSION.INCREMENTAL + "\nVersion.Release="
				+ Build.VERSION.RELEASE + " ; Version.SDK=" + Build.VERSION.SDK
				+ "\n";
		return text;
	}

	public String getProviderInfos() {
		String NetOp = mTelephonyManager.getNetworkOperator();
		String NetOpName = mTelephonyManager.getNetworkOperatorName();
		String MCC = NetOp.substring(0, 3);
		String MNC = NetOp.substring(3);

		String text = NetOpName + "(MCC=" + MCC + " / MNC="
				+ MNC + ")";

		return text;
	}

	public String getCellInfos() {
		GsmCellLocation mLocation = (GsmCellLocation) mTelephonyManager
				.getCellLocation();
		int itype = mTelephonyManager.getNetworkType();
		String type;
		switch (itype) {
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			type = "GSM";
			break;
		case TelephonyManager.NETWORK_TYPE_GPRS:
			type = "GPRS";
			break;
		case TelephonyManager.NETWORK_TYPE_EDGE:
			type = "EDGE";
			break;
		case TelephonyManager.NETWORK_TYPE_UMTS:
			type = "UMTS";
			break;
		case 4: // TelephonyManager.NETWORK_TYPE_HSDPA not in API
			type = "HSDPA";
			break;
		default:
			type = "GSM";
			break;
		}

		String text = "Cell: Type=" + type + " / Lac=" + mLocation.getLac()
				+ "(" + Integer.toHexString(mLocation.getLac()) + ") / Cid="
				+ mLocation.getCid() + "("
				+ Integer.toHexString(mLocation.getCid()) + ") / ss="
				+ signalStrengthAsu + "//"
				+ signalStrengthCdmadBm + "/" + signalStrengthCdmaEcio + "/" + signalStrengthEvdodBm
				 + "/"+ signalStrengthEvdoEcio + "/" + signalStrengthSnr + "/" + signalStrengthBitErrorRate
				 + "/"+ signalStrengthGsm + "/" + signalStrengthIsGsm ;
		text += "\n" + getSignalStrength();
		return text;
	}

	private String getSignalStrength() {
		return "Signal Strength dBm: " + signalStrengthdBm + " / Asu: "
				+ signalStrengthAsu;
	}

	public String getNeighboursInfos() {
		ArrayList<NeighboringCellInfo> mNeighboringCellInfo;
		mNeighboringCellInfo = new ArrayList<NeighboringCellInfo>();
		mNeighboringCellInfo = (ArrayList<NeighboringCellInfo>) mTelephonyManager
				.getNeighboringCellInfo();
		String text = "Neighbours ("+ mNeighboringCellInfo.size() +"):\n";
		if (mNeighboringCellInfo.size() == 0) {
			text += "";
			// Log.i(TAG, text);
		} else {
			for (int i = 0; i < mNeighboringCellInfo.size(); ++i) {
				text += "Type=" + mNeighboringCellInfo.get(i).getNetworkType()
						+ " / Psc="	+ mNeighboringCellInfo.get(i).getPsc()
						+ " / Cid="	+ mNeighboringCellInfo.get(i).getCid()
						+ " / Lac=" + mNeighboringCellInfo.get(i).getLac()
						+ " / Rssi=" + mNeighboringCellInfo.get(i).getRssi()
						//+ " Cidhexa=" + Integer.toHexString(mNeighboringCellInfo.get(i)
						//		.getCid())
						+ "\n";
				// Log.i(TAG, text);
			}
		}
		return text;
	}

	// converts gps time into String
	private String gpsTime2String(long gpsTime) {
		Time mygpstime = new Time();
		mygpstime.set(gpsTime);
		return mygpstime.format("%Y%m%d%H%M%S");
	}
	
	public String getGPSInfos(Location currentLoc) {
		String gps_infos = "";
		try {
			// GpsStatus mystatus = myLocationManager.getGpsStatus(null);
			gps_infos = gpsTime2String(currentLoc.getTime()) + " / " + currentLoc.getLatitude() + " / "
					+ currentLoc.getLongitude() + " / " + currentLoc.getAltitude()
					+ " / " + currentLoc.getSpeed() + " / " + currentLoc.getAccuracy() 
					+ " / " + currentLoc.getExtras();// .getInt("satellites");
		} catch (Exception e) {
			Log.e(TAG, "getGPSInfos Exception: " + e.toString());
		}
		return "GPS: " + gps_infos;
	}

	public String getDeviceInfo() {
		String result = getSoftwareRevision();
		result += getHardwareRevision();
		return result;
	}

	/**
	 * @return Returns the IMEI.
	 */
	private String getImei() {
		TelephonyManager mTelephonyMgr = (TelephonyManager) ls
				.getSystemService(Context.TELEPHONY_SERVICE);
		return mTelephonyMgr.getDeviceId();
	}

	/**
	 * @return Returns the MSISDN.
	 */
	private String getMsisdn() {
		TelephonyManager mTelephonyMgr = (TelephonyManager) ls
				.getSystemService(Context.TELEPHONY_SERVICE);
		return mTelephonyMgr.getLine1Number();
	}

	/**
	 * @return Returns the cell ID.
	 */
	private int getCellID() {
		TelephonyManager mTelephonyMgr = (TelephonyManager) ls
				.getSystemService(Context.TELEPHONY_SERVICE);
		GsmCellLocation location = (GsmCellLocation) mTelephonyMgr
				.getCellLocation();
		return location.getCid();
	}

	/**
	 * @return Returns the software revision.
	 */
	private String getSoftwareRevision() {
		String result = "soft\n";
		Runtime runtime = Runtime.getRuntime();
		try {
			Process proc = runtime.exec("cat /proc/version");
			int exit = proc.waitFor();
			if (exit == 0) {
				String content = getContent(proc.getInputStream());
				int index = content.indexOf(')');
				if (index >= 0) {
					result += "kernel=" + content.substring(0, index + 1)
							+ "\n";
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		result += "buildNumber=" + Build.PRODUCT + Build.VERSION.RELEASE + "\n";
		return result;
	}

	private String getHardwareRevision() {
		String result = "hard\n";
		Runtime runtime = Runtime.getRuntime();
		try {
			Process proc = runtime.exec("cat /proc/cpuinfo");
			int exit = proc.waitFor();
			if (exit == 0) {
				String content = getContent(proc.getInputStream());
				String[] lines = content.split("\n");
				String[] hInfo = { "Processor", "Hardware", "Revision" };
				if (lines != null) {
					for (String line : lines) {
						for (String info : hInfo) {
							int index = line.indexOf(info);
							if (index >= 0) {
								result += info.toLowerCase() + "=";
								int vIndex = line.indexOf(':');
								result += line.substring(vIndex + 1);
								result += "\n";
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		result += "\n";
		return result;
	}

	/**
	 * @param input
	 *            the input stream.
	 * @return Returns the content string of the input stream.
	 * @throws IOException
	 *             the Java exception.
	 */
	public static String getContent(InputStream input) throws IOException {
		if (input == null) {
			return null;
		}
		byte[] b = new byte[1024];
		int readBytes = 0;
		String result = "";
		while ((readBytes = input.read(b)) >= 0) {
			result += new String(b, 0, readBytes);
		}
		return result;
	}

}
