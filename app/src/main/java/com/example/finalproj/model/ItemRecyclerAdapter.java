package com.example.finalproj.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproj.R;
import com.example.finalproj.services.DatabaseService;

import java.util.List;

import android.util.Base64;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class ItemRecyclerAdapter extends RecyclerView.Adapter<ItemRecyclerAdapter.ItemViewHolder> {

    private final Context context;
    private final List<Item> items;
    private final SparseBooleanArray expandState = new SparseBooleanArray();
    private final DatabaseService databaseService;

    public ItemRecyclerAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
        this.databaseService = DatabaseService.getInstance();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_row, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Item item = items.get(position);

        holder.txtName.setText(item.getName());

        String typePrefix = item.isLost() ? "Lost at: " : "Found at: ";
        holder.txtLost.setText(item.isLost() ? "Lost Item" : "Found Item");
        holder.txtDate.setText(typePrefix + item.getDate());

        holder.txtPosition.setText(Html.fromHtml("<b>Location:</b> " + item.getPosition()));
        holder.txtDetails.setText(Html.fromHtml("<b>Details:</b> " + item.getDetails()));

        // Image Loading
        try {
            String base64String = item.getPic();
            if (base64String != null && !base64String.isEmpty()) {
                // Decode the Base64 string to a byte array
                byte[] imageByteArray = Base64.decode(base64String, Base64.DEFAULT);

                // Use Glide to load the byte array into the single ImageView
                Glide.with(context)
                        .asBitmap()
                        .load(imageByteArray)
                        .placeholder(R.drawable.ic_launcher_background) // placeholder while loading
                        .error(R.drawable.ic_launcher_background)       // fallback on error
                        .diskCacheStrategy(DiskCacheStrategy.NONE)      // don't cache raw byte arrays to disk
                        .into(holder.imgItem);
            } else {
                holder.imgItem.setImageResource(R.drawable.ic_launcher_background);
            }
        } catch (Exception e) {
            // base64 string is corrupted or decoding fails
            holder.imgItem.setImageResource(R.drawable.ic_launcher_background);
        }

        // Load User Data
        databaseService.getUser(item.getUserId(), new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                if (user != null) {
                    holder.txtUserName.setText("Posted by: " + user.getfName() + " " + user.getlName());
                    holder.txtUserPhone.setText("Contact: " + user.getPhone());
                }
            }
            @Override
            public void onFailed(Exception e) { /* Handle error */ }
        });

        // Expand Logic
        boolean expanded = expandState.get(position, false);
        holder.layoutExpanded.setVisibility(expanded ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            expandState.put(position, !expanded);
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        ImageView imgItem;
        TextView txtName, txtDate, txtLost;
        TextView txtPosition, txtDetails, txtUserName, txtUserPhone;
        LinearLayout layoutExpanded;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            imgItem = itemView.findViewById(R.id.imgItem);

            txtName = itemView.findViewById(R.id.txtName);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtLost = itemView.findViewById(R.id.txtLost);
            txtPosition = itemView.findViewById(R.id.txtPosition);
            txtDetails = itemView.findViewById(R.id.txtDetails);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtUserPhone = itemView.findViewById(R.id.txtUserPhone);

            layoutExpanded = itemView.findViewById(R.id.layoutExpanded);
        }
    }
}

