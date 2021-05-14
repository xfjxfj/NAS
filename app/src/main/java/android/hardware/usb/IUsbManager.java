package android.hardware.usb;

/**
 * Created by レインマン on 2021/05/13 19:33 with Android Studio.
 */
public interface IUsbManager extends android.os.IInterface {

	/**
	 * Local-side IPC implementation stub class.
	 */
	abstract class Stub extends android.os.Binder implements android.hardware.usb.IUsbManager {
		/**
		 * Construct the stub at attach it to the interface.
		 */
		public Stub() {
			throw new RuntimeException("Stub!");
		}

		/**
		 * Cast an IBinder object into an android.hardware.usb.IUsbManager interface,
		 * generating a proxy if needed.
		 */
		public static android.hardware.usb.IUsbManager asInterface(android.os.IBinder obj) {
			throw new RuntimeException("Stub!");
		}

		public android.os.IBinder asBinder() {
			throw new RuntimeException("Stub!");
		}

		public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException {
			throw new RuntimeException("Stub!");
		}

		static final int TRANSACTION_getDeviceList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
		static final int TRANSACTION_openDevice = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
		static final int TRANSACTION_getCurrentAccessory = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
		static final int TRANSACTION_openAccessory = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
		static final int TRANSACTION_setDevicePackage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
		static final int TRANSACTION_setAccessoryPackage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
		static final int TRANSACTION_hasDevicePermission = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
		static final int TRANSACTION_hasAccessoryPermission = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
		static final int TRANSACTION_requestDevicePermission = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
		static final int TRANSACTION_requestAccessoryPermission = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
		static final int TRANSACTION_grantDevicePermission = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
		static final int TRANSACTION_grantAccessoryPermission = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
		static final int TRANSACTION_hasDefaults = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
		static final int TRANSACTION_clearDefaults = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
		static final int TRANSACTION_setCurrentFunction = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
		static final int TRANSACTION_setMassStorageBackingFile = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
	}

	/* Returns a list of all currently attached USB devices */
	void getDeviceList(android.os.Bundle devices) throws android.os.RemoteException;

	/* Returns a file descriptor for communicating with the USB device.
	 * The native fd can be passed to usb_device_new() in libusbhost.
	 */
	android.os.ParcelFileDescriptor openDevice(java.lang.String deviceName) throws android.os.RemoteException;

	/* Returns the currently attached USB accessory */
	android.hardware.usb.UsbAccessory getCurrentAccessory() throws android.os.RemoteException;

	/* Returns a file descriptor for communicating with the USB accessory.
	 * This file descriptor can be used with standard Java file operations.
	 */
	android.os.ParcelFileDescriptor openAccessory(android.hardware.usb.UsbAccessory accessory) throws android.os.RemoteException;

	/* Sets the default package for a USB device
	 * (or clears it if the package name is null)
	 */
	void setDevicePackage(android.hardware.usb.UsbDevice device, java.lang.String packageName) throws android.os.RemoteException;

	/* Sets the default package for a USB accessory
	 * (or clears it if the package name is null)
	 */
	void setAccessoryPackage(android.hardware.usb.UsbAccessory accessory, java.lang.String packageName) throws android.os.RemoteException;

	/* Returns true if the caller has permission to access the device. */
	boolean hasDevicePermission(android.hardware.usb.UsbDevice device) throws android.os.RemoteException;

	/* Returns true if the caller has permission to access the accessory. */
	boolean hasAccessoryPermission(android.hardware.usb.UsbAccessory accessory) throws android.os.RemoteException;

	/* Requests permission for the given package to access the device.
	 * Will display a system dialog to query the user if permission
	 * had not already been given.
	 */
	void requestDevicePermission(android.hardware.usb.UsbDevice device, java.lang.String packageName, android.app.PendingIntent pi) throws android.os.RemoteException;

	/* Requests permission for the given package to access the accessory.
	 * Will display a system dialog to query the user if permission
	 * had not already been given. Result is returned via pi.
	 */
	void requestAccessoryPermission(android.hardware.usb.UsbAccessory accessory, java.lang.String packageName, android.app.PendingIntent pi) throws android.os.RemoteException;

	/* Grants permission for the given UID to access the device */
	void grantDevicePermission(android.hardware.usb.UsbDevice device, int uid) throws android.os.RemoteException;

	/* Grants permission for the given UID to access the accessory */
	void grantAccessoryPermission(android.hardware.usb.UsbAccessory accessory, int uid) throws android.os.RemoteException;

	/* Returns true if the USB manager has default preferences or permissions for the package */
	boolean hasDefaults(java.lang.String packageName) throws android.os.RemoteException;

	/* Clears default preferences and permissions for the package */
	void clearDefaults(java.lang.String packageName) throws android.os.RemoteException;

	/* Sets the current USB function. */
	void setCurrentFunction(java.lang.String function, boolean makeDefault) throws android.os.RemoteException;

	/* Sets the file path for USB mass storage backing file. */
	void setMassStorageBackingFile(java.lang.String path) throws android.os.RemoteException;
}
