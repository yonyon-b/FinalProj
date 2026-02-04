package com.example.finalproj;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalproj.model.User;
import com.example.finalproj.services.DatabaseService;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;

public class EditProfile extends BaseActivity implements View.OnClickListener {
    EditText fName, lName, phoneNum;
    String uid;
    Button submit;
    DatabaseService databaseService;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        fName = findViewById(R.id.etFnameEdit);
        lName = findViewById(R.id.etLnameEdit);
        phoneNum = findViewById(R.id.etNumEdit);
        submit = findViewById(R.id.btnSubmitEdit);
        submit.setOnClickListener(this);
        databaseService = DatabaseService.getInstance();
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                fName.setText(user.getfName());
                lName.setText(user.getlName());
                phoneNum.setText(user.getPhone());
            }

            @Override
            public void onFailed(Exception e) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                user.setfName(fName.toString());
                user.setlName(lName.toString());
                user.setPhone(phoneNum.toString());
            }

            @Override
            public void onFailed(Exception e) {

            }
        });
    }
}