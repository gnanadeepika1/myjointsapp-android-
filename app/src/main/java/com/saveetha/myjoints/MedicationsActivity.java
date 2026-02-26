package com.saveetha.myjoints;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MedicationsActivity extends AppCompatActivity {

    private static final String PREFS_NAME     = "patient_prefs";
    private static final String KEY_PATIENT_ID = "patient_id";

    // ✅ SAME server & folder as doctor
    private static final String BASE_URL            = "http://14.139.187.229:8081/aug_batch2025/myjoints/";
    private static final String GET_MEDICATIONS_URL = BASE_URL + "get_medications.php";

    private RecyclerView rvMedications;
    private ImageView backBtn;

    private final List<Medication> medicationList = new ArrayList<>();
    private MedicationAdapter adapter;
    private String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medications);

        rvMedications = findViewById(R.id.rvMedications);
        backBtn       = findViewById(R.id.back_btn);

        backBtn.setOnClickListener(v -> onBackPressed());

        // Get patient ID saved at login
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        patientId = prefs.getString(KEY_PATIENT_ID, null);

        if (TextUtils.isEmpty(patientId)) {
            Toast.makeText(this,
                    "No patient ID found. Please login again.",
                    Toast.LENGTH_LONG).show();
            // Optional: you can finish() here
            // finish();
            // return;
        }

        adapter = new MedicationAdapter(this, medicationList);
        rvMedications.setLayoutManager(new LinearLayoutManager(this));
        rvMedications.setAdapter(adapter);

        // Load from server (doctor-entered data)
        loadMedicationsFromServer();
    }

    // ================================================================
    // LOAD MEDICATIONS FROM SERVER (READ ONLY)
    // ================================================================
    private void loadMedicationsFromServer() {
        if (TextUtils.isEmpty(patientId)) return;

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(GET_MEDICATIONS_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                // Send JSON body with patient_id
                JSONObject body = new JSONObject();
                body.put("patient_id", patientId);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                InputStream is = (conn.getResponseCode() < 400)
                        ? conn.getInputStream()
                        : conn.getErrorStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject response = new JSONObject(sb.toString());

                if (response.optBoolean("success")) {
                    JSONArray arr = response.getJSONArray("medications");

                    // Build lines for the single "Medications" card
                    List<String> lines = new ArrayList<>();

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject m = arr.getJSONObject(i);
                        String name   = m.optString("name", "");
                        String dose   = m.optString("dose", "");
                        String period = m.optString("period", "");

                        StringBuilder lineBuilder = new StringBuilder();
                        lineBuilder.append(name);
                        if (!TextUtils.isEmpty(dose)) {
                            lineBuilder.append(" | Dose: ").append(dose);
                        }
                        if (!TextUtils.isEmpty(period)) {
                            lineBuilder.append(" | Period: ").append(period);
                        }
                        lines.add(lineBuilder.toString());
                    }

                    medicationList.clear();
                    if (!lines.isEmpty()) {
                        // Medication(title, List<String> items)
                        medicationList.add(new Medication("Medications", lines));
                    }

                    runOnUiThread(() -> adapter.notifyDataSetChanged());

                } else {
                    String msg = response.optString("message", "Failed to load medications");
                    runOnUiThread(() ->
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this,
                                "Error loading medications",
                                Toast.LENGTH_LONG).show());
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}
