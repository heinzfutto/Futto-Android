package org.futto.app.session;

import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.futto.app.BackgroundService;
import org.futto.app.R;
import org.futto.app.RunningBackgroundServiceActivity;
import org.futto.app.storage.PersistentData;
import org.futto.app.ui.registration.ResetPasswordActivity;
import org.futto.app.ui.user.AboutActivityLoggedIn;
import org.futto.app.ui.user.GraphActivity;
import org.futto.app.ui.user.LoginActivity;

/**All Activities in the app WHICH REQUIRE THE USER TO BE LOGGED IN extend this Activity.
 * If the user is not logged in, he/she is bumped to a login screen.
 * This Activity also extends RunningBackgroundServiceActivity, which makes the app's key
 * services run before the interface is allowed to interact with it.
 * @author Eli Jones, Josh Zagorsky */
public class SessionActivity extends RunningBackgroundServiceActivity {
	
	/*####################################################################
	########################## Log-in Logic ##############################
	####################################################################*/
	
	/** when onResume is called we need to authenticate the user and
	 * bump them to the login screen if they have timed out. */
	@Override
	protected void onResume() {
		super.onResume();
		PersistentData.initialize(getApplicationContext()); // this function has been rewritten to efficiently handle getting called too much.  Don't worry about it.
		checkPermissionsLogic();
	}
	
	/** When onPause is called we need to set the timeout. */
	@Override
	protected void onPause() {
		super.onPause();
		if (backgroundService != null) {
			//If an activity is active there is a countdown to bump a user to a login screen after
			// some amount of time (setting is pushed by study).  If we leave the session activity
			// we need to cancel that action.
			//This issue has occurred literally once ever (as of February 27 2016) but the prior
			// behavior was broken and caused the app to crash.  Really, this state is incomprehensible
			// (activity is open an mature enough that onPause can occur, yet the background service
			// has not started?) so a crash does at least reboot Beiwe into a functional state,
			// but that obviously has its own problems.  Updated code should merely be bad UX as
			// a user could possibly get bumped to the login screen from another app.
			BackgroundService.clearAutomaticLogoutCountdownTimer(); }
		else { Log.w("SessionActivity bug","the background service was not running, could not cancel UI bump to login screen."); }
	}
	
	@Override
	/** Sets the logout timer, should trigger whenever onResume is called. */
	protected void doBackgroundDependentTasks() {
		// Log.i("SessionActivity", "printed from SessionActivity");
		authenticateAndLoginIfNecessary();
	}
	
	/** If the user is NOT logged in, take them to the login page */
	protected void authenticateAndLoginIfNecessary() {
		if ( PersistentData.isLoggedIn() ) {
			BackgroundService.startAutomaticLogoutCountdownTimer(); }
		else {
			startActivity(new Intent(this, LoginActivity.class) ); }
	}

	/** Display the LoginActivity, and invalidate the login in SharedPreferences */
	protected void logoutUser() {
		PersistentData.logout();
		startActivity(new Intent(this, LoginActivity.class));
	}
	
	
	/*####################################################################
	########################## Common UI #################################
	####################################################################*/
	
	/** Sets up the contents of the menu button. */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.logged_in_menu, menu);
		//menu.findItem(R.id.menu_call_clinician).setTitle(PersistentData.getCallClinicianButtonText());
		return true;
	}

	/** Sets up the behavior of the items in the menu. */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.menu_change_password:
			startActivity(new Intent(getApplicationContext(), ResetPasswordActivity.class));
			return true;
		case R.id.menu_signout:
			logoutUser();
			return true;
		case R.id.menu_about:
			startActivity(new Intent(getApplicationContext(), AboutActivityLoggedIn.class));
			return true;
		case R.id.view_survey_answers:
			startActivity(new Intent(getApplicationContext(), GraphActivity.class));
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
