package com.example.cpc;

import static android.app.PendingIntent.getActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RecordsCustomArrayAdapter extends BaseAdapter {

    private Context context;
    private JSONArray recordsArray;
    private LayoutInflater inflater;

    public RecordsCustomArrayAdapter(Context context, JSONArray recordsArray) {
        this.context = context;
        this.recordsArray = recordsArray;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return recordsArray.length();
    }

    @Override
    public Object getItem(int position) {
        try {
            return recordsArray.getJSONObject(position);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = inflater.inflate(R.layout.patient_record_row, parent, false);

        try {
            JSONObject record = recordsArray.getJSONObject(position);

            TextView doctorNameTextView = rowView.findViewById(R.id.tv_doctor_name);
            TextView recordTypeTextView = rowView.findViewById(R.id.tv_record_type);
            TextView recordDateTextView = rowView.findViewById(R.id.tv_record_date);
            TextView recordTimeTextView = rowView.findViewById(R.id.tv_record_time);
            Button viewButton = rowView.findViewById(R.id.btn_view);
            ImageView btn_delete = rowView.findViewById(R.id.btn_delete);
            btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int recordId = record.getInt("record_id");
                        deleteRecord(recordId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });


            doctorNameTextView.setText(record.getString("doctor_name"));
            recordTypeTextView.setText("Type: " + record.getString("type"));

            String createdAt = record.getString("created_at");
            String[] dateTimeParts = createdAt.split(" ");
            if (dateTimeParts.length == 2) {
                recordDateTextView.setText("Date: " + dateTimeParts[0]);
                recordTimeTextView.setText("Time: " + dateTimeParts[1]);
            }
            else {
                recordDateTextView.setText("Date: -");
                recordTimeTextView.setText("Time: -");
            }

            TextView contentTextView = rowView.findViewById(R.id.tv_record_content);
            contentTextView.setVisibility(View.GONE);

            viewButton.setOnClickListener(v -> {
                if (contentTextView.getVisibility() == View.GONE) {
                    try {
                        contentTextView.setText(record.getString("content"));
                        contentTextView.setVisibility(View.VISIBLE);
                        viewButton.setText("Hide");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    contentTextView.setVisibility(View.GONE);
                    viewButton.setText("View Record");
                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return rowView;
    }

    public void updateData(JSONArray newData) {
        this.recordsArray = newData;
        notifyDataSetChanged();
    }
    private void deleteRecord(int recordId) {
        String url = "http://10.0.2.2/testfyp/delete_record_patient.php?record_id=" + recordId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.contains("Success")) {

                            Toast.makeText(context, "Record deleted", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        Volley.newRequestQueue(context).add(request);
    }

}
