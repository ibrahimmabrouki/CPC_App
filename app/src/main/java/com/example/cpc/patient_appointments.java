package com.example.cpc;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class patient_appointments extends Fragment implements RefreshableFragment {

    private String userId;
    private List<String> doctorsList = new ArrayList<>();
    private Spinner doctorSpinner, timeSlotSpinner;
    private EditText reasonForVisit;
    private Button submitAppointment;
    private CalendarView appointmentCalendar;

    private String selectedDate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_appointments, container, false);
        //userId = "4";
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("user_id", null);

        doctorSpinner = view.findViewById(R.id.doctorSpinner);
        timeSlotSpinner = view.findViewById(R.id.timeSlotSpinner);
        reasonForVisit = view.findViewById(R.id.reasonForVisit);
        appointmentCalendar = view.findViewById(R.id.appointmentCalendar);
        submitAppointment = view.findViewById(R.id.submitAppointment);

        appointmentCalendar.setMinDate(System.currentTimeMillis());
        Calendar calendar = Calendar.getInstance();
        selectedDate = formatDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        appointmentCalendar.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedDate = formatDate(year, month, dayOfMonth);
            Log.d("SelectedDate", "Date: " + selectedDate);
            updateTimeSlots();
        });


        /*if (getArguments() != null) {
            userId = getArguments().getString("user_id");
            Log.d("PatientAppointments", "User ID: " + userId);
        }*/

        getDoctorsFromServer();

        doctorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTimeSlots();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        submitAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedDoctor = doctorSpinner.getSelectedItem() != null ? doctorSpinner.getSelectedItem().toString().trim() : "";
                String time = timeSlotSpinner.getSelectedItem() != null ? timeSlotSpinner.getSelectedItem().toString().trim() : "";
                String reason = reasonForVisit.getText().toString().trim();

                if (selectedDoctor.isEmpty() || selectedDate == null || selectedDate.isEmpty() || time.isEmpty() || reason.isEmpty() || time.equals("No available slots")) {
                    showCustomToast("Please fill all fields properly", R.drawable.ic_uncheck);
                    return;
                }
                submitAppointment(userId, selectedDoctor, selectedDate, time, reason);
            }
        });

        return view;
    }

    private void getDoctorsFromServer() {
        String url = "http://10.21.134.17/clinic/get_doctors.php";
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        doctorsList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            String doctorName = response.getString(i);
                            doctorsList.add(doctorName);
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, doctorsList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        doctorSpinner.setAdapter(adapter);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Error parsing doctors list", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(jsonArrayRequest);
    }

    private void updateTimeSlots() {
        if (selectedDate == null || doctorSpinner.getSelectedItem() == null) return;

        String doctorName = doctorSpinner.getSelectedItem().toString();
        String url = "http://10.21.134.17/clinic/get_available_slots.php?doctor=" + Uri.encode(doctorName) + "&date=" + Uri.encode(selectedDate);
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<String> availableSlots = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            availableSlots.add(response.getString(i));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    if (availableSlots.isEmpty()) {
                        availableSlots.add("No available slots");
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, availableSlots);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    timeSlotSpinner.setAdapter(adapter);
                },
                error -> Toast.makeText(requireContext(), "Error loading time slots: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(jsonArrayRequest);
    }



    private void submitAppointment(String patientId, String doctorName, String date, String time, String reason) {
        String url = "http://10.21.134.17/clinic/submit_appointment.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.contains("Success")) {
                        showCustomToast("Appointment booked!", R.drawable.ic_check);
                    } else {
                        showCustomToast("Failed: " + response, R.drawable.ic_uncheck);
                    }
                },
                error -> Toast.makeText(getActivity(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("patient_id", patientId);
                params.put("doctor_name", doctorName); // e.g., "Layla Farah"
                params.put("date", date);
                params.put("time", time);
                params.put("reason_for_visit", reason);
                return params;
            }
        };

        Volley.newRequestQueue(getActivity()).add(stringRequest);
    }

    private void showCustomToast(String message, int icon) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView toastText = layout.findViewById(R.id.toast_text);
        toastText.setText(message);

        ImageView custom_icon = layout.findViewById(R.id.custom_icon);
        custom_icon.setImageResource(icon);
        Toast toast = new Toast(getActivity());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER, 0, -100);
        toast.show();
    }



    private String formatDate(int year, int month, int day) {
        return String.format("%04d-%02d-%02d", year, month + 1, day);
    }

    @Override
    public void onRefresh() {
        getDoctorsFromServer();
    }
}
