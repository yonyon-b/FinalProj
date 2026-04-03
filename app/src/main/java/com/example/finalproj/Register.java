package com.example.finalproj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalproj.model.User;
import com.example.finalproj.services.DatabaseService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Register";
    public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences sharedpreferences;
    TextInputEditText etFname, etLname, etMail, etPhone, etPassword;
    TextInputLayout boxFname, boxLname, boxEmail, boxPhone, boxPassword;
    String fName, lName, email, phone, password;
    Button btnSubmit;
    TextView tvLogin;
    private DatabaseService databaseService;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseService=DatabaseService.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etFname = findViewById(R.id.etFname);
        etLname = findViewById(R.id.etLname);
        etMail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);

        boxFname = findViewById(R.id.boxFname);
        boxLname = findViewById(R.id.boxLname);
        boxEmail = findViewById(R.id.boxEmail);
        boxPhone = findViewById(R.id.boxPhone);
        boxPassword = findViewById(R.id.boxPassword);

        btnSubmit = findViewById(R.id.btnSubmit);
        tvLogin = findViewById(R.id.tvLogin);

        btnSubmit.setOnClickListener(this);
        tvLogin.setOnClickListener(this);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
    }

    public void onClick(View v) {

        if (v.getId() == btnSubmit.getId()) {
            Log.d(TAG, "onClick: Register button clicked");

            /// get the input from the user
            fName = etFname.getText().toString();
            lName = etLname.getText().toString();
            email = etMail.getText().toString();
            phone = etPhone.getText().toString();
            password = etPassword.getText().toString();

            if (!validateInput(fName, lName, email, phone, password)){
                return;
            }

            /// Validate input
            Log.d(TAG, "onClick: Registering user...");

            /// Register user
            registerUser(fName, lName, phone, email, password);
        }
        else if (v.getId() == tvLogin.getId()){
            Intent i = new Intent(this, Login.class);
            startActivity(i);
        }
    }
    /// Register the user
    private void registerUser(String fname, String lname, String phone, String email, String password) {
        Log.d(TAG, "registerUser: Registering user...");

        databaseService.checkIfEmailExists(email, new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(Boolean exists) {
                if (exists) {
                    Log.e(TAG, "onCompleted: Email already exists");
                    /// show error message to user
                    Toast.makeText(Register.this, "Email already exists", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(authTask -> {

                                if (!authTask.isSuccessful()) {
                                    Toast.makeText(Register.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                User user = new User(uid, fname, lname, email, phone, password, false);

                                createUserInDatabase(user);
                            });

                    SharedPreferences.Editor editor = sharedpreferences.edit();

                    editor.putString("email", email);
                    editor.putString("password", password);

                    editor.commit();
                }
            }
            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "onFailed: Failed to check if email exists", e);
                /// show error message to user
                Toast.makeText(Register.this, "Failed to register user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createUserInDatabase(User user) {
        databaseService.createNewUser(user, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                Log.d(TAG, "createUserInDatabase: User created successfully");
                /// save the user to shared preferences

                Log.d(TAG, "createUserInDatabase: Redirecting to MainActivity");
                /// Redirect to MainActivity and clear back stack to prevent user from going back to register screen
                Intent intent = new Intent(Register.this, ProfilePicSetUp.class);
                /// clear the back stack (clear history) and start the MainActivity
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "createUserInDatabase: Failed to create user", e);
                /// show error message to user
                Toast.makeText(Register.this, "Failed to register user", Toast.LENGTH_SHORT).show();
                /// sign out the user if failed to register

            }
        });
    }
    private boolean validateInput(String fName, String lName, String email, String phone, String password) {
        boolean valid = true;

        // first name (3 - 20 char)
        if (fName.length() < 3 || fName.length() > 20) {
            boxFname.setBoxStrokeColor(Color.parseColor("#e1403d"));
            boxFname.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#e1403d")));
            boxFname.setHint("Invalid First Name! (3-20 characters)");
            valid = false;
        } else {
            boxFname.setBoxStrokeColor(Color.parseColor("#000000"));
            boxFname.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#000000")));
            boxFname.setHint("First Name");
        }

        // last name (3 - 20 char)
        if (lName.length() < 3 || lName.length() > 20) {
            boxLname.setBoxStrokeColor(Color.parseColor("#e1403d"));
            boxLname.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#e1403d")));
            boxLname.setHint("Invalid Last Name! (3-20 characters)");
            valid = false;
        } else {
            boxLname.setBoxStrokeColor(Color.parseColor("#000000"));
            boxLname.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#000000")));
            boxLname.setHint("Last Name");
        }

        // email check with patterns util
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            boxEmail.setBoxStrokeColor(Color.parseColor("#e1403d"));
            boxEmail.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#e1403d")));
            boxEmail.setHint("Invalid email format!");
            valid = false;
        } else {
            boxEmail.setBoxStrokeColor(Color.parseColor("#000000"));
            boxEmail.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#000000")));
            boxEmail.setHint("Email");
        }

        // phone number check with patterns
        if (phone.isEmpty() || !Patterns.PHONE.matcher(phone).matches() || phone.length() < 7) {
            boxPhone.setBoxStrokeColor(Color.parseColor("#e1403d"));
            boxPhone.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#e1403d")));
            boxPhone.setHint("Invalid phone number!");
            valid = false;
        } else {
            boxPhone.setBoxStrokeColor(Color.parseColor("#000000"));
            boxPhone.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#000000")));
            boxPhone.setHint("Phone");
        }

        // password (6 - 20 char)
        if (password.length() < 6 || password.length() > 20) {
            boxPassword.setBoxStrokeColor(Color.parseColor("#e1403d"));
            boxPassword.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#e1403d")));
            boxPassword.setHint("Must be 6-20 characters!");
            valid = false;
        } else {
            boxPassword.setBoxStrokeColor(Color.parseColor("#000000"));
            boxPassword.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#000000")));
            boxPassword.setHint("Password");
        }

        return valid;
    }
}