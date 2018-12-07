package org.futto.app;

import java.util.Calendar;

import org.futto.app.storage.PersistentData;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/** The Timer class provides a meeans of setting various timers.  These are used by the BackgroundService
 * for devices that must be turned on/off, and timing the user to automatically logout after a period of time.
 * This class includes all the Intents and IntentFilters we for trigged broadcasts.
 * @author Eli, Dor */
public class Timer {
	private AlarmManager alarmManager;
	private Context appContext;
	
	public static final long ONE_DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000L;
	public static final long ONE_WEEK_IN_MILLISECONDS = 7 * ONE_DAY_IN_MILLISECONDS;
	
	// Control Message Intents
	public static Intent accelerometerOffIntent;
	public static Intent accelerometerOnIntent;
	public static Intent bluetoothOffIntent;
	public static Intent bluetoothOnIntent;
	public static Intent dailySurveyIntent;
	public static Intent gpsOffIntent;
	public static Intent gpsOnIntent;
	public static Intent signoutIntent;
	public static Intent voiceRecordingIntent;
	public static Intent weeklySurveyIntent;
	public static Intent wifiLogIntent;
	public static Intent uploadDatafilesIntent;
	public static Intent createNewDataFilesIntent;
	public static Intent checkForNewSurveysIntent;
	public static Intent checkForNewSetting;

	// Constructor
	public Timer( BackgroundService backgroundService ) {
		appContext = backgroundService.getApplicationContext();
		alarmManager = (AlarmManager)( backgroundService.getSystemService( Context.ALARM_SERVICE ));
		
		// double alarm intents
		accelerometerOffIntent = setupIntent( appContext.getString(R.string.turn_accelerometer_off) );
		accelerometerOnIntent = setupIntent( appContext.getString(R.string.turn_accelerometer_on) );
		bluetoothOffIntent = setupIntent( appContext.getString(R.string.turn_bluetooth_off) );
		bluetoothOnIntent = setupIntent( appContext.getString(R.string.turn_bluetooth_on) );
		gpsOffIntent = setupIntent( appContext.getString(R.string.turn_gps_off) );
		gpsOnIntent = setupIntent( appContext.getString(R.string.turn_gps_on) );
		
		// Set up event triggering alarm intents
		signoutIntent = setupIntent( appContext.getString(R.string.signout_intent) );
		wifiLogIntent = setupIntent( appContext.getString(R.string.run_wifi_log) );
		uploadDatafilesIntent = setupIntent( appContext.getString(R.string.upload_data_files_intent) );
		createNewDataFilesIntent = setupIntent( appContext.getString(R.string.create_new_data_files_intent) );
		checkForNewSurveysIntent = setupIntent( appContext.getString(R.string.check_for_new_surveys_intent) );
		checkForNewSetting = setupIntent( appContext.getString(R.string.check_for_new_settings_intent) );
	}
	
	/* ######################################################################
	 * ############################ Common Code #############################
	 * ####################################################################*/
	
	// Setup custom intents to be sent to the listeners running in the background service
	private static Intent setupIntent( String action ){
		Intent newIntent = new Intent();
		newIntent.setAction( action );
		return newIntent;
	}
	
	/* ###############################################################################################
	 * ############################ The Various Types of Alarms Creation #############################
	 * #############################################################################################*/
	
	/** Single exact alarm for an event that happens once.
	 * @return a long of the system time in milliseconds that the alarm was set for. */
	public Long setupExactSingleAlarm(Long milliseconds, Intent intentToBeBroadcast) {
		Long triggerTime = System.currentTimeMillis() + milliseconds;
		PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intentToBeBroadcast, 0);
		setExactAlarm(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
		return triggerTime;
	}
	
