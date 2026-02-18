package com.example.finalproj;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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
import com.google.firebase.auth.FirebaseAuth;
import com.yalantis.ucrop.UCrop;

import java.io.File;

public class ProfilePicSetUp extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ProfilePicSetUp";
    private String uid;
    private Button btnSubmit, btnSkip;
    private ImageView imagePfp, imageCamera, imageGallery;
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
        setContentView(R.layout.activity_profile_pic_set_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnSubmit = findViewById(R.id.btnSumbitPfp);
        btnSkip = findViewById(R.id.btnSkipPfp);
        imagePfp = findViewById(R.id.imgChangePfp);
        imageCamera = findViewById(R.id.imgCameraPfp);
        imageGallery = findViewById(R.id.imgGalleryPfp);
        mAuth = FirebaseAuth.getInstance();
        databaseService = DatabaseService.getInstance();
        imageCamera.setOnClickListener(this);
        imageGallery.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        btnSkip.setOnClickListener(this);
        uid = mAuth.getCurrentUser().getUid();

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
        if (v.getId() == imageCamera.getId()){
            openCamera();
        }

        if (v.getId() == imageGallery.getId()){
            pickImageLauncher.launch("image/*");
        }

        if (v.getId() == btnSubmit.getId()){
            databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
                @Override
                public void onCompleted(User user) {
                    user.setProfilePicture(ImageUtil.convertTo64Base(imagePfp));
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
                    Intent i = new Intent(ProfilePicSetUp.this, UserActivity.class);
                    startActivity(i);
                }

                @Override
                public void onFailed(Exception e) {

                }
            });
        }

        if (v.getId() == btnSkip.getId()){
            Intent i = new Intent(this, UserActivity.class);
            startActivity(i);
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
                imagePfp.setImageURI(resultUri);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            if (cropError != null) {
                cropError.printStackTrace();
            }
        }
    }
}