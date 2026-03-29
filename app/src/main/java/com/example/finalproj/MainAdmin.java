package com.example.finalproj;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;

public class MainAdmin extends BaseActivity implements View.OnClickListener {
    private Button btnUserList;
    private DatabaseService databaseService;
    private PieChart itemPieChart, userPieChart;
    private long lostCount = -1, foundCount = -1, onlineCount = -1, offlineCount = -1;
    private TextView tvTotalItems, tvTotalUsers, tvOnlineUsers, tvOfflineUsers, tvFoundItems, tvLostItems;

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
        databaseService = DatabaseService.getInstance();
        btnUserList = findViewById(R.id.btnUserList);
        btnUserList.setOnClickListener(this);
        itemPieChart = findViewById(R.id.itemPieChart);
        userPieChart = findViewById(R.id.userPieChart);
        tvTotalItems = findViewById(R.id.tvTotalItems);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvOnlineUsers = findViewById(R.id.tvOnlineUsers);
        tvOfflineUsers = findViewById(R.id.tvOfflineUsers);
        tvFoundItems = findViewById(R.id.tvFoundItems);
        tvLostItems = findViewById(R.id.tvLostItems);

        setupPieChartStyle(itemPieChart, "Items");
        setupPieChartStyle(userPieChart, "Users");
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
        databaseService.getNodeCountWithFilter("items", "lost", true, new DatabaseService.DatabaseCallback<Long>() {
            @Override
            public void onCompleted(Long count) { lostCount = count; checkAndDrawItemChart();
            }
            @Override public void onFailed(Exception e) { }
        });
        databaseService.getNodeCountWithFilter("items", "lost", false, new DatabaseService.DatabaseCallback<Long>() {
            @Override
            public void onCompleted(Long count) { foundCount = count; checkAndDrawItemChart(); }
            @Override public void onFailed(Exception e) { }
        });
        databaseService.getNodeCountWithFilter("users", "isOnline", true, new DatabaseService.DatabaseCallback<Long>() {
            @Override
            public void onCompleted(Long count) { onlineCount = count; checkAndDrawUserChart(); }
            @Override public void onFailed(Exception e) { }
        });

        databaseService.getNodeCountWithFilter("users", "isOnline", false, new DatabaseService.DatabaseCallback<Long>() {
            @Override
            public void onCompleted(Long count) { offlineCount = count; checkAndDrawUserChart(); }
            @Override public void onFailed(Exception e) { }
        });
    }
    private void setupPieChartStyle(PieChart chart, String centerText) {
        chart.getDescription().setEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setCenterText(centerText);
        chart.setCenterTextSize(20f);
        chart.setCenterTextTypeface(Typeface.DEFAULT_BOLD);
        chart.setCenterTextColor(MaterialColors.getColor(this, android.R.attr.textColorPrimary, Color.BLACK));
        chart.getLegend().setEnabled(false);
        chart.setUsePercentValues(true);
    }
    private void checkAndDrawItemChart() {
        if (lostCount != -1 && foundCount != -1) {

            ArrayList<PieEntry> entries = new ArrayList<>();

            if (lostCount > 0) {
                entries.add(new PieEntry(lostCount));
            }
            if (foundCount > 0) {
                entries.add(new PieEntry(foundCount));
            }

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setValueFormatter(new ValueFormatter() {
                @Override public String getFormattedValue(float value) {
                    return (int) value + "%"; }
            });
            dataSet.setColors(new int[]{ Color.parseColor("#FF6B6B"), Color.parseColor("#4ECDC4") });
            dataSet.setValueTextSize(16f);
            dataSet.setValueTextColor(Color.WHITE);

            PieData data = new PieData(dataSet);
            itemPieChart.setData(data);
            itemPieChart.invalidate();
            itemPieChart.animateY(1000);

            tvTotalItems.setText(String.valueOf(lostCount + foundCount));
            tvFoundItems.setText("Found: " + foundCount);
            tvLostItems.setText("Lost: " + lostCount);
        }
    }
    private void checkAndDrawUserChart() {
        if (onlineCount != -1 && offlineCount != -1) {

            ArrayList<PieEntry> entries = new ArrayList<>();

            if (onlineCount > 0) entries.add(new PieEntry(onlineCount));
            if (offlineCount > 0) entries.add(new PieEntry(offlineCount));

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setValueFormatter(new ValueFormatter() {
                @Override public String getFormattedValue(float value) {
                    return (int) value + "%"; }
            });
            dataSet.setColors(new int[]{ Color.parseColor("#4CAF50"), Color.parseColor("#9E9E9E") });
            dataSet.setValueTextSize(16f);
            dataSet.setValueTextColor(Color.WHITE);

            PieData data = new PieData(dataSet);
            userPieChart.setData(data);
            userPieChart.invalidate();
            userPieChart.animateY(1000);

            tvTotalUsers.setText(String.valueOf(onlineCount + offlineCount));
            tvOnlineUsers.setText("Online: " + onlineCount);
            tvOfflineUsers.setText("Offline: " + offlineCount);
        }
    }

    protected int getNavigationMenuItemId() {
        return 0;
    }
}