/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/murphy/data/android/workspace/AndroidCells/src/com/googlecode/androidcells/LogServiceInterface.aidl
 */
package com.googlecode.androidcells;
public interface LogServiceInterface extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.googlecode.androidcells.LogServiceInterface
{
private static final java.lang.String DESCRIPTOR = "com.googlecode.androidcells.LogServiceInterface";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.googlecode.androidcells.LogServiceInterface interface,
 * generating a proxy if needed.
 */
public static com.googlecode.androidcells.LogServiceInterface asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.googlecode.androidcells.LogServiceInterface))) {
return ((com.googlecode.androidcells.LogServiceInterface)iin);
}
return new com.googlecode.androidcells.LogServiceInterface.Stub.Proxy(obj);
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
case TRANSACTION_getProviderInfos:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getProviderInfos();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_nbGpsLocations:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.nbGpsLocations();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_nbCellLocations:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.nbCellLocations();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_nbNeighborsLocations:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.nbNeighborsLocations();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_nbWifiLocations:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.nbWifiLocations();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.googlecode.androidcells.LogServiceInterface
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
public java.lang.String getProviderInfos() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getProviderInfos, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int nbGpsLocations() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_nbGpsLocations, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int nbCellLocations() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_nbCellLocations, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int nbNeighborsLocations() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_nbNeighborsLocations, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int nbWifiLocations() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_nbWifiLocations, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getProviderInfos = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_nbGpsLocations = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_nbCellLocations = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_nbNeighborsLocations = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_nbWifiLocations = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
}
public java.lang.String getProviderInfos() throws android.os.RemoteException;
public int nbGpsLocations() throws android.os.RemoteException;
public int nbCellLocations() throws android.os.RemoteException;
public int nbNeighborsLocations() throws android.os.RemoteException;
public int nbWifiLocations() throws android.os.RemoteException;
}
