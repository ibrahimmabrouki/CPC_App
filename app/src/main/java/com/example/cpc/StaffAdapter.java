package com.example.cpc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class StaffAdapter extends ArrayAdapter<StaffItem> {

    public StaffAdapter(Context context, List<StaffItem> staffList) {
        super(context, 0, staffList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        StaffItem staff = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.staff_item, parent, false);
        }

        ImageView imgProfile = convertView.findViewById(R.id.img_profile);
        TextView tvName = convertView.findViewById(R.id.staff_name);
        View unreadDot = convertView.findViewById(R.id.unread_dot);

        tvName.setText(staff.getName());

        if (staff.getType().equalsIgnoreCase("doctor")) {
            imgProfile.setImageResource(R.drawable.ic_profile_doctor);
        } else if (staff.getType().equalsIgnoreCase("pharmacist")) {
            imgProfile.setImageResource(R.drawable.ic_profile_pharmacist);
        } else if (staff.getType().equalsIgnoreCase("lab")) {
            imgProfile.setImageResource(R.drawable.ic_profile_lab);
        }

        if (staff.hasUnread()) {
            unreadDot.setVisibility(View.VISIBLE);
        } else {
            unreadDot.setVisibility(View.GONE);
        }

        return convertView;
    }
}
