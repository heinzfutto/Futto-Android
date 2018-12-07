package org.futto.app.listeners;

import java.util.List;

import org.futto.app.storage.EncryptionEngine;
import org.futto.app.storage.TextFileManager;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

/**WifiListener
 * WifiListener houses a single public function, scanWifi.  This function grabs the mac
 * addresses of local wifi beacons and writes them to the wifiLog.  It only gets the data
 * if wifi is enabled.
 * @author Eli */
public class WifiListener {
	private static WifiManager wifiManager;
	public static String header = "hashed MAC, frequency, RSSI";
	
	/** WifiListener requires an application context in order to access 
	 * the devices wifi info.  
	 * @param appContext requires a Context */
	private WifiListener (Context appContext) {
		wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE); }
	
	public static void initialize( Context context ) { new WifiListener( context ); } 
	
	//#######################################################################################
	//#############################  WIFI STATE #############################################
	//#######################################################################################

	/** Writes to the wifiLog file all mac addresses of local wifi beacons. */
	public static void scanWifi() {
		if ( wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED ) {
			List<ScanResult> scanResults = wifiManager.getScanResults();
			if (scanResults != null) {
				//we save some compute on the encryption here by dumping all the lines to print in one go.
				StringBuilder data = new StringBuilder();
				for (ScanResult result : scanResults){
					data.append( EncryptionEngine.safeHash( result.BSSID) + "," + result.frequency + "," + result.level );
					data.append("\n"); }

				// Create a new file, write the data to it, and close the file
				TextFileManager.getWifiLogFile().newFile(); //note: the file name's timestamp is actually relevant, so we always make a new file.
				TextFileManager.getWifiLogFile().writeEncrypted( data.toString() );
				TextFileManager.getWifiLogFile().closeFile();
//				Log.d("WIFI", "DONE GONE DID SCAN.");
			} // and provide a debug log statement for data researchers if we cannot get scan data right now.
		} else {
//			Log.d("WIFI", "YAP SKIPPING DUE TO DISABLEMENT.");
			TextFileManager.getDebugLogFile().writeEncrypted(System.currentTimeMillis() + " wifi is not available for scanning at this time."); }
	}
}