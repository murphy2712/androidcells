/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/murphy/Dropbox/workspaceAndroid/AndroidCells/src/com/googlecode/androidcells/LogServiceInterfaceResponse.aidl
 */
package com.googlecode.androidcells;
public interface LogServiceInterfaceResponse extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.googlecode.androidcells.LogServiceInterfaceResponse
{
private static final java.lang.String DESCRIPTOR = "com.googlecode.androidcells.LogServiceInterfaceResponse";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.googlecode.androidcells.LogServiceInterfaceResponse interface,
 * generating a proxy if needed.
 */
public static com.googlecode.androidcells.LogServiceInterfaceResponse asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.googlecode.androidcells.LogServiceInterfaceResponse))) {
return ((com.googlecode.androidcells.LogServiceInterfaceResponse)iin);
}
return new com.googlecode.androidcells.LogServiceInterfaceResponse.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_nbGpsLocation:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.nbGpsLocation(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.googlecode.androidcells.LogServiceInterfaceResponse
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public void nbGpsLocation(int nb) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(nb);
mRemote.transact(Stub.TRANSACTION_nbGpsLocation, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_nbGpsLocation = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void nbGpsLocation(int nb) throws android.os.RemoteException;
}
