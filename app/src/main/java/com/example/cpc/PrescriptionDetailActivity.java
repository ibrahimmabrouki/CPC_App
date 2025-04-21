package com.example.cpc;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class PrescriptionDetailActivity extends AppCompatActivity {

    private final String BASE_URL = "http://10.21.148.28/clinic";
    private TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription_detail);

        tvContent = findViewById(R.id.tvPrescriptionContent);

        int recordId = getIntent().getIntExtra("record_id", -1);
        if (recordId != -1) {
            fetchRecordContent(recordId);
        } else {
            tvContent.setText("Invalid prescription.");
        }
    }

    private void fetchRecordContent(int recordId) {
        String url = BASE_URL + "/getPrescriptionById.php?record_id=" + recordId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> tvContent.setText(response),
                error -> Toast.makeText(this, "Error loading prescription", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(this).add(request);
    }
}
