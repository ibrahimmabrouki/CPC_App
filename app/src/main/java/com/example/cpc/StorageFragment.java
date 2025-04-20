package com.example.cpc;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;

public class StorageFragment extends Fragment {

    private final String BASE_URL = "http://10.21.186.199/clinic";
    private LinearLayout storageContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_storage, container, false);
        storageContainer = view.findViewById(R.id.storageContainer);

        fetchMedicines();
        return view;
    }

    private void fetchMedicines() {
        String url = BASE_URL + "/getMedicines.php";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    storageContainer.removeAllViews();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject med = response.getJSONObject(i);
                            String name = med.getString("name");
                            int quantity = med.getInt("quantity");
                            String imageKey = med.getString("image_url");

                            addMedicineItem(name, quantity, imageKey);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> {
                    TextView errorMsg = new TextView(getContext());
                    errorMsg.setText("Failed to load medicines.");
                    storageContainer.addView(errorMsg);
                });

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void addMedicineItem(String name, int quantity, String imageName) {
        View item = LayoutInflater.from(getContext()).inflate(R.layout.item_medicine, storageContainer, false);

        TextView nameView = item.findViewById(R.id.tvMedicineName);
        TextView qtyView = item.findViewById(R.id.tvQuantity);
        ImageView imgView = item.findViewById(R.id.imgMedicine);

        nameView.setText(name);
        qtyView.setText("Quantity: " + quantity);

        try {
            int imageResId = getResources().getIdentifier(imageName, "drawable", requireContext().getPackageName());
            if (imageResId != 0) {
                imgView.setImageResource(imageResId);
            } else {
                imgView.setImageResource(R.drawable.placeholder);
            }
        } catch (Resources.NotFoundException e) {
            imgView.setImageResource(R.drawable.placeholder);
        }

        storageContainer.addView(item);
    }
}
