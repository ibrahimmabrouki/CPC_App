package com.example.cpc;

import android.content.Context;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    private final List<Patient> patientList;
    private final Context context;
    private final String BASE_URL;
    private final int doctorId;
    private PopupMenu activePopup;

    public PatientAdapter(Context context, List<Patient> list, String baseUrl, int doctorId) {
        this.context = context;
        this.patientList = list;
        this.BASE_URL = baseUrl;
        this.doctorId = doctorId;
    }


    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_patient, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patientList.get(position);
        holder.nameId.setText(patient.name + " (ID: " + patient.id + ")");
        holder.reason.setText("Reason: " + patient.reason);

        holder.btnView.setOnClickListener(v ->
                Toast.makeText(context, "Viewing record of " + patient.name, Toast.LENGTH_SHORT).show()
        );

        holder.btnGenerate.setOnClickListener(v -> {
            if (activePopup != null) activePopup.dismiss();

            ContextThemeWrapper themedContext = new ContextThemeWrapper(context, R.style.PopupMenuStyle);
            activePopup = new PopupMenu(themedContext, v);
            MenuInflater inflater = activePopup.getMenuInflater();
            inflater.inflate(R.menu.generate_menu, activePopup.getMenu());

            activePopup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.menu_prescription) {
                    Intent prescriptionIntent = new Intent(context, PrescriptionActivity.class);
                    prescriptionIntent.putExtra("patient_id", patient.id);
                    context.startActivity(prescriptionIntent);
                    return true;
                } else if (id == R.id.menu_medical_record) {
                    Intent medicalIntent = new Intent(context, MedicalRecordActivity.class);
                    medicalIntent.putExtra("patient_id", patient.id);
                    context.startActivity(medicalIntent);
                    return true;
                } else if (id == R.id.menu_lab_test) {
                    Intent labIntent = new Intent(context, LabTestOrderActivity.class);
                    labIntent.putExtra("patient_id", patient.id);
                    context.startActivity(labIntent);
                    return true;
                }
                return false;
            });

            activePopup.show();
        });

        holder.btnDelete.setOnClickListener(v -> {
            String url = BASE_URL + "/hidePatient.php";
            RequestQueue queue = Volley.newRequestQueue(context);

            StringRequest request = new StringRequest(Request.Method.POST, url,
                    response -> {
                        if (response.equals("success")) {
                            patientList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, patientList.size());
                            Toast.makeText(context, "Patient hidden successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to hide patient", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("doctor_id", String.valueOf(doctorId));
                    params.put("patient_id", patient.id);
                    return params;
                }
            };

            queue.add(request);
        });

    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    public static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView nameId, reason;
        Button btnView, btnGenerate;
        ImageView btnDelete;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            nameId = itemView.findViewById(R.id.tv_name_id);
            reason = itemView.findViewById(R.id.tv_reason);
            btnView = itemView.findViewById(R.id.btn_view);
            btnGenerate = itemView.findViewById(R.id.btn_generate);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
