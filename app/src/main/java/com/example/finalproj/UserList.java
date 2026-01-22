package com.example.finalproj;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproj.model.Item;
import com.example.finalproj.model.ItemAdapter;
import com.example.finalproj.model.User;
import com.example.finalproj.model.UserAdapter;
import com.example.finalproj.services.DatabaseService;

import java.util.ArrayList;
import java.util.List;

public class UserList extends BaseActivity {

    private static final String TAG = "UserList";
    private UserAdapter userAdapter;
    private TextView tvUserCount;
    private DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        RecyclerView usersList = findViewById(R.id.rv_users_list);
        databaseService = DatabaseService.getInstance();
        usersList.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                // Handle user click
                Log.d(TAG, "User clicked: " + user);

                Intent intent = new Intent(UserList.this, UserProfile.class);
                intent.putExtra("USER_UID", user.getId());
                startActivity(intent);
            }

            @Override
            public void onLongUserClick(View view, User user) {
                // Handle long user click
                Log.d(TAG, "User long clicked: " + user);

                PopupMenu popupMenu = new PopupMenu(UserList.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.user_popup_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();

                    if (id == R.id.action_view_profile) {
                        Intent intent = new Intent(UserList.this, UserProfile.class);
                        intent.putExtra("USER_UID", user.getId());
                        startActivity(intent);
                        return true;

                    } else if (id == R.id.action_admin) {
                        // Handle message action
                        Log.d(TAG, "Message user: " + user.getId());
                        return true;

                    } else if (id == R.id.action_delete) {
                        // Handle delete action
                        Log.d(TAG, "Delete user: " + user.getId());
                        return true;
                    }

                    return false;
                });

                popupMenu.show();
            }
        });
        usersList.setAdapter(userAdapter);
    }
    @Override
    protected void onResume() {
        super.onResume();
        databaseService.getUserList(new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<User> users) {
                userAdapter.setUserList(users);
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Failed to get users list", e);
            }
        });
    }
}