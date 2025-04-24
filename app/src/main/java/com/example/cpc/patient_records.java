package com.example.cpc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class patient_records extends Fragment implements RefreshableFragment{
    private String userId;
    ListView recordsListView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_records_ibrahim, container, false);
        recordsListView = view.findViewById(R.id.Records_lv);
        //userId = "4";
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("user_id", null);
        fetchMedicalRecords(userId);

        /*if (getArguments() != null) {
            userId = getArguments().getString("user_id");  // Get the user_id from the arguments
            // Use the user_id as needed (e.g., query database, display in UI, etc.)
            Log.d("PatientRecords", "Received User ID: " + userId);
        }*/
        return view;
    }

    private void fetchMedicalRecords(String userId) {
        String url = "http://10.21.148.28/clinic/get_records_by_patient.php?patient_id=" + userId;

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);

                            if (jsonResponse.getBoolean("Success")) {
                                JSONArray records = jsonResponse.getJSONArray("records");

                                // Create and set a custom adapter for the records
                                RecordsCustomArrayAdapter adapter = new RecordsCustomArrayAdapter(getActivity().getApplicationContext(), records);
                                recordsListView.setAdapter(adapter);

                            } else {
                                String message = jsonResponse.optString("message", "No records found");
                                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "Error parsing medical records", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(stringRequest);
    }
    @Override
    public void onRefresh() {
        if (userId != null) {
            fetchMedicalRecords(userId);
        }
    }
}