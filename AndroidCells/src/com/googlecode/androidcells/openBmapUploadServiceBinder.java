package com.googlecode.androidcells;

import android.os.RemoteException;

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
public class openBmapUploadServiceBinder extends openBmapUploadServiceInterface.Stub {

	private openBmapUploadService service = null;


	public openBmapUploadServiceBinder(openBmapUploadService aservice) {
		super(); 
		this.service = aservice; 
	}

	@Override
	public void registerCallback(openBmapUploadServiceInterfaceCallback callback_)
	throws RemoteException {
		if(callback_ != null){ 
			service.getCallbacks().register(callback_);
		}
	}

	@Override
	public void unregisterCallback(openBmapUploadServiceInterfaceCallback callback_)
	throws RemoteException {
		if(callback_ != null){ 
			service.getCallbacks().unregister(callback_);
		}
	}

	@Override
	public void uploadCellularFiles() throws RemoteException {
		// TODO Auto-generated method stub
		service.uploadCellularFiles();
	}

	@Override
	public void uploadWifiFiles() throws RemoteException {
		// TODO Auto-generated method stub
		service.uploadWifiFiles();
	}


}
