package com.googlecode.androidcells;

interface LogServiceInterface {
	boolean isRecording();
	void startRecording();
	void stopRecording();
	String getProviderInfos();
	int nbGpsLocations();
	int nbCellLocations();
	int nbNeighborsLocations();
	int nbWifiLocations();
}