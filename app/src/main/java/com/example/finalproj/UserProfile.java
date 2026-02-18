package com.example.finalproj;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproj.model.ImageUtil;
import com.example.finalproj.model.User;
import com.example.finalproj.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class UserProfile extends BaseActivity implements View.OnClickListener {
    private TextView userName, phoneNumber, mail;
    private String uid, fullName, phoneNum, email;
    private ImageView pfp, editProfileImg;
    private RecyclerView itemListForUser;
    private FirebaseAuth mAuth;
    private DatabaseService databaseService;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        databaseService = DatabaseService.getInstance();
        userName = findViewById(R.id.txtUserNameProfile);
        phoneNumber = findViewById(R.id.txtUserPhoneProfile);
        mail = findViewById(R.id.txtUserEmailProfile);
        pfp = findViewById(R.id.userProfilePfp);
        editProfileImg = findViewById(R.id.imgEditProfile);
        itemListForUser = findViewById(R.id.rvUserProfile);
        editProfileImg.setOnClickListener(this);


        Intent i = getIntent();
        uid = i.getStringExtra("USER_UID");
        if (uid == null)
            uid = mAuth.getCurrentUser().getUid();
        databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                fullName = user.getfName() + " " + user.getlName();
                phoneNum = user.getPhone();
                email = user.getEmail();

                userName.setText(fullName);
                phoneNumber.setText(phoneNum);
                mail.setText(email);
                if (user.getProfilePicture() != null){
                    pfp.setImageBitmap(ImageUtil.convertFrom64base(user.getProfilePicture()));
                }
            }

            @Override
            public void onFailed(Exception e) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == editProfileImg.getId()){
            Intent i = new Intent(this, EditProfile.class);
            startActivity(i);
        }
    }
}