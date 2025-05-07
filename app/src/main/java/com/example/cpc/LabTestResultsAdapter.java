package com.example.cpc;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.List;

public class LabTestResultsAdapter extends RecyclerView.Adapter<LabTestResultsAdapter.ViewHolder> {

    private final List<LabTestRecord> records;
    private final Context context;
    private final String BASE_URL = "http://10.21.139.29/clinic";

    public LabTestResultsAdapter(Context context, List<LabTestRecord> records) {
        this.context = context;
        this.records = records;
    }

    @NonNull
    @Override
    public LabTestResultsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lab_test_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LabTestResultsAdapter.ViewHolder holder, int position) {
        LabTestRecord record = records.get(position);
        holder.tvInfo.setText("Patient ID: " + record.patientId + "\n" + record.content);

        checkIfGenerated(record.recordId, isGenerated -> {
            if (isGenerated) {
                holder.btnView.setEnabled(true);
                holder.btnView.setBackgroundTintList(context.getColorStateList(R.color.green));
                holder.btnGenerateOrSubmit.setText("Submit");
                holder.btnGenerateOrSubmit.setBackgroundTintList(context.getColorStateList(R.color.primary));
                holder.btnGenerateOrSubmit.setOnClickListener(v -> submitResult(record.recordId));
                holder.btnView.setOnClickListener(v -> {
                    Intent intent = new Intent(context, ViewGeneratedResultActivity.class);
                    intent.putExtra("record_id", record.recordId);
                    context.startActivity(intent);
                });
            } else {
                holder.btnView.setEnabled(false);
                holder.btnGenerateOrSubmit.setText("Generate Result");
                holder.btnGenerateOrSubmit.setBackgroundTintList(context.getColorStateList(R.color.blue));
                holder.btnGenerateOrSubmit.setOnClickListener(v -> {
                    Intent intent = new Intent(context, GenerateLabResultActivity.class);
                    intent.putExtra("record_id", record.recordId);
                    intent.putExtra("patient_id", record.patientId);
                    intent.putExtra("doctor_id", record.doctorId);
                    context.startActivity(intent);
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInfo;
        Button btnView, btnGenerateOrSubmit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInfo = itemView.findViewById(R.id.tvRecordInfo);
            btnView = itemView.findViewById(R.id.btnViewResult);
            btnGenerateOrSubmit = itemView.findViewById(R.id.btnGenerateOrSubmit);
        }
    }

    private void checkIfGenerated(int recordId, LabTestResultsFragment.ResultGeneratedCallback callback) {
        String url = BASE_URL + "/checkGeneratedResult.php?record_id=" + recordId;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> callback.onCheck(response.length() > 0),
                error -> callback.onCheck(false));
        Volley.newRequestQueue(context).add(request);
    }

    private void generateResult(int recordId, int patientId, int doctorId, String content) {
        String url = BASE_URL + "/generateLabResult.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {},
                error -> {}) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("record_id", String.valueOf(recordId));
                params.put("patient_id", String.valueOf(patientId));
                params.put("doctor_id", String.valueOf(doctorId));
                params.put("content", content + "\n\nResult: (Pending content...)");
                return params;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }

    private void submitResult(int recordId) {
        String url = BASE_URL + "/submitLabResult.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {},
                error -> {}) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("record_id", String.valueOf(recordId));
                return params;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }
}
