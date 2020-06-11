package org.futto.app.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.futto.app.R;
import org.futto.app.ui.registration.RegisterActivity;

import androidx.appcompat.app.AppCompatActivity;

public class EntryActivity extends AppCompatActivity implements View.OnClickListener{
    private Button loginBtn;
    private Button registerBtn;
    private Button forgotPwdBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        loginBtn = (Button) findViewById(R.id.login_button);
        registerBtn = (Button) findViewById(R.id.register_button);
        forgotPwdBtn = (Button) findViewById(R.id.forgot_password_button);

        loginBtn.setOnClickListener(this);
        registerBtn.setOnClickListener(this);
        forgotPwdBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_button:
                //navigate to login page
                Intent loginIntent = new Intent(EntryActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                break;
            case R.id.register_button:
                //navigate to register page
                Intent registerIntent = new Intent(EntryActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
                break;
            case R.id.forgot_password_button:
                //show forgot password toast
                Toast toast = Toast.makeText(EntryActivity.this, "Contact ridehailing@andrew.cmu.edu with your user account", Toast.LENGTH_SHORT);
                toast.show();
                break;
        }

    }
}