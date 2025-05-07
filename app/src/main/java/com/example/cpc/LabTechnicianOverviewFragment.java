package com.example.cpc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LabTechnicianOverviewFragment extends Fragment implements RefreshableFragment {

    private TextView tvAnnouncements, tvGreeting, tvPendingOrders;
    private String labTechId;
    private final String BASE_URL = "http://10.21.139.29/clinic";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview_lab, container, false);

        tvAnnouncements = view.findViewById(R.id.tvAnnouncements);
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvPendingOrders = view.findViewById(R.id.tvPendingOrders);

        if (getArguments() != null) {
            labTechId = getArguments().getString("user_id", "0");
        }

        fetchGreeting();
        fetchAnnouncements();
        fetchPendingLabOrders();
        setupSuggestionForm(view);

        return view;
    }

    private void fetchGreeting() {
        String url = BASE_URL + "/getLabTechName.php?lab_tech_id=" + labTechId;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> tvGreeting.setText("Welcome back, " + response + " 👋"),
                error -> tvGreeting.setText("Welcome back, Lab Technician 👋"));

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void fetchAnnouncements() {
        String url = BASE_URL + "/getAnnouncements.php";
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

    private void fetchPendingLabOrders() {
        String url = BASE_URL + "/getPendingLabOrdersCount.php";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    tvPendingOrders.setText(response.trim() + " lab test orders pending.");
                },
                error -> {
                    tvPendingOrders.setText("Unable to load pending orders.");
                });

        Volley.newRequestQueue(requireContext()).add(request);
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
                    params.put("staff_id", labTechId);
                    return params;
                }
            };

            Volley.newRequestQueue(requireContext()).add(request);
        });
    }

    @Override
    public void onRefresh() {
        fetchGreeting();
        fetchAnnouncements();
        fetchPendingLabOrders();
    }
}
