package com.googlecode.androidcells;

import com.googlecode.androidcells.LogServiceInterfaceResponse;

interface LogServiceInterface {
	void setCallback(LogServiceInterfaceResponse callback);
	boolean isRecording();
	void startRecording();
	void stopRecording();
	String getProviderInfos();
	int nbGpsLocations();
	int nbCellLocations();
	int nbNeighborsLocations();
	int nbWifiLocations();
}