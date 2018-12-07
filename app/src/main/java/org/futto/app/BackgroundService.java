package org.futto.app;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import org.futto.app.listeners.GPSListener;
import org.futto.app.listeners.PowerStateListener;
import org.futto.app.listeners.WifiListener;
import org.futto.app.networking.PostRequest;
import org.futto.app.networking.SettingUpdate;
import org.futto.app.networking.SurveyDownloader;
import org.futto.app.storage.PersistentData;
import org.futto.app.storage.TextFileManager;
import org.futto.app.survey.SurveyScheduler;
import org.futto.app.ui.user.LoginActivity;
import org.futto.app.ui.utils.SurveyNotifications;

import java.util.Calendar;
import java.util.List;

import io.sentry.Sentry;
import io.sentry.android.AndroidSentryClientFactory;
import io.sentry.dsn.InvalidDsnException;

public class BackgroundService extends Service {
	private Context appContext;
	public GPSListener gpsListener;
	public PowerStateListener powerStateListener;
	public static Timer timer;

	//localHandle is how static functions access the currently instantiated background service.
	//It is to be used ONLY to register new surveys with the running background service, because
	//that code needs to be able to update the IntentFilters associated with timerReceiver.
	//This is Really Hacky and terrible style, but it is okay because the scheduling code can only ever
	//begin to run with an already fully instantiated background service.
	private static BackgroundService localHandle;

	@Override
	/** onCreate is essentially the constructor for the service, initialize variables here. */
	public void onCreate() {
		appContext = this.getApplicationContext();
		try {
			String sentryDsn = BuildConfig.SENTRY_DSN;
			Sentry.init(sentryDsn, new AndroidSentryClientFactory(appContext));
		}
		catch (InvalidDsnException ie){
			Sentry.init(new AndroidSentryClientFactory(appContext));
		}

		Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(appContext));
		PersistentData.initialize( appContext );
		TextFileManager.initialize( appContext );
		PostRequest.initialize( appContext );
		localHandle = this;  //yes yes, hacky, I know.
		registerTimers(appContext);

