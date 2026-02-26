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

public class InvestigationsActivity extends AppCompatActivity {

    private static final String PREFS_NAME     = "patient_prefs";
    private static final String KEY_PATIENT_ID = "patient_id";

    // SAME server & folder as doctor
    private static final String BASE_URL = "http://14.139.187.229:8081/aug_batch2025/myjoints/";
    private static final String GET_INVESTIGATIONS_URL =
            BASE_URL + "get_investigations.php";

    private RecyclerView rvInvestigations;
    private ImageView backBtn;
    private final List<Investigation> data = new ArrayList<>();
    private InvestigationAdapter adapter;
    private String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investigations);

        rvInvestigations = findViewById(R.id.rvInvestigations);
        backBtn          = findViewById(R.id.back_btn);

        backBtn.setOnClickListener(v -> onBackPressed());

        // 1) get patient_id from SharedPreferences (set during signup/login)
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        patientId = prefs.getString(KEY_PATIENT_ID, null);

        // 2) fallback: maybe passed via Intent extras
        if (TextUtils.isEmpty(patientId)) {
            patientId = getIntent().getStringExtra("patient_id");
        }

        if (TextUtils.isEmpty(patientId)) {
            Toast.makeText(this,
                    "No patient ID found for Investigations",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        adapter = new InvestigationAdapter(this, data);
        rvInvestigations.setLayoutManager(new LinearLayoutManager(this));
        rvInvestigations.setAdapter(adapter);

        // Load from server
        loadInvestigationsFromServer();
    }

    private void loadInvestigationsFromServer() {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(GET_INVESTIGATIONS_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                // JSON body: { "patient_id": "P0001" }
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
                br.close();

                JSONObject response = new JSONObject(sb.toString());

                if (response.optBoolean("success")) {
                    JSONArray arr = response.optJSONArray("investigations");
                    data.clear();
                    if (arr != null) {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            List<String> details = new ArrayList<>();

                            addIfNotEmpty(details, "Hb", obj.optString("hb", ""));
                            addIfNotEmpty(details, "Total Leukocyte Count", obj.optString("total_leukocyte", ""));
                            addIfNotEmpty(details, "Differential Count", obj.optString("differential_count", ""));
                            addIfNotEmpty(details, "Platelet Count", obj.optString("platelet_count", ""));
                            addIfNotEmpty(details, "ESR", obj.optString("esr", ""));
                            addIfNotEmpty(details, "CRP", obj.optString("crp", ""));
                            addIfNotEmpty(details, "LFT Total Bilirubin", obj.optString("lft_total_bilirubin", ""));
                            addIfNotEmpty(details, "LFT Direct Bilirubin", obj.optString("lft_direct_bilirubin", ""));
                            addIfNotEmpty(details, "AST", obj.optString("ast", ""));
                            addIfNotEmpty(details, "ALT", obj.optString("alt", ""));
                            addIfNotEmpty(details, "Albumin", obj.optString("albumin", ""));
                            addIfNotEmpty(details, "Total Protein", obj.optString("total_protein", ""));
                            addIfNotEmpty(details, "GGT", obj.optString("ggt", ""));
                            addIfNotEmpty(details, "Urea", obj.optString("urea", ""));
                            addIfNotEmpty(details, "Creatinine", obj.optString("creatinine", ""));
                            addIfNotEmpty(details, "Uric Acid", obj.optString("uric_acid", ""));
                            addIfNotEmpty(details, "Urine Routine", obj.optString("urine_routine", ""));
                            addIfNotEmpty(details, "Urine PCR", obj.optString("urine_pcr", ""));
                            addIfNotEmpty(details, "RA Factor", obj.optString("ra_factor", ""));
                            addIfNotEmpty(details, "ANTI CCP", obj.optString("anti_ccp", ""));
                            addIfNotEmpty(details, "created_at", obj.optString("created_at", ""));

                            data.add(new Investigation("Investigation", details));
                        }
                    }
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                } else {
                    String msg = response.optString("message",
                            "Failed to load investigations");
                    runOnUiThread(() ->
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this,
                                "Error loading investigations",
                                Toast.LENGTH_LONG).show());
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private void addIfNotEmpty(List<String> list, String label, String value) {
        if (!TextUtils.isEmpty(value)) {
            String v = value.trim();
            if (!v.isEmpty()) {
                list.add(label + ": " + v);
            }
        }
    }
}
