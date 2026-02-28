package com.example.finalproj;

import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalproj.model.ImageUtil;
import com.example.finalproj.model.Item;
import com.example.finalproj.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;

public class AddItem extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "AddItem";
    EditText itemName, itemLocation, itemDesc;
    Spinner itemType;
    Button btnSelect, btnCamera, btnSubmit;
    NumberPicker day, month, year;
    ImageView img;
    Item item;
    private DatabaseService databaseService;
    private FirebaseAuth mAuth;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri cameraImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_item);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initView();

        takePictureLauncher =
                registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                    if (success) {
                        img.setImageURI(cameraImageUri);
                    }
                });

        pickImageLauncher =
                registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) {
                        img.setImageURI(uri);
                    }
                });
    }
    @Override
    public void onClick(View view) {
        if (view.getId() == btnSubmit.getId()){
            item.setId(databaseService.generateItemId());
            item.setName(itemName.getText().toString());
            item.setLost(itemType.getSelectedItem().toString().equals("Lost"));
            item.setPosition(itemLocation.getText().toString());
            item.setDate(day.getValue() + "/" + month.getValue() + "/" + year.getValue());
            item.setPic(ImageUtil.convertTo64Base(img));
            item.setDetails(itemDesc.getText().toString());
            item.setUserId(mAuth.getCurrentUser().getUid());
            databaseService.createNewItem(item, new DatabaseService.DatabaseCallback<Void>() {
                @Override
                public void onCompleted(Void object) {
                    Log.d(TAG, "Item created successfully");

                    Intent i = new Intent(AddItem.this, UserActivity.class);
                    startActivity(i);
                }

                @Override
                public void onFailed(Exception e) {
                    Log.d(TAG, "Failed to create item", e);
                }
            });
        }
        if (view.getId() == btnSelect.getId()){
            pickImageLauncher.launch("image/*");
        }
        if (view.getId() == btnCamera.getId()){
            openCamera();
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
    public void initView(){
        itemName = findViewById(R.id.etItemName);
        itemLocation = findViewById(R.id.etItemLocation);
        itemDesc = findViewById(R.id.etItemDesc);
        itemType = findViewById(R.id.spItemType);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.LostOrFound, R.layout.spinner_add_item);
        adapter.setDropDownViewResource(R.layout.spinner_add_item);
        itemType.setAdapter(adapter);

        btnSelect = findViewById(R.id.btnSelectImg);
        btnCamera = findViewById(R.id.btnCamera);
        btnSubmit = findViewById(R.id.btnItemSubmit);

        img = findViewById(R.id.imgItem);
        day = findViewById(R.id.npItemDay);
        month = findViewById(R.id.npItemMonth);
        year = findViewById(R.id.npItemYear);

        day.setMinValue(1);
        day.setMaxValue(31);
        month.setMinValue(1);
        month.setMaxValue(12);
        year.setMinValue(2025);
        year.setMaxValue(2026);

        databaseService = DatabaseService.getInstance();
        mAuth = FirebaseAuth.getInstance();

        btnSelect.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);

        item = new Item();
    }
}