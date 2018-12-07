package org.futto.app.listeners;

import org.futto.app.storage.TextFileManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

/** Listens for power state changes.
 *  Screen On/Off, Power Connect/Disconnect, Airplane Mode.
 *  @author Josh Zagorsky, Eli Jones, May/June 2014 */
public class PowerStateListener extends BroadcastReceiver {
	//The Power State Manager can receive broadcasts before the app is even running.
	// This would cause a a crash because we need the TextFileManager to be available.
	// The started variable is set to true during the startup process for the app.
	private static Boolean started = false;
	
	private static PowerManager powerManager;
	public static void start(Context context){
		started = true;
		powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
	}
	
	public static String header = "timestamp, event";	
	
	/** Handles the logging, includes a new line for the CSV files.
	 * This code is otherwise reused everywhere.*/
	private void makeLogStatement(String message) {
		Log.i("PowerStateListener", message);
		Long javaTimeCode = System.currentTimeMillis();
		TextFileManager.getPowerStateFile().writeEncrypted(javaTimeCode.toString() + TextFileManager.DELIMITER + message );
	}
	
	
	@Override
	public void onReceive(Context externalContext, Intent intent) {
		if (!started) { return; }
		String action = intent.getAction();

		// Screen on/off
		if (action.equals(Intent.ACTION_SCREEN_OFF)) { makeLogStatement("Screen turned off"); }
		if (action.equals(Intent.ACTION_SCREEN_ON)) { makeLogStatement("Screen turned on"); }
		
		// Power connected/disconnected
		if (action.equals(Intent.ACTION_POWER_CONNECTED)) { makeLogStatement("Power connected"); }
		if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) { makeLogStatement("Power disconnected"); }
		
		// Shutdown/Restart
		if (action.equals(Intent.ACTION_SHUTDOWN)) { makeLogStatement("Device shut down signal received"); }
		if (action.equals(Intent.ACTION_REBOOT)) { makeLogStatement("Device reboot signal received"); }
		
		//android 5.0+  Power save mode is a low-battery state where android turns off battery draining features.
		if ( android.os.Build.VERSION.SDK_INT >= 21 && action.equals(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED) ) {
			if (powerManager.isPowerSaveMode()) {
				makeLogStatement("Power Save Mode state change signal received; device in power save state."); }
			else { makeLogStatement("Power Save Mode change signal received; device not in power save state."); }
		}
		
		//andoird 6.0+.  This indicates that Doze mode has been entered
		if ( android.os.Build.VERSION.SDK_INT >= 23 && action.equals(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED) ) {
			if (powerManager.isDeviceIdleMode()) {
				makeLogStatement("Device Idle (Doze) state change signal received; device in idle state."); }
			else { makeLogStatement("Device Idle (Doze) state change signal received; device not in idle state."); }				
			Log.d("device idle state", "" + powerManager.isDeviceIdleMode() );
		}
	}
}