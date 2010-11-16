package com.googlecode.androidcells;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemClock;
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

import com.measureplace.utils.FilesUtils;

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


	// <gps time="20100416145550" lng="0.1003742" lat="43.224068" alt="280" hdg="89.52558" spe="17.136" hdop="4.1" vdop="4.66" pdop="6.2" /> 
	private String gps_start_description_cellular;


	private int cellular_automaton = 0;
	private String cellular_description;
	private String cellular_mcc;
	private String cellular_mnc;
	private int cellular_lac;
	private int cellular_cid = 0;

	// to be displayed on screen
	private String cell_info;

	private Gps current_gps_for_cellular;
	private Date nextScanDate;

	private int wifi_automaton = 0;
	private String wifi_ap_description;
	private Gps current_gps_for_wifi;
	private Gps last_gps_scan_for_wifi;
	private String gps_start_description_wifi;
	private String gps_end_description_wifi;


	public DeviceInformation(Service logService, Context ctx) {
		// initialize database
		acDB = new AndroidCellsDB();
		//acDB.getNbProviders();
		pref = PreferenceManager.getDefaultSharedPreferences(ctx);

		// a scan every two seconds
		Date now = new Date();
		nextScanDate = new Date(now.getTime() + 1000 * 2);	

		cell_info = "to be defined";

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

	private FileWriter aFileWriterCellular;
	private int byteCounterCellular = 0;

	private void openZoneFileCellular(String mcc) {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			Date now = new Date();
			String date = formatter.format(now);

			String manufacturer = Build.MANUFACTURER;
			String model = Build.MODEL;
			String revision = Build.VERSION.RELEASE;

			File aFile = new File(FilesUtils.cellularFilesStorage+"V2_"+mcc+"_log"+date+".xml");
			aFileWriterCellular = new FileWriter(aFile);
			String xmlTag = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			String logTag = "<logfile manufacturer=\""+manufacturer+"\" model=\""+model+"\" revision=\""+revision+"\" swid=\"AndroidCellsOpenBmap\" swver=\"00.01.00\" >";
			aFileWriterCellular.write(xmlTag);
			aFileWriterCellular.write(logTag);
			aFileWriterCellular.flush();

			byteCounterCellular += xmlTag.length();
			byteCounterCellular += logTag.length();

		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void closeZoneFileCellular() {
		try {
			if (aFileWriterCellular!= null) {
				aFileWriterCellular.write("</logfile>");
				aFileWriterCellular.flush();
				aFileWriterCellular.close();
				byteCounterCellular=0;
				aFileWriterCellular = null;
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void writeZoneFileCellular(String mcc, double dis) {
		try {
			if (aFileWriterCellular== null) openZoneFileCellular(mcc);

			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			Date now = new Date();
			String date = formatter.format(now);

			String scanTag = "<scan time=\""+date+"\" distance=\""+dis+"\" >\n";
			String endScanTag = "</scan>\n";

			int current_scan_length = scanTag.length() + gps_start_description_cellular.length() + cellular_description.length()+ endScanTag.length();

			if ( (byteCounterCellular+current_scan_length) > 20479) {
				closeZoneFileCellular();
				openZoneFileCellular(mcc);
			}else{
				byteCounterCellular += current_scan_length;
			}

			//GsmCellLocation mLocation = (GsmCellLocation) mTelephonyManager.getCellLocation();
			//ArrayList<NeighboringCellInfo> mNeighboringCellInfo = (ArrayList<NeighboringCellInfo>) mTelephonyManager.getNeighboringCellInfo();
			//updateOpenBmapCell(mLocation);
			//updateOpenBmapNeighbors(mNeighboringCellInfo);

			aFileWriterCellular.write(scanTag);
			aFileWriterCellular.write(gps_start_description_cellular);
			aFileWriterCellular.write(cellular_description);
			aFileWriterCellular.write(endScanTag);
			aFileWriterCellular.flush();

		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	private FileWriter aFileWriterWifi;
	private int byteCounterWifi = 0;

	private void openZoneFileWifi() {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			Date now = new Date();
			String date = formatter.format(now);

			String manufacturer = Build.MANUFACTURER;
			String model = Build.MODEL;
			String revision = Build.VERSION.RELEASE;

			File aFile = new File(FilesUtils.wifiFilesStorage+"V1_log"+date+".xml");
			aFileWriterWifi = new FileWriter(aFile);
			String xmlTag = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			String logTag = "<logfile manufacturer=\""+manufacturer+"\" model=\""+model+"\" revision=\""+revision+"\" swid=\"AndroidCellsOpenBmap\" swver=\"00.01.00\" >";
			aFileWriterWifi.write(xmlTag);
			aFileWriterWifi.write(logTag);
			aFileWriterWifi.flush();

			byteCounterWifi += xmlTag.length();
			byteCounterWifi += logTag.length();

		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void closeZoneFileWifi() {
		try {
			if (aFileWriterWifi!= null) {
				aFileWriterWifi.write("</logfile>");
				aFileWriterWifi.flush();
				aFileWriterWifi.close();
				byteCounterWifi=0;
				aFileWriterWifi = null;
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void writeZoneFileWifi(double dis) {
		try {
			if (aFileWriterWifi== null) openZoneFileWifi();

			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			Date now = new Date();
			String date = formatter.format(now);

			String scanTag = "<scan time=\""+date+"\" distance=\""+dis+"\" >\n";
			String endScanTag = "</scan>\n";

			int current_scan_length = scanTag.length() +
			gps_start_description_wifi.length() +
			wifi_ap_description.length()+
			gps_end_description_wifi.length() +
			endScanTag.length();

			if ( (byteCounterWifi+current_scan_length) > 20000) {
				closeZoneFileWifi();
				openZoneFileWifi();
			}else{
				byteCounterWifi += current_scan_length;
			}

			//GsmCellLocation mLocation = (GsmCellLocation) mTelephonyManager.getCellLocation();
			//ArrayList<NeighboringCellInfo> mNeighboringCellInfo = (ArrayList<NeighboringCellInfo>) mTelephonyManager.getNeighboringCellInfo();
			//updateOpenBmapCell(mLocation);
			//updateOpenBmapNeighbors(mNeighboringCellInfo);

			aFileWriterWifi.write(scanTag);
			aFileWriterWifi.write(gps_start_description_wifi);
			aFileWriterWifi.write(wifi_ap_description);
			aFileWriterWifi.write(gps_end_description_wifi);
			aFileWriterWifi.write(endScanTag);
			aFileWriterWifi.flush();

		}catch(Exception e) {
			e.printStackTrace();
		}
	}




	private boolean isRecordingCell() {
		return pref.getBoolean("cell_checkbox", true);
	}
	
	private boolean isRecordingWifi() {
		return pref.getBoolean("wifi_checkbox", true);
	}
	
	private int prefGPSDistanceAccuracy() {
		return pref.getInt("GPSAccuracy", 20);
	}
	
	private int prefCellDistanceFilter() {
		return pref.getInt("CellFilter", 10);
	}
	
	private int prefWifiDistanceFilter() {
		return pref.getInt("WifiFilter", 5);
	}

	public String prefMeasurePlaceLogin() {
		return pref.getString("MeasurePlace_login","login");
	}

	public String prefMeasurePlacePassword() {
		return pref.getString("MeasurePlace_password","password");
	}

	protected void closeDB() {
		closeZoneFileCellular();
		closeZoneFileWifi();
		acDB.closeDB();
	}
	
	private int getGpsAccuracy(int distanceFilter) {
		int gpsAccuracy = prefGPSDistanceAccuracy();
		/*if (distanceFilter < GPS_ACCURACY/2) {
			gpsAccuracy /= 2;
		}*/
		return gpsAccuracy;
	}
	
	// add signals into DataBase
	public void updateDB(Location currentLoc) {
		boolean updateGPS = false;


		Gps gps = new Gps();
		gps.gpsdate = gpsTime2String(currentLoc.getTime());
		gps.lat = currentLoc.getLatitude();
		gps.lon = currentLoc.getLongitude();
		gps.alt = currentLoc.getAltitude();
		gps.speed = currentLoc.getSpeed();
		gps.accuracy = currentLoc.getAccuracy();
		gps.bearing = currentLoc.getBearing();
		double dis = 0.0;
		if (current_gps_for_cellular!=null) {
			dis = gps.distance(current_gps_for_cellular);
		}

		// record a scan every 2 seconds at most
		Date now = new Date();


		if (isRecordingCell()&&(!now.before(nextScanDate))) {
			nextScanDate = new Date(now.getTime() + 1000 * 2);	
			GsmCellLocation mLocation = (GsmCellLocation) mTelephonyManager.getCellLocation();
			ArrayList<NeighboringCellInfo> mNeighboringCellInfo = (ArrayList<NeighboringCellInfo>) mTelephonyManager.getNeighboringCellInfo();

			// Checks if the GPS info is accurate (< GPS_ACCURACY or GPS_ACCURACY/2 meters)
			if (currentLoc.getAccuracy() < getGpsAccuracy(prefCellDistanceFilter())) {
				// record if distance with last point is at least 35 meters
				// or if the connected cell has changed
				if ((dis > 35.0 )||(mLocation.getCid() != cellular_cid)) {
					current_gps_for_cellular = gps;
					switch(cellular_automaton) {
					case 0:

						//Log.v(TAG,"GPS_ACCURACY<"+getGpsAccuracy(prefCellDistanceFilter())+"m : "+currentLoc.getAccuracy());
						// Checks if there is not a nearby cell position in DB

						if (isValidCell(mLocation)) {
							Log.v(TAG, "Recording Cell in DB...");
							updateGPS = true;
							current_gps_for_cellular = gps;
							gps_start_description_cellular = updateOpenBmapGps(gps);
							updateOpenBmapCell(mLocation);
							updateOpenBmapNeighbors(mNeighboringCellInfo);
							writeZoneFileCellular(mTelephonyManager.getNetworkOperator().substring(0,3), dis);
						}
						//cellular_automaton = 1;

						break;
					case 1:
						if (isRecordingCell()) {
							current_gps_for_cellular = gps;
							gps_start_description_cellular = updateOpenBmapGps(gps);
							cellular_automaton = 0;
						}
						break;
					}
				}

			} else {
				Log.v(TAG,"GPS_ACCURACY>"+getGpsAccuracy(prefCellDistanceFilter())+"m : "+currentLoc.getAccuracy());
			}
		}

		if (currentLoc.getAccuracy() < getGpsAccuracy(prefWifiDistanceFilter())) {
			//Log.v(TAG,"GPS_ACCURACY<"+getGpsAccuracy(prefWifiDistanceFilter())+"m : "+currentLoc.getAccuracy());
			if (isRecordingWifi() && wm.isWifiEnabled()) {
				// Checks if there is not a nearby Wifi position in DB
				//if (!acDB.wifiLocationNearBD(currentLoc, prefWifiDistanceFilter())) {
				if (updateWifiDB(wm, currentLoc)) {
					//updateGPS = true;
				}
				//}
			}
		} else {
			Log.v(TAG,"GPS_ACCURACY>"+getGpsAccuracy(prefWifiDistanceFilter())+"m : "+currentLoc.getAccuracy());
		}

		/*
		if (updateGPS) {
			updateGpsDB(currentLoc);
		}
		 */
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

	private String updateOpenBmapGps(Gps gps) {


		//<gps time="20100416145530" lng="0.0983764" lat="43.2240413" alt="280.9" hdg="88.6563" spe="14.796" hdop="4.09" vdop="4.66" pdop="6.2" /> 
		String agps_start_description = "<gps"+
		" time=\""+gps.gpsdate+"\""+
		" lng=\""+gps.lon+"\""+
		" lat=\""+gps.lat+"\""+
		" alt=\""+gps.alt+"\""+
		" hdg=\""+gps.bearing+"\""+
		" spe=\""+gps.speed+"\""+
		//" hdop=\"4.09\""+
		//" vdop=\"4.66\""+
		//" pdop=\"6.2\""+
		" accuracy=\""+gps.accuracy+"\""+
		" />\n";

		return agps_start_description;
	}


	private void updateOpenBmapCell(GsmCellLocation mLocation) {
		Cell cell = new Cell();
		cell.operator_name = mTelephonyManager.getNetworkOperatorName();
		cell.operator = mTelephonyManager.getNetworkOperator();

		cell.mcc = mTelephonyManager.getNetworkOperator().substring(0,3);
		cell.mnc = mTelephonyManager.getNetworkOperator().substring(3);
		cell.type = mTelephonyManager.getNetworkType();
		cell.cid = mLocation.getCid();
		cell.lac = mLocation.getLac();
		//cell.psc = mLocation.Cid();
		cell.strenght_asu = signalStrengthAsu;
		cell.signalStrengthdBm = -113 + 2 * signalStrengthAsu;

		String act = getNetworkType(mTelephonyManager.getNetworkType());
		// v2: <gsmserving mcc="208" mnc="1" lac="12803" id="53762" ss="-73" act="GSM" rxlev="40" /> 
		cellular_description = "<gsmserving mcc=\""+cell.mcc +"\""+
		" mnc=\""+cell.mnc+"\""+
		" lac=\""+cell.lac+"\""+
		" id=\""+cell.cid+"\""+
		" ss=\""+cell.signalStrengthdBm+"\""+
		" act=\""+act+"\""+
		" rxlev=\""+cell.strenght_asu+"\" />\n"; 

		cellular_mcc = cell.mcc;
		cellular_mnc = cell.mnc;
		cellular_lac = cell.lac;
		cellular_cid = cell.cid;	

		cell_info = act+"/"+cellular_mcc+"/"+cellular_mnc+"/"+cell.lac+"/"+cell.cid+"/"+cell.signalStrengthdBm;
	}

	private void updateOpenBmapNeighbors(ArrayList<NeighboringCellInfo> mNeighboringCellInfo) {
		//cellular_description += "mNeighboringCellInfo.size()="+mNeighboringCellInfo.size()+"\n";
		if (mNeighboringCellInfo.size() != 0) {
			for (int i = 0; i < mNeighboringCellInfo.size(); ++i) {

				//				cellular_description += "signalStrengthAsu="+NeighboringCellInfo.UNKNOWN_RSSI+"\n";
				//				cellular_description += "mNeighboringCellInfo.get(i).getLac()="+mNeighboringCellInfo.get(i).getLac()+"\n";

				// checks if cell info is valid
				if ((signalStrengthAsu != NeighboringCellInfo.UNKNOWN_RSSI)


						//		&& mNeighboringCellInfo.get(i).getLac() != 0
						//		&& mNeighboringCellInfo.get(i).getLac() != -1
						//		&& mNeighboringCellInfo.get(i).getCid() != 0
						//		&& mNeighboringCellInfo.get(i).getCid() != -1
				) {
					//	if (mNeighboringCellInfo.get(i).getLac() != 0) {


					Neighbor n = new Neighbor();
					n.type = mNeighboringCellInfo.get(i).getNetworkType();
					n.cid = mNeighboringCellInfo.get(i).getCid();
					n.lac = mNeighboringCellInfo.get(i).getLac();
					n.psc = mNeighboringCellInfo.get(i).getPsc();
					n.strenght_asu = mNeighboringCellInfo.get(i).getRssi();

					// v2:<gsmneighbour mcc="208" mnc="1" lac="12803" id="2903" rxlev="27" c1="28" c2="28" /> 
					cellular_description += "<gsmneighbour"+
					" mcc=\""+cellular_mcc+"\""+
					" mnc=\""+cellular_mnc+"\""+
					" lac=\""+n.lac+"\""+
					" id=\""+n.cid+"\""+
					" psc=\""+n.psc+"\""+
					" rxlev=\""+n.strenght_asu+"\""+
					//" c1=\""+n.c1+""+
					//" c2=\""+n.c2+"\""+
					" act=\""+getNetworkType(n.type)+"\""+
					" />\n";
				}
			}
		}
	}




	// add cell signal
	private void updateCellDB(Location currentLoc, GsmCellLocation mLocation) {
		Cell cell = new Cell();
		cell.gpsdate = gpsTime2String(currentLoc.getTime());
		cell.operator_name = mTelephonyManager.getNetworkOperatorName();
		cell.operator = mTelephonyManager.getNetworkOperator();

		cell.mcc = mTelephonyManager.getNetworkOperator().substring(0,3);
		cell.mnc = mTelephonyManager.getNetworkOperator().substring(3);

		cell.type = mTelephonyManager.getNetworkType();
		cell.cid = mLocation.getCid();
		cell.lac = mLocation.getLac();
		//cell.psc = mLocation.Cid();
		cell.strenght_asu = signalStrengthAsu;
		cell.signalStrengthdBm = -113 + 2 * signalStrengthAsu;

		acDB.insertCell(cell);

		// v2: <gsmserving mcc="208" mnc="1" lac="12803" id="53762" ss="-73" act="GSM" rxlev="40" /> 
		cellular_description = "<gsmserving mcc=\""+cell.operator_name +"\""+
		" mnc=\""+cell.operator+"\""+
		" lac=\""+cell.lac+"\""+
		" id=\""+cell.cid+"\""+
		" ss=\""+cell.signalStrengthdBm+"\""+
		" act=\""+mTelephonyManager.getNetworkType()+"\""+
		" rxlev=\""+cell.strenght_asu+"\" />\n"; 

		cellular_mcc = cell.mcc;
		cellular_mnc = cell.mnc;

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
					acDB.insertNeighbor(gpsTime2String(currentLoc.getTime()),
							mNeighboringCellInfo.get(i).getNetworkType(),
							mNeighboringCellInfo.get(i).getCid(),
							mNeighboringCellInfo.get(i).getLac(),
							mNeighboringCellInfo.get(i).getPsc(),
							mNeighboringCellInfo.get(i).getRssi());


					// v2:<gsmneighbour mcc="208" mnc="1" lac="12803" id="2903" rxlev="27" c1="28" c2="28" /> 
					cellular_description += "<gsmneighbour"+
					" mcc=\""+cellular_mcc+"\""+
					" mnc=\""+cellular_mnc+"\""+
					" lac=\""+n.lac+"\""+
					" id=\""+n.cid+"\""+
					" psc=\""+n.psc+"\""+
					" rxlev=\""+n.strenght_asu+"\""+
					//" c1=\""+n.c1+""+
					//" c2=\""+n.c2+"\""+
					" act=\""+n.type+"\""+
					" />\n";


				}
			}
		}
	}




	private static boolean wifiScanRequested = false;
	public static boolean wifiScanResultReceived = false ;

	public boolean updateWifiDB(WifiManager wm, Location currentLoc){
		boolean updateWifi = false;


		Gps gps = new Gps();
		gps.gpsdate = gpsTime2String(currentLoc.getTime());
		gps.lat = currentLoc.getLatitude();
		gps.lon = currentLoc.getLongitude();
		gps.alt = currentLoc.getAltitude();
		gps.speed = currentLoc.getSpeed();
		gps.accuracy = currentLoc.getAccuracy();
		gps.bearing = currentLoc.getBearing();
		double dis = 0.0;
		double lastSuccessfulScanDis = 0.0;
		boolean lastSuccessfulScanTooClose = false;

		if (wm.isWifiEnabled()) {

			if (!wifiScanRequested) {

				if (last_gps_scan_for_wifi!=null) {
					lastSuccessfulScanDis = gps.distance(last_gps_scan_for_wifi);
					if (lastSuccessfulScanDis < 35.0) lastSuccessfulScanTooClose = true;
				}

				if (!lastSuccessfulScanTooClose) {
					wifi_ap_description = "";
					if (wm.startScan()) {	
						// write GPS
						wifi_ap_description = "";
						gps_end_description_wifi = "";
						current_gps_for_wifi = gps;
						gps_start_description_wifi = updateOpenBmapGps(gps);
						wifiScanRequested = true;
					}else{
						Log.v(TAG, "Impossible to start a wifi scan");
					}
				}else{
					Log.v(TAG, "Last succesful wifi scan to close");
				}
			}else{
				if (wifiScanResultReceived){
					dis = gps.distance(current_gps_for_wifi);

					if (dis < 35.0) {
						gps_end_description_wifi = updateOpenBmapGps(gps);

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
								//acDB.insertWifi(w);
								idx++;

								//  <wifiap bssid="000B6B4F82BF" md5essid="245e48daf53033d2974674766566f4b1" ntiu="OFDM24" enc="1" ss="-85" im="1" /> 
								wifi_ap_description +=  "<wifiap bssid=\""+w.BSSID.replaceAll(":", "")+"\" md5essid=\"" + md5(w.SSID) + "\" ";
								if (w.capabilities!=null) wifi_ap_description += "capa=\"" + w.capabilities + "\" ";
								wifi_ap_description += "ss=\""+w.level + "\" ntiu=\""+w.frequency+"\" />\n";
							} 
							// write in file
							writeZoneFileWifi(dis);
							last_gps_scan_for_wifi = gps;
						} else {
							Log.v(TAG, "No Wifi signal in scan");
						}
						wifiScanRequested = false;
						wifiScanResultReceived = false;

					}else{
						Log.v(TAG, "Moving too fast");
					}
				}else{
					Log.v(TAG, "No Wifi scan received yet");
				}
			}
		}
		return updateWifi;
	}

	public String md5(String s) {  
		try {  
			// Create MD5 Hash  
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");  
			digest.update(s.getBytes());  
			byte messageDigest[] = digest.digest();  

			// Create Hex String  
			StringBuffer hexString = new StringBuffer();  
			for (int i=0; i<messageDigest.length; i++)  
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));  
			return hexString.toString();  

		} catch (NoSuchAlgorithmException e) {  
			e.printStackTrace();  
		}  
		return "";  
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

	public String lastCellInfo() {
		return cell_info;
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


	private String getNetworkType(int atype) {
		switch(atype) {
		case TelephonyManager.NETWORK_TYPE_UNKNOWN :
			return "NA";
		case TelephonyManager.NETWORK_TYPE_GPRS:
			return "GSM";
		case TelephonyManager.NETWORK_TYPE_EDGE:
			return "EDGE";
		case TelephonyManager.NETWORK_TYPE_UMTS:
			return "UMTS";
		case TelephonyManager.NETWORK_TYPE_CDMA:
			return "CDMA";
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return "EDV0_0";
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return "EDV0_A";
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return "1xRTT";
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			return "HSDPA";
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			return "HSUPA";
		case TelephonyManager.NETWORK_TYPE_HSPA:
			return "HSPA";
		}
		return "NA";

	}



}
