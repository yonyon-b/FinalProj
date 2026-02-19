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

        if (item.isLost()) {
            holder.txtLost.setText("Lost Item");
            holder.txtDate.setText(Html.fromHtml("<b>Lost at:</b> " + item.getDate()));
            holder.txtPosition.setText(Html.fromHtml("<b>Location Lost:</b><br>" + item.getPosition()));
        } else {
            holder.txtLost.setText("Found Item");
            holder.txtDate.setText(Html.fromHtml("<b>Found at:</b> " + item.getDate()));
            holder.txtPosition.setText(Html.fromHtml("<b>Location Found:</b><br>" + item.getPosition()));
        }

        holder.txtDetails.setText(
                Html.fromHtml("<b>Details:</b><br>" + item.getDetails())
        );

        try {
            holder.imgItem.setImageBitmap(ImageUtil.convertFrom64base(item.getPic()));
            holder.imgItemEx.setImageBitmap(ImageUtil.convertFrom64base(item.getPic()));
        } catch (Exception e) {
            holder.imgItem.setImageResource(R.drawable.ic_launcher_background);
            holder.imgItemEx.setImageResource(R.drawable.ic_launcher_background);
        }

        databaseService.getUser(item.getUserId(),
                new DatabaseService.DatabaseCallback<User>() {
                    @Override
                    public void onCompleted(User user) {
                        try {
                            holder.txtUserName.setText(
                                    Html.fromHtml("<b>User:</b><br>" + user.getfName() + " " + user.getlName()));
                            holder.txtUserPhone.setText(
                                    Html.fromHtml("<b>Phone Number:</b><br>" + user.getPhone()));
                        } catch (Exception e) {
                            holder.txtUserName.setText(
                                    Html.fromHtml("<b>User:</b><br>" + "Deleted User"));
                            holder.txtUserPhone.setText(
                                    Html.fromHtml("<b>Phone Number:</b><br>" + "Unknown"));
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {

                    }
                });

        // Expand / collapse state
        boolean expanded = expandState.get(position, false);
        holder.layoutExpanded.setVisibility(expanded ? View.VISIBLE : View.GONE);
        holder.imgItemEx.setVisibility(expanded ? View.VISIBLE : View.GONE);
        holder.imgItem.setVisibility(expanded ? View.GONE : View.VISIBLE);

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

        ImageView imgItem, imgItemEx;
        TextView txtName, txtDate, txtLost;
        TextView txtPosition, txtDetails, txtUserName, txtUserPhone;
        LinearLayout layoutExpanded;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            imgItem = itemView.findViewById(R.id.imgItem);
            imgItemEx = itemView.findViewById(R.id.imgItemExpanded);

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

