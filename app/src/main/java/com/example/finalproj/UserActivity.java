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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproj.model.GalleryAdapter;
import com.example.finalproj.model.Item;
import com.example.finalproj.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserActivity extends BaseActivity implements View.OnClickListener {

    private RecyclerView rvGallery;
    private GalleryAdapter adapter;
    private List<Item> galleryItems = new ArrayList<>();
    private DatabaseService databaseService;

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
        rvGallery = findViewById(R.id.rvGallery);
        rvGallery.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new GalleryAdapter(this, galleryItems);
        rvGallery.setAdapter(adapter);

        databaseService = DatabaseService.getInstance();
        loadLatestItems();

    }
    private void loadLatestItems() {
        databaseService.getItemList(new DatabaseService.DatabaseCallback<List<Item>>() {
            @Override
            public void onCompleted(List<Item> items) {
                galleryItems.clear();

                // Reversing the chronologically generated Firebase IDs to put the newest at the top
                Collections.reverse(items);

                // Capture only the top 4 items
                for (int i = 0; i < items.size() && i < 4; i++) {
                    galleryItems.add(items.get(i));
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("UserActivity", "Firebase error", e);
            }
        });
    }
    @Override
    public void onClick(View view) {

    }
    protected int getNavigationMenuItemId() {
        return R.id.nav_home;
    }
}