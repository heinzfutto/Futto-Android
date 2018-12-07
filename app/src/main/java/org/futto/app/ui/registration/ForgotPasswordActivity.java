package org.futto.app.ui.registration;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.futto.app.R;
import org.futto.app.RunningBackgroundServiceActivity;
import org.futto.app.session.ResetPassword;
import org.futto.app.storage.PersistentData;
import org.futto.app.survey.TextFieldKeyboard;

/**
 * @author Dor Samet, Eli Jones
 */
public class ForgotPasswordActivity extends RunningBackgroundServiceActivity {
	private EditText tempPasswordInput;
	private EditText newPasswordInput;
	private EditText confirmNewPasswordInput;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgot_password);

		/* Add the user's Patient ID to the heading in the activity, so the user can tell it to the
		 * administrator when the user calls the research assistant asking for a temporary password. */
		TextView instructionsText = (TextView) findViewById(R.id.forgotPasswordInstructionsText);
		String instructionsTextWithPlaceholder = getApplicationContext().getString(R.string.forgot_password_instructions_text);
		String instructionsTextFilledOut = String.format(instructionsTextWithPlaceholder, PersistentData.getPatientID());
		instructionsText.setText(instructionsTextFilledOut);

		tempPasswordInput = (EditText) findViewById(R.id.forgotPasswordTempPasswordInput);
		newPasswordInput = (EditText) findViewById(R.id.forgotPasswordNewPasswordInput);
		confirmNewPasswordInput = (EditText) findViewById(R.id.forgotPasswordConfirmNewPasswordInput);
		TextFieldKeyboard textFieldKeyboard = new TextFieldKeyboard(getApplicationContext());
		textFieldKeyboard.makeKeyboardBehave(tempPasswordInput);
		textFieldKeyboard.makeKeyboardBehave(newPasswordInput);
		textFieldKeyboard.makeKeyboardBehave(confirmNewPasswordInput);
	}

	/** Kill this activity and go back to the homepage */
	public void cancelButtonPressed(View view) {
		this.finish();
	}

	/** calls the reset password HTTPAsync query. */
	public void registerNewPassword(View view) {
		// Get the user's temporary password (they get this from a human admin by calling the research assistant)
		String tempPassword = tempPasswordInput.getText().toString();

		// Get the new, permanent password the user wants
		String newPassword = newPasswordInput.getText().toString();

		// Get the confirmation of the new, permanent password (should be the same as the previous field)
		String confirmNewPassword = confirmNewPasswordInput.getText().toString();

		/* Pass all three to the ResetPassword class, which will check validity, and, if valid,
		 * reset the permanent password */
		ResetPassword resetPassword = new ResetPassword(this);
		resetPassword.checkInputsAndTryToResetPassword(tempPassword, newPassword, confirmNewPassword);
	}

	public void callResetPassword(View view) {
		super.callResearchAssistant(view);
	}
}
