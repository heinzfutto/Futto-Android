package org.futto.app.networking;

import android.os.AsyncTask;
import android.util.Log;

//TODO: Low priority: Eli. Redoc.

/**HTTPAsync is a... special AsyncTask for handling network (HTTP) requests using our PostRequest class.
 * HTTPAsync handles the asynchronous requirement for UI threads, and automatically handles user
 * notification for the well defined HTTP errors.
 * 
 * HTTPAsync objects start executing on instantiation. While working it pops up an android UI spinner.
 * If the spinner UI element ("progressBar"?) is not declared in the activity's manifest it will instead run "silently" 
 * 
 * Inside your overridden doInBackground function you must assign the HTTP return value to responseCode (as an int).
 * 
 * @author Eli */
public class HTTPAsync extends AsyncTask<Void, Void, Void> {
	
	protected String url;
	protected String parameters = "";
	protected int responseCode = -1;

	/**An HTTPAsyc instance will begin execution immediately upon instantiation.
	 * @param url a string containing The URL with which you will connect. */
	public HTTPAsync(String url) { this.url = url; }
	
	/** Your code should override doInBackground function, do NOT call super.doInBackground().*/
	@Override
	protected Void doInBackground(Void... arg0) {
		Log.e("AsyncPostRequest", "You are not using this right, exiting program for your own good");
		System.exit(1);
		return null;
	}
	
	/** Your code should override the onPostExecute function, and handle
	 * any additional special response and user notification logic.
	 * If you do not override the app will log any bad responses from the HTTP request.*/
	@Override
	protected void onPostExecute(Void arg) { alertSystem(); }
	
	/**Does the logging operation executed in onPostExecute.*/
	protected void alertSystem() {
		if (responseCode == -1 ) {
			Log.w("HTTPAsync", "WARNING: the responseCode was never set; HTTPAsync is unable check validity.");
		} else if (responseCode != 200) {
			Log.e("HTTPAsync", "Received HTTP response code " + responseCode);
		}
	}
}
