package com.example.cpc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DoctorCustomeAdapter extends BaseAdapter
{
    //Fields or attributes
    String []text;
    Context context;
    //int [] imageids;
    LayoutInflater inflater;

    public DoctorCustomeAdapter(Context context, String[] text){
        this.context = context;
        this.text = text;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  // Initialize inflater
    }

    @Override
    public int getCount() {
        return text.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rowview=inflater.inflate(R.layout.doctor_row, null);
        TextView Doctor_name = (TextView) rowview.findViewById(R.id.Doctor_name);
        Doctor_name.setText(Doctor_name.getText().toString().trim()+ text[i]);

        return rowview;
    }
}
