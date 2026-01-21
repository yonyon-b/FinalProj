package com.example.finalproj.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.util.Base64;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.finalproj.R;
import com.example.finalproj.services.DatabaseService;

import org.w3c.dom.Text;

import java.util.List;

public class ItemAdapter extends ArrayAdapter<Item> {

    private final Context context;
    private final List<Item> items;
    private final SparseBooleanArray expandState = new SparseBooleanArray();
    private DatabaseService databaseService;

    public ItemAdapter(Context context, List<Item> items) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_row, parent, false);
        }
        Item item = items.get(position);

        ImageView imgItem = convertView.findViewById(R.id.imgItem);
        TextView txtName = convertView.findViewById(R.id.txtName);
        TextView txtDate = convertView.findViewById(R.id.txtDate);
        TextView txtLost = convertView.findViewById(R.id.txtLost);

        TextView txtPosition = convertView.findViewById(R.id.txtPosition);
        TextView txtDetails = convertView.findViewById(R.id.txtDetails);
        TextView txtUserName = convertView.findViewById(R.id.txtUserName);
        TextView txtUserPhone = convertView.findViewById(R.id.txtUserPhone);
        ImageView imgItemEx = convertView.findViewById(R.id.imgItemExpanded);
        databaseService = DatabaseService.getInstance();

        LinearLayout layoutExpanded = convertView.findViewById(R.id.layoutExpanded);

        txtName.setText(item.getName());
        txtDate.setText(item.getDate());

        if (item.isLost()) {
            txtLost.setText("Lost Item");
            txtDate.setText(Html.fromHtml("<b>Lost at:</b> " + item.getDate()));
            txtPosition.setText(Html.fromHtml("<b>Location Lost:</b> " + "<br></br>" + item.getPosition()));
        } else {
            txtLost.setText("Found Item");
            txtDate.setText(Html.fromHtml("<b>Found at:</b> " + item.getDate()));
            txtPosition.setText(Html.fromHtml("<b>Location Found:</b> " + "<br></br>" + item.getPosition()));
        }

        try {
            imgItem.setImageBitmap(ImageUtil.convertFrom64base(item.getPic()));
            imgItemEx.setImageBitmap(ImageUtil.convertFrom64base(item.getPic()));
        } catch (Exception e) {
            imgItem.setImageResource(R.drawable.ic_launcher_background);
            imgItemEx.setImageResource(R.drawable.ic_launcher_background);
        }

        txtDetails.setText(Html.fromHtml("<b>Details:</b> " + "<br></br>" + item.getDetails()));

        databaseService.getUser(item.getUserId(), new DatabaseService.DatabaseCallback<User>(){
            @Override
            public void onCompleted(User object) {
                try {
                    txtUserName.setText(Html.fromHtml("<b>User: </b>" + "<br></br>" + object.getfName() + " " + object.getlName()));
                    txtUserPhone.setText(Html.fromHtml("<b>Phone Number:</b> " + "<br></br>" + object.getPhone()));
                } catch (Exception e) {
                    txtUserName.setText(Html.fromHtml("<b>User: </b>" + "<br></br>" + "Deleted User"));
                    txtUserPhone.setText(Html.fromHtml("<b>Phone Number:</b> " + "<br></br>" + "Unknown"));
                }
            }

            @Override
            public void onFailed(Exception e) {

            }
        });

        // handle expand/collapse state
        boolean expanded = expandState.get(position, false);

        layoutExpanded.setVisibility(expanded ? View.VISIBLE : View.GONE);
        imgItemEx.setVisibility(expanded ? View.VISIBLE : View.GONE);
        if (expanded) {
            imgItem.setVisibility(View.GONE);
        } else {
            imgItem.setVisibility(View.VISIBLE);
        }

        convertView.setOnClickListener(v -> {
            boolean current = expandState.get(position, false);
            expandState.put(position, !current);

            notifyDataSetChanged();
        });

        return convertView;
    }
}
