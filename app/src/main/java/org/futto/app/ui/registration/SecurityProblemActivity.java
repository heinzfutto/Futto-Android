package org.futto.app.ui.registration;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.futto.app.R;
import org.futto.app.RunningBackgroundServiceActivity;
import org.futto.app.networking.HTTPUIAsync;
import org.futto.app.networking.PostRequest;
import org.futto.app.session.ResetPassword;
import org.futto.app.storage.PersistentData;
import org.futto.app.survey.TextFieldKeyboard;
import org.futto.app.ui.user.LoginActivity;
import org.futto.app.ui.utils.AlertsManager;

import java.util.HashMap;

import static org.futto.app.networking.PostRequest.addWebsitePrefix;

/**
 * An activity to manage users who forgot their passwords.
 * @author Dor Samet
 */

@SuppressLint("ShowToast")
public class SecurityProblemActivity extends RunningBackgroundServiceActivity {
	private TextView securityProblemText;
	private EditText securityAnswerInput;
	private Activity currentActivity;
	private Context appContext;
	private String securityAnswer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_security_problem);
		this.currentActivity = SecurityProblemActivity.this;
		this.appContext = currentActivity.getApplicationContext();
		securityProblemText = (TextView) findViewById(R.id.securityProblemText);
		securityAnswerInput = (EditText) findViewById(R.id.securityAnswerInput);
		securityProblemText.setText(PersistentData.getSecurityQuestion());
		securityAnswer = PersistentData.getSecurityAnswer();
	}


	public void authenticateAnswer(){
		if(!securityAnswerInput.getText().equals(securityAnswer)){
			Toast.makeText(appContext, "Incorrect Answer", Toast.LENGTH_LONG).show();
		}else{
			startActivity( new Intent(appContext, ForgotPasswordActivity.class) );
			finish();
		}
	}

}
