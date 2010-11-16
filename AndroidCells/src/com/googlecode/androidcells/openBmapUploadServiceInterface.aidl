package com.googlecode.androidcells;

import com.googlecode.androidcells.openBmapUploadServiceInterfaceCallback;

interface openBmapUploadServiceInterface {
	void registerCallback(openBmapUploadServiceInterfaceCallback callback);
	void unregisterCallback(openBmapUploadServiceInterfaceCallback callback);
	void uploadCellularFiles();
	void uploadWifiFiles();
}