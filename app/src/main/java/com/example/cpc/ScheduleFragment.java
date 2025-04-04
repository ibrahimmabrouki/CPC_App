package com.example.cpc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import java.util.Calendar;


import androidx.fragment.app.Fragment;

import org.json.JSONObject;

public class ScheduleFragment extends Fragment implements RefreshableFragment{

    private View rootView;
    private LinearLayout scheduleContainer;
    private final String BASE_URL = "http://10.21.166.221/clinic";
    private final int doctorId = 8;
    private String currentSelectedDay = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_schedule, container, false);
        scheduleContainer = rootView.findViewById(R.id.schedule_container);
        setupDayClickListeners();
        selectTodayAutomatically();
        return rootView;
    }

    private void setupDayClickListeners() {
        int[] ids = {
                R.id.day_mon,
                R.id.day_tue,
                R.id.day_wed,
                R.id.day_thu,
                R.id.day_fri
        };

        for (int id : ids) {
            TextView day = rootView.findViewById(id);
            if (day != null) {
                day.setOnClickListener(v -> handleDayClick((TextView) v));
            }
        }
    }

    private void handleDayClick(TextView clickedDay) {
        resetDayTabStyles();
        clickedDay.setBackgroundResource(R.drawable.day_tab_selected);
        clickedDay.setTextColor(getResources().getColor(android.R.color.white));

        String shortDay = clickedDay.getText().toString().trim().toLowerCase();
        String weekday;

        switch (shortDay) {
            case "mon": weekday = "Monday"; break;
            case "tue": weekday = "Tuesday"; break;
            case "wed": weekday = "Wednesday"; break;
            case "thu": weekday = "Thursday"; break;
            case "fri": weekday = "Friday"; break;
            default: weekday = ""; break;
        }

        currentSelectedDay = weekday;
        fetchAppointmentsForDay(weekday);
    }


    private void resetDayTabStyles() {
        int[] ids = {
                R.id.day_mon,
                R.id.day_tue,
                R.id.day_wed,
                R.id.day_thu,
                R.id.day_fri
        };

        for (int id : ids) {
            TextView day = rootView.findViewById(id);
            if (day != null) {
                day.setBackgroundResource(R.drawable.day_tab_bg);
                day.setTextColor(getResources().getColor(R.color.primary_dark));
            }
        }
    }
    private void fetchAppointmentsForDay(String weekday) {
        String url = BASE_URL + "/getSchedule.php?doctor_id=" + doctorId + "&weekday=" + weekday;
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    scheduleContainer.removeAllViews(); // Clear old data
                    if (response.length() == 0) {
                        addScheduleItem("No appointments scheduled.");
                        return;
                    }

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String time = obj.getString("time").substring(0, 5); // HH:mm
                            String patient = obj.getString("patient_name");
                            addScheduleItem(time + " - " + patient);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> {
                    scheduleContainer.removeAllViews();
                    addScheduleItem("Failed to load schedule.");
                });

        queue.add(request);
    }

    private void addScheduleItem(String text) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View item = inflater.inflate(R.layout.item_schedule, scheduleContainer, false);

        TextView tv = item.findViewById(R.id.tv_schedule_text);
        tv.setText(text);

        scheduleContainer.addView(item);
    }


    private void selectTodayAutomatically() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // 1 = Sunday, 2 = Monday, ...

        TextView targetDay = null;
        switch (dayOfWeek) {
            case Calendar.MONDAY: targetDay = rootView.findViewById(R.id.day_mon); currentSelectedDay = "mon"; break;
            case Calendar.TUESDAY: targetDay = rootView.findViewById(R.id.day_tue); currentSelectedDay = "tue"; break;
            case Calendar.WEDNESDAY: targetDay = rootView.findViewById(R.id.day_wed); currentSelectedDay = "wed"; break;
            case Calendar.THURSDAY: targetDay = rootView.findViewById(R.id.day_thu); currentSelectedDay = "thu"; break;
            case Calendar.FRIDAY: targetDay = rootView.findViewById(R.id.day_fri); currentSelectedDay = "fri"; break;
            default: return;
        }

        if (targetDay != null) {
            targetDay.performClick();
        }
    }
    @Override
    public void onRefresh() {
        fetchAppointmentsForDay(currentSelectedDay);
    }

}
