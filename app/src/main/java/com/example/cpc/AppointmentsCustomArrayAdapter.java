package com.example.cpc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AppointmentsCustomArrayAdapter extends BaseAdapter {

    private Context context;
    private JSONArray appointments;
    private LayoutInflater inflater;

    public AppointmentsCustomArrayAdapter(Context context, JSONArray appointments) {
        this.context = context;
        this.appointments = appointments;
        this.inflater = LayoutInflater.from(context);  // Initialize LayoutInflater
    }

    @Override
    public int getCount() {
        return appointments.length();  // Return the number of appointments
    }

    @Override
    public Object getItem(int position) {
        try {
            return appointments.getJSONObject(position);  // Return the appointment at the position
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;  // Return the position as the ID
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the custom row layout
        View rowView = inflater.inflate(R.layout.patient_appointment_row, parent, false);

        try {
            // Get the current appointment data from the JSON array
            JSONObject appointment = appointments.getJSONObject(position);

            // Get references to TextViews in the row layout
            TextView doctorNameTextView = rowView.findViewById(R.id.doctorName);
            TextView reasonTextView = rowView.findViewById(R.id.appointmentReason);
            TextView dateTextView = rowView.findViewById(R.id.appointmentDate);
            TextView timeTextView = rowView.findViewById(R.id.appointmentTime);
            Button editButton = rowView.findViewById(R.id.editButton);

            // Set the appointment data into the TextViews
            doctorNameTextView.setText(appointment.getString("doctor_name"));
            reasonTextView.setText(appointment.getString("reason_for_visit"));
            dateTextView.setText(appointment.getString("date"));
            timeTextView.setText(appointment.getString("time"));


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rowView;  // Return the populated row view
    }
}
