package org.futto.app.ui.registration;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.futto.app.BuildConfig;
import org.futto.app.DeviceInfo;
import org.futto.app.PermissionHandler;
import org.futto.app.R;
import org.futto.app.RunningBackgroundServiceActivity;
import org.futto.app.networking.HTTPUIAsync;
import org.futto.app.networking.PostRequest;
import org.futto.app.storage.EncryptionEngine;
import org.futto.app.storage.PersistentData;
import org.futto.app.survey.TextFieldKeyboard;
import org.futto.app.ui.utils.AlertsManager;

import static org.futto.app.networking.PostRequest.addWebsitePrefix;


/**Activity used to log a user in to the application for the first time. This activity should only be called on ONCE,
 * as once the user is logged in, data is saved on the phone.
 * @author Dor Samet, Eli Jones, Josh Zagorsky */

@SuppressLint("ShowToast")
public class RegisterActivity extends RunningBackgroundServiceActivity {
//	private EditText serverUrlInput;
	private EditText userIdInput;
	private EditText tempPasswordInput;
	private EditText newPasswordInput;
	private EditText confirmNewPasswordInput;
	private Toolbar toolbar;
	private final static int PERMISSION_CALLBACK = 0; //This callback value can be anything, we are not really using it
	private final static int REQUEST_PERMISSIONS_IDENTIFIER = 1500;
	
	/** Users will go into this activity first to register information on the phone and on the server. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

//		if (!BuildConfig.CUSTOMIZABLE_SERVER_URL) {
//			EditText serverUrlInput = (EditText) findViewById(R.id.serverUrlInput);
//			serverUrlInput.setVisibility(View.GONE);
//		}

//		serverUrlInput = (EditText) findViewById(R.id.serverUrlInput);
		userIdInput = (EditText) findViewById(R.id.registerUserIdInput);
		tempPasswordInput = (EditText) findViewById(R.id.registerTempPasswordInput);
		newPasswordInput = (EditText) findViewById(R.id.registerNewPasswordInput);
		confirmNewPasswordInput = (EditText) findViewById(R.id.registerConfirmNewPasswordInput);
		TextFieldKeyboard textFieldKeyboard = new TextFieldKeyboard(getApplicationContext());
//		textFieldKeyboard.makeKeyboardBehave(serverUrlInput);
		textFieldKeyboard.makeKeyboardBehave(userIdInput);
		textFieldKeyboard.makeKeyboardBehave(tempPasswordInput);
		textFieldKeyboard.makeKeyboardBehave(newPasswordInput);
		textFieldKeyboard.makeKeyboardBehave(confirmNewPasswordInput);

		newPasswordInput.setHint(String.format(getString(R.string.registration_replacement_password_hint), PersistentData.minPasswordLength()));
		confirmNewPasswordInput.setHint(String.format(getString(R.string.registration_replacement_password_hint), PersistentData.minPasswordLength()));
		displayToobar();
	}

	private void displayToobar() {
		toolbar = findViewById(R.id.toolbar_main);
		setSupportActionBar(toolbar);
		toolbar.setTitleTextColor(Color.WHITE);
//		getSupportActionBar().setTitle("Register on Futto");
	}



	/** Registration sequence begins here, called when the submit button is pressed.
	 * @param view */
	public synchronized void registerButtonPressed(View view) {
//		String serverUrl = serverUrlInput.getText().toString();
		String serverUrl = "https://test.findyourdreamjob.org/";
		String userID = userIdInput.getText().toString();
		String tempPassword = tempPasswordInput.getText().toString();
		String newPassword = newPasswordInput.getText().toString();
		String confirmNewPassword = confirmNewPasswordInput.getText().toString();

//		if ((serverUrl.length() == 0) && (BuildConfig.CUSTOMIZABLE_SERVER_URL)) {
//			// If the study URL is empty, alert the user
//			AlertsManager.showAlert(getString(R.string.url_too_short), getString(R.string.couldnt_register), this);
//		} else
 		if (userID.length() == 0) {
			// If the user id length is too short, alert the user
			AlertsManager.showAlert(getString(R.string.invalid_user_id), getString(R.string.couldnt_register), this);
			return;
		} else if (tempPassword.length() < 1) {
			// If the temporary registration password isn't filled in
			AlertsManager.showAlert(getString(R.string.empty_temp_password), getString(R.string.couldnt_register), this);
		} else if (!PersistentData.passwordMeetsRequirements(newPassword)) {
			// If the new password has too few characters
			String alertMessage = String.format(getString(R.string.password_too_short), PersistentData.minPasswordLength());
			AlertsManager.showAlert(alertMessage, getString(R.string.couldnt_register), this);
			return;
		} else if (!newPassword.equals(confirmNewPassword)) {
			// If the new password doesn't match the confirm new password
			AlertsManager.showAlert(getString(R.string.password_mismatch), getString(R.string.couldnt_register), this);
			return;
		} else {
			if (BuildConfig.CUSTOMIZABLE_SERVER_URL) {
				PersistentData.setServerUrl(serverUrl);
			}
			PersistentData.setLoginCredentials(userID, tempPassword);
			// Log.d("RegisterActivity", "trying \"" + LoginManager.getPatientID() + "\" with password \"" + LoginManager.getPassword() + "\"" );
			tryToRegisterWithTheServer(this, addWebsitePrefix(getApplicationContext().getString(R.string.register_url)), newPassword);
		}
	}
	
	
	/**Implements the server request logic for user, device registration. 
	 * @param url the URL for device registration*/
	static private void tryToRegisterWithTheServer(final Activity currentActivity, final String url, final String newPassword) {
		new HTTPUIAsync(url, currentActivity ) {
			@Override
			protected Void doInBackground(Void... arg0) {
				DeviceInfo.initialize(currentActivity.getApplicationContext());
				parameters= PostRequest.makeParameter("bluetooth_id", DeviceInfo.getBluetoothMAC() ) +
							PostRequest.makeParameter("new_password", newPassword) +
							PostRequest.makeParameter("phone_number", "" ) +
							PostRequest.makeParameter("device_id", DeviceInfo.getAndroidID() ) +
							PostRequest.makeParameter("device_os", "Android") +
							PostRequest.makeParameter("os_version", DeviceInfo.getAndroidVersion() ) +
							PostRequest.makeParameter("hardware_id", DeviceInfo.getHardwareId() ) +
							PostRequest.makeParameter("brand", DeviceInfo.getBrand() ) +
							PostRequest.makeParameter("manufacturer", DeviceInfo.getManufacturer() ) +
							PostRequest.makeParameter("model", DeviceInfo.getModel() ) +
							PostRequest.makeParameter("product", DeviceInfo.getProduct() ) +
							PostRequest.makeParameter("beiwe_version", DeviceInfo.getBeiweVersion() );
				responseCode = PostRequest.httpRegister(parameters, url);
				return null;
			}
		
			@Override
			protected void onPostExecute(Void arg) {
				super.onPostExecute(arg);
				if (responseCode == 200) {
					PersistentData.setPassword(newPassword);
					activity.startActivity(new Intent(activity.getApplicationContext(), ConsentFormActivity.class) );
					activity.finish();
				} else {
					AlertsManager.showAlert(responseCode, currentActivity.getString(R.string.couldnt_register), currentActivity);
				}
			}
		};
	}
	

	
	
	/*####################################################################
	###################### Permission Prompting ##########################
	####################################################################*/
	
	private static Boolean prePromptActive = false;
	private static Boolean postPromptActive = false;
	private static Boolean thisResumeCausedByFalseActivityReturn = false;
	private static Boolean aboutToResetFalseActivityReturn = false;
	private static Boolean activityNotVisible = false;

	private void goToSettings() {
		// Log.i("reg", "goToSettings");
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(myAppSettings, REQUEST_PERMISSIONS_IDENTIFIER);
    }

	
	@Override
	protected void onResume() {  
		// Log.i("reg", "onResume");
		super.onResume();
		activityNotVisible = false;

		// This used to be in an else block, its idempotent and we appear to have been having problems with it not having been run.
		DeviceInfo.initialize(getApplicationContext());

		if (aboutToResetFalseActivityReturn) {
			aboutToResetFalseActivityReturn = false;
			thisResumeCausedByFalseActivityReturn = false;
			return;
		}

	}
	
	@Override
	protected void onPause() {
		super.onPause();
		activityNotVisible = true;
	};
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Log.i("reg", "onActivityResult. requestCode: " + requestCode + ", resultCode: " + resultCode );
		aboutToResetFalseActivityReturn = true;
    }

	@Override
	public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
		// Log.i("reg", "onRequestPermissionResult");
		if (activityNotVisible) return; //this is identical logical progression to the way it works in SessionActivity.
		for (int i = 0; i < grantResults.length; i++) {
			if ( permissions[i].equals( Manifest.permission.READ_SMS ) ) {
//				Log.i("permiss", "permission return: " + permissions[i]);
				if ( grantResults[i] == PermissionHandler.PERMISSION_GRANTED ) { break; }
				if ( shouldShowRequestPermissionRationale(permissions[i]) ) { showPostPermissionAlert(this); } //(shouldShow... "This method returns true if the app has requested this permission previously and the user denied the request.")
			}
//			else { Log.w("permiss", "permission return: " + permissions[i]); }
		}
	}
	
	/* Message Popping */
	

	
	public static void showPostPermissionAlert(final RegisterActivity activity) {
		// Log.i("reg", "showPostAlert");
		if (postPromptActive) { return; }
		postPromptActive = true;
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Permissions Requirement:");
		builder.setMessage(R.string.permission_registration_actually_need_sms_alert);
		builder.setOnDismissListener( new DialogInterface.OnDismissListener() { @Override public void onDismiss(DialogInterface dialog) {
			thisResumeCausedByFalseActivityReturn = true;
			activity.goToSettings();
			postPromptActive = false;
		} } );
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { @Override public void onClick(DialogInterface arg0, int arg1) {  } } ); //Okay button
		builder.create().show();
	}
}
