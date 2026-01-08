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

import com.example.finalproj.model.User;
import com.example.finalproj.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class BaseActivity extends AppCompatActivity{
    private static final String TAG = "BaseActivity";
    private FirebaseAuth mAuth;
    private DatabaseService databaseService;
    private User currentUser;

    @Override
    public void setContentView(int layoutResID) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Inflate base layout
        View base = inflater.inflate(R.layout.activity_base, null);

        // Inflate child layout inside placeholder
        FrameLayout content = base.findViewById(R.id.content_frame);
        inflater.inflate(layoutResID, content, true);

        super.setContentView(base);

        Toolbar toolBar = findViewById(R.id.toolBarBase);
        setSupportActionBar(toolBar);
        toolBar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.overflow_menu_icon));

        if (shouldShowBackButton()) {
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_arrow);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem item = menu.findItem(R.id.action_sign_out);
        SpannableString str = new SpannableString(item.getTitle());
        str.setSpan(new ForegroundColorSpan(Color.RED), 0, str.length(), 0);
        item.setTitle(str);

        /// Admin page visibility & color
        DatabaseService.getInstance().getUser(FirebaseAuth.getInstance().getCurrentUser().getUid(), new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User object) {
                currentUser = object;
                if (currentUser.getAdmin()) {
                    MenuItem item2 = menu.findItem(R.id.action_admin);
                    item2.setVisible(true);
                    SpannableString str2 = new SpannableString(item2.getTitle());
                    str2.setSpan(new ForegroundColorSpan(Color.CYAN), 0, str2.length(), 0);
                    item2.setTitle(str2);
                }
            }

            @Override
            public void onFailed(Exception e) {
                Log.d(TAG, "onFailed: failed" + e);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings){
            Intent i = new Intent(this, Settings.class);
            startActivity(i);
        }
        else if (item.getItemId() == R.id.action_profile){
            Intent i = new Intent(this, UserProfile.class);
            startActivity(i);
        }
        else if (item.getItemId() == R.id.action_admin){
            Intent i = new Intent(this, MainAdmin.class);
            startActivity(i);
        }
        else if (item.getItemId() == R.id.action_sign_out){
            mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();
            Intent i = new Intent(this, Register.class);
            startActivity(i);
        }
        return true;
    }
    protected boolean shouldShowBackButton() {
        return true;
    }

    // Handle back button click
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
    }

}