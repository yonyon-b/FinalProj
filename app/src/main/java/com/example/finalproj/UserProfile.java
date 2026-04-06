package com.example.finalproj;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproj.model.ImageUtil;
import com.example.finalproj.model.Item;
import com.example.finalproj.model.ItemRecyclerAdapter;
import com.example.finalproj.model.User;
import com.example.finalproj.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class UserProfile extends BaseActivity implements View.OnClickListener {
    private TextView userName, phoneNumber, mail, emptyList;
    private String uid, fullName, phoneNum, email;
    private ImageView pfp;
    private Button editProfile, allItems;
    private ImageButton ibSettings;
    private RecyclerView userItemList;
    private ItemRecyclerAdapter adapter;
    private ArrayList<Item> dataList = new ArrayList<>();
    private FirebaseAuth mAuth;
    private DatabaseService databaseService;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        databaseService = DatabaseService.getInstance();
        userName = findViewById(R.id.txtUserNameProfile);
        phoneNumber = findViewById(R.id.txtUserPhoneProfile);
        mail = findViewById(R.id.txtUserEmailProfile);
        pfp = findViewById(R.id.userProfilePfp);
        editProfile = findViewById(R.id.btnEditProfile);
        userItemList = findViewById(R.id.rvUserProfile);
        ibSettings = findViewById(R.id.ibSettings);
        emptyList = findViewById(R.id.tvEmptyList);
        allItems = findViewById(R.id.btnAllPostedItems);
        allItems.setOnClickListener(this);
        editProfile.setOnClickListener(this);
        ibSettings.setOnClickListener(this);

        userItemList.setLayoutManager(new LinearLayoutManager(this));
        userItemList.setHasFixedSize(true);
        adapter = new ItemRecyclerAdapter(this, dataList);
        userItemList.setAdapter(adapter);

        Intent i = getIntent();
        uid = i.getStringExtra("USER_UID");
        if (uid == null)
            uid = mAuth.getCurrentUser().getUid();
        else{
            editProfile.setText("Message User");
            ibSettings.setVisibility(View.GONE);
        }
        databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                fullName = user.getfName() + " " + user.getlName();
                phoneNum = user.getPhone();
                email = user.getEmail();

                userName.setText(fullName);
                phoneNumber.setText(phoneNum);
                mail.setText(email);
                if (user.getProfilePicture() != null){
                    pfp.setImageBitmap(ImageUtil.convertFrom64base(user.getProfilePicture()));
                }
            }
            @Override
            public void onFailed(Exception e) {

            }
        });
        loadUserItems(uid);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == editProfile.getId()){
            Intent i;
            if (editProfile.getText().toString().equals("Edit Profile")) {
                i = new Intent(this, EditProfile.class);
            }
            else{
                i = new Intent(this, ChatActivity.class);
                i.putExtra("otherUserId", getIntent().getStringExtra("USER_UID"));
            }
            startActivity(i);
        }
        else if (v.getId() == allItems.getId()){
            Intent intent = new Intent(this, ItemList.class);
            if (fullName != null && !fullName.isEmpty()) {
                intent.putExtra("SEARCH_QUERY", fullName.trim());
            }
            startActivity(intent);
        }
        else if (v.getId() == ibSettings.getId()){
            Intent i = new Intent(this, Settings.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
    }
    private void loadUserItems(String profileUid) {
        databaseService.getItemList(new DatabaseService.DatabaseCallback<List<Item>>() {
            @Override
            public void onCompleted(List<Item> items) {
                dataList.clear();
                List<Item> userItems = new ArrayList<>();

                for (Item item : items) {
                    if (item.getUserId() != null && item.getUserId().equals(profileUid)) {
                        userItems.add(item);
                    }
                }

                int size = userItems.size();
                if (size > 2) {
                    dataList.addAll(userItems.subList(size - 2, size));
                }
                else if (size == 0){
                    userItemList.setVisibility(View.GONE);
                    emptyList.setVisibility(View.VISIBLE);
                }
                else {
                    dataList.addAll(userItems);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("UserProfile", "Failed to load items for user", e);
            }
        });
    }
    protected int getNavigationMenuItemId() {
        return R.id.nav_profile;
    }
}