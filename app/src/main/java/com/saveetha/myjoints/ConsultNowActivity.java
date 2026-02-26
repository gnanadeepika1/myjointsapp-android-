package com.saveetha.myjoints;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ConsultNowActivity extends AppCompatActivity {

    private LinearLayout llDoctors;
    private ImageView backBtn;
    private EditText etComplaint;
    private Button btnSend;

    private RadioButton selectedRadio = null;
    private String selectedDoctorId = null;

    private static final String PREFS_NAME = "patient_prefs";
    private static final String KEY_PATIENT_ID = "patient_id";

    private static final String GET_DOCTORS_URL =
            "http://14.139.187.229:8081/aug_batch2025/myjoints/get_doctors.php";

    private static final String ASSIGN_URL =
            "http://14.139.187.229:8081/aug_batch2025/myjoints/assign_doctor_to_patient.php";

    static class Doctor {
        final String emoji;
        final String name;
        final String role;
        final String doctorId;

        Doctor(String emoji, String name, String role, String doctorId) {
            this.emoji = emoji;
            this.name = name;
            this.role = role;
            this.doctorId = doctorId;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consult_now);

        llDoctors = findViewById(R.id.llDoctors);
        backBtn = findViewById(R.id.back_btn);
        etComplaint = findViewById(R.id.etComplaint);
        btnSend = findViewById(R.id.btnSend);

        backBtn.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> {
            if (selectedDoctorId == null) {
                Toast.makeText(this, "Please select a doctor", Toast.LENGTH_SHORT).show();
                return;
            }

            String complaint = etComplaint.getText().toString().trim();
            if (complaint.isEmpty()) {
                Toast.makeText(this, "Please describe your complaint", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String patientId = prefs.getString(KEY_PATIENT_ID, "");

            if (patientId.isEmpty()) {
                Toast.makeText(this, "Please login again", Toast.LENGTH_LONG).show();
                return;
            }

            assignDoctorToPatient(patientId, selectedDoctorId, complaint);
        });
    }

    // 🔥 IMPORTANT FIX
    // Reload doctors every time this page becomes visible
    @Override
    protected void onResume() {
        super.onResume();
        fetchDoctors();
    }

    private void fetchDoctors() {
        new Thread(() -> {
            try {
                URL url = new URL(GET_DOCTORS_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject json = new JSONObject(sb.toString());
                if (!json.getBoolean("success")) return;

                JSONArray arr = json.getJSONArray("doctors");
                List<Doctor> doctors = new ArrayList<>();

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject d = arr.getJSONObject(i);
                    doctors.add(new Doctor(
                            "👨‍⚕️",
                            d.getString("name"),
                            d.getString("specialization"),
                            d.getString("doctor_id")
                    ));
                }

                runOnUiThread(() -> populateDoctors(doctors));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to load doctors", Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private void populateDoctors(List<Doctor> doctors) {
        llDoctors.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        selectedDoctorId = null;
        selectedRadio = null;

        for (Doctor d : doctors) {
            View card = inflater.inflate(
                    R.layout.item_consult_card, llDoctors, false);

            TextView tvEmoji = card.findViewById(R.id.tvEmoji);
            TextView tvDoctorName = card.findViewById(R.id.tvDoctorName);
            TextView tvDoctorRole = card.findViewById(R.id.tvDoctorRole);
            RadioButton radio = card.findViewById(R.id.radioSelect);

            tvEmoji.setText(d.emoji);
            tvDoctorName.setText(d.name);
            tvDoctorRole.setText(d.role);

            View.OnClickListener selectListener = v -> {
                if (selectedRadio != null && selectedRadio != radio) {
                    selectedRadio.setChecked(false);
                }
                radio.setChecked(true);
                selectedRadio = radio;
                selectedDoctorId = d.doctorId;
            };

            card.setOnClickListener(selectListener);
            radio.setOnClickListener(selectListener);

            llDoctors.addView(card);
        }
    }

    private void assignDoctorToPatient(
            String patientId,
            String doctorId,
            String complaint
    ) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(ASSIGN_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                JSONObject body = new JSONObject();
                body.put("patient_id", patientId);
                body.put("doctor_id", doctorId);
                body.put("complaint", complaint);

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

                JSONObject resp = new JSONObject(sb.toString());
                boolean success = resp.optBoolean("success");
                String msg = resp.optString("message");

                runOnUiThread(() -> {
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    if (success) finish();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(
                                this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}
