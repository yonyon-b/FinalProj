package com.example.finalproj;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalproj.model.ImageUtil;
import com.example.finalproj.model.User;
import com.example.finalproj.services.DatabaseService;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;

import com.yalantis.ucrop.UCrop;

import java.io.File;

public class EditProfile extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "EditProfile";
    private EditText fName, lName, phoneNum;
    private TextInputLayout boxFname, boxLname, boxPhone;
    private String uid;
    private Boolean imageChanged, imageRemoved;
    private Button changePfp, removePfp, submit;
    private ImageView camera, gallery, pfp;
    private LinearLayout picturesLayout, buttonsLayout;
    private DatabaseService databaseService;
    private FirebaseAuth mAuth;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri cameraImageUri;

    @SuppressLint("MissingInflatedId")
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
        boxFname = findViewById(R.id.boxFnameEdit);
        boxLname = findViewById(R.id.boxLnameEdit);
        boxPhone = findViewById(R.id.boxPhoneEdit);
        submit = findViewById(R.id.btnSubmitEdit);
        submit.setOnClickListener(this);
        changePfp = findViewById(R.id.btnChangePfp);
        changePfp.setOnClickListener(this);
        removePfp = findViewById(R.id.btnRemovePfp);
        removePfp.setOnClickListener(this);
        camera = findViewById(R.id.imgCameraChangePfp);
        camera.setOnClickListener(this);
        gallery = findViewById(R.id.imgGalleryChangePfp);
        gallery.setOnClickListener(this);
        pfp = findViewById(R.id.changePfpImg);
        picturesLayout = findViewById(R.id.picturesLayout);
        buttonsLayout = findViewById(R.id.buttons_layout);
        databaseService = DatabaseService.getInstance();
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        imageChanged = false;
        imageRemoved = false;

        databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                fName.setText(user.getfName());
                lName.setText(user.getlName());
                phoneNum.setText(user.getPhone());
                if (user.getProfilePicture() != null){
                    pfp.setImageBitmap(ImageUtil.convertFrom64base(user.getProfilePicture()));
                }
            }

            @Override
            public void onFailed(Exception e) {

            }
        });

        takePictureLauncher =
                registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                    if (success) {
                        startCrop(cameraImageUri);
                    }
                });

        pickImageLauncher =
                registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) {
                        startCrop(uri);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (!validateInput())
            return;
        if (v.getId() == submit.getId()) {
            databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
                @Override
                public void onCompleted(User user) {
                    user.setfName(fName.getText().toString());
                    user.setlName(lName.getText().toString());
                    user.setPhone(phoneNum.getText().toString());
                    if (imageChanged)
                        user.setProfilePicture(ImageUtil.convertTo64Base(pfp));
                    else if (imageRemoved)
                        user.setProfilePicture(null);

                    databaseService.updateUser(user, new DatabaseService.DatabaseCallback<Void>() {
                        @Override
                        public void onCompleted(Void object) {
                            Log.d(TAG, "User updated.");
                            Intent i = new Intent(EditProfile.this, UserProfile.class);
                            startActivity(i);
                            finish();
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Log.e(TAG, "User failed to update", e);
                        }
                    });
                }

                @Override
                public void onFailed(Exception e) {

                }
            });
        }
        else if (v.getId() == changePfp.getId()){
            buttonsLayout.setVisibility(View.GONE);
            picturesLayout.setVisibility(View.VISIBLE);
        }
        else if (v.getId() == removePfp.getId()){
            pfp.setImageResource(R.drawable.user_pfp);
            imageRemoved = true;
            imageChanged = false;
        }
        else if (v.getId() == camera.getId()){
            openCamera();
        }

        else if (v.getId() == gallery.getId()){
            pickImageLauncher.launch("image/*");
        }
    }

    private void openCamera() {
        try {
            File imageFile = File.createTempFile(
                    "photo_", ".jpg",
                    getExternalCacheDir()
            );

            cameraImageUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    imageFile
            );

            takePictureLauncher.launch(cameraImageUri);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void startCrop(@NonNull Uri sourceUri) {

        Uri destinationUri = Uri.fromFile(
                new File(getCacheDir(), "cropped_image.jpg")
        );

        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true); // circle overlay
        options.setShowCropFrame(false);
        options.setShowCropGrid(false);

        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1) // Square for profile pic
                .withOptions(options)
                .start(this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                pfp.setImageURI(resultUri);
                imageChanged = true;
                imageRemoved = false;
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            if (cropError != null) {
                cropError.printStackTrace();
            }
        }
    }
    private boolean validateInput(){
        boolean valid = true;
        String fnameStr = fName.getText().toString();
        String lnameStr = lName.getText().toString();
        String phoneStr = phoneNum.getText().toString();

        // first name (3 - 20 char)
        if (fnameStr.length() < 3 || fnameStr.length() > 20) {
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
        if (lnameStr.length() < 3 || lnameStr.length() > 20) {
            boxLname.setBoxStrokeColor(Color.parseColor("#e1403d"));
            boxLname.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#e1403d")));
            boxLname.setHint("Invalid Last Name! (3-20 characters)");
            valid = false;
        } else {
            boxLname.setBoxStrokeColor(Color.parseColor("#000000"));
            boxLname.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#000000")));
            boxLname.setHint("Last Name");
        }
        // phone number check with patterns
        if (phoneStr.isEmpty() || !Patterns.PHONE.matcher(phoneStr).matches() || phoneStr.length() < 7) {
            boxPhone.setBoxStrokeColor(Color.parseColor("#e1403d"));
            boxPhone.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#e1403d")));
            boxPhone.setHint("Invalid phone number!");
            valid = false;
        } else {
            boxPhone.setBoxStrokeColor(Color.parseColor("#000000"));
            boxPhone.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#000000")));
            boxPhone.setHint("Phone");
        }
        return valid;
    }
    protected int getNavigationMenuItemId() {
        return 0;
    }
}