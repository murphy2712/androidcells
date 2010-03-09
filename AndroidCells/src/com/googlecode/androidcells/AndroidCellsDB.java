package com.googlecode.androidcells;

import com.googlecode.androidcells.Constants;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.SystemClock;
import android.util.FloatMath;
import android.util.Log;

public class AndroidCellsDB implements Constants {
	private static final String TAG = "AndroidCells.AndroidCellsDB";
	private static final String CREATE_TABLE_CELLS = "CREATE TABLE IF NOT EXISTS cells" +
			" (gpsdate DATE PRIMARY KEY," +
			" operator_name TEXT NOT NULL," +
			" operator TEXT NOT NULL," +
			" type INTEGER NOT NULL," +
			" cid INTEGER NOT NULL," +
			" lac INTEGER NOT NULL," +
			" psc INTEGER NOT NULL," +
			" strenght_asu INTEGER NOT NULL);";
	private static final String CREATE_TABLE_NEIGHBORS = "CREATE TABLE IF NOT EXISTS neighbors" +
			" (gpsdate DATE NOT NULL," +
			" type INTEGER NOT NULL," +
			" cid INTEGER NOT NULL," +
			" lac INTEGER NOT NULL," +
			" psc INTEGER NOT NULL," +
			" strenght_asu INTEGER NOT NULL," +
			" PRIMARY KEY(gpsdate,cid,lac,psc));";
	private static final String CREATE_TABLE_GPS = "CREATE TABLE IF NOT EXISTS gps" +
			" (gpsdate DATE PRIMARY KEY," +
			" lat DOUBLE NOT NULL," +
			" lon DOUBLE NOT NULL," +
			" alt DOUBLE NOT NULL," +
			" speed FLOAT NOT NULL," +
			" accuracy FLOAT NOT NULL," +
			" bearing FLOAT NOT NULL);";
	private static final String CREATE_TABLE_WIFI = "CREATE TABLE IF NOT EXISTS wifi" +
			" (gpsdate DATE NOT NULL," +
			" bssid TEXT NOT NULL," +
			" ssid TEXT NOT NULL," +
			" capabilities TEXT NOT NULL," +
			" frequency INTEGER NOT NULL," +
			" level INTEGER NOT NULL," +
			" PRIMARY KEY(gpsdate,bssid));";
	private static final String CELLS_TABLE = "cells";
	private static final String NEIGHBORS_TABLE = "neighbors";
	private static final String GPS_TABLE = "gps";
	private static final String WIFI_TABLE = "wifi";
	private static final String DATABASE_NAME = "/sdcard/androidcells.db3"; // "androidcells.db3"
	
	private SQLiteDatabase db;
	
	protected AndroidCellsDB() {
		//db = ctx.openOrCreateDatabase(DATABASE_NAME, Context.MODE_WORLD_WRITEABLE, null);
		db = SQLiteDatabase.openOrCreateDatabase(DATABASE_NAME, null);
		db.execSQL(CREATE_TABLE_CELLS);
		db.execSQL(CREATE_TABLE_NEIGHBORS);
		db.execSQL(CREATE_TABLE_GPS);
		db.execSQL(CREATE_TABLE_WIFI);
	}
	
	protected void closeDB() {
		db.close();
	}
	
	protected boolean insertCell(Cell c) {
		ContentValues values = new ContentValues();
		values.put("gpsdate", c.gpsdate);
		values.put("operator_name", c.operator_name);
		values.put("operator", c.operator);
		values.put("type", c.type);
		values.put("cid", c.cid);
		values.put("lac", c.lac);
		values.put("psc", c.psc);
		values.put("strenght_asu", c.strenght_asu);
		return (db.insert(CELLS_TABLE, null, values) > 0);
	}
	
	protected boolean insertNeighbor(Neighbor n) {
		ContentValues values = new ContentValues();
		values.put("gpsdate", n.gpsdate);
		values.put("type", n.type);
		values.put("cid", n.cid);
		values.put("lac", n.lac);
		values.put("psc", n.psc);
		values.put("strenght_asu", n.strenght_asu);
		return (db.insert(NEIGHBORS_TABLE, null, values) > 0);
	}
	
	protected boolean insertGps(Gps g) {
		ContentValues values = new ContentValues();
		values.put("gpsdate", g.gpsdate);
		values.put("lat", round(g.lat, LAT_LON_ACCURACY));
		values.put("lon", round(g.lon, LAT_LON_ACCURACY));
		values.put("alt", round(g.alt, ALT_ACCURACY));
		values.put("speed", round(g.speed, SPEED_ACCURACY));
		values.put("accuracy", g.accuracy);
		values.put("bearing", round(g.bearing, BEARING_ACCURACY));
		return (db.insert(GPS_TABLE, null, values) > 0);
	}
	
	protected boolean insertWifi(Wifi w) {
		ContentValues values = new ContentValues();
		values.put("gpsdate", w.gpsdate);
		values.put("bssid", w.BSSID);
		values.put("ssid", w.SSID);
		values.put("capabilities", w.capabilities);
		values.put("frequency", w.frequency);
		values.put("level", w.level);
		return (db.insert(WIFI_TABLE, null, values) > 0);
	}
	
