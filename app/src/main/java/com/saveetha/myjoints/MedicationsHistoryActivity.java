package com.saveetha.myjoints;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.saveetha.myjoints.util.AiMedicationAdvisor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MedicationsHistoryActivity extends AppCompatActivity {

    private static final String TAG = "MED_DEBUG";

    private ImageView backBtn;
    private RecyclerView rvMedications;
    private TextView tvPatientId;
    private FloatingActionButton fabAddMedication;
    private TextView tvAiSuggestion;

    private final List<MedicationHistoryItem> items = new ArrayList<>();
    private MedicationHistoryAdapter adapter;
    private String patientId;

    private static final String BASE_URL =
            "http://14.139.187.229:8081/aug_batch2025/myjoints/";
    private static final String GET_MEDICATIONS_URL =
            BASE_URL + "get_medications.php";
    private static final String ADD_MEDICATION_URL =
            BASE_URL + "add_medication.php";
    private static final String DELETE_MEDICATION_URL =
            BASE_URL + "delete_medication.php";
    private static final String GET_GRAPH_URL =
            BASE_URL + "get_graph.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medications_history);

        backBtn = findViewById(R.id.back_btn);
        rvMedications = findViewById(R.id.rvMedications);
        tvPatientId = findViewById(R.id.tvPatientId);
        fabAddMedication = findViewById(R.id.fabAddMedication);
        tvAiSuggestion = findViewById(R.id.tvAiSuggestion);

        backBtn.setOnClickListener(v -> onBackPressed());

        Intent intent = getIntent();
        patientId = intent != null ? intent.getStringExtra("patient_id") : null;

        if (TextUtils.isEmpty(patientId)) {
            Toast.makeText(this, "No patient ID provided", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        patientId = patientId.toLowerCase().trim();
        if (!patientId.matches("^pat_\\d{4}$")) {
            Toast.makeText(this, "Invalid patient ID", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvPatientId.setText("Patient ID: " + patientId);

        adapter = new MedicationHistoryAdapter(
                items,
                this::confirmDeleteMedication
        );
        rvMedications.setLayoutManager(new LinearLayoutManager(this));
        rvMedications.setAdapter(adapter);

        loadMedicationsFromServer();
        loadLatestScoresAndRunAI();

        fabAddMedication.setOnClickListener(v -> showAddMedicationDialog());
    }

    // =====================================================
    // ADD MEDICATION DIALOG (FINAL VALIDATIONS)
    // =====================================================
    private void showAddMedicationDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_medication, null);

        EditText etName = view.findViewById(R.id.etMedicationName);
        EditText etDose = view.findViewById(R.id.etDose);
        EditText etPeriod = view.findViewById(R.id.etPeriod);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button btnAdd = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnAdd.setOnClickListener(v -> {

                String name = etName.getText().toString().trim();
                String dose = etDose.getText().toString().trim();
                String period = etPeriod.getText().toString().trim();

                // Required
                if (TextUtils.isEmpty(name)) {
                    etName.setError("Required");
                    return;
                }
                if (TextUtils.isEmpty(dose)) {
                    etDose.setError("Required");
                    return;
                }
                if (TextUtils.isEmpty(period)) {
                    etPeriod.setError("Required");
                    return;
                }

                // Name: letters + spaces
                if (!name.matches("^[A-Za-z ]+$")) {
                    etName.setError("Only letters and spaces allowed");
                    return;
                }

                // Dose: must contain mg
                if (!dose.matches("^\\d+\\s*mg$")) {
                    etDose.setError("Dose must be in mg (e.g. 500mg)");
                    return;
                }

                // Period: letters + numbers + spaces
                if (!period.matches("^[A-Za-z0-9 ]+$")) {
                    etPeriod.setError("Only letters, numbers and spaces allowed");
                    return;
                }

                addMedicationToServer(name, dose, period);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    // =====================================================
    // ADD MEDICATION
    // =====================================================
    private void addMedicationToServer(String name, String dose, String period) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(ADD_MEDICATION_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("patient_id", patientId);
                body.put("name", name);
                body.put("dose", dose);
                body.put("period", period);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.close();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                conn.getResponseCode() < 400
                                        ? conn.getInputStream()
                                        : conn.getErrorStream()
                        )
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject res = new JSONObject(sb.toString());
                boolean success = res.optBoolean("success", false);

                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(this, "Medication added", Toast.LENGTH_SHORT).show();
                        loadMedicationsFromServer();
                    } else {
                        Toast.makeText(
                                this,
                                res.optString("message", "Failed"),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Add medication error", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Server error", Toast.LENGTH_LONG).show()
                );
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    // =====================================================
    // DELETE MEDICATION
    // =====================================================
    private void confirmDeleteMedication(MedicationHistoryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Medication")
                .setMessage("Are you sure you want to delete this medication?")
                .setPositiveButton("Yes", (d, w) -> deleteMedicationFromServer(item))
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteMedicationFromServer(MedicationHistoryItem item) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(DELETE_MEDICATION_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                JSONObject body = new JSONObject();
                body.put("patient_id", patientId);
                body.put("name", item.getName());
                body.put("dose", item.getDose());
                body.put("period", item.getPeriod());

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.close();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                br.close();

                runOnUiThread(() -> {
                    Toast.makeText(this, "Medication deleted", Toast.LENGTH_SHORT).show();
                    loadMedicationsFromServer();
                });

            } catch (Exception e) {
                Log.e(TAG, "Delete medication error", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Delete failed", Toast.LENGTH_LONG).show()
                );
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    // =====================================================
    // LOAD MEDICATIONS
    // =====================================================
    private void loadMedicationsFromServer() {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(GET_MEDICATIONS_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                JSONObject body = new JSONObject();
                body.put("patient_id", patientId);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.close();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject json = new JSONObject(sb.toString());
                JSONArray arr = json.getJSONArray("medications");

                items.clear();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject m = arr.getJSONObject(i);
                    items.add(new MedicationHistoryItem(
                            m.getString("name"),
                            m.getString("dose"),
                            m.getString("period")
                    ));
                }

                runOnUiThread(() -> adapter.notifyDataSetChanged());

            } catch (Exception e) {
                Log.e(TAG, "Load medications error", e);
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    // =====================================================
    // AI SUGGESTION (UNCHANGED)
    // =====================================================
    private void loadLatestScoresAndRunAI() {
        new Thread(() -> {
            try {
                URL url = new URL(GET_GRAPH_URL + "?patient_id=" + patientId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject json = new JSONObject(sb.toString());
                JSONArray arr = json.getJSONArray("data");
                if (arr.length() == 0) return;

                JSONObject last = arr.getJSONObject(arr.length() - 1);
                double sdai = last.getDouble("sdai");
                double das28 = last.getDouble("das28_crp");

                String suggestion =
                        AiMedicationAdvisor.getSuggestion(sdai, das28);

                runOnUiThread(() ->
                        tvAiSuggestion.setText(
                                "🧠 AI Assessment\n" +
                                        "SDAI: " + sdai +
                                        " | DAS28: " + das28 +
                                        "\n💡 " + suggestion
                        )
                );

            } catch (Exception e) {
                Log.e(TAG, "AI error", e);
            }
        }).start();
    }
}
