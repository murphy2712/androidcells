package com.measureplace.utils;

import java.io.File;

import com.googlecode.androidcells.Constants;


/**
 * 
 * This file is part of openBmap program.
 * 
 * openBmap is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * openBmap is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with openBmap. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @Copyright 2006, 2007, 2008, 2009, 2010 M.Beauvais
 * @author M.Beauvais
 * @version $Revision: $
 * 
 */
public class FilesUtils implements Constants {

	public static String cellularFilesStorage = "/sdcard/MeasurePlace/cellular/";
	public static String wifiFilesStorage = "/sdcard/MeasurePlace/wifi/";

	public static String cellularProcessedFilesStorage = "/sdcard/MeasurePlace/ProcessedFiles/cellular/";
	public static String wifiProcessedFilesStorage = "/sdcard/MeasurePlace/ProcessedFiles/wifi/";
	
	public static String[] getListOfFile(String dir) {
		
		File fileDir = new File(dir);
		String[] list = fileDir.list();
		return list;
	}
	
	
	public static void checkFolders() {
		
		File aFile = new File(cellularFilesStorage);
		if (!aFile.exists()) aFile.mkdirs();
		
		aFile = new File(cellularProcessedFilesStorage);
		if (!aFile.exists()) aFile.mkdirs();
		
		aFile = new File(wifiFilesStorage);
		if (!aFile.exists()) aFile.mkdirs();
		
		aFile = new File(wifiProcessedFilesStorage);
		if (!aFile.exists()) aFile.mkdirs();
	}
	
	public static boolean moveFile( String sourceName, String destName ){
		File source = new File(sourceName);
		File dest = new File(destName);
		
		return source.renameTo(dest);
	}
	
	
}