	/** setupExactTimeAlarm creates an Exact Alarm that will go off at a specific time within a
	 * period, e.g. every hour (period), at 47 minutes past the hour (start time within period).
	 * setupExactTimeAlarm is used for the Bluetooth timer, so that every device that has this app
	 * turns on its Bluetooth at the same moment. */
	public void setupExactSingleAbsoluteTimeAlarm(long period, long startTimeInPeriod, Intent intentToBeBroadcast) {
		long currentTime = System.currentTimeMillis();
		// current unix time (mod) 3,600,000 milliseconds = the next hour-boundry, to which we add the EXACT_REPEAT_TIMER_OFFSET.
		Long nextTriggerTime = currentTime - ( currentTime % period ) + startTimeInPeriod;
		if (nextTriggerTime < currentTime) { nextTriggerTime += period; }
		PendingIntent pendingTimerIntent = PendingIntent.getBroadcast(appContext, 0, intentToBeBroadcast, 0);
		setExactAlarm(AlarmManager.RTC_WAKEUP, nextTriggerTime, pendingTimerIntent);
	}
	
	public void startSurveyAlarm(String surveyId, Calendar alarmTime){
		Intent intentToBeBroadcast = new Intent(surveyId);
		// Log.d("timer", "action: " + intentToBeBroadcast.getAction() );
		setupSurveyAlarm(surveyId, intentToBeBroadcast, alarmTime);
	}
	
	/**Takes a specially prepared intent and sets it to go off at the day and time provided
	 * @param intentToBeBroadcast an intent that has been prepared by the startWeeklyAlarm function.*/
	public void setupSurveyAlarm(String surveyId, Intent intentToBeBroadcast, Calendar alarmTime) {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intentToBeBroadcast, 0);
		long nextTriggerTime = alarmTime.getTimeInMillis();
//		triggerAtMillis = System.currentTimeMillis() + 15000; //hax, debug code.
//		long timeTillFire = nextTriggerTime - System.currentTimeMillis();
		// Log.i("Timer.java", "next alarm triggers in = " + timeTillFire / 1000 + " seconds.");
		setExactAlarm(AlarmManager.RTC_WAKEUP, nextTriggerTime, pendingIntent);
		PersistentData.setMostRecentSurveyAlarmTime(surveyId, nextTriggerTime);
	}
	
	
	/* ##################################################################################
	 * ############################ Other Utility Functions #############################
	 * ################################################################################*/	
	
	/** In API 19 and above, alarms are inexact (to save power).  In API 18 and
	 *  below, alarms are exact.
	 *  This function checks the phone's operating system's API version and
	 *  returns TRUE if alarms are exact in this version (i.e., if it's API 18
	 *  or below), and returns FALSE if alarms are inexact.  */
	public static Boolean alarmsAreExactInThisApiVersion() {
		int currentApiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentApiVersion < android.os.Build.VERSION_CODES.KITKAT) {
			return true; }
		else { return false; }
	}
	
	/** Calls AlarmManager.set() for API < 19, and AlarmManager.setExact() for API 19+
	 * For an exact alarm, it seems you need to use .set() for API 18 and below, and
	 * .setExact() for API 19 (KitKat) and above. */
	private void setExactAlarm(int type, long triggerAtMillis, PendingIntent operation) {
		if (alarmsAreExactInThisApiVersion()) {			
			alarmManager.set(type, triggerAtMillis, operation); }
		else { alarmManager.setExact(type, triggerAtMillis, operation); }
	}
	
	/**Cancels an alarm, does not return any info about whether the alarm existed.
	 * @param intentToBeBroadcast an Intent identifying the alarm to cancel. */
	public void cancelAlarm(Intent intentToBeBroadcast) {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intentToBeBroadcast, 0);
		alarmManager.cancel(pendingIntent);
	}
		
	/**Checks if an alarm is set.
	 * @param intent an Intent identifying the alarm to check.
	 * @return Returns TRUE if there is an alarm set matching that intent; otherwise false. */
	public Boolean alarmIsSet(Intent intent) {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(appContext, 0, intent, PendingIntent.FLAG_NO_CREATE);
		if (pendingIntent == null) { return false; }
		else { return true; }
	}
}