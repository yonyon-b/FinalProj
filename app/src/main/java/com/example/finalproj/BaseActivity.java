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

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.activity_base);

        FrameLayout content = findViewById(R.id.content_frame);

        if (content != null) {
            getLayoutInflater().inflate(layoutResID, content, true);
        }

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigation, (v, insets) -> {
            return insets;
        });
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == getNavigationMenuItemId()) {
                return true;
            }
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
            else if (itemId == R.id.nav_chat) {
                startActivity(new Intent(this, ChatList.class));
                return true;
            }
            return false;
        });
        int selectedItemId = getNavigationMenuItemId();
        if (selectedItemId != 0) {
            bottomNavigation.getMenu().findItem(selectedItemId).setChecked(true);
        }
    }
    protected abstract int getNavigationMenuItemId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
    }
}