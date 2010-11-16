package com.googlecode.androidcells;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.preference.PreferenceManager;
import android.util.Log;

import com.measureplace.utils.FilesUtils;

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
public class openBmapUploadService extends Service {

	private static final String TAG = "AndroidCells.openBmapUploadService";


	private static SharedPreferences pref;

	public static String pathCellularQuery = "/upload/upl.php5";
	public static String hostCellularQuery = "upload.measureplace.com";
	public static String urlCellularString = "http://upload.measureplace.com/upload/upl.php5";

	public static String pathWifiQuery = "/upload_wifi/upl.php5";
	public static String hostWifiQuery = "upload.measureplace.com";
	public static String urlWifiString = "http://upload.measureplace.com/upload_wifi/upl.php5";


	private Timer timer ; 

	final RemoteCallbackList<openBmapUploadServiceInterfaceCallback> callbacks = new RemoteCallbackList<openBmapUploadServiceInterfaceCallback>(); 

	public RemoteCallbackList<openBmapUploadServiceInterfaceCallback> getCallbacks() { 
		return callbacks; 
	}


	private openBmapUploadServiceBinder binder;

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		Log.v(TAG, "onCreate()");
		super.onCreate();
		binder = new openBmapUploadServiceBinder(this); 

		pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

	}


	@Override 
	public void onDestroy() { 
		super.onDestroy(); 
		//this.mBinder = null; 
		this.callbacks.kill(); // Désactive tous les éléments de la liste 
		this.timer.cancel(); 
	}

	public void uploadWifiFiles() {
		try {
			Thread athread = new Thread() { 
				public void run() {
					try {
						String[] logFiles = FilesUtils.getListOfFile(FilesUtils.wifiFilesStorage);
						if ((logFiles == null)||(logFiles.length==0)) {
							int N = callbacks.beginBroadcast();
							for (int j = 0; j < N; j++) { 
								callbacks.getBroadcastItem(j).nbWifiFilesChanged(0);
							}
							callbacks.finishBroadcast();							
						}else{
							int nbWifiFiles = logFiles.length - 1;

							for (int i=0; i < logFiles.length; i++) {
								String filename = logFiles[i];
								if (filename.startsWith("V1_")&&(filename.endsWith(".xml"))) {
									openBmapUploadService.uploadLogFiles("wifi",  null, FilesUtils.wifiFilesStorage+filename);
									FilesUtils.moveFile(FilesUtils.wifiFilesStorage+filename, FilesUtils.wifiProcessedFilesStorage+filename);
								}
								int N = callbacks.beginBroadcast();
								for (int j = 0; j < N; j++) { 
									callbacks.getBroadcastItem(j).nbWifiFilesChanged(nbWifiFiles-i);
								}
								callbacks.finishBroadcast();
							}
						}
					}catch(Exception e) {
						e.printStackTrace();
					}
				} 
			}; 		
			athread.start();

		}catch(Exception e4) {
			e4.printStackTrace();
		}
	}

	public void uploadCellularFiles() {

		try {
			System.out.println("uploadCellularFiles in");
			
			
			Thread athread = new Thread() { 
				public void run() {
					try {
						String[] logFiles = FilesUtils.getListOfFile(FilesUtils.cellularFilesStorage);
						if ((logFiles == null)||(logFiles.length==0)) {
							System.out.println("Threaddddddddddddddddd - uploadCellularFiles nbCellularFiles=0");
							int N = callbacks.beginBroadcast();
							for (int j = 0; j < N; j++) { 
								callbacks.getBroadcastItem(j).nbCellularFilesChanged(0);
							}
							callbacks.finishBroadcast();							
						}else{
							int nbCellularFiles = logFiles.length - 1;
							System.out.println("Threaddddddddddddddddd - uploadCellularFiles nbCellularFiles="+nbCellularFiles);
							for (int i=0; i < logFiles.length; i++) {
								String filename = logFiles[i];
								if (filename.startsWith("V2_")&&(filename.endsWith(".xml"))) {
									//openBmapLogUpload.uploadCellLogFiles(lsInterface.getMeasurePlaceLogin(), lsInterface.getMeasurePlacePassword(), FilesUtils.MeasurePlace_Directory+filename);
									// move file to uploaded file directory
									openBmapUploadService.uploadLogFiles("cellular", null, FilesUtils.cellularFilesStorage+filename);
									FilesUtils.moveFile(FilesUtils.cellularFilesStorage+filename, FilesUtils.cellularProcessedFilesStorage+filename);
									Thread.sleep(500);
								}
								int N = callbacks.beginBroadcast();
								for (int j = 0; j < N; j++) { 
									callbacks.getBroadcastItem(j).nbCellularFilesChanged(nbCellularFiles-i);
								}
								callbacks.finishBroadcast();

							}
						}
					}catch(Exception e) {
						e.printStackTrace();
					}
				} 
			}; 		
			athread.start();
			System.out.println("uploadCellularFiles out");
		}catch(Exception e4) {
			e4.printStackTrace();
		}

	}
	
	public static void uploadLogFiles(String type, String redirectLoc, String existingFileName) {
		String pathQuery = pathCellularQuery;
		String hostQuery = hostCellularQuery;
		String urlString = urlCellularString;

		if (type.equals("wifi")) {
			pathQuery = pathWifiQuery;
			hostQuery = hostWifiQuery;
			urlString = urlWifiString;
		}

		String login = pref.getString("MeasurePlace_login","login");
		String password = pref.getString("MeasurePlace_password","password");


		System.out.println("uploadLogFiles - type="+type);
		System.out.println("uploadLogFiles - redirectLoc="+redirectLoc);
		System.out.println("uploadLogFiles - existingFileName="+existingFileName);
				
		try {
			if (redirectLoc!=null) {
				urlString = redirectLoc;
				URL aURL = new URL(urlString);
				hostQuery = aURL.getHost();

			}
			
			
			System.out.println("uploadLogFiles - hostQuery="+hostQuery);
			System.out.println("uploadLogFiles - urlString="+urlString);

			System.out.println("uploadLogFiles in");
			
			HttpURLConnection conn = null;
			DataOutputStream dos = null;
			DataInputStream inStream = null;

			File logFile = new File(existingFileName);
			String filename = logFile.getName();

			String lineEnd = "\r\n";
			String twoHyphens = "--";
			String boundary =  "---------------------------"+Long.toString(System.currentTimeMillis(), 16);

			int bytesRead, bytesAvailable, bufferSize;

			byte[] buffer;

			int maxBufferSize = 1*1024*1024;


			try
			{
				String PostData = twoHyphens + boundary + lineEnd;
				PostData +="Content-Disposition: form-data; name=\"openBmap_login\"" + lineEnd +	lineEnd + login + lineEnd;
				//PostData +=lineEnd;
				PostData +=twoHyphens + boundary + lineEnd;
				PostData +="Content-Disposition: form-data; name=\"openBmap_passwd\"" + lineEnd + lineEnd + password + lineEnd;
				//PostData +=lineEnd;		
				PostData +=twoHyphens + boundary + lineEnd;
				PostData +="Content-Disposition: form-data; name=\"file\";"	+ " filename=\""+filename+"\"" + lineEnd + "Content-Type: text/xml"+ lineEnd;
				PostData +=lineEnd;	

				// create a buffer of maximum size
				String FileData = "";
				FileInputStream fileInputStream = new FileInputStream( new File(existingFileName) );
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				// read file and write it into form...
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				while (bytesRead > 0)
				{
					FileData += new String(buffer,0,bufferSize,"UTF-8");
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				}
				fileInputStream.close();
				PostData +=FileData;


				PostData +=lineEnd;	
				PostData +=twoHyphens + boundary + twoHyphens;

				String lengt = new Integer(PostData.length()).toString();

				String contents = "POST " + pathQuery + " HTTP/1.1" + lineEnd +
				"Cache-Control: max-age=259200"+lineEnd +
				"Connection: keep-alive"+lineEnd +
				"Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"+lineEnd +
				"Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7"+lineEnd +
				"Accept-Encoding: gzip,deflate"+lineEnd +
				"Accept-Language: fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3"+lineEnd +
				"Host: " + hostQuery +lineEnd +
				"User-Agent: Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.8.1.4) Gecko/20070515 Firefox/2.0.0.4"+lineEnd +
				"Content-Length: "+lengt+lineEnd +
				"Content-Type: multipart/form-data; boundary="+boundary+lineEnd+
				"Keep-Alive: 300"+lineEnd;


				// open a URL connection to the Servlet 
				URL url = new URL(urlString);	
				conn = (HttpURLConnection) url.openConnection();

				// several settings
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setUseCaches(false);
				conn.setDefaultUseCaches(false);
				//conn.setFollowRedirects(false);
				HttpURLConnection.setFollowRedirects(false);
				conn.setInstanceFollowRedirects(false);

				conn.setRequestProperty("Accept", "*/*");
				conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("Cache-Control", "no-cache");

				dos = new DataOutputStream( conn.getOutputStream() );
				dos.writeBytes(contents+PostData);
				dos.flush();

				// get response
				inStream = new DataInputStream ( conn.getInputStream() );

				int loc = conn.getResponseCode();//.getHeaderField("Location");
				System.out.println("+++++++++++++++++++++++++++" + loc);

				if (loc == 301)
				{

					String locc = conn.getHeaderField("Location");
					System.out.println("redirect performed" + locc);


					// close streams
					dos.close();
					inStream.close();
/*
					aFileWriter.write("\nredirection: "+locc);
					aFileWriter.flush();
					aFileWriter.close();
*/
					uploadLogFiles(type, locc, existingFileName);

				}else{

					//------------------ read the SERVER RESPONSE
					String str;
					String results="";
					while (( str = inStream.readLine()) != null)
					{
						//System.out.println("Server response is: "+str+"\n");

						results+=str;
					}
/*
					aFileWriter.write("\nServer response is: "+results);
					aFileWriter.flush();
					aFileWriter.close();
*/
					// close streams
					dos.close();
					inStream.close();

				}

			}
			catch (MalformedURLException ex)
			{
				ex.printStackTrace();
			}
			catch (IOException ioex)
			{
				ioex.printStackTrace();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}

	}
}
