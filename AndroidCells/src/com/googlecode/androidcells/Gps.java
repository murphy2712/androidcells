package com.googlecode.androidcells;

public class Gps {
	String gpsdate;
	double lat;
	double lon;
	double alt;
	float speed;
	float accuracy;
	float bearing;

	public double distance(Gps destGps) {
		double rlo1 = Math.toRadians(lon);
		double  rla1 = Math.toRadians(lat);
		double  rlo2 = Math.toRadians(destGps.lon);
		double  rla2 = Math.toRadians(destGps.lat);
		double  dlo = (rlo2 - rlo1) / 2;
		double  dla = (rla2 - rla1) / 2;
		double  a = Math.sin(dla) * Math.sin(dla) + Math.cos(rla1) * Math.cos(rla2) * Math.sin(dlo) * Math.sin(dlo);
		return (6378137 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
	}

}
