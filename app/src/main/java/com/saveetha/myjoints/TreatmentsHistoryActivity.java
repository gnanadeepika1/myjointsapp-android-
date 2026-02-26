package com.saveetha.myjoints;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TreatmentsHistoryActivity extends AppCompatActivity {

    private static final String TAG = "TREAT_DEBUG";

    private static final String BASE_URL =
            "http://14.139.187.229:8081/aug_batch2025/myjoints/";
    private static final String ADD_URL    = BASE_URL + "add_treatment.php";
    private static final String GET_URL    = BASE_URL + "get_treatments.php";
    private static final String DELETE_URL = BASE_URL + "delete_treatment.php";

    private ImageView backBtn;
    private TextView tvPatientId;
    private RecyclerView rvTreatments;
    private FloatingActionButton fabAddTreatment;

    private final List<TreatmentRecord> items = new ArrayList<>();
    private TreatmentsHistoryAdapter adapter;
    private String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treatments_history);

        backBtn = findViewById(R.id.back_btn);
        tvPatientId = findViewById(R.id.tvPatientId);
        rvTreatments = findViewById(R.id.rvTreatments);
        fabAddTreatment = findViewById(R.id.fabAddTreatment);

        backBtn.setOnClickListener(v -> onBackPressed());

        patientId = getIntent().getStringExtra("patient_id");
        if (TextUtils.isEmpty(patientId)) {
            Toast.makeText(this, "Patient ID missing", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvPatientId.setText("Patient ID: " + patientId);

        rvTreatments.setLayoutManager(new LinearLayoutManager(this));
        rvTreatments.setItemAnimator(null);

        adapter = new TreatmentsHistoryAdapter(items, this::confirmDeleteTreatment);
        rvTreatments.setAdapter(adapter);

        loadTreatmentsFromServer();

        fabAddTreatment.setOnClickListener(v -> showAddTreatmentDialog());
    }

    // =====================================================
    // ADD TREATMENT (ALL VALIDATIONS PRESENT)
    // =====================================================
    private void showAddTreatmentDialog() {
        View v = getLayoutInflater().inflate(R.layout.dialog_add_treatment, null);

        EditText etName = v.findViewById(R.id.etMedicationName);
        EditText etDose = v.findViewById(R.id.etDose);
        Spinner spinnerRoute = v.findViewById(R.id.spinnerRoute);
        EditText etFreqNum = v.findViewById(R.id.etFrequencyNumber);
        EditText etFreqText = v.findViewById(R.id.etFrequencyText);
        EditText etDuration = v.findViewById(R.id.etTimePeriodWeeks);

        String[] routes = {"Tablet", "Injection", "Infusion", "Other"};
        spinnerRoute.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                routes
        ));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Treatment")
                .setView(v)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button add = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            add.setOnClickListener(btn -> {

                String name = etName.getText().toString().trim();
                String dose = etDose.getText().toString().trim();
                String route = spinnerRoute.getSelectedItem().toString();
                String freqNum = etFreqNum.getText().toString().trim();
                String freqText = etFreqText.getText().toString().trim();
                String duration = etDuration.getText().toString().trim();

                // ===== Required =====
                if (TextUtils.isEmpty(name)) { etName.setError("Required"); return; }
                if (TextUtils.isEmpty(dose)) { etDose.setError("Required"); return; }
                if (TextUtils.isEmpty(freqNum)) { etFreqNum.setError("Required"); return; }
                if (TextUtils.isEmpty(freqText)) { etFreqText.setError("Required"); return; }
                if (TextUtils.isEmpty(duration)) { etDuration.setError("Required"); return; }

                // ===== Name =====
                if (!name.matches("^[A-Za-z ]+$")) {
                    etName.setError("Only letters and spaces allowed");
                    return;
                }

                // ===== Dose =====
                if (!dose.matches("^\\d+\\s*mg$")) {
                    etDose.setError("Dose must be in mg (e.g. 500mg)");
                    return;
                }
                int doseVal = Integer.parseInt(dose.replaceAll("\\D+", ""));
                if (doseVal < 1 || doseVal > 1000) {
                    etDose.setError("Dose must be between 1 and 1000 mg");
                    return;
                }

                // ===== Frequency Number =====
                if (!freqNum.matches("^\\d+$")) {
                    etFreqNum.setError("Numbers only");
                    return;
                }
                int freq = Integer.parseInt(freqNum);
                if (freq < 1 || freq > 10) {
                    etFreqNum.setError("Enter between 1 and 10");
                    return;
                }

                // ===== Frequency Text =====
                if (!freqText.matches("^[A-Za-z0-9 ]+$")) {
                    etFreqText.setError("Only letters, numbers and spaces allowed");
                    return;
                }

                // ===== Duration =====
                if (!duration.matches("^\\d+$")) {
                    etDuration.setError("Numbers only");
                    return;
                }
                int dur = Integer.parseInt(duration);
                if (dur < 1 || dur > 52) {
                    etDuration.setError("Enter between 1 and 52");
                    return;
                }

                addTreatmentToServer(
                        name, dose, route,
                        freqNum, freqText, duration
                );

                dialog.dismiss();
            });
        });

        dialog.show();
    }

    // =====================================================
    // ADD TREATMENT TO SERVER
    // =====================================================
    private void addTreatmentToServer(
            String name,
            String dose,
            String route,
            String freqNum,
            String freqText,
            String duration
    ) {
        new Thread(() -> {
            try {
                HttpURLConnection conn =
                        (HttpURLConnection) new URL(ADD_URL).openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                JSONObject body = new JSONObject();
                body.put("patient_id", patientId);
                body.put("name", name);
                body.put("dose", dose);
                body.put("route", route);
                body.put("frequency_number", freqNum);
                body.put("frequency_text", freqText);
                body.put("duration", duration);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                br.close();

                runOnUiThread(this::loadTreatmentsFromServer);

            } catch (Exception e) {
                Log.e(TAG, "ADD ERROR", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Server error", Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    // =====================================================
    // LOAD TREATMENTS
    // =====================================================
    private void loadTreatmentsFromServer() {
        new Thread(() -> {
            try {
                HttpURLConnection conn =
                        (HttpURLConnection) new URL(GET_URL).openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                JSONObject body = new JSONObject();
                body.put("patient_id", patientId);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject json = new JSONObject(sb.toString());
                JSONArray arr = json.optJSONArray("treatments");

                List<TreatmentRecord> temp = new ArrayList<>();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject t = arr.getJSONObject(i);
                        temp.add(new TreatmentRecord(
                                t.getInt("id"),
                                t.getString("name"),
                                t.getString("dose"),
                                t.getString("route"),
                                t.getString("frequency_number"),
                                t.getString("frequency_text"),
                                t.getString("duration"),
                                patientId
                        ));
                    }
                }

                runOnUiThread(() -> {
                    items.clear();
                    items.addAll(temp);
                    adapter.notifyDataSetChanged();
                });

            } catch (Exception e) {
                Log.e(TAG, "LOAD ERROR", e);
            }
        }).start();
    }

    private void confirmDeleteTreatment(TreatmentRecord r) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Treatment")
                .setMessage("Are you sure you want to delete this treatment?")
                .setPositiveButton("Yes", (d, w) -> deleteTreatmentFromServer(r))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteTreatmentFromServer(TreatmentRecord r) {
        new Thread(() -> {
            try {
                HttpURLConnection conn =
                        (HttpURLConnection) new URL(DELETE_URL).openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                JSONObject body = new JSONObject();
                body.put("id", r.getId());

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                br.close();

                runOnUiThread(this::loadTreatmentsFromServer);

            } catch (Exception e) {
                Log.e(TAG, "DELETE ERROR", e);
            }
        }).start();
    }
}
