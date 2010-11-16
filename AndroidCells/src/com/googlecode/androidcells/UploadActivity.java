package com.googlecode.androidcells;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.TextView;
import android.widget.Toast;

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
public class UploadActivity extends Activity {

	private TextView nbCellularFilesToSend;
	private int nbCellularFiles;

	private TextView nbWifiFilesToSend;
	private int nbWifiFiles;

	private Intent openBmapUploadSvc;
	
	private boolean remoteIsBound = false;




	private openBmapUploadServiceInterface uploadInterface;

	private ServiceConnection remoteConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			uploadInterface = openBmapUploadServiceInterface.Stub.asInterface(service);
			try {
				remoteIsBound = true;
				uploadInterface.registerCallback(callback); // initialize callback
				uploadInterface.uploadCellularFiles();
				uploadInterface.uploadWifiFiles();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			uploadInterface = null;		
			remoteIsBound = false;
		}
	};

	private final openBmapUploadServiceInterfaceCallback.Stub callback = new openBmapUploadServiceInterfaceCallback.Stub() {
		@Override
		public void nbCellularFilesChanged(int nb) throws RemoteException {
			try {
				nbCellularFiles = nb;
				updateInfosUI();
			}catch(Exception e){
				e.printStackTrace();
				e.printStackTrace();
			}

		}

		@Override
		public void nbWifiFilesChanged(int nb) throws RemoteException {
			try {
				nbWifiFiles = nb;
				updateInfosUI();
			}catch(Exception e){
				e.printStackTrace();
				e.printStackTrace();
			}
		}
	};


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload);
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		nbCellularFilesToSend = (TextView) findViewById(R.id.nbCellularFilesToSend);
		nbCellularFilesToSend.setText("before");
		nbWifiFilesToSend = (TextView) findViewById(R.id.nbWifiFilesToSend);
		nbWifiFilesToSend.setText("before");


	}

	@Override
	public void onResume() {
		super.onResume();

		openBmapUploadSvc = new Intent(this, openBmapUploadService.class);
		
		startService(openBmapUploadSvc);
		bindService(openBmapUploadSvc, remoteConnection, BIND_AUTO_CREATE);

	}

	@Override
	public void onPause() {
	    super.onPause();
	    if (remoteIsBound) {
	        // Detach our existing connection.
	        unbindService(remoteConnection);
	        remoteIsBound = false;
	    }
	 // Tell the user we stopped.
        Toast.makeText(this, "Unbind done", Toast.LENGTH_SHORT).show();

	}
	
	
	void doUnbindService() {
	}
	
	
	private synchronized void updateInfosUI() {
		//long time=SystemClock.uptimeMillis();
		new updateInfosUITask().execute();
		//Log.v(TAG,"UPDATE_1="+ (float)(SystemClock.uptimeMillis()-time)/1000 );
	}

	private class updateInfosUITask extends AsyncTask<Integer, Integer, String[]> {
		//long time=SystemClock.uptimeMillis();
		@Override
		protected String[] doInBackground(Integer... params) {
			String[] valeurs = {"","","","",""};
			try {
				valeurs[0] = ""+nbCellularFiles;
				valeurs[1] = ""+nbWifiFiles;
			} catch (Exception e) {}
			return valeurs;
		}
		@Override
		protected void onPostExecute(String[] result) {
			nbCellularFilesToSend.setText(result[0]);
			nbWifiFilesToSend.setText(result[1]);
		}
	}

}
