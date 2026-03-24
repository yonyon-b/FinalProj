package com.example.finalproj.model;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproj.ItemList;
import com.example.finalproj.R;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    private final Context context;
    private final List<Item> items;

    public GalleryAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gallery, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        Item item = items.get(position);
        holder.txtName.setText(item.getName());

        holder.cardMain.setOnClickListener(v -> {
            Intent i = new Intent(context, ItemList.class);
            i.putExtra("SEARCH_QUERY", item.getName());
            context.startActivity(i);
        });

        if (item.isLost()) {
            holder.txtType.setText("Lost Item");
            holder.cardType.setCardBackgroundColor(Color.parseColor("#e1403d"));
        } else {
            holder.txtType.setText("Found Item");
            holder.cardType.setCardBackgroundColor(Color.parseColor("#3ea65f"));
        }

        try {
            holder.imgItem.setImageBitmap(ImageUtil.convertFrom64base(item.getPic()));
        } catch (Exception e) {
            holder.imgItem.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgItem;
        TextView txtName, txtType;
        CardView cardType, cardMain;

        GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgItem = itemView.findViewById(R.id.imgGalleryItem);
            txtName = itemView.findViewById(R.id.txtGalleryItemName);
            txtType = itemView.findViewById(R.id.txtGalleryItemType);
            cardType = itemView.findViewById(R.id.cardGalleryItemType);
            cardMain = itemView.findViewById(R.id.cvGallery);
        }
    }
}