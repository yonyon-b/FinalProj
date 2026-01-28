package com.example.finalproj;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproj.model.User;
import com.example.finalproj.model.UserAdapter;
import com.example.finalproj.services.DatabaseService;

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
                Log.d(TAG, "User long clicked: " + user);

                PopupMenu popupMenu = new PopupMenu(UserList.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.user_popup_menu, popupMenu.getMenu());
                MenuItem adminItem = popupMenu.getMenu().findItem(R.id.action_admin);
                if (user.getAdmin()){
                    adminItem.setTitle("Remove Admin");
                }
                else{
                    adminItem.setTitle("Add as Admin");
                }

                popupMenu.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();


                    if (id == R.id.action_view_profile) {
                        Intent intent = new Intent(UserList.this, UserProfile.class);
                        intent.putExtra("USER_UID", user.getId());
                        startActivity(intent);
                        return true;

                    } else if (id == R.id.action_admin) {
                        // Handle message action
                        if (user.getAdmin()){
                            user.setAdmin(false);
                        }
                        else{
                            user.setAdmin(true);
                        }
                        databaseService.updateUser(user, new DatabaseService.DatabaseCallback<Void>() {
                            @Override
                            public void onCompleted(Void object) {
                                Log.d(TAG, "User updated.");
                            }

                            @Override
                            public void onFailed(Exception e) {
                                Log.e(TAG, "User failed to update", e);
                            }
                        });
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