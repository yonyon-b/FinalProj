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
import com.example.finalproj.services.DatabaseService;

import androidx.appcompat.widget.SearchView;
import java.util.ArrayList;
import java.util.List;

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

        loadItemsFromFirebase();
    }
    private void loadItemsFromFirebase() {
        databaseService.getItemList(new DatabaseService.DatabaseCallback<List<Item>>() {
            @Override
            public void onCompleted(List<Item> items) {
                dataList.clear();
                allItems.clear();

                allItems.addAll(items);
                dataList.addAll(items);

                adapter.notifyDataSetChanged();
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
        if (isLost == null){
            btnFilterAll.setBackgroundColor(getColor(R.color.toolBarItems));
            btnFilterFound.setBackgroundColor(getColor(R.color.bgCardGreen));
            btnFilterLost.setBackgroundColor(getColor(R.color.bgCardGreen));
        }
        else if (isLost){
            btnFilterAll.setBackgroundColor(getColor(R.color.bgCardGreen));
            btnFilterFound.setBackgroundColor(getColor(R.color.bgCardGreen));
            btnFilterLost.setBackgroundColor(getColor(R.color.toolBarItems));
        }
        else{
            btnFilterAll.setBackgroundColor(getColor(R.color.bgCardGreen));
            btnFilterFound.setBackgroundColor(getColor(R.color.toolBarItems));
            btnFilterLost.setBackgroundColor(getColor(R.color.bgCardGreen));
        }
        applyFilters();
    }

    private void applyFilters() {
        dataList.clear();
        for (Item item : allItems) {
            boolean matchesSearch = true;
            boolean matchesCategory = true;

            if (currentSearchText != null && !currentSearchText.trim().isEmpty()) {
                if (!item.getName().toLowerCase().contains(currentSearchText.toLowerCase())) {
                    matchesSearch = false;
                }
            }
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
            //No items found
        }
        adapter.notifyDataSetChanged();
    }
}