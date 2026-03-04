package com.example.finalproj;

import android.os.Bundle;
import android.util.Log;
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

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

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
                filterList(newText);
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
    private void filterList(String text) {
        ArrayList<Item> filteredList = new ArrayList<>();

        for (Item item : allItems) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }

        dataList.clear();
        if (filteredList.isEmpty()) {
            // "No Data Found"
        } else {
            dataList.addAll(filteredList);
        }
        adapter.notifyDataSetChanged();
    }
}