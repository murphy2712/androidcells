package com.googlecode.androidcells;

public interface Constants {
	/**
	 * Defines the accuracy for recording GPS positions (in meters)
	 */
	public static final int GPS_ACCURACY = 20;
	/**
	 * Defines the number of decimals to keep for latitude and longitude
	 */
	public static final int LAT_LON_ACCURACY = 9;
	/**
	 * Defines the number of decimals to keep for altitude
	 */
	public static final int ALT_ACCURACY = 1;
	/**
	 * Defines the number of decimals to keep for accuracy
	 */
	public static final int SPEED_ACCURACY = 3;
	/**
	 * Defines the number of decimals to keep for bearing
	 */
	public static final int BEARING_ACCURACY = 9;
	
	
	public static final int CELL_DECIMAL_FILTER = 3;
	
	/**
	 * Filter distance to avoid duplicates locations (in meters)
	 */
	//public static final int CELL_DISTANCE_FILTER = 10;
	
	/**
	 * Filter distance to avoid duplicates locations (in meters)
	 */
	//public static final int WIFI_DISTANCE_FILTER = 5;
	/**
	 * Define how many decimals to keep for filtering far wifi locations in SQL query:
		1°			111 km (~100 km),
		0.1°		11 km (~10 km),
		0.01°		1.1 km (~1 km),
		0.001°		110 m (~100 m),
		0.0001°		11 m (~10 m),
		0.00001°	1.1 m (~1 m),
		0.000001°	11 cm (~10 cm)
		@see http://en.wikipedia.org/wiki/Wikipedia:WikiProject_Geographical_coordinates#Precision
	 */
	public static final int WIFI_DECIMAL_FILTER = 3;
}
