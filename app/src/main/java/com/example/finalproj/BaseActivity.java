package com.example.finalproj;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalproj.model.User;
import com.example.finalproj.services.DatabaseService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    @Override
    public void setContentView(int layoutResID) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Inflate base layout
        View base = inflater.inflate(R.layout.activity_base, null);

        // Inflate child layout inside placeholder
        FrameLayout content = base.findViewById(R.id.content_frame);
        inflater.inflate(layoutResID, content, true);

        super.setContentView(base);
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigation, (v, insets) -> {
            return insets;
        });
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, UserActivity.class));
                return true;
            } else if (itemId == R.id.nav_add_item) {
                startActivity(new Intent(this, AddItem.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, UserProfile.class));
                return true;
            } else if (itemId == R.id.nav_list_item) {
                startActivity(new Intent(this, ItemList.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
    }
}