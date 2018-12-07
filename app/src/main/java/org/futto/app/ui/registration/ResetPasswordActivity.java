package org.futto.app.ui.registration;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.futto.app.R;
import org.futto.app.RunningBackgroundServiceActivity;
import org.futto.app.session.ResetPassword;
import org.futto.app.survey.TextFieldKeyboard;

/**
 * An activity to manage users who forgot their passwords.
 * @author Dor Samet
 */

@SuppressLint("ShowToast")
public class ResetPasswordActivity extends RunningBackgroundServiceActivity {
	private EditText currentPasswordInput;
	private EditText newPasswordInput;
	private EditText confirmNewPasswordInput;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_password);

		currentPasswordInput = (EditText) findViewById(R.id.resetPasswordCurrentPasswordInput);
		newPasswordInput = (EditText) findViewById(R.id.resetPasswordNewPasswordInput);
		confirmNewPasswordInput = (EditText) findViewById(R.id.resetPasswordConfirmNewPasswordInput);
		TextFieldKeyboard textFieldKeyboard = new TextFieldKeyboard(getApplicationContext());
		textFieldKeyboard.makeKeyboardBehave(currentPasswordInput);
		textFieldKeyboard.makeKeyboardBehave(newPasswordInput);
		textFieldKeyboard.makeKeyboardBehave(confirmNewPasswordInput);
	}

	/** Kill this activity and go back to the homepage */
	public void cancelButtonPressed(View view) {
		this.finish();
	}

	/** calls the reset password HTTPAsync query. */
	public void registerNewPassword(View view) {
		// Get the user's current password
		String currentPassword = currentPasswordInput.getText().toString();

		// Get the new, permanent password the user wants
		String newPassword = newPasswordInput.getText().toString();
		
		// Get the confirmation of the new, permanent password (should be the same as the previous field)
		String confirmNewPassword = confirmNewPasswordInput.getText().toString();

		/* Pass all three to the ResetPassword class, which will check validity, and, if valid,
		 * reset the permanent password */
		ResetPassword resetPassword = new ResetPassword(this);
		resetPassword.checkInputsAndTryToResetPassword(currentPassword, newPassword, confirmNewPassword);
	}
}
