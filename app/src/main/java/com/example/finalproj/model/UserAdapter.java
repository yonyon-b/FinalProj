package com.example.finalproj.model;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.finalproj.R;
import com.example.finalproj.model.User;
import com.example.finalproj.services.DatabaseService;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {


    public interface OnUserClickListener {
        void onUserClick(User user);
        void onLongUserClick(View view, User user);
    }

    private final List<User> userList;
    private final OnUserClickListener onUserClickListener;
    public UserAdapter(@Nullable final OnUserClickListener onUserClickListener) {
        userList = new ArrayList<>();
        this.onUserClickListener = onUserClickListener;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        if (user == null) return;

        holder.tvName.setText(user.getfName() + " " + user.getlName());
        holder.tvEmail.setText(user.getEmail());
        holder.tvPhone.setText(user.getPhone());

        if (user.getProfilePicture() != null) {
            String base64String = user.getProfilePicture();
            if (base64String != null && !base64String.isEmpty()) {
                // Decode the Base64 string to a byte array
                byte[] imageByteArray = Base64.decode(base64String, Base64.DEFAULT);

                // Use Glide to load the byte array into the single ImageView
                Glide.with(holder.itemView.getContext())
                        .asBitmap()
                        .load(imageByteArray)
                        .placeholder(R.drawable.user_pfp) // placeholder while loading
                        .error(R.drawable.user_pfp)       // fallback on error
                        .diskCacheStrategy(DiskCacheStrategy.NONE)      // don't cache raw byte arrays to disk
                        .into(holder.ivPfp);
            } else {
                holder.ivPfp.setImageResource(R.drawable.user_pfp);
            }
        }

        // Show admin chip if user is admin
        if (user.getAdmin()) {
            holder.chipRole.setVisibility(View.VISIBLE);
            holder.chipRole.setText("Admin");
        } else {
            holder.chipRole.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onUserClick(user);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onLongUserClick(v, user);
            }
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setUserList(List<User> users) {
        userList.clear();
        userList.addAll(users);
        notifyDataSetChanged();
    }

    public void addUser(User user) {
        userList.add(user);
        notifyItemInserted(userList.size() - 1);
    }
    public void updateUser(User user) {
        int index = userList.indexOf(user);
        if (index == -1) return;
        userList.set(index, user);
        notifyItemChanged(index);
    }

    public void removeUser(User user) {
        int index = userList.indexOf(user);
        if (index == -1) return;
        userList.remove(index);
        notifyItemRemoved(index);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvPhone, tvInitials;
        ImageView ivPfp;
        Chip chipRole;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_user_name);
            tvEmail = itemView.findViewById(R.id.tv_item_user_email);
            tvPhone = itemView.findViewById(R.id.tv_item_user_phone);
            ivPfp = itemView.findViewById(R.id.iv_item_user_pfp);
            chipRole = itemView.findViewById(R.id.chip_user_role);
        }
    }
}