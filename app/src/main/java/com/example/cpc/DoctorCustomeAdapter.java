package com.example.cpc;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class DoctorCustomeAdapter extends BaseAdapter
{
    //Fields or attributes
    private List<String> doctorsList;
    Context context;
    //int [] imageids;
    LayoutInflater inflater;

    public DoctorCustomeAdapter(Context context, List<String> doctorsList){
        this.context = context;
        this.doctorsList = doctorsList;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  // Initialize inflater
    }

    @Override
    public int getCount() {
        return doctorsList.size();
    }

    @Override
    public Object getItem(int i) {
        return doctorsList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;

        if (convertView == null) {
            textView = new TextView(context);
            textView.setLayoutParams(new ListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));

            // 🖌 APPLY THE STYLING HERE (THIS IS THE PART I MEANT!)
            textView.setTextSize(18); // Smaller, clean
            textView.setGravity(Gravity.LEFT); // Center text
            textView.setPadding(0, 24, 0, 24); // Top and bottom padding
            textView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD); // Use clean bold font
            textView.setTextColor(ContextCompat.getColor(context, R.color.black)); // Text color black
        } else {
            textView = (TextView) convertView;
        }

        textView.setText("Doctor " + doctorsList.get(position)); // Set the name

        return textView;
    }
}