	private float round(double nb, int x) {
		return (float)((Math.round(nb * Math.pow(10, x))) / (Math.pow(10, x))); 
	}
	
	private float nbCeil(float nb, int[] x) {
		float nbCeil = (FloatMath.ceil(nb * (float)Math.pow(10, x[0]))) / (float)(Math.pow(10, x[0]));
		nbCeil += (float) Math.pow(10, -x[0]) * x[1];
		return nbCeil;
	}
	
	private float nbFloor(float nb, int[] x) {
		float nbFloor = (FloatMath.floor(nb * (float) Math.pow(10, x[0]))) / (float)(Math.pow(10, x[0]));
		nbFloor -= (float) Math.pow(10, -x[0]) * x[1];
		return nbFloor;
	}
	
	/**
	 * Define how many decimals to keep for filtering far locations in SQL query:
		1°			111 km (~100 km),	0
		0.1°		11 km (~10 km),		1
		0.01°		1.1 km (~1 km),		2
		0.001°		110 m (~100 m),		3
		0.0001°		11 m (~10 m),		4
		0.00001°	1.1 m (~1 m),		5
		0.000001°	11 cm (~10 cm)		6
		@see http://en.wikipedia.org/wiki/Wikipedia:WikiProject_Geographical_coordinates#Precision
		val=1,5,10,15,20,35,50,65,80,100m
	 */
	private int[] distance2GpsDecimal_filter(int distance) {
		int decimal[] = {0,0};
		if (distance <= 1) {
			decimal[0] = 5;
			decimal[1] = 7;
		} else if (distance <= 5) {
			decimal[0] = 4;
			decimal[1] = 2;
		} else if (distance <= 10) {
			decimal[0] = 4;
			decimal[1] = 3;
		} else if (distance <= 20) {
			decimal[0] = 4;
			decimal[1] = 5;
		} else if (distance <= 35) {
			decimal[0] = 4;
			decimal[1] = 6;
		} else if (distance <= 50) {
			decimal[0] = 4;
			decimal[1] = 8;
		} else if (distance <= 65) {
			decimal[0] = 4;
			decimal[1] = 9;
		} else if (distance <= 80) {
			decimal[0] = 3;
			decimal[1] = 1;
		} else if (distance > 80) {
			decimal[0] = 3;
			decimal[1] = 2;
		}
		return decimal;
	}
	
	protected boolean cellLocationNearBD(Location currentLoc, int distance_filter,
											String operator, int networkType) {
		if (distance_filter==0) return false; // Always log
		boolean gpsLocationNearDB = false;
		int[] decimal_filter = distance2GpsDecimal_filter(distance_filter);
		float latMin = nbFloor((float) currentLoc.getLatitude(), decimal_filter);
		float latMax = nbCeil((float) currentLoc.getLatitude(), decimal_filter);
		float lonMin = nbFloor((float) currentLoc.getLongitude(), decimal_filter);
		float lonMax = nbCeil((float) currentLoc.getLongitude(), decimal_filter);
		String sql = "SELECT lat,lon" +
				" FROM gps" +
				" JOIN cells ON gps.gpsdate=cells.gpsdate" +
				" WHERE lat>"+latMin+" AND lat<"+latMax+" AND lon>"+lonMin+" AND lon<"+lonMax +
				" AND cells.operator="+operator +
				" AND cells.type="+networkType;
		//String selection = "lat>"+latMin+" AND lat<"+latMax+" AND lon>"+lonMin+" AND lon<"+lonMax;
		//Log.v(TAG, selection);
		//Log.v(TAG, sql);
		/*Cursor cur = db.query(GPS_TABLE,
				new String [] {"lat", "lon"},
				selection,
				null, null, null, null);*/
		Cursor cur = db.rawQuery(sql, null);
		if ((cur != null) && (cur.moveToFirst())) {
			Location bdLoc = new Location("");
			int latName = cur.getColumnIndex("lat");
			int lonName = cur.getColumnIndex("lon");
			do {
	            // Set bdLoc location from DB
	            bdLoc.setLatitude(cur.getFloat(latName));
	            bdLoc.setLongitude(cur.getFloat(lonName));
	            float dist = currentLoc.distanceTo(bdLoc);
	            if (dist < distance_filter) {
	            	Log.v(TAG, "Location gps (lat, lon) near DB: "
	            			+dist+"m, dist="+distance_filter+"m," +
	            					"deci0="+decimal_filter[0]+", deci1="+decimal_filter[1]);
	            	cur.close();
	            	return true; // GPS location near DB
	            } else {
	            	//Log.v(TAG, "Location in DB is far:"+dist+"m");
	            }
	        } while (cur.moveToNext());
			cur.close();
		} else { // GPS location not near DB (filtered by SQL)
			//Log.v(TAG, "Nothing similar in DB (filtered by SQL query," +
			//		"deci0="+decimal_filter[0]+", deci1="+decimal_filter[1]+")");
			cur.close();
		}
		//return gpsLocationNearDB;
		return true;
	}
	
