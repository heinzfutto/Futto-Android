package org.futto.app.ui.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import org.futto.app.R;
import org.futto.app.networking.NetworkUtility;
import org.futto.app.ui.registration.ForgotPasswordActivity;
import org.futto.app.ui.registration.ResetPasswordActivity;

/**
 * This is a class that holds the function to show alerts. In case we want to use other alert functionalities,
 * these should be put here.
 * 
 * @author Dor Samet
 *
 */
public class AlertsManager {

	/** 
	 * Pops up an alert with the "message" on the user's "activity" screen.
	 * This alert is designed to have one OK button, can later implement it so that it will have more buttons
	 * 
	 * @param message
	 * @param activity
	 */
	public static void showAlert(String message, Activity activity) {
		showAlert(message, "Alert", activity);
	}

	public static void showAlert(int httpResponseCode, String title, Activity activity) {
		String message = httpResponseCodeExplanation(httpResponseCode, activity);
		showAlert(message, title, activity);
	}

	public static void showAlert(String message, String title, Activity activity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Nothing!
			}
		});
		builder.create().show();
	}

	public static void showErrorAlert(String message, Activity activity, final int alertNumber) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("A critical error occured");
		builder.setMessage(message);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//this system.exit() operation does not work if the app loads a different activity over the activity that spawned this Alert
				System.exit(alertNumber);
			}
		});		
		builder.create().show();
	}

	/**Checks a given HTTP response code sent from the server, and then returns a string explaining
	 * that code's meaning.  These response codes and messages are specific to Beiwe, and may not
	 * have identical meaning to the (strict) HTTP spec.
	 * @param httpResponseCode HTTP response code
	 * @return String to be displayed on the Alert in case of a problem	 */
	private static String httpResponseCodeExplanation(int httpResponseCode, Activity activity) {
		Log.i("HTTPAsync", "got HTTP response code " + httpResponseCode);
		if (httpResponseCode == 200) {return "OK";}
		else if (httpResponseCode == 1) {return activity.getString(R.string.http_message_1);}
		else if (httpResponseCode == 2) {return activity.getString(R.string.invalid_encryption_key);}
		else if (httpResponseCode == 400) {return activity.getString(R.string.http_message_400);}
		else if (httpResponseCode == 405) {return activity.getString(R.string.http_message_405);}
		else if (httpResponseCode == 502 && !NetworkUtility.networkIsAvailable(activity)) {
			return activity.getString(R.string.http_message_internet_disconnected);}
		else if (httpResponseCode == 502 || httpResponseCode == 404) {
			return activity.getString(R.string.http_message_server_not_found);}
		else if (httpResponseCode == 403) {
			if (activity.getClass() == ResetPasswordActivity.class) {
				return activity.getString(R.string.http_message_403_wrong_password);
			} else if (activity.getClass() == ForgotPasswordActivity.class) {
				return activity.getString(R.string.http_message_403_wrong_password_forgot_password_page);
			} else {
				return activity.getString(R.string.http_message_403_during_registration);
			}
		}
		else {
			Log.e("HTTPAsync", "unknown response code: " + httpResponseCode);
			return activity.getString(R.string.http_message_unknown_response_code);
		}
	}
}
