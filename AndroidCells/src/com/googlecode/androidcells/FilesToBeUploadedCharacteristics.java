package com.googlecode.androidcells;

import android.os.Parcel;
import android.os.Parcelable;

public class FilesToBeUploadedCharacteristics implements Parcelable {
	private int nbOfCellularFiles;
	public int getNbOfCellularFiles() {
		return nbOfCellularFiles;
	}

	private int nbOfWifiFiles;

	
    public int getNbOfWifiFiles() {
		return nbOfWifiFiles;
	}

	public FilesToBeUploadedCharacteristics(int nbcellularfiles, int nbwififiles) { 
        super(); 
        this.nbOfCellularFiles = nbcellularfiles; 
        this.nbOfWifiFiles = nbwififiles; 
    } 
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}

}
