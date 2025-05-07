package com.example.cpc;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class patient_overview extends Fragment implements RefreshableFragment {

    private String userId;
    private ListView appointmentsListView;
    private TextView tvGreeting;
    private TextView tvAnnouncements;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_overview, container, false);

        //userId = "4";
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("user_id", null);
        tvAnnouncements = view.findViewById(R.id.tvAnnouncements);
        tvGreeting = view.findViewById(R.id.tvGreeting);
        /*if (getArguments() != null) {
            //userId = getArguments().getString("user_id");  // Retrieve the patient user ID passed as an argument
            Log.d("PatientRecords", "Received User ID: " + userId);
            //fetchAppointments(userId);  // Call the function to fetch appointments
        }*/
        fetchUsername(userId);
        fetchAppointments(userId);
        fetchAnnouncements();
        appointmentsListView = view.findViewById(R.id.appointments_lv);
        return view;
    }
    private void fetchAnnouncements() {
        String url = "http://10.21.139.29/clinic/getAnnouncements.php";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String content = obj.getString("content");
                            String date = obj.getString("date_only");
                            builder.append("• ").append(content).append(" (").append(date).append(")\n");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    tvAnnouncements.setText(builder.length() > 0 ? builder.toString().trim() : "No announcements yet.");
                },
                error -> {
                    tvAnnouncements.setText("Error fetching announcements.");
                });

        Volley.newRequestQueue(requireContext()).add(request);
    }
    private void fetchUsername(String userId) {
        String url = "http://10.21.139.29/clinic/get_patient_username.php?id=" + userId;

        RequestQueue requestQueue = Volley.newRequestQueue(requireActivity());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d("UsernameResponse", "Raw Response: " + response);

                            JSONObject jsonResponse = new JSONObject(response);

                            if (jsonResponse.has("username")) {
                                String username = jsonResponse.getString("username");
                                tvGreeting.setText("Welcome back, \uD83D\uDC4B "+ username);
                            }

                            else if (jsonResponse.has("error")) {
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
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

    private void fetchAppointments(String userId) {
        String url = "http://10.21.139.29/clinic/get_appointments_by_patient.php?patient_id=" + userId;

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //Log.d("AppointmentsResponse", "Raw Response: " + response);

                            JSONObject jsonResponse = new JSONObject(response);
                            //tvGreeting.setText(response);

                            if (jsonResponse.getBoolean("Success")) {
                                JSONArray appointments = jsonResponse.getJSONArray("appointments");
                                //tvGreeting.setText(response.toString());
                                //Log.d("AppointmentsResponse", "Number of appointments: " + appointments.length());

                                AppointmentsCustomArrayAdapter adapter = new AppointmentsCustomArrayAdapter(getActivity().getApplicationContext(), appointments);
                                appointmentsListView.setAdapter(adapter);

                            } else {
                                Toast.makeText(getActivity(), "No appointments found", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            //tvGreeting.setText(e.toString());
                            //Toast.makeText(getActivity(), "Error parsing appointments", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle network or server errors
                        //tvGreeting.setText(error.toString());
                        Toast.makeText(getActivity(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(stringRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (userId != null) {
            fetchAppointments(userId);
            fetchUsername(userId);
        }
    }

    @Override
    public void onRefresh() {
        if (userId != null) {
            fetchAppointments(userId);
            fetchUsername(userId);
        }
    }
}