package com.example.cpc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class ScheduleFragment extends Fragment {

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_schedule, container, false);

        setupDayClickListeners();
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

        Toast.makeText(getContext(), "Clicked: " + clickedDay.getText(), Toast.LENGTH_SHORT).show();
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
}
