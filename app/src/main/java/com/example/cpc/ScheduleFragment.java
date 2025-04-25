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
import android.util.Log;

import androidx.fragment.app.Fragment;

import org.json.JSONObject;

public class ScheduleFragment extends Fragment implements RefreshableFragment{

    private View rootView;
    private LinearLayout scheduleContainer;
    private final String BASE_URL = "http://10.21.134.17/clinic";
    private String doctorId;
    private String currentSelectedDay = null;
    private TextView weekendMessage;
    private boolean hasSelectedToday = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_schedule, container, false);
        scheduleContainer = rootView.findViewById(R.id.schedule_container);
        weekendMessage = rootView.findViewById(R.id.weekend_message);
        doctorId = getArguments().getString("user_id");
        setupDayClickListeners();

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (!hasSelectedToday) {
                selectTodayAutomatically();
                hasSelectedToday = true;
            }
        });

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
        if (!isAdded()) return;

        String shortDay = clickedDay.getText().toString().trim().toLowerCase();
        String weekday = "";

        if (shortDay.equals("mon")) weekday = "Monday";
        else if (shortDay.equals("tue")) weekday = "Tuesday";
        else if (shortDay.equals("wed")) weekday = "Wednesday";
        else if (shortDay.equals("thu")) weekday = "Thursday";
        else if (shortDay.equals("fri")) weekday = "Friday";

        // Always update UI regardless of currentSelectedDay
        weekendMessage.setVisibility(View.GONE);
        resetDayTabStyles();
        clickedDay.setBackgroundResource(R.drawable.day_tab_selected);
        clickedDay.setTextColor(getResources().getColor(android.R.color.white));

        // Only fetch if different
        if (!weekday.equals(currentSelectedDay)) {
            currentSelectedDay = weekday;
            fetchAppointmentsForDay(weekday);
        }
    }


    private void resetDayTabStyles() {
        if (!isAdded()) return;
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
                    scheduleContainer.removeAllViews();
                    if (response.length() == 0) {
                        addScheduleItem("No appointments scheduled.");
                        return;
                    }

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String time = obj.getString("time").substring(0, 5);
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
        if (!isAdded() || currentSelectedDay != null) return;

        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        boolean isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);

        if (isWeekend) {
            weekendMessage.setVisibility(View.VISIBLE);
            scheduleContainer.removeAllViews();
        } else {
            weekendMessage.setVisibility(View.GONE);
            TextView targetDay = null;

            if (dayOfWeek == Calendar.MONDAY) targetDay = rootView.findViewById(R.id.day_mon);
            else if (dayOfWeek == Calendar.TUESDAY) targetDay = rootView.findViewById(R.id.day_tue);
            else if (dayOfWeek == Calendar.WEDNESDAY) targetDay = rootView.findViewById(R.id.day_wed);
            else if (dayOfWeek == Calendar.THURSDAY) targetDay = rootView.findViewById(R.id.day_thu);
            else if (dayOfWeek == Calendar.FRIDAY) targetDay = rootView.findViewById(R.id.day_fri);

            final TextView finalTargetDay = targetDay;
            if (finalTargetDay != null && isAdded()) {
                finalTargetDay.post(() -> {
                    if (isAdded()) {
                        finalTargetDay.performClick();
                    }
                });
            }
        }
    }


    @Override
    public void onRefresh() {
        if (currentSelectedDay != null) {
            fetchAppointmentsForDay(currentSelectedDay);
        } else {
            selectTodayAutomatically();
        }
    }
}
