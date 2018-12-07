package org.futto.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.PowerManager;

import org.futto.app.storage.PersistentData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PermissionHandler {
	public static final int PERMISSION_GRANTED = PackageManager.PERMISSION_GRANTED;
	public static int PERMISSION_DENIED = PackageManager.PERMISSION_DENIED;
	public static String POWER_EXCEPTION_PERMISSION = "POWER_EXCEPTION_PERMISSION";
	
	public static Map <String, Integer> permissionMap = new HashMap <String, Integer> ();	  
	static {permissionMap.put( Manifest.permission.ACCESS_FINE_LOCATION, 1 );
			permissionMap.put( Manifest.permission.ACCESS_NETWORK_STATE, 2 );
			permissionMap.put( Manifest.permission.ACCESS_WIFI_STATE, 3 );
			permissionMap.put( Manifest.permission.BLUETOOTH, 5 );
			permissionMap.put( Manifest.permission.BLUETOOTH_ADMIN, 6 );
			permissionMap.put( Manifest.permission.INTERNET, 9 );
			permissionMap.put( Manifest.permission.READ_PHONE_STATE, 12 );
			permissionMap.put( Manifest.permission.RECEIVE_BOOT_COMPLETED, 13 );
			permissionMap.put( Manifest.permission.RECORD_AUDIO, 14 );
			permissionMap = Collections.unmodifiableMap(permissionMap); }
	
	private static Map <String, String> permissionMessages = new HashMap <String, String> ();
	static {permissionMessages.put( Manifest.permission.ACCESS_FINE_LOCATION, "use Location Services." );
			permissionMessages.put( Manifest.permission.ACCESS_NETWORK_STATE, "view your Network State." );
			permissionMessages.put( Manifest.permission.ACCESS_WIFI_STATE, "view your Wifi State." );
			permissionMessages.put( Manifest.permission.BLUETOOTH, "use Bluetooth." );
			permissionMessages.put( Manifest.permission.BLUETOOTH_ADMIN, "use Bluetooth." );
			permissionMessages.put( Manifest.permission.INTERNET, "access The Internet." );
			permissionMessages.put( Manifest.permission.READ_PHONE_STATE, "access your Phone service." );
			permissionMessages.put( Manifest.permission.RECEIVE_BOOT_COMPLETED, "start up on Boot." );
			permissionMessages.put( Manifest.permission.RECORD_AUDIO, "access your Microphone." );
			permissionMessages = Collections.unmodifiableMap(permissionMessages); }
	
	public static String getNormalPermissionMessage(String permission) {
		return String.format("For this study Beiwe needs permission to %s Please press allow on the following permissions request.", permissionMessages.get(permission) );
	}
	public static String getBumpingPermissionMessage(String permission) {
		return String.format("To be fully enrolled in this study Beiwe needs permission to %s You appear to have denied or removed this permission. Beiwe will now bump you to its settings page so you can manually enable it.", permissionMessages.get(permission) );
	}
	
	/* The following are enabled by default.
	 *  AccessNetworkState
	 *  AccessWifiState
	 *  Bluetooth
	 *  BluetoothAdmin
	 *  Internet
	 *  ReceiveBootCompleted
	 * 
	 * the following are enabled on the registration screen
	 *  ReadSms
	 *  ReceiveMms
	 *  ReceiveSms
	 * 
	 * This leaves the following to be prompted for at session activity start
	 *  AccessFineLocation - GPS
	 *  CallPhone - Calls
	 *  ReadCallLog - Calls
	 *  ReadContacts - Calls and SMS
	 *  ReadPhoneState - Calls
	 *  WriteCallLog - Calls
	 *  
	 *  We will check for microphone recording as a special condition on the audio recording screen. */
	public static Boolean checkAccessCoarseLocation(Context context) { if ( android.os.Build.VERSION.SDK_INT >= 23) { return context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED; } else { return true; } }
	public static Boolean checkAccessFineLocation(Context context) { if ( android.os.Build.VERSION.SDK_INT >= 23) { return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED; } else { return true; } }
	public static Boolean checkAccessNetworkState(Context context) { if ( android.os.Build.VERSION.SDK_INT >= 23) { return context.checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) == PERMISSION_GRANTED; } else { return true; } }
	public static Boolean checkAccessWifiState(Context context) { if ( android.os.Build.VERSION.SDK_INT >= 23) { return context.checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) == PERMISSION_GRANTED; } else { return true; } }
	public static Boolean checkAccessBluetooth(Context context) { if ( android.os.Build.VERSION.SDK_INT >= 23) { return context.checkSelfPermission(Manifest.permission.BLUETOOTH) == PERMISSION_GRANTED; } else { return true; } }
	public static Boolean checkAccessBluetoothAdmin(Context context) { if ( android.os.Build.VERSION.SDK_INT >= 23) { return context.checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PERMISSION_GRANTED; } else { return true; } }
	public static Boolean checkAccessReadCallLog(Context context) { if ( android.os.Build.VERSION.SDK_INT >= 23) { return context.checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PERMISSION_GRANTED; } else { return true; } }
	public static Boolean checkAccessReadContacts(Context context) { if ( android.os.Build.VERSION.SDK_INT >= 23) { return context.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PERMISSION_GRANTED; } else { return true; } }
	public static Boolean checkAccessReadPhoneState(Context context) { if ( android.os.Build.VERSION.SDK_INT >= 23) { return context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PERMISSION_GRANTED; } else { return true; } }
	public static Boolean checkAccessReadSms(Context context) { if ( android.os.Build.VERSION.SDK_INT >= 23) { return context.checkSelfPermission(Manifest.permission.READ_SMS) == PERMISSION_GRANTED; } else { return true; } }
	public static Boolean checkAccessReceiveMms(Context context) { if ( android.os.Build.VERSION.SDK_INT >= 23) { return context.checkSelfPermission(Manifest.permission.RECEIVE_MMS) == PERMISSION_GRANTED; } else { return true; } }
	public static Boolean checkAccessReceiveSms(Context context) { if ( android.os.Build.VERSION.SDK_INT >= 23) { return context.checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PERMISSION_GRANTED; } else { return true; } }
	public static Boolean checkAccessRecordAudio(Context context) { if ( android.os.Build.VERSION.SDK_INT >= 23) { return context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PERMISSION_GRANTED;} else { return true; } }
	
	public static boolean checkGpsPermissions( Context context ) { return ( checkAccessFineLocation(context) ); }
	public static boolean checkTextsPermissions( Context context ) { return ( checkAccessReadContacts(context) && checkAccessReadSms(context) && checkAccessReceiveMms(context) && checkAccessReceiveSms(context) ); }
	// TODO: for unknown reasons, at some point in the past, the checkwifipermissions function included checkAccessCoarseLocation. This has been removed and tested on chris' android 6.0 phone and the nexus 7 tablet and does not appear to be necessary.
	// public static boolean checkWifiPermissions( Context context ) { return ( checkAccessWifiState(context) && checkAccessNetworkState(context) && checkAccessCoarseLocation(context) ) ; }
	public static boolean checkWifiPermissions( Context context ) { return ( checkAccessWifiState(context) && checkAccessNetworkState(context)) ; }
	public static boolean checkBluetoothPermissions( Context context ) { return ( checkAccessBluetooth(context) && checkAccessBluetoothAdmin(context)); }

	public static boolean confirmWifi( Context context ) { return ( PersistentData.getWifiEnabled() && checkWifiPermissions(context) && checkAccessFineLocation(context) && checkAccessCoarseLocation(context) ) ; }
	public static boolean confirmBluetooth( Context context ) { return ( PersistentData.getBluetoothEnabled() && checkBluetoothPermissions(context)); }
	
	public static String getNextPermission(Context context, Boolean includeRecording) {
		if (PersistentData.getGpsEnabled()) {
			if ( !checkAccessFineLocation(context) ) { return Manifest.permission.ACCESS_FINE_LOCATION; } }
		if (PersistentData.getWifiEnabled()) {
			if ( !checkAccessWifiState(context)) return Manifest.permission.ACCESS_WIFI_STATE;
			if ( !checkAccessNetworkState(context)) return Manifest.permission.ACCESS_NETWORK_STATE;
			if ( !checkAccessCoarseLocation(context)) return Manifest.permission.ACCESS_COARSE_LOCATION;
			if ( !checkAccessFineLocation(context)) return Manifest.permission.ACCESS_FINE_LOCATION; }
		if (includeRecording) {
			if ( !checkAccessRecordAudio(context)) { return Manifest.permission.RECORD_AUDIO; } }


		if ( android.os.Build.VERSION.SDK_INT >= 23 ) {
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			if (! pm.isIgnoringBatteryOptimizations(context.getPackageName()) ) {
				return POWER_EXCEPTION_PERMISSION;
			}
		}
		return null;
	}
}