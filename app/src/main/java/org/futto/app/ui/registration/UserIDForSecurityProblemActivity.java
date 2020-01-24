package org.futto.app.ui.registration;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.futto.app.R;
import org.futto.app.RunningBackgroundServiceActivity;
import org.futto.app.storage.PersistentData;
import org.futto.app.ui.user.LoginActivity;

/**
 * An activity to manage users who forgot their passwords.
 * @author Lexi
 */

@SuppressLint("ShowToast")
public class UserIDForSecurityProblemActivity extends RunningBackgroundServiceActivity {
	private EditText useridText;
	private Activity currentActivity;
	private Context appContext;
	private String userid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userid_security_problem);
		this.currentActivity = UserIDForSecurityProblemActivity.this;
		this.appContext = currentActivity.getApplicationContext();
		useridText = (EditText) findViewById(R.id.useridsecuritytext);

	}


    public void getSecurityProblem(View view){
		userid = useridText.getText().toString();
		StartSecurityPage(userid);
	}

	public void StartSecurityPage(String userid){
		LoginActivity login = new LoginActivity();
		login.getSecurityProblemAndAnswer(this, userid);
	}


	/** Kill this activity and go back to the homepage */
	public void cancelButtonPressed(View view) {
		this.finish();
	}

}
