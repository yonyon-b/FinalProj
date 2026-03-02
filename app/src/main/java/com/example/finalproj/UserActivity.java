package com.example.finalproj;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserActivity extends BaseActivity implements View.OnClickListener {
    CardView btnItemCreate, btnItemList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnItemCreate = findViewById(R.id.btnItemCreate);
        btnItemList = findViewById(R.id.btnItemList);
        btnItemCreate.setOnClickListener(this);
        btnItemList.setOnClickListener(this);

    }
    @Override
    public void onClick(View view) {
        if (view.getId() == btnItemCreate.getId()){
            Intent i = new Intent(this, AddItem.class);
            startActivity(i);
        }
        else if (view.getId() == btnItemList.getId()){
            Intent i = new Intent(this, ItemList.class);
            startActivity(i);
        }
    }
}