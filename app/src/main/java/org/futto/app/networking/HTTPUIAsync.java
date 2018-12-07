package org.futto.app.networking;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import org.futto.app.R;

//TODO: Low priority. Eli. Redoc.

/**HTTPAsync is a... special AsyncTask for handling network (HTTP) requests using our PostRequest class.
 * HTTPAsync handles the asynchronous requirement for UI threads, and automatically handles user
 * notification for the well defined HTTP errors.
 * 
 * HTTPAsync objects start executing on instantiation. While working it pops up an android UI spinner.
 * If the spinner UI element ("progressBar"?) is not declared in the activity's manifest it will instead run "silently" 
 * 
 * Inside your overridden doInBackground function you must assign the HTTP return value to response (as an int).
 *
 * @author Eli */
public class HTTPUIAsync extends HTTPAsync {
	//Private UI element
	private View alertSpinner;
	private Button submitButton;
	// Common variables
	protected Activity activity;

	/**An HTTPAsyc instance will begin execution immediately upon instantiation.
	 * @param url a string containing The URL with which you will connect. 
	 * @param activity The current visible activity */
	public HTTPUIAsync(String url, Activity activity) {
		super(url);
		this.activity = activity;
		this.execute(); //Wow, you can do this?
	}
	
	/** You may want to override the onPreExecute function (your pre-logic should occur outside
	 * the instantiation of the HTTPAsync instance), if you do you should call super.onPreExecute()
	 * as the first line in your custom logic. This is when the spinner will appear.*/
	@Override
	protected void onPreExecute() {
		// If there's a progress spinner, make it appear
		alertSpinner = (ProgressBar) activity.findViewById(R.id.progressBar);
		if (alertSpinner != null) alertSpinner.setVisibility(View.VISIBLE);

		// If there's a submit button, disable it so the user can't submit twice
		submitButton = (Button) activity.findViewById(R.id.submitButton);
		if (submitButton != null) submitButton.setEnabled(false);
	}
	
	/** Your code should override the onPostExecute function, call super.onPostExecute(), and handle
	 * any additional special response and user notification logic required by your code.
	 * */
	@Override
	protected void onPostExecute(Void arg) {
		super.onPostExecute(arg);

		// Hide the progress spinner
		if (alertSpinner != null) alertSpinner.setVisibility(View.GONE);

		// Re-enable the submit button
		if (submitButton != null) submitButton.setEnabled(true);
	}
}
