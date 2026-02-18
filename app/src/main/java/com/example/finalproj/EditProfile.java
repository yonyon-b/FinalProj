package com.example.finalproj;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;

import com.yalantis.ucrop.UCrop;

import java.io.File;

public class EditProfile extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "EditProfile";
    private EditText fName, lName, phoneNum;
    private String uid;
    private Boolean imageChanged;
    private Button changePfp, submit;
    private ImageView camera, gallery, pfp;
    private LinearLayout picturesLayout;
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
        submit = findViewById(R.id.btnSubmitEdit);
        submit.setOnClickListener(this);
        changePfp = findViewById(R.id.btnChangePfp);
        changePfp.setOnClickListener(this);
        camera = findViewById(R.id.imgCameraChangePfp);
        camera.setOnClickListener(this);
        gallery = findViewById(R.id.imgGalleryChangePfp);
        gallery.setOnClickListener(this);
        pfp = findViewById(R.id.changePfpImg);
        picturesLayout = findViewById(R.id.picturesLayout);
        databaseService = DatabaseService.getInstance();
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
        imageChanged = false;

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
        if (v.getId() == submit.getId()) {
            databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
                @Override
                public void onCompleted(User user) {
                    user.setfName(fName.getText().toString());
                    user.setlName(lName.getText().toString());
                    user.setPhone(phoneNum.getText().toString());
                    if (imageChanged)
                        user.setProfilePicture(ImageUtil.convertTo64Base(pfp));

                    databaseService.updateUser(user, new DatabaseService.DatabaseCallback<Void>() {
                        @Override
                        public void onCompleted(Void object) {
                            Log.d(TAG, "User updated.");
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Log.e(TAG, "User failed to update", e);
                        }
                    });
                    Intent i = new Intent(EditProfile.this, UserProfile.class);
                    startActivity(i);
                }

                @Override
                public void onFailed(Exception e) {

                }
            });
        }
        else if (v.getId() == changePfp.getId()){
            changePfp.setVisibility(View.GONE);
            picturesLayout.setVisibility(View.VISIBLE);
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
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            if (cropError != null) {
                cropError.printStackTrace();
            }
        }
    }
}