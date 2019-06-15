package org.futto.app.ui.user;

import org.futto.app.BuildConfig;
import org.futto.app.DeviceInfo;
import org.futto.app.R;
import org.futto.app.RunningBackgroundServiceActivity;
import org.futto.app.networking.HTTPUIAsync;
import org.futto.app.networking.PostRequest;
import org.futto.app.storage.EncryptionEngine;
import org.futto.app.storage.PersistentData;
import org.futto.app.survey.TextFieldKeyboard;
import org.futto.app.ui.registration.ConsentFormActivity;
import org.futto.app.ui.registration.ForgotPasswordActivity;
import org.futto.app.ui.registration.RegisterActivity;
import org.futto.app.ui.utils.AlertsManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import okhttp3.Response;

import static org.futto.app.networking.PostRequest.addWebsitePrefix;


/**The LoginActivity presents the user with a password prompt.
 * When the app has had no activity (defined as the time since a SessionActiviy last loaded)
 * it bumps the user to this screen.  This timer occurs in the Background Service, so the
 * timer still triggers even if the app has been backgrounded or killed from the task switcher.
 * @authors Dor Samet, Eli Jones */
public class LoginActivity extends RunningBackgroundServiceActivity {	
	private EditText password;
	private EditText userID;
	private Context appContext;
	private Toolbar toolbar;
	@Override
	/**The login activity prompts the user for the password. */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		appContext = getApplicationContext();

		setContentView(R.layout.activity_login);
		password = (EditText) findViewById(R.id.editText2);
		userID = (EditText) findViewById(R.id.usernameText);
		TextFieldKeyboard textFieldKeyboard = new TextFieldKeyboard(appContext);
		textFieldKeyboard.makeKeyboardBehave(password);
		displayToobar();
	}

	private void displayToobar() {
		toolbar = findViewById(R.id.toolbar_main);
		setSupportActionBar(toolbar);
		toolbar.setTitleTextColor(Color.WHITE);
//		getSupportActionBar().setTitle("Futto Main Menu");
	}
//

//    public void loginButton(View view) {
//        if ( PersistentData.checkPassword( password.getText().toString() ) ) {
//            PersistentData.loginOrRefreshLogin();
//            finish();
//            return;
//        }
//        AlertsManager.showAlert("Incorrect password", this);
//    }

	/**The Login Button
	 * IF session is logged in (value in shared prefs), keep the session logged in.
	 * IF session is not logged in, wait for user input.
	 * @param view*/
//	public synchronized void loginButton(View view) {
//		if ( PersistentData.isLoggedIn()&&PersistentData.checkPassword( password.getText().toString() ) ) {
//			PersistentData.loginOrRefreshLogin();
//			finish();
//			return;
//		}else {
////			String userid =  userID.getText().toString();
//////			String password = password.getText().toString();
////			String hashedCurrentPassword = EncryptionEngine.safeHash(password.getText().toString());
////			String url = addWebsitePrefix(getApplicationContext().getString(R.string.login_url));
////			String parameters = PostRequest.makeParameter("patient_id",userid ) +
////					PostRequest.makeParameter("password", hashedCurrentPassword);
////			//responseCode = PostRequest.httpLogin(parameters, url);
////			int responseCode = PostRequest.httpRequestcode2(userid,parameters, url, hashedCurrentPassword);
////			if(responseCode != 200) {
////				AlertsManager.showAlert("Incorrect password or userid", this);
////			}else{
////				PersistentData.setLoginCredentials(userid,password.getText().toString());
////				PersistentData.loginOrRefreshLogin();
////				finish();
////				return;
//////				this.startActivity(new Intent(Activity.getApplicationContext(), ConsentFormActivity.class) );
//////				this.finish();
////			}
//			tryToLoginInWithTheServer(LoginActivity.this, addWebsitePrefix(getApplicationContext().getString(R.string.login_url)), userID.getText().toString(),password.getText().toString() );
//		}
//	}


	public synchronized void loginButton(View view) {
		if ( PersistentData.isLoggedIn()&&PersistentData.checkPassword( password.getText().toString() ) ) {
			PersistentData.loginOrRefreshLogin();
			finish();
			return;
		}else {
			// Log.d("RegisterActivity", "trying \"" + LoginManager.getPatientID() + "\" with password \"" + LoginManager.getPassword() + "\"" );
			tryToLoginInWithTheServer(this, addWebsitePrefix(getApplicationContext().getString(R.string.login_url)), userID.getText().toString(),password.getText().toString());
		}
	}


	/**Implements the server request logic for user, device registration.
	 * @param url the URL for device registration*/
	static private void tryToLoginInWithTheServer(final Activity currentActivity, final String url, final String userid,  final String password) {
		new HTTPUIAsync(url, currentActivity ) {
			@Override
			protected Void doInBackground(Void... arg0) {
				DeviceInfo.initialize(currentActivity.getApplicationContext());
				String hashedCurrentPassword = EncryptionEngine.safeHash(password);
				parameters = PostRequest.makeParameter("patient_id",userid ) +
						PostRequest.makeParameter("password", hashedCurrentPassword);
				//responseCode = PostRequest.httpLogin(parameters, url);
				responseCode = PostRequest.httpRequestcode2(userid,parameters, url, hashedCurrentPassword);
				return null;
			}

			@Override
			protected void onPostExecute(Void arg) {
				super.onPostExecute(arg);
				if (responseCode == 200) {
					PersistentData.setLoginCredentials(userid,password);
//					AlertsManager.showAlert(responseCode, currentActivity.getString(R.string.couldnt_register), currentActivity);
					activity.startActivity(new Intent(activity.getApplicationContext(), ConsentFormActivity.class) );
					activity.finish();
				} else {
					AlertsManager.showAlert(responseCode, "Incorrect password or userid", currentActivity);
				}
			}
		};
	}


	/**Move user to the forgot password activity.
	 * @param view */
	public void forgotPassword(View view) {
		startActivity( new Intent(appContext, ForgotPasswordActivity.class) );
		finish();
	}

	public void register(View view) {
		startActivity( new Intent(appContext, RegisterActivity.class) );
		finish();
	}


	@Override
	/** LoginActivity needs to suppress use of the back button, otherwise it would log the user in without a password. */
	public void onBackPressed() { }
}