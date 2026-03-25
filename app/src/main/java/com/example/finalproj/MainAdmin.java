package com.example.finalproj;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalproj.services.DatabaseService;

public class MainAdmin extends BaseActivity implements View.OnClickListener {
    Button btnUserList;
    TextView tvTotalUsers, tvTotalItems, tvTotalFound, tvTotalLost;
    DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalItems = findViewById(R.id.tvTotalItems);
        tvTotalFound = findViewById(R.id.tvTotalFound);
        tvTotalLost = findViewById(R.id.tvTotalLost);
        databaseService = DatabaseService.getInstance();
        btnUserList = findViewById(R.id.btnUserList);
        btnUserList.setOnClickListener(this);
        loadDashBoardStats();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btnUserList.getId()){
            Intent i = new Intent(this, UserList.class);
            startActivity(i);
        }
    }
    private void loadDashBoardStats() {
        databaseService.getNodeCount("users", new DatabaseService.DatabaseCallback<Long>() {
            @Override
            public void onCompleted(Long count) {
                tvTotalUsers.setText("Total Users: " + count.toString());
                Log.d("AdminStats", "Total Users: " + count);
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("AdminStats", "Failed to count users", e);
            }
        });
        databaseService.getNodeCount("items", new DatabaseService.DatabaseCallback<Long>() {
            @Override
            public void onCompleted(Long count) {
                tvTotalItems.setText("Total Items: " + count.toString());
                Log.d("AdminStats", "Total Items: " + count);
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("AdminStats", "Failed to count items", e);
            }
        });
        databaseService.getNodeCountWithFilter("items", "lost", true, new DatabaseService.DatabaseCallback<Long>() {
            @Override
            public void onCompleted(Long count) {
                Log.d("AdminStats", "Total Lost Items: " + count);
                tvTotalLost.setText("Lost: " + String.valueOf(count));
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("AdminStats", "Failed to count lost items", e);
            }
        });
        databaseService.getNodeCountWithFilter("items", "lost", false, new DatabaseService.DatabaseCallback<Long>() {
            @Override
            public void onCompleted(Long count) {
                Log.d("AdminStats", "Total Found Items: " + count);
                tvTotalFound.setText("Found: " + String.valueOf(count));
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("AdminStats", "Failed to count found items", e);
            }
        });
    }
    protected int getNavigationMenuItemId() {
        return 0;
    }
}