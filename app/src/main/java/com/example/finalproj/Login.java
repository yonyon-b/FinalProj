package com.example.finalproj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalproj.services.DatabaseService;
import com.google.android.gms.common.util.SharedPreferencesUtils;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "Login";
    public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences sharedpreferences;
    private String emailPref, passPref;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);
        btnLogin = findViewById(R.id.btn_login_login);
        databaseService=DatabaseService.getInstance();

        btnLogin.setOnClickListener(this);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        emailPref = sharedpreferences.getString("email","");
        passPref = sharedpreferences.getString("password","");
        etEmail.setText(emailPref);
        etPassword.setText(passPref);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btnLogin.getId()) {
            Log.d(TAG, "onClick: Login button clicked");

            /// get the email and password entered by the user
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            /// log the email and password
            Log.d(TAG, "onClick: Email: " + email);
            Log.d(TAG, "onClick: Password: " + password);

            Log.d(TAG, "onClick: Validating input...");
            /// Validate input

            Log.d(TAG, "onClick: Logging in user...");

            /// Login user
            loginUser(email, password);
        }
    }
    private void loginUser(String email, String password) {
        databaseService.LoginUser(email, password, new DatabaseService.DatabaseCallback<String>() {
            /// Callback method called when the operation is completed
            @Override
            public void onCompleted(String  uid) {
                Log.d(TAG, "onCompleted: User logged in: " + uid.toString());
                /// save the user data to shared preferences
                // SharedPreferencesUtil.saveUser(LoginActivity.this, user);
                /// Redirect to main activity and clear back stack to prevent user from going back to login screen
                Intent mainIntent = new Intent(Login.this, UserActivity.class);
                /// Clear the back stack (clear history) and start the MainActivity
                startActivity(mainIntent);
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "onFailed: Failed to retrieve user data", e);
                /// Show error message to user
                etPassword.setError("Invalid email or password");
                etPassword.requestFocus();
                /// Sign out the user if failed to retrieve user data
                /// This is to prevent the user from being logged in again
                //SharedPreferencesUtil.signOutUser(LoginActivity.this);
            }
        });
    }
}