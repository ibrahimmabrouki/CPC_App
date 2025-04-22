package com.example.cpc;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class RecordDetailActivity extends AppCompatActivity {

    private final String BASE_URL = "http://10.21.148.28/clinic";
    private TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_detail);

        tvContent = findViewById(R.id.tvPrescriptionContent); // Reuse same ID

        int appointmentId = getIntent().getIntExtra("appointment_id", -1);
        if (appointmentId != -1) {
            fetchRecordContent(appointmentId);
        } else {
            tvContent.setText("Invalid appointment.");
        }
    }

    private void fetchRecordContent(int appointmentId) {
        String url = BASE_URL + "/getRecordByAppointmentId.php?appointment_id=" + appointmentId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> tvContent.setText(response),
                error -> Toast.makeText(this, "Error loading record", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(this).add(request);
    }
}
