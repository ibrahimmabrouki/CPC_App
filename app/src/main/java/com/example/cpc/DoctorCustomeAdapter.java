package com.example.cpc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

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
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowview=inflater.inflate(R.layout.doctor_row, null);
        TextView Doctor_name = (TextView) rowview.findViewById(R.id.Doctor_name);
        Doctor_name.setText(Doctor_name.getText().toString().trim()+ " " + doctorsList.get(i));

        return rowview;
    }
}
