package com.example.finalproj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import com.example.finalproj.model.Item;
import com.example.finalproj.model.User;
import com.example.finalproj.services.DatabaseService;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    private String currentTheme;

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
        DatabaseService.getInstance().getUser(FirebaseAuth.getInstance().getCurrentUser().getUid(), new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User currentUser) {
                if (currentUser.getAdmin()){
                    bottomNavigation.getMenu().findItem(R.id.nav_admin).setVisible(true);
                }
            }
            @Override
            public void onFailed(Exception e) {

            }
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
            else if (itemId == R.id.nav_admin) {
                startActivity(new Intent(this, MainAdmin.class));
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
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SharedPreferences prefs = getSharedPreferences("preferences_" + userId, Context.MODE_PRIVATE);

        currentTheme = prefs.getString("theme_preference", "default");
        switch (currentTheme) {
            case "dark":
                setTheme(R.style.Theme_FinalProj_Dark);
                break;
            case "light":
                EdgeToEdge.enable(this, SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
                setTheme(R.style.Theme_FinalProj_Light);
                break;
            case "default":
            default:
                setTheme(R.style.Theme_FinalProj_Default);
                break;
        }
        super.onCreate(savedInstanceState);

        // enable edgeToEdge after creating view if not in light mode
        if (!currentTheme.equals("light"))
            EdgeToEdge.enable(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        // fetch the current preference
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SharedPreferences prefs = getSharedPreferences("preferences_" + userId, Context.MODE_PRIVATE);
        String selectedTheme = prefs.getString("theme_preference", "default");

        // if the theme in SharedPreferences doesn't match the theme this
        // activity was created with, recreate the activity to apply the new theme
        if (!currentTheme.equals(selectedTheme)) {
            recreate();
        }
        DatabaseService.getInstance().setupPresenceSystem();
    }
}