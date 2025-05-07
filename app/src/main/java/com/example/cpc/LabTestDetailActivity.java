package com.example.cpc;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class LabTestDetailActivity extends AppCompatActivity {
    private final String BASE_URL = "http://10.21.139.29/clinic";
    private TextView tvLabTestContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab_test_detail);

        tvLabTestContent = findViewById(R.id.tvLabTestContent);
        int recordId = getIntent().getIntExtra("record_id", -1);

        if (recordId != -1) {
            fetchLabTestDetails(recordId);
        } else {
            tvLabTestContent.setText("Invalid lab test.");
        }
    }

    private void fetchLabTestDetails(int recordId) {
        String url = BASE_URL + "/getLabTestById.php?record_id=" + recordId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> tvLabTestContent.setText(response),
                error -> Toast.makeText(this, "Error loading lab test", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(this).add(request);
    }
}
