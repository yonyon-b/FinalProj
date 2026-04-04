package com.example.finalproj;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;

public class AddItem extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "AddItem";
    private EditText itemName, itemLocation, itemDesc;
    private Spinner itemType;
    private Button btnSelect, btnCamera, btnSubmit;
    private NumberPicker day, month, year;
    private ImageView img;
    private TextView tvItemImage;
    private TextInputLayout boxItemName, boxItemLocation, boxItemDesc;
    private Item item;
    private DatabaseService databaseService;
    private FirebaseAuth mAuth;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri cameraImageUri;
    private boolean isImageSet = false;

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
                        isImageSet = true;
                    }
                });

        pickImageLauncher =
                registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                    if (uri != null) {
                        img.setImageURI(uri);
                        isImageSet = true;
                    }
                });
    }
    @Override
    public void onClick(View view) {
        if (view.getId() == btnSubmit.getId()){
            if (!validateInput())
                return;
            item.setId(databaseService.generateItemId());
            item.setName(itemName.getText().toString());
            item.setLost(itemType.getSelectedItem().toString().equals("Lost"));
            item.setPosition(itemLocation.getText().toString());
            item.setDate(day.getValue() + "/" + month.getValue() + "/" + year.getValue());
            item.setPic(ImageUtil.convertTo64Base(img));
            if (!itemDesc.getText().toString().isEmpty())
                item.setDetails(itemDesc.getText().toString());
            else
                item.setDetails("No Details Provided.");
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
    private boolean validateInput(){
        String name = itemName.getText().toString().trim();
        boolean valid = true;
        if (name.length() < 3 || name.length() > 20) {
            boxItemName.setBoxStrokeColor(Color.parseColor("#e1403d"));
            boxItemName.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#e1403d")));
            boxItemName.setHint("Name must be between 3 and 20 characters!");
            valid = false;
        }
        else {
            // change back to black after correcting
            boxItemName.setBoxStrokeColor(Color.parseColor("#000000"));
            boxItemName.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#000000")));
            boxItemName.setHint("Item Name");
        }

        String location = itemLocation.getText().toString().trim();
        if (location.length() > 20) {
            boxItemLocation.setBoxStrokeColor(Color.parseColor("#e1403d"));
            boxItemLocation.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#e1403d")));
            boxItemLocation.setHint("Location must be between 3 and 20 characters!");
            valid = false;
        }
        else {
            boxItemLocation.setBoxStrokeColor(Color.parseColor("#000000"));
            boxItemLocation.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#000000")));
            boxItemLocation.setHint("Item Location (optional)");
        }

        String desc = itemDesc.getText().toString().trim();
        if (!desc.isEmpty()) {
            String[] lines = desc.split("\r\n|\r|\n");
            if (lines.length > 2) {
                boxItemDesc.setBoxStrokeColor(Color.parseColor("#e1403d"));
                boxItemDesc.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#e1403d")));
                boxItemDesc.setHint("Item description must not exceed 2 lines!");
                valid = false;
            }
            else {
                boxItemDesc.setBoxStrokeColor(Color.parseColor("#000000"));
                boxItemDesc.setHintTextColor(ColorStateList.valueOf(Color.parseColor("#000000")));
                itemDesc.setHint("Item Description (optional)");
            }
        }

        if (!isImageSet) {
            tvItemImage.setText("Must add an image of the item!");
            tvItemImage.setTextColor(Color.parseColor("#e1403d"));
            valid = false;
        }
        else {
            tvItemImage.setText("Item Image");
            tvItemImage.setTextColor(Color.parseColor("#000000"));
        }
        return valid;
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
        tvItemImage = findViewById(R.id.tvItemImage);
        boxItemName = findViewById(R.id.boxItemName);
        boxItemLocation = findViewById(R.id.boxItemLocation);
        boxItemDesc = findViewById(R.id.boxItemDesc);

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
        year.setMinValue(2026);
        year.setMaxValue(2026);

        NumberPicker.OnValueChangeListener dateChangeListener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateMaxDays();
            }
        };
        month.setOnValueChangedListener(dateChangeListener);
        year.setOnValueChangedListener(dateChangeListener);
        updateMaxDays();

        databaseService = DatabaseService.getInstance();
        mAuth = FirebaseAuth.getInstance();

        btnSelect.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);

        item = new Item();
    }
    private void updateMaxDays() {
        int m = month.getValue();
        int y = year.getValue();
        int maxDays = 31;

        if (m == 4 || m == 6 || m == 9 || m == 11)
            maxDays = 30;
        else if (m == 2)
            maxDays = 28;
        if (day.getValue() > maxDays) {
            day.setValue(maxDays);
        }
        day.setMaxValue(maxDays);
    }
    @Override
    protected int getNavigationMenuItemId() {
        return R.id.nav_add_item;
    }
}