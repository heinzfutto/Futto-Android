package org.futto.app.ui.registration;

import org.futto.app.R;
import org.futto.app.RunningBackgroundServiceActivity;
import org.futto.app.storage.PersistentData;
import org.futto.app.survey.TextFieldKeyboard;
import org.futto.app.ui.utils.AlertsManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class PhoneNumberEntryActivity extends RunningBackgroundServiceActivity {
	private EditText primaryCarePhone;
	private EditText passwordResetPhone;
	private int phoneNumberLength = 10;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phone_number_entry);
		primaryCarePhone = (EditText) findViewById(R.id.primaryCareNumber);
		passwordResetPhone = (EditText) findViewById(R.id.passwordResetNumber);

		TextFieldKeyboard textFieldKeyboard = new TextFieldKeyboard( getApplicationContext() );
		textFieldKeyboard.makeKeyboardBehave(primaryCarePhone);
		textFieldKeyboard.makeKeyboardBehave(passwordResetPhone);
	}
	
	public void checkAndPromptConsent(View view) {
		String primary = primaryCarePhone.getText().toString().replaceAll("\\D+", "");
		String reset = passwordResetPhone.getText().toString().replaceAll("\\D+", "");
		
		if (primary == null || primary.length() == 0 || reset == null || reset.length() == 0 ){
			AlertsManager.showAlert( getString(R.string.enter_phone_numbers), this );
			return;
		}
		if (primary.length() != phoneNumberLength || reset.length() != phoneNumberLength){
			AlertsManager.showAlert( String.format( getString(R.string.phone_number_length_error), phoneNumberLength), this );
			return;
		}
		
		PersistentData.setPrimaryCareNumber(primary);
		PersistentData.setPasswordResetNumber(reset);
		startActivity(new Intent(getApplicationContext(), ConsentFormActivity.class));
		finish();
	}
}
