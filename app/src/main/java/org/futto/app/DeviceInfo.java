package org.futto.app;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;

import org.futto.app.storage.EncryptionEngine;

/**This is a class that NEEDS to be instantiated in the background service. In order to get the Android ID, the class needs
 * Context. Once instantiated, the class assigns two variables for AndroidID and BluetoothMAC. Once they are instantiated,
 * they can be called from different classes to be used. They are hashed when they are called.
 *
 * The class is used to grab unique ID data, and pass it to the server. The data is used while authenticating users
 *
 * @author Dor Samet, Eli Jones */

public class DeviceInfo {
	/* TODO:  Ensure this number is updated whenever a version of the app is pushed to the website for any reason.
	 * Don't forget to update the version in the android manifest, only the version string is visible to the user.
	 * Version history:
	 * 1: add additional device data during the registration process, including this version number.
	 * 2: added sms debugging
	 * 3: added universal crash log to the app
	 * 4: added a lot of CrashHandler integration to all sorts of error-handled conditions the codebase.
	 * 5: Initial version of Android 6.
	 * 		Enhanced Audio UI rewrite included,raw recording code is functional but not enabled, no standard on parsing audio surveys for their type.
	 * 6: second version of Android 6.
	 * 		Enhanced audio functionality is included and functional, fixed crash that came up on a device when the app was uninstalled and then reinstalled?
	 * 		A functionally identical 6 was later released, it contains a new error message for a new type of iOS-related registration error.
	 * 7: audio survey files now contain the surveyid for the correct audio survey.
	 * 8: Now support a flag for data upload over cellular, fixed bug in wifi scanning.
	 * 9: Moves Record/Play buttons outside the scroll window on voice recording screen.
	 * 10: app version 2.0, Change to TextFileManager to potentially improve uninitialized errors, added device idle and low power mode change to power state listener.
	 * 11: development version of 12.
	 * 12: app version 2.1, several bug fixes including a fix for the on-opening-surveyactivity crash and a crash that could occur when when encrypting audio files. Adds support for branching serveys (conditional display logic)
	 * 13: app version 2.1.1, data improvements to GPS and WIFI data streams, improvements in said data streams and additional context provided in the app log (debug log).
	 * 14: app version 2.1.2, minor behavior improvement in extremely rare occurrence inside of sessionActivity, see inline documentation there for details.
	 * 15: bug fix in crash handler
	 * 16: app version 2.1.3, rewrite of file uploading to fix App Not Responding (ANR) errors; also BackgroundService.onStartCommand() now uses START_REDELIVER_INTENT
	 * 17: app version 2.1.4, fixed a bug that still showed the next survey, even if that survey time had been deleted in the backend and the update had propagated to the phone
	 * 18: app version 2.1.5, fixed bugs with recording received SMS messages and sent MMS messages
	 * 19: app version 2.2.0, enabled app to point to any server URL; improved Registration and Password Reset interfaces and networking.
	 * 20: app version 2.2.1, updated text on Registration screens
	 * 21: app version 2.2.2, updates styles, restores persistent, individual survey notifications in Android 7
	 * 22: app version 2.2.3, improves error messages
	 * 23: app version 2.2.4, OnnelaLabServer version and GooglePlayStore version (with customizable URL) have different names (Beiwe vs. Beiwe2)
	 * 24: app version 2.2.5, handle null Bluetooth MAC Address in Android 8.0 and above
	 * 25: app version 2.2.6, fix crash on opening app from audio survey notification when AppContext is null
	 * 26: app version 2.2.7, Added Sentry */

	private static String androidID;
	private static String bluetoothMAC;
	private static Context context;

	/** grab the Android ID and the Bluetooth's MAC address */
	@SuppressLint("HardwareIds")
	public static void initialize(Context appContext) {
		context = appContext;
		androidID = Settings.Secure.getString(appContext.getContentResolver(), Settings.Secure.ANDROID_ID);
		
		/* If the BluetoothAdapter is null, or if the BluetoothAdapter.getAddress() returns null,
		 * record an empty string for the Bluetooth MAC Address.
		 * The Bluetooth MAC Address is always empty in Android 8.0 and above, because the app needs
		 * the LOCAL_MAC_ADDRESS permission, which is a system permission that it's not allowed to
		 * have:
		 * https://android-developers.googleblog.com/2017/04/changes-to-device-identifiers-in.html
		 * The Bluetooth MAC Address is also sometimes empty on Android 7 and lower. */
		if (android.os.Build.VERSION.SDK_INT >= 23) { //This will not work on all devices: http://stackoverflow.com/questions/33377982/get-bluetooth-local-mac-address-in-marshmallow
			String bluetoothAddress = Settings.Secure.getString(appContext.getContentResolver(), "bluetooth_address");
			if (bluetoothAddress == null) {
				bluetoothAddress = "";
			}
			bluetoothMAC = EncryptionEngine.safeHash(bluetoothAddress);
		} else { //Android before version 6
			BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (bluetoothAdapter == null || bluetoothAdapter.getAddress() == null) {
				bluetoothMAC = "";
			} else {
				bluetoothMAC = bluetoothAdapter.getAddress();
			}
		}
	}

	public static String getBeiweVersion() {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(),0);
			return String.valueOf(info.versionCode) + "-" + BuildConfig.BUILD_TYPE;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return "unknown";
		}
	}
	public static String getAndroidVersion() { return android.os.Build.VERSION.RELEASE; }
	public static String getProduct() { return android.os.Build.PRODUCT; }
	public static String getBrand() { return android.os.Build.BRAND; }
	public static String getHardwareId() { return android.os.Build.HARDWARE; }
	public static String getManufacturer() { return android.os.Build.MANUFACTURER; }
	public static String getModel() { return android.os.Build.MODEL; }
	public static String getAndroidID() { return EncryptionEngine.safeHash(androidID); }
	public static String getBluetoothMAC() { return EncryptionEngine.safeHash(bluetoothMAC); }
}