package com.example.finalproj;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproj.model.Item;
import com.example.finalproj.model.ItemRecyclerAdapter;
import com.example.finalproj.model.User;
import com.example.finalproj.services.DatabaseService;
import com.google.android.material.color.MaterialColors;
import android.content.res.ColorStateList;

import androidx.appcompat.widget.SearchView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemList extends BaseActivity {

    private RecyclerView recyclerView;
    private ArrayList<Item> dataList = new ArrayList<>();
    private ArrayList<Item> allItems = new ArrayList<>();
    private ItemRecyclerAdapter adapter;
    private DatabaseService databaseService;
    private SearchView searchView;
    private Button btnFilterAll, btnFilterLost, btnFilterFound;
    private String currentSearchText = "";
    private Boolean currentFilterIsLost = null;
    private Map<String, User> userCache = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        recyclerView = findViewById(R.id.rvItems);
        searchView = findViewById(R.id.searchView);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterLost = findViewById(R.id.btnFilterLost);
        btnFilterFound = findViewById(R.id.btnFilterFound);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        btnFilterAll.setOnClickListener(v -> filterList(null));
        btnFilterLost.setOnClickListener(v -> filterList(true));
        btnFilterFound.setOnClickListener(v -> filterList(false));

        adapter = new ItemRecyclerAdapter(this, dataList);
        recyclerView.setAdapter(adapter);

        databaseService = DatabaseService.getInstance();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });
        loadUsersAndItems();
        String incomingQuery = getIntent().getStringExtra("SEARCH_QUERY");
        if (incomingQuery != null && !incomingQuery.isEmpty()) {
            searchView.setQuery(incomingQuery, false);
            searchView.clearFocus();
        }
    }
    private void loadUsersAndItems() {
        // Fetch all users once and cache them
        databaseService.getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                userCache.clear();
                for (User user : users) {
                    userCache.put(user.getId(), user);
                }
                // Now load the items
                loadItemsFromFirebase();
            }

            @Override
            public void onFailed(Exception e) {
                Log.e("ItemList", "Failed to load users for caching", e);
                // Even if users fail, try to load items
                loadItemsFromFirebase();
            }
        });
    }
    private void loadItemsFromFirebase() {
        databaseService.getItemList(new DatabaseService.DatabaseCallback<List<Item>>() {
            @Override
            public void onCompleted(List<Item> items) {
                allItems.clear();
                allItems.addAll(items);

                applyFilters();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(ItemList.this,
                        "Failed to load items",
                        Toast.LENGTH_SHORT).show();
                Log.e("ItemList", "Firebase error", e);
            }
        });
    }
    protected int getNavigationMenuItemId() {
        return R.id.nav_list_item;
    }

    private void searchList(String text) {
        currentSearchText = text;
        applyFilters();
    }

    private void filterList(Boolean isLost) {
        currentFilterIsLost = isLost;
        // set color for filters
        int selectedColor = MaterialColors.getColor(btnFilterAll, R.attr.filterSelected);
        int notSelectedColor = MaterialColors.getColor(btnFilterAll, R.attr.itemRowBackground);
        int textSelectedColor = MaterialColors.getColor(btnFilterAll, R.attr.itemRowTextColorSecondary);
        int textNotSelectedColor = MaterialColors.getColor(btnFilterAll, R.attr.itemRowTextColorPrimary);

        if (isLost == null){
            btnFilterAll.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
            btnFilterAll.setTextColor(textSelectedColor);
            btnFilterFound.setBackgroundTintList(ColorStateList.valueOf(notSelectedColor));
            btnFilterFound.setTextColor(textNotSelectedColor);
            btnFilterLost.setBackgroundTintList(ColorStateList.valueOf(notSelectedColor));
            btnFilterLost.setTextColor(textNotSelectedColor);
        }
        else if (isLost){
            btnFilterAll.setBackgroundTintList(ColorStateList.valueOf(notSelectedColor));
            btnFilterAll.setTextColor(textNotSelectedColor);
            btnFilterFound.setBackgroundTintList(ColorStateList.valueOf(notSelectedColor));
            btnFilterFound.setTextColor(textNotSelectedColor);
            btnFilterLost.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
            btnFilterLost.setTextColor(textSelectedColor);
        }
        else{
            btnFilterAll.setBackgroundTintList(ColorStateList.valueOf(notSelectedColor));
            btnFilterAll.setTextColor(textNotSelectedColor);
            btnFilterFound.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
            btnFilterFound.setTextColor(textSelectedColor);
            btnFilterLost.setBackgroundTintList(ColorStateList.valueOf(notSelectedColor));
            btnFilterLost.setTextColor(textNotSelectedColor);
        }
        applyFilters();
    }


    private void applyFilters() {
        dataList.clear();
        for (Item item : allItems) {
            boolean matchesSearch = true;
            boolean matchesCategory = true;

            // fetch owner name from local cache
            String ownerName = "";
            User owner = userCache.get(item.getUserId());
            if (owner != null) {
                String fName = owner.getfName() != null ? owner.getfName() : "";
                String lName = owner.getlName() != null ? owner.getlName() : "";
                ownerName = fName + " " + lName;
            }

            // item matches search text (item name or owner name)
            if (currentSearchText != null && !currentSearchText.trim().isEmpty()) {
                String query = currentSearchText.toLowerCase();
                boolean nameMatches = item.getName() != null && item.getName().toLowerCase().contains(query);
                boolean ownerMatches = ownerName.toLowerCase().equals(query);

                if (!nameMatches && !ownerMatches) {
                    matchesSearch = false;
                }
            }

            // item matches lost/found filter
            if (currentFilterIsLost != null) {
                if (item.isLost() != currentFilterIsLost) {
                    matchesCategory = false;
                }
            }

            if (matchesSearch && matchesCategory) {
                dataList.add(item);
            }
        }

        if (dataList.isEmpty()) {
            // No items found
        }
        adapter.notifyDataSetChanged();
    }
}