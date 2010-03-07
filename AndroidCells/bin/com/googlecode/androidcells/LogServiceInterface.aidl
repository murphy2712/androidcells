package com.googlecode.androidcells;

interface LogServiceInterface {
	String getProviderInfos();
	int nbGpsLocations();
	int nbCellLocations();
	int nbNeighborsLocations();
	int nbWifiLocations();
}