package com.example.cpc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OverviewFragment extends Fragment implements RefreshableFragment{

    private TextView tvAppointments, tvAnnouncements;
    private String doctorId;
    private final String BASE_URL = "http://10.21.139.29/clinic";
    private TextView tvGreeting;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);

        doctorId = getArguments().getString("user_id");
        tvAppointments = view.findViewById(R.id.tvAppointments);
        tvAnnouncements = view.findViewById(R.id.tvAnnouncements);
        tvGreeting = view.findViewById(R.id.tvGreeting);
        fetchDoctorName();

        fetchAppointments();
        fetchAnnouncements();

        setupSuggestionForm(view); //for suggestion form
        return view;
    }

    private void fetchAppointments() {
        String url = BASE_URL + "/getAppointmentsToday.php?doctor_id=" + doctorId;
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (response.length() == 0) {
                        tvAppointments.setText("No appointments today.");
                        return;
                    }

                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String time = obj.getString("time").substring(0, 5); // HH:mm
                            String name = obj.getString("patient_name");
                            builder.append("• ").append(time).append(" – ").append(name).append("\n");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    tvAppointments.setText(builder.toString().trim());
                },
                error -> {
                    tvAppointments.setText("Error fetching appointments.");
                    Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                });

        queue.add(request);
    }

    private void fetchAnnouncements() {
        String url = BASE_URL + "/getAnnouncements.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (response.length() == 0) {
                        tvAnnouncements.setText("No announcements yet.");
                        return;
                    }

                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String content = obj.getString("content");
                            String date = obj.getString("date_only");  // Use this if your PHP returns it as "date_only"
                            builder.append("• ").append(content).append(" (").append(date).append(")\n");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    tvAnnouncements.setText(builder.toString().trim());
                },
                error -> {
                    tvAnnouncements.setText("Error fetching announcements.");
                    Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                });

        queue.add(request);
    }


    private void setupSuggestionForm(View view) {
        EditText etSuggestion = view.findViewById(R.id.etSuggestion);
        Button btnSubmit = view.findViewById(R.id.btnSubmitSuggestion);

        btnSubmit.setOnClickListener(v -> {
            String suggestion = etSuggestion.getText().toString().trim();
            if (suggestion.isEmpty()) {
                Toast.makeText(getContext(), "Please write something", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = BASE_URL + "/submitSuggestion.php";
            RequestQueue queue = Volley.newRequestQueue(requireContext());

            StringRequest request = new StringRequest(Request.Method.POST, url,
                    response -> {
                        if (response.equals("success")) {
                            Toast.makeText(getContext(), "Suggestion sent!", Toast.LENGTH_SHORT).show();
                            etSuggestion.setText("");
                        } else {
                            Toast.makeText(getContext(), "Error sending suggestion", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show()) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("suggestion", suggestion);
                    params.put("staff_id", String.valueOf(doctorId));
                    return params;
                }
            };

            queue.add(request);
        });
    }

    private void fetchDoctorName() {
        String url = BASE_URL + "/getDoctorName.php?doctor_id=" + doctorId;
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> tvGreeting.setText("Welcome back, " + response + " 👋"),
                error -> tvGreeting.setText("Welcome back, Doctor 👋"));

        queue.add(request);
    }


    @Override
    public void onRefresh() {
        fetchAppointments();
        fetchAnnouncements();
    }
}
