package org.futto.app.ui;

import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;

import org.futto.app.BackgroundService;
import org.futto.app.BuildConfig;
import org.futto.app.CrashHandler;
import org.futto.app.PermissionHandler;
import org.futto.app.R;
import org.futto.app.Timer;
import org.futto.app.networking.PostRequest;
import org.futto.app.networking.SurveyDownloader;
import org.futto.app.session.SessionActivity;
import org.futto.app.storage.EncryptionEngine;
import org.futto.app.storage.PersistentData;
import org.futto.app.storage.TextFileManager;
import org.futto.app.survey.JsonSkipLogic;
import org.futto.app.ui.user.MainMenuActivity;
import org.futto.app.ui.utils.SurveyNotifications;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class DebugInterfaceActivity extends SessionActivity {
	//extends a session activity.
	Context appContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug_interface);
		appContext = this.getApplicationContext();

		if (BuildConfig.APP_IS_DEV) {
			((TextView) findViewById(R.id.debugtexttwenty)).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.button)).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.buttonPrintInternalLog)).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.buttonClearInternalLog)).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.buttonDeleteEverything)).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.buttonListFiles)).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.buttonTimer)).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.buttonGetKeyFile)).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.testEncryption)).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.buttonLogDataToggles)).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.buttonAlarmStates)).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.buttonFeaturesEnabled)).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.buttonFeaturesPermissable)).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.buttonCrashUi)).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.buttonCrashBackground)).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.buttonCrashBackgroundInFive)).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.buttonTestManualErrorReport)).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.stopBackgroundService)).setVisibility(View.VISIBLE);
		}

	}

	//Intent triggers caught in BackgroundService
	public void accelerometerOn (View view) { appContext.sendBroadcast( Timer.accelerometerOnIntent ); }
	public void accelerometerOff (View view) { appContext.sendBroadcast( Timer.accelerometerOffIntent ); }
	public void gpsOn (View view) { appContext.sendBroadcast( Timer.gpsOnIntent ); }
	public void gpsOff (View view) { appContext.sendBroadcast( Timer.gpsOffIntent ); }
	public void scanWifi (View view) { appContext.sendBroadcast( Timer.wifiLogIntent ); }
	public void bluetoothButtonStart (View view) { appContext.sendBroadcast(Timer.bluetoothOnIntent); }
	public void bluetoothButtonStop (View view) { appContext.sendBroadcast(Timer.bluetoothOffIntent); }

	//raw debugging info
	public void printInternalLog(View view) {
		// Log.i("print log button pressed", "press.");
		String log = TextFileManager.getDebugLogFile().read();
		for( String line : log.split("\n") ) {
			Log.i( "log file...", line ); }
//		Log.i("log file encrypted", EncryptionEngine.encryptAES(log) );
	}
	public void testEncrypt (View view) {
		Log.i("Debug..", TextFileManager.getKeyFile().read());
		String data = TextFileManager.getKeyFile().read();
		Log.i("reading keyFile:", data );
		try { EncryptionEngine.readKey(); }
		catch (InvalidKeySpecException e) {
			Log.e("DebugInterfaceActivity", "this is only partially implemented, unknown behavior");
			e.printStackTrace();
			throw new NullPointerException("some form of encryption error, type 1");
		}
		String encrypted;
		try { encrypted = EncryptionEngine.encryptRSA("ThIs Is a TeSt".getBytes() ).toString(); }
		catch (InvalidKeySpecException e) {
			Log.e("DebugInterfaceActivity", "this is only partially implemented, unknown behavior");
			e.printStackTrace();
			throw new NullPointerException("some form of encryption error, type 2");
		}
		Log.i("test encrypt - length:", "" + encrypted.length() );
		Log.i("test encrypt - output:", encrypted );
		Log.i("test hash:", EncryptionEngine.safeHash( encrypted ) );
	}
	public void logDataToggles(View view) {
//		Log.i("DebugInterfaceActivity.logDataToggles()", "Accelerometer: " + Boolean.toString(PersistentData.getAccelerometerEnabled()));
		Log.i("DebugInterfaceActivity.logDataToggles()", "GPS: " + Boolean.toString(PersistentData.getGpsEnabled()));
//		Log.i("DebugInterfaceActivity.logDataToggles()", "Calls: " + Boolean.toString(PersistentData.getCallsEnabled()));
//		Log.i("DebugInterfaceActivity.logDataToggles()", "Texts: " + Boolean.toString(PersistentData.getTextsEnabled()));
		Log.i("DebugInterfaceActivity.logDataToggles()", "WiFi: " + Boolean.toString(PersistentData.getWifiEnabled()));
		Log.i("DebugInterfaceActivity.logDataToggles()", "Bluetooth: " + Boolean.toString(PersistentData.getBluetoothEnabled()));
		Log.i("DebugInterfaceActivity.logDataToggles()", "Power State: " + Boolean.toString(PersistentData.getPowerStateEnabled()));
	}
	public void getAlarmStates(View view) {
		List<String> ids = PersistentData.getSurveyIds();
		for (String surveyId : ids){
			Log.i("most recent alarm state", "survey id: " + surveyId + ", " +PersistentData.getMostRecentSurveyAlarmTime(surveyId) + ", " + PersistentData.getSurveyNotificationState(surveyId)) ;
		}
	}

	public void getEnabledFeatures(View view) {
		if ( PersistentData.getAccelerometerEnabled() ) { Log.i("features", "Accelerometer Enabled." ); } else { Log.e("features", "Accelerometer Disabled."); }
		if ( PersistentData.getGpsEnabled() ) { Log.i("features", "Gps Enabled." ); } else { Log.e("features", "Gps Disabled."); }
		if ( PersistentData.getCallsEnabled() ) { Log.i("features", "Calls Enabled." ); } else { Log.e("features", "Calls Disabled."); }
		if ( PersistentData.getTextsEnabled() ) { Log.i("features", "Texts Enabled." ); } else { Log.e("features", "Texts Disabled."); }
		if ( PersistentData.getWifiEnabled() ) { Log.i("features", "Wifi Enabled." ); } else { Log.e("features", "Wifi Disabled."); }
		if ( PersistentData.getBluetoothEnabled() ) { Log.i("features", "Bluetooth Enabled." ); } else { Log.e("features", "Bluetooth Disabled."); }
		if ( PersistentData.getPowerStateEnabled() ) { Log.i("features", "PowerState Enabled." ); } else { Log.e("features", "PowerState Disabled."); }
	}

	public void getPermissableFeatures(View view) {
		if (PermissionHandler.checkAccessFineLocation(getApplicationContext())) { Log.i("permissions", "AccessFineLocation enabled."); } else { Log.e("permissions", "AccessFineLocation disabled."); }
		if (PermissionHandler.checkAccessNetworkState(getApplicationContext())) { Log.i("permissions", "AccessNetworkState enabled."); } else { Log.e("permissions", "AccessNetworkState disabled."); }
		if (PermissionHandler.checkAccessWifiState(getApplicationContext())) { Log.i("permissions", "AccessWifiState enabled."); } else { Log.e("permissions", "AccessWifiState disabled."); }
		if (PermissionHandler.checkAccessBluetooth(getApplicationContext())) { Log.i("permissions", "Bluetooth enabled."); } else { Log.e("permissions", "Bluetooth disabled."); }
		if (PermissionHandler.checkAccessBluetoothAdmin(getApplicationContext())) { Log.i("permissions", "BluetoothAdmin enabled."); } else { Log.e("permissions", "BluetoothAdmin disabled."); }
//		if (PermissionHandler.checkAccessCallPhone(getApplicationContext())) { Log.i("permissions", "CallPhone enabled."); } else { Log.e("permissions", "CallPhone disabled."); }
//		if (PermissionHandler.checkAccessReadCallLog(getApplicationContext())) { Log.i("permissions", "ReadCallLog enabled."); } else { Log.e("permissions", "ReadCallLog disabled."); }
//		if (PermissionHandler.checkAccessReadContacts(getApplicationContext())) { Log.i("permissions", "ReadContacts enabled."); } else { Log.e("permissions", "ReadContacts disabled."); }
//		if (PermissionHandler.checkAccessReadPhoneState(getApplicationContext())) { Log.i("permissions", "ReadPhoneState enabled."); } else { Log.e("permissions", "ReadPhoneState disabled."); }
//		if (PermissionHandler.checkAccessReadSms(getApplicationContext())) { Log.i("permissions", "ReadSms enabled."); } else { Log.e("permissions", "ReadSms disabled."); }
//		if (PermissionHandler.checkAccessReceiveMms(getApplicationContext())) { Log.i("permissions", "ReceiveMms enabled."); } else { Log.e("permissions", "ReceiveMms disabled."); }
//		if (PermissionHandler.checkAccessReceiveSms(getApplicationContext())) { Log.i("permissions", "ReceiveSms enabled."); } else { Log.e("permissions", "ReceiveSms disabled."); }
//		if (PermissionHandler.checkAccessRecordAudio(getApplicationContext())) { Log.i("permissions", "RecordAudio enabled."); } else { Log.e("permissions", "RecordAudio disabled."); }
	}

	public void clearInternalLog(View view) { TextFileManager.getDebugLogFile().deleteSafely(); }
	public void getKeyFile(View view) { Log.i("DEBUG", "key file data: " + TextFileManager.getKeyFile().read()); }


	//network operations
	public void uploadDataFiles(View view) { PostRequest.uploadAllFiles(); }
	public void runSurveyDownload(View view) { SurveyDownloader.downloadSurveys(getApplicationContext()); }
	public void buttonTimer(View view) { backgroundService.startTimers(); }


	//file operations
	public void makeNewFiles(View view) { TextFileManager.makeNewFilesForEverything(); }
	public void deleteEverything(View view) {
//		Log.i("Delete Everything button pressed", "poke.");
		String[] files = TextFileManager.getAllFiles();
		Arrays.sort(files);
		for( String file : files ) { Log.i( "files...", file); }
		TextFileManager.deleteEverything(); }
	public void listFiles(View view){
		Log.w( "files...", "UPLOADABLE FILES");
		String[] files = TextFileManager.getAllUploadableFiles();
		Arrays.sort(files);
		for( String file : files ) { Log.i( "files...", file); }
		Log.w( "files...", "ALL FILES");
		files = TextFileManager.getAllFiles();
		Arrays.sort(files);
		for( String file : files ) { Log.i( "files...", file); }
	}

	//ui operations
	public void loadMainMenu(View view) { startActivity(new Intent(appContext, MainMenuActivity.class) ); }
	public void popSurveyNotifications(View view) {
		for (String surveyId : PersistentData.getSurveyIds()){
			SurveyNotifications.displaySurveyNotification(appContext, surveyId);
		}
	}

	//crash operations (No, really, we actually need this.)
	public void crashUi(View view) { throw new NullPointerException("oops, you bwoke it."); }
	public void crashBackground(View view) { BackgroundService.timer.setupExactSingleAlarm((long) 0, new Intent("crashBeiwe")); }
	public void crashBackgroundInFive(View view) { BackgroundService.timer.setupExactSingleAlarm((long) 5000, new Intent("crashBeiwe")); }
	public void stopBackgroundService(View view) { backgroundService.stop(); }
	public void testManualErrorReport(View view) {
		try{ throw new NullPointerException("this is a test null pointer exception from the debug interface"); }
		catch (Exception e) { CrashHandler.writeCrashlog(e, getApplicationContext()); }
	}

	//runs tests on the json logic parser
	public void testJsonLogicParser(View view) {
		String JsonQuestionsListString = "[{\"question_text\": \"In the last 7 days, how OFTEN did you EAT BROCCOLI?\", \"question_type\": \"radio_button\", \"answers\": [{\"text\": \"Never\"}, {\"text\": \"Rarely\"}, {\"text\": \"Occasionally\"}, {\"text\": \"Frequently\"}, {\"text\": \"Almost Constantly\"}], \"question_id\": \"6695d6c4-916b-4225-8688-89b6089a24d1\"}, {\"display_if\": {\">\": [\"6695d6c4-916b-4225-8688-89b6089a24d1\", 0]}, \"question_text\": \"In the last 7 days, what was the SEVERITY of your CRAVING FOR BROCCOLI?\", \"question_type\": \"radio_button\", \"answers\": [{\"text\": \"None\"}, {\"text\": \"Mild\"}, {\"text\": \"Moderate\"}, {\"text\": \"Severe\"}, {\"text\": \"Very Severe\"}], \"question_id\": \"41d54793-dc4d-48d9-f370-4329a7bc6960\"}, {\"display_if\": {\"and\": [{\">\": [\"6695d6c4-916b-4225-8688-89b6089a24d1\", 0]}, {\">\": [\"41d54793-dc4d-48d9-f370-4329a7bc6960\", 0]}]}, \"question_text\": \"In the last 7 days, how much did your CRAVING FOR BROCCOLI INTERFERE with your usual or daily activities, (e.g. eating cauliflower)?\", \"question_type\": \"radio_button\", \"answers\": [{\"text\": \"Not at all\"}, {\"text\": \"A little bit\"}, {\"text\": \"Somewhat\"}, {\"text\": \"Quite a bit\"}, {\"text\": \"Very much\"}], \"question_id\": \"5cfa06ad-d907-4ba7-a66a-d68ea3c89fba\"}, {\"display_if\": {\"or\": [{\"and\": [{\"<=\": [\"6695d6c4-916b-4225-8688-89b6089a24d1\", 3]}, {\"==\": [\"41d54793-dc4d-48d9-f370-4329a7bc6960\", 2]}, {\"<\": [\"5cfa06ad-d907-4ba7-a66a-d68ea3c89fba\", 3]}]}, {\"and\": [{\"<=\": [\"6695d6c4-916b-4225-8688-89b6089a24d1\", 3]}, {\"<\": [\"41d54793-dc4d-48d9-f370-4329a7bc6960\", 3]}, {\"==\": [\"5cfa06ad-d907-4ba7-a66a-d68ea3c89fba\", 2]}]}, {\"and\": [{\"==\": [\"6695d6c4-916b-4225-8688-89b6089a24d1\", 4]}, {\"<=\": [\"41d54793-dc4d-48d9-f370-4329a7bc6960\", 1]}, {\"<=\": [\"5cfa06ad-d907-4ba7-a66a-d68ea3c89fba\", 1]}]}]}, \"question_text\": \"While broccoli is a nutritious and healthful food, it's important to recognize that craving too much broccoli can have adverse consequences on your health.  If in a single day you find yourself eating broccoli steamed, stir-fried, and raw with a 'vegetable dip', you may be a broccoli addict.  This is an additional paragraph (following a double newline) warning you about the dangers of broccoli consumption.\", \"question_type\": \"info_text_box\", \"question_id\": \"9d7f737d-ef55-4231-e901-b3b68ca74190\"}, {\"display_if\": {\"or\": [{\"and\": [{\"==\": [\"6695d6c4-916b-4225-8688-89b6089a24d1\", 4]}, {\"or\": [{\">=\": [\"41d54793-dc4d-48d9-f370-4329a7bc6960\", 2]}, {\">=\": [\"5cfa06ad-d907-4ba7-a66a-d68ea3c89fba\", 2]}]}]}, {\"or\": [{\">=\": [\"41d54793-dc4d-48d9-f370-4329a7bc6960\", 3]}, {\">=\": [\"5cfa06ad-d907-4ba7-a66a-d68ea3c89fba\", 3]}]}]}, \"question_text\": \"OK, it sounds like your broccoli habit is getting out of hand.  Please call your clinician immediately.\", \"question_type\": \"info_text_box\", \"question_id\": \"59f05c45-df67-40ed-a299-8796118ad173\"}, {\"question_text\": \"How many pounds of broccoli per day could a woodchuck chuck if a woodchuck could chuck broccoli?\", \"text_field_type\": \"NUMERIC\", \"question_type\": \"free_response\", \"question_id\": \"9745551b-a0f8-4eec-9205-9e0154637513\"}, {\"display_if\": {\"<\": [\"9745551b-a0f8-4eec-9205-9e0154637513\", 10]}, \"question_text\": \"That seems a little low.\", \"question_type\": \"info_text_box\", \"question_id\": \"cedef218-e1ec-46d3-d8be-e30cb0b2d3aa\"}, {\"display_if\": {\"==\": [\"9745551b-a0f8-4eec-9205-9e0154637513\", 10]}, \"question_text\": \"That sounds about right.\", \"question_type\": \"info_text_box\", \"question_id\": \"64a2a19b-c3d0-4d6e-9c0d-06089fd00424\"}, {\"display_if\": {\">\": [\"9745551b-a0f8-4eec-9205-9e0154637513\", 10]}, \"question_text\": \"What?! No way- that's way too high!\", \"question_type\": \"info_text_box\", \"question_id\": \"166d74ea-af32-487c-96d6-da8d63cfd368\"}, {\"max\": \"5\", \"question_id\": \"059e2f4a-562a-498e-d5f3-f59a2b2a5a5b\", \"question_text\": \"On a scale of 1 (awful) to 5 (delicious) stars, how would you rate your dinner at Chez Broccoli Restaurant?\", \"question_type\": \"slider\", \"min\": \"1\"}, {\"display_if\": {\">=\": [\"059e2f4a-562a-498e-d5f3-f59a2b2a5a5b\", 4]}, \"question_text\": \"Wow, you are a true broccoli fan.\", \"question_type\": \"info_text_box\", \"question_id\": \"6dd9b20b-9dfc-4ec9-cd29-1b82b330b463\"}, {\"question_text\": \"THE END. This survey is over.\", \"question_type\": \"info_text_box\", \"question_id\": \"ec0173c9-ac8d-449d-d11d-1d8e596b4ec9\"}]";
		JsonSkipLogic steve;
		JSONArray questions;
		Boolean runDisplayLogic = true;
		try {
			questions = new JSONArray(JsonQuestionsListString);
			steve = new JsonSkipLogic(questions, runDisplayLogic, getApplicationContext());
		} catch (JSONException e) {
			Log.e("Debug", "it dun gon wronge.");
			e.printStackTrace();
			throw new NullPointerException("it done gon wronge");
		}
		int i = 0;
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
		steve.getNextQuestion();
		Log.v("debug", "" + i); i++;
	}


}
