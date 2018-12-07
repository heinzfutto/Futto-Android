package org.futto.app.networking;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;

import org.futto.app.storage.PersistentData;


/** Contains a single function to check whether wifi is active and functional.
 * @author Eli Jones, Joshua Zagorsky */

//TODO: Eli. low priority. Investigate the class local option that android studio is prompting for
public class NetworkUtility {
	
	/**Return TRUE if WiFi is connected; FALSE otherwise.
	 * Android 6 adds support for multiple network connections of the same type and the older
	 * get-network-by-type command is deprecated.
	 * We need to handle both cases.
	 * @return boolean value of whether the wifi is on and network connectivity is available. */
	public static boolean canUpload(Context appContext) {
		// If you're allowed to upload over cellular data, simply check whether the phone's
		// connected to the internet at all.
		if (PersistentData.getAllowUploadOverCellularData()) {
			Log.i("WIFICHECK", "ALLOW OVER CELLULAR!!!!");
			if (networkIsAvailable(appContext)) return true; }

		// If you're only allowed to upload over WiFi, or if the simple networkIsAvailable() check
		// returned false, check if a WiFi network is connected.
		ConnectivityManager connManager =
				(ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		// Check if WiFi is connected for Android version 5 and below
		if (android.os.Build.VERSION.SDK_INT < 23) { return oldTimeyWiFiConnectivityCheck(connManager); }
		// Check if WiFi is connected for Android version 6 and above
		return newFangledWiFiConnectivityCheck(connManager);
	}
	

	public static boolean networkIsAvailable(Context appContext) {
		ConnectivityManager connManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
		return (activeNetwork != null) && (activeNetwork.isConnected()) && (activeNetwork.isAvailable());
	}


	@SuppressWarnings("deprecation")
	/** This is the function for running pre-Android 6 wifi connectivity checks.
	 *  This code is separated so that the @SuppressWarnings("deprecation") decorator 
	 *  does not cause headaches if something else is deprecated in the future. */
	private static boolean oldTimeyWiFiConnectivityCheck( ConnectivityManager connManager ){
		Log.i("WIFICHECK", "oldTimeyWiFiConnectivityCheck");
		NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo networkInfo_mobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		return (networkInfo_mobile.isConnected() || networkInfo.isConnected()) && networkInfo.isAvailable();
	}
	
	
	/** This is the function for running Android 6+ wifi connectivity checks. */
	private static boolean newFangledWiFiConnectivityCheck( ConnectivityManager connManager ){
		Log.i("WIFICHECK", "newFangledWiFiConnectivityCheck");
		Network[] networks = connManager.getAllNetworks();
		if (networks == null) { //No network connectivity at all,
			return false; }     //so return no connectivity.
		
		for (Network network : networks ) {
			NetworkInfo networkInfo = connManager.getNetworkInfo(network);
			if ((networkInfo.getType() == ConnectivityManager.TYPE_MOBILE || networkInfo.getType() == ConnectivityManager.TYPE_WIFI )&& networkInfo.isConnected() && networkInfo.isAvailable() ) {
				return true; } //return true if there is a connected and available wifi connection! 
		}
		return false; //there were no wifi-type network connections active and available, return false.
	}
	
}