	protected boolean wifiLocationNearBD(Location currentLoc, int distance_filter) {
		if (distance_filter==0) return false; // Always log
		boolean gpsLocationNearDB = false;
		int[] decimal_filter = distance2GpsDecimal_filter(distance_filter);
		float latMin = nbFloor((float) currentLoc.getLatitude(), decimal_filter);
		float latMax = nbCeil((float) currentLoc.getLatitude(), decimal_filter);
		float lonMin = nbFloor((float) currentLoc.getLongitude(), decimal_filter);
		float lonMax = nbCeil((float) currentLoc.getLongitude(), decimal_filter);
		String sql = "SELECT lat,lon" +
				" FROM gps" +
				" JOIN wifi ON gps.gpsdate=wifi.gpsdate" +
				" WHERE lat>"+latMin+" AND lat<"+latMax+" AND lon>"+lonMin+" AND lon<"+lonMax;
		//Log.v(TAG, sql);
		Cursor cur = db.rawQuery(sql, null);
		if ((cur != null) && (cur.moveToFirst())) {
			Location bdLoc = new Location("");
			int latName = cur.getColumnIndex("lat");
			int lonName = cur.getColumnIndex("lon");
			do {
	            // Set bdLoc location from DB
	            bdLoc.setLatitude(cur.getFloat(latName));
	            bdLoc.setLongitude(cur.getFloat(lonName));
	            float dist = currentLoc.distanceTo(bdLoc);
	            if (dist < distance_filter) {
	            	Log.v(TAG, "Location gps (lat, lon) near DB: "+dist+"m");
	            	cur.close();
	            	return true; // GPS location near DB
	            } else {
	            	//Log.v(TAG, "Location in DB is far:"+dist+"m");
	            }
	        } while (cur.moveToNext());
			cur.close();
		} else { // GPS location not near DB (filtered by SQL)
			//Log.v(TAG, "Nothing similar in DB (filtered by SQL query, decimal_filter="+decimal_filter+")");
			cur.close();
		}
		return gpsLocationNearDB;
	}
	
	protected int nbGpsLocations() {
		int nbGpsLocations = 0;
		Cursor cur = db.query(GPS_TABLE, new String [] {"count(gpsdate) as nb"}, null, null, null, null, null);
		if ((cur != null) && (cur.moveToFirst())) {
			nbGpsLocations = cur.getInt( cur.getColumnIndex("nb") );
		}
		cur.close();
		return nbGpsLocations;
	}
	
	protected int nbCellLocations() {
		int nbCellLocations = 0;
		Cursor cur = db.query(CELLS_TABLE, new String [] {"count(gpsdate) as nb"}, null, null, null, null, null);
		if ((cur != null) && (cur.moveToFirst())) {
			nbCellLocations = cur.getInt( cur.getColumnIndex("nb") );
		}
		cur.close();
		return nbCellLocations;
	}
	
	protected int nbNeighborsLocations() {
		int nbNeiborsLocations = 0;
		Cursor cur = db.query(NEIGHBORS_TABLE, new String [] {"count(gpsdate) as nb"}, null, null, null, null, null);
		if ((cur != null) && (cur.moveToFirst())) {
			nbNeiborsLocations = cur.getInt( cur.getColumnIndex("nb") );
		}
		cur.close();
		return nbNeiborsLocations;
	}
	
	protected int nbWifiLocations() {
		int nbWifiLocations = 0;
		//long time=SystemClock.uptimeMillis();
		//Log.v(TAG,"DÉBUT="+ (float)(SystemClock.uptimeMillis()-time)/1000 );
		Cursor cur = db.query(WIFI_TABLE, new String [] {"count(gpsdate) as nb"}, null, null, null, null, null);
		//Log.v(TAG,"MILIEU="+ (float)(SystemClock.uptimeMillis()-time)/1000 );
		if ((cur != null) && (cur.moveToFirst())) {
			nbWifiLocations = cur.getInt( cur.getColumnIndex("nb") );
		}
		//Log.v(TAG,"FIN="+ (float)(SystemClock.uptimeMillis()-time)/1000 );
		cur.close();
		return nbWifiLocations;
	}
	
	
	protected void getNbProviders() {
		String sql = "select count(*) as nb, operator_name from cells group by operator_name";
		long time=SystemClock.uptimeMillis();
		Log.v(TAG,"DÉBUT="+ (float)(SystemClock.uptimeMillis()-time)/1000 );
		Cursor cur = db.rawQuery(sql, null);
		Log.v(TAG,"MILIEU="+ (float)(SystemClock.uptimeMillis()-time)/1000 );
		if ((cur != null) && (cur.moveToFirst())) {
			int nb = cur.getColumnIndex("nb");
			int operator_name = cur.getColumnIndex("operator_name");
			do {
				Log.v(TAG, cur.getString(operator_name) + " (" + cur.getString(nb)+")");
			} while (cur.moveToNext());
		}
		Log.v(TAG,"FIN="+ (float)(SystemClock.uptimeMillis()-time)/1000 );
		cur.close();
	}
}