		doSetup();
	}

	public void doSetup() {
		//Accelerometer and power state don't need permissons
		startPowerStateListener();
		gpsListener = new GPSListener(appContext); // Permissions are checked in the broadcast receiver
		WifiListener.initialize( appContext );

		//Bluetooth, wifi, gps, calls, and texts need permissions
		if ( PermissionHandler.confirmWifi(appContext) ) { WifiListener.initialize( appContext ); }
		if ( PersistentData.isRegistered() ) {
			DeviceInfo.initialize( appContext ); //if at registration this has already been initialized. (we don't care.)
			startTimers();
		}
	}

	/** Stops the BackgroundService instance. */
	public void stop() { if (BuildConfig.APP_IS_BETA) { this.stopSelf(); } }

	/*#############################################################################
	#########################         Starters              #######################
	#############################################################################*/

	/** Initializes the PowerStateListener.
	 * The PowerStateListener requires the ACTION_SCREEN_OFF and ACTION_SCREEN_ON intents
	 * be registered programatically. They do not work if registered in the app's manifest.
	 * Same for the ACTION_POWER_SAVE_MODE_CHANGED and ACTION_DEVICE_IDLE_MODE_CHANGED filters,
	 * though they are for monitoring deeper power state changes in 5.0 and 6.0, respectively. */
	@SuppressLint("InlinedApi")
	private void startPowerStateListener() {
		if(powerStateListener == null) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			if (android.os.Build.VERSION.SDK_INT >= 21) {
				filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
			}
			if (android.os.Build.VERSION.SDK_INT >= 23) {
				filter.addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);
			}
			powerStateListener = new PowerStateListener();
			registerReceiver(powerStateListener, filter);
			PowerStateListener.start(appContext);
		}
	}


	/** create timers that will trigger events throughout the program, and
	 * register the custom Intents with the controlMessageReceiver. */
	@SuppressWarnings("static-access")
	public static void registerTimers(Context appContext) {
		localHandle.timer = new Timer(localHandle);
		IntentFilter filter = new IntentFilter();
		filter.addAction( appContext.getString( R.string.turn_gps_off ) );
		filter.addAction( appContext.getString( R.string.turn_gps_on ) );
		filter.addAction( appContext.getString( R.string.signout_intent ) );
		filter.addAction( appContext.getString( R.string.run_wifi_log ) );
		filter.addAction( appContext.getString( R.string.upload_data_files_intent ) );
		filter.addAction( appContext.getString( R.string.create_new_data_files_intent ) );
		filter.addAction( appContext.getString( R.string.check_for_new_surveys_intent ) );
		filter.addAction("crashBeiwe");
		List<String> surveyIds = PersistentData.getSurveyIds();
		for (String surveyId : surveyIds) { filter.addAction(surveyId); }
		appContext.registerReceiver(localHandle.timerReceiver, filter);
	}

	/*#############################################################################
	####################            Timer Logic             #######################
	#############################################################################*/

	public void startTimers() {
        Long now = System.currentTimeMillis();
        Log.i("BackgroundService", "running startTimer logic.");
        if ( PersistentData.getMostRecentAlarmTime(getString( R.string.turn_gps_on )) < now || !timer.alarmIsSet(Timer.gpsOnIntent) ) {
            sendBroadcast( Timer.gpsOnIntent ); }
        else if(PersistentData.getGpsEnabled() && timer.alarmIsSet(Timer.gpsOffIntent)
                && PersistentData.getMostRecentAlarmTime(getString( R.string.turn_gps_on )) - PersistentData.getGpsOffDurationMilliseconds() + 1000 > now ) {
            gpsListener.turn_on();
        }

        if ( PersistentData.getMostRecentAlarmTime( getString(R.string.run_wifi_log)) < now || //the most recent wifi log time is in the past or
                !timer.alarmIsSet(Timer.wifiLogIntent) ) {
            sendBroadcast( Timer.wifiLogIntent ); }

        // Functionality timers. We don't need aggressive checking for if these timers have been missed, as long as they run eventually it is fine.
        if (!timer.alarmIsSet(Timer.uploadDatafilesIntent)) { timer.setupExactSingleAlarm(PersistentData.getUploadDataFilesFrequencyMilliseconds(), Timer.uploadDatafilesIntent); }
        if (!timer.alarmIsSet(Timer.createNewDataFilesIntent)) { timer.setupExactSingleAlarm(PersistentData.getCreateNewDataFilesFrequencyMilliseconds(), Timer.createNewDataFilesIntent); }
        if (!timer.alarmIsSet(Timer.checkForNewSurveysIntent)) { timer.setupExactSingleAlarm(PersistentData.getCheckForNewSurveysFrequencyMilliseconds(), Timer.checkForNewSurveysIntent); }

        //checks for the current expected state for survey notifications,
        for (String surveyId : PersistentData.getSurveyIds() ){
            if ( PersistentData.getSurveyNotificationState(surveyId) || PersistentData.getMostRecentSurveyAlarmTime(surveyId) < now ) {
                //if survey notification should be active or the most recent alarm time is in the past, trigger the notification.
                SurveyNotifications.displaySurveyNotification(appContext, surveyId); } }

        //checks that surveys are actually scheduled, if a survey is not scheduled, schedule it!
        for (String surveyId : PersistentData.getSurveyIds() ) {
            if ( !timer.alarmIsSet( new Intent(surveyId) ) ) { SurveyScheduler.scheduleSurvey(surveyId); } }

        Intent restartServiceIntent = new Intent( getApplicationContext(), BackgroundService.class);
        restartServiceIntent.setPackage( getPackageName() );
        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, 0 );
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService( Context.ALARM_SERVICE );
        alarmService.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000 * 60 * 2, 1000 * 60 * 2, restartServicePendingIntent);
	}

	/**Refreshes the logout timer.
	 * This function has a THEORETICAL race condition, where the BackgroundService is not fully instantiated by a session activity,
	 * in this case we log an error to the debug log, print the error, and then wait for it to crash.  In testing on a (much) older
	 * version of the app we would occasionally see the error message, but we have never (august 10 2015) actually seen the app crash
	 * inside this code. */
	public static void startAutomaticLogoutCountdownTimer(){
		if (timer == null) {
			Log.e("bacgroundService", "timer is null, BackgroundService may be about to crash, the Timer was null when the BackgroundService was supposed to be fully instantiated.");
			TextFileManager.getDebugLogFile().writeEncrypted("our not-quite-race-condition encountered, Timer was null when the BackgroundService was supposed to be fully instantiated");
		}
		timer.setupExactSingleAlarm(PersistentData.getMillisecondsBeforeAutoLogout(), Timer.signoutIntent);
		PersistentData.loginOrRefreshLogin();
	}

	/** cancels the signout timer */
	public static void clearAutomaticLogoutCountdownTimer() { timer.cancelAlarm(Timer.signoutIntent); }

	/** The Timer requires the BackgroundService in order to create alarms, hook into that functionality here. */
	public static void setSurveyAlarm(String surveyId, Calendar alarmTime) { timer.startSurveyAlarm(surveyId, alarmTime); }

	public static void cancelSurveyAlarm(String surveyId) { timer.cancelAlarm(new Intent(surveyId)); }

	/**The timerReceiver is an Android BroadcastReceiver that listens for our timer events to trigger,
	 * and then runs the appropriate code for that trigger.
	 * Note: every condition has a return statement at the end; this is because the trigger survey notification
	 * action requires a fairly expensive dive into PersistantData JSON unpacking.*/
    private BroadcastReceiver timerReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context appContext, Intent intent) {
            Log.d("BackgroundService - timers", "Received broadcast: " + intent.toString() );
            TextFileManager.getDebugLogFile().writeEncrypted(System.currentTimeMillis() + " Received Broadcast: " + intent.toString() );
            String broadcastAction = intent.getAction();

            /** For GPS and Accelerometer the failure modes are:
             * 1. If a recording event is triggered and followed by Doze being enabled then Beiwe will record until the Doze period ends.
             * 2. If, after Doze ends, the timers trigger out of order Beiwe ceaces to record and triggers a new recording event in the future. */

            /** Disable active sensor */

            if (broadcastAction.equals( appContext.getString(R.string.turn_gps_off) ) ) {
                if ( PermissionHandler.checkGpsPermissions(appContext) ) { gpsListener.turn_off(); }
                return; }

            /** Enable active sensors, reset timers. */

            //GPS. Almost identical logic to accelerometer above.
            if (broadcastAction.equals( appContext.getString(R.string.turn_gps_on) ) ) {
                if ( !PersistentData.getGpsEnabled() ) { Log.e("BackgroundService Listener", "invalid GPS on received"); return; }
                gpsListener.turn_on();
                timer.setupExactSingleAlarm(PersistentData.getGpsOnDurationMilliseconds(), Timer.gpsOffIntent);
                long alarmTime = timer.setupExactSingleAlarm(PersistentData.getGpsOnDurationMilliseconds() + PersistentData.getGpsOffDurationMilliseconds(), Timer.gpsOnIntent);
                PersistentData.setMostRecentAlarmTime(getString(R.string.turn_gps_on), alarmTime );
                return; }
            //run a wifi scan.  Most similar to GPS, but without an off-timer.
            if (broadcastAction.equals( appContext.getString(R.string.run_wifi_log) ) ) {
                if ( !PersistentData.getWifiEnabled() ) { Log.e("BackgroundService Listener", "invalid WiFi scan received"); return; }
                if ( PermissionHandler.checkWifiPermissions(appContext) ) { WifiListener.scanWifi(); }
                else { TextFileManager.getDebugLogFile().writeEncrypted(System.currentTimeMillis() + " user has not provided permission for Wifi."); }
                long alarmTime = timer.setupExactSingleAlarm(PersistentData.getWifiLogFrequencyMilliseconds(), Timer.wifiLogIntent);
                PersistentData.setMostRecentAlarmTime( getString(R.string.run_wifi_log), alarmTime );
                return; }

            /** Bluetooth timers are unlike GPS and Accelerometer because it uses an absolute-point-in-time as a trigger, and therefore we don't need to store most-recent-timer state.
             * The Bluetooth-on action sets the corresponding Bluetooth-off timer, the Bluetooth-off action sets the next Bluetooth-on timer.*/


            //starts a data upload attempt.
            if (broadcastAction.equals( appContext.getString(R.string.upload_data_files_intent) ) ) {
                PostRequest.uploadAllFiles();
                timer.setupExactSingleAlarm(PersistentData.getUploadDataFilesFrequencyMilliseconds(), Timer.uploadDatafilesIntent);
                return; }
            //creates new data files
            if (broadcastAction.equals( appContext.getString(R.string.create_new_data_files_intent) ) ) {
                TextFileManager.makeNewFilesForEverything();
                timer.setupExactSingleAlarm(PersistentData.getCreateNewDataFilesFrequencyMilliseconds(), Timer.createNewDataFilesIntent);
                PostRequest.uploadAllFiles();
                return; }

            //Downloads the most recent survey questions and schedules the surveys.
            if (broadcastAction.equals( appContext.getString(R.string.check_for_new_surveys_intent))) {
                SurveyDownloader.downloadSurveys( getApplicationContext() );
                timer.setupExactSingleAlarm(PersistentData.getCheckForNewSurveysFrequencyMilliseconds(), Timer.checkForNewSurveysIntent);
                return; }

            //Downloads the most recent settings
            if (broadcastAction.equals( appContext.getString(R.string.check_for_new_settings_intent))){
                SettingUpdate.downloadSetting( getApplicationContext() );
                timer.setupExactSingleAlarm(1L, Timer.checkForNewSetting);
                return; }

            // Signs out the user. (does not set up a timer, that is handled in activity and sign-in logic)
            if (broadcastAction.equals( appContext.getString(R.string.signout_intent) ) ) {
                PersistentData.logout();
                Intent loginPage = new Intent(appContext, LoginActivity.class);
                loginPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                appContext.startActivity(loginPage);
                return; }


            //checks if the action is the id of a survey (expensive), if so pop up the notification for that survey, schedule the next alarm
            if ( PersistentData.getSurveyIds().contains( broadcastAction ) ) {
//				Log.i("BACKGROUND SERVICE", "new notification: " + broadcastAction);
                SurveyNotifications.displaySurveyNotification(appContext, broadcastAction);
                SurveyScheduler.scheduleSurvey(broadcastAction);
                return; }

            if ( PersistentData.isRegistered() && broadcastAction.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_TYPE);
                if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI || networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    PostRequest.uploadAllFiles();
                    return;
                }
            }

            //this is a special action that will only run if the app device is in debug mode.
            if (broadcastAction.equals("crashBeiwe") && BuildConfig.APP_IS_BETA) {
                throw new NullPointerException("beeeeeoooop."); }
            //this is a special action that will only run if the app device is in debug mode.
            if (broadcastAction.equals("enterANR") && BuildConfig.APP_IS_BETA) {
                try {
                    Thread.sleep(100000);
                }
                catch(InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
    };

	/*##########################################################################################
	############## code related to onStartCommand and binding to an activity ###################
	##########################################################################################*/
	@Override
	public IBinder onBind(Intent arg0) { return new BackgroundServiceBinder(); }

	/**A public "Binder" class for Activities to access.
	 * Provides a (safe) handle to the background Service using the onStartCommand code
	 * used in every RunningBackgroundServiceActivity */
	public class BackgroundServiceBinder extends Binder {
		public BackgroundService getService() {
			return BackgroundService.this;
		}
	}

	/*##############################################################################
	########################## Android Service Lifecycle ###########################
	##############################################################################*/
    /** The BackgroundService is meant to be all the time, so we return START_STICKY */
    @Override public int onStartCommand(Intent intent, int flags, int startId){ //Log.d("BackroundService onStartCommand", "started with flag " + flags );
        TextFileManager.getDebugLogFile().writeEncrypted(System.currentTimeMillis()+" "+"started with flag " + flags);
        return START_STICKY;
        //we are testing out this restarting behavior for the service.  It is entirely unclear that this will have any observable effect.
        //return START_REDELIVER_INTENT;
    }
    //(the rest of these are identical, so I have compactified it)
    @Override public void onTaskRemoved(Intent rootIntent) { //Log.d("BackroundService onTaskRemoved", "onTaskRemoved called with intent: " + rootIntent.toString() );
        TextFileManager.getDebugLogFile().writeEncrypted(System.currentTimeMillis()+" "+"onTaskRemoved called with intent: " + rootIntent.toString());
        restartService(); }
    @Override public boolean onUnbind(Intent intent) { //Log.d("BackroundService onUnbind", "onUnbind called with intent: " + intent.toString() );
        TextFileManager.getDebugLogFile().writeEncrypted(System.currentTimeMillis()+" "+"onUnbind called with intent: " + intent.toString());
        restartService();
        return super.onUnbind(intent); }
    @Override public void onDestroy() { //Log.w("BackgroundService", "BackgroundService was destroyed.");
        //note: this does not run when the service is killed in a task manager, OR when the stopService() function is called from debugActivity.
        TextFileManager.getDebugLogFile().writeEncrypted(System.currentTimeMillis()+" "+"BackgroundService was destroyed.");
        restartService();
        super.onDestroy(); }
    @Override public void onLowMemory() { //Log.w("BackroundService onLowMemory", "Low memory conditions encountered");
        TextFileManager.getDebugLogFile().writeEncrypted(System.currentTimeMillis()+" "+"onLowMemory called.");
        restartService(); }

    /** Sets a timer that starts the service if it is not running in ten seconds. */
    private void restartService(){
        //how does this even...  Whatever, 10 seconds later the background service will start.
        Intent restartServiceIntent = new Intent( getApplicationContext(), this.getClass() );
        restartServiceIntent.setPackage( getPackageName() );
        PendingIntent restartServicePendingIntent = PendingIntent.getService( getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT );
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService( Context.ALARM_SERVICE );
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 500, restartServicePendingIntent);
    }

    public void crashBackgroundService() { if (BuildConfig.APP_IS_BETA) {
        throw new NullPointerException("stop poking me!"); } }
}