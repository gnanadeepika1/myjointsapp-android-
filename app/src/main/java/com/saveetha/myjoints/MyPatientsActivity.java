package com.saveetha.myjoints;

import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.saveetha.network.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyPatientsActivity extends AppCompatActivity {

    private LinearLayout containerCards;
    private ImageView btnBack, btnLogout, btnDeleteAccount;
    private TextView tvDoctorHeaderName, tvDoctorCardName, tvDoctorCardEmail, tvPatientsTitle;

    private static final String PREFS_NAME = "doctor_prefs";
    private static final String KEY_DOCTOR_ID = "doctor_id";

    private static final String GET_DOCTOR_URL =
            RetrofitClient.BASE_URL + "get_doctor.php";
    private static final String GET_PATIENTS_URL =
            RetrofitClient.BASE_URL + "get_patients.php";
    private static final String DELETE_DOCTOR_URL =
            RetrofitClient.BASE_URL + "delete_doctor_account.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_patients);

        containerCards = findViewById(R.id.containerCards);
        btnBack = findViewById(R.id.btnBack);
        btnLogout = findViewById(R.id.btnLogout);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        tvDoctorHeaderName = findViewById(R.id.tvDoctorHeaderName);
        tvDoctorCardName = findViewById(R.id.tvDoctorCardName);
        tvDoctorCardEmail = findViewById(R.id.tvDoctorCardEmail);
        tvPatientsTitle = findViewById(R.id.tvPatientsTitle);

        btnBack.setOnClickListener(v -> onBackPressed());
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmation());

        loadDoctorNameFromServer();
        loadPatientsFromServer();
    }

    // ================= LOGOUT =================
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (d, w) -> {
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                            .edit().clear().apply();

                    Intent i = new Intent(this, DoctorLoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    // ================= DELETE ACCOUNT =================
    private void showDeleteAccountConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete the account?")
                .setPositiveButton("Yes", (d, w) -> deleteDoctorAccount())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteDoctorAccount() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String doctorId = prefs.getString(KEY_DOCTOR_ID, "");

        if (doctorId.isEmpty()) {
            Toast.makeText(this, "Doctor ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(DELETE_DOCTOR_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("doctor_id", doctorId);

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

                JSONObject response = new JSONObject(sb.toString());

                runOnUiThread(() -> {
                    if (response.optBoolean("success")) {
                        prefs.edit().clear().apply();
                        Intent i = new Intent(this, DoctorLoginActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(this,
                                response.optString("message", "Delete failed"),
                                Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this,
                                "Server error while deleting account",
                                Toast.LENGTH_LONG).show());
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    // ================= FETCH DOCTOR =================
    private void loadDoctorNameFromServer() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String doctorId = prefs.getString(KEY_DOCTOR_ID, "");
        if (doctorId.isEmpty()) return;

        new Thread(() -> {
            try {
                URL url = new URL(GET_DOCTOR_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("doctor_id", doctorId);

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
                if (resp.optBoolean("success")) {
                    JSONObject d = resp.optJSONObject("doctor");
                    if (d != null) {
                        runOnUiThread(() -> {
                            String name = "DR " + d.optString("full_name");
                            tvDoctorHeaderName.setText(name);
                            tvDoctorCardName.setText(name);
                            tvDoctorCardEmail.setText(d.optString("email"));
                        });
                    }
                }
            } catch (Exception ignored) {}
        }).start();
    }

    // ================= FETCH PATIENTS =================
    private void loadPatientsFromServer() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String doctorId = prefs.getString(KEY_DOCTOR_ID, "");
        if (doctorId.isEmpty()) return;

        new Thread(() -> {
            try {
                URL url = new URL(GET_PATIENTS_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("doctor_id", doctorId);

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
                JSONArray arr = resp.optJSONArray("patients");

                runOnUiThread(() -> {
                    containerCards.removeAllViews();
                    if (arr == null) {
                        tvPatientsTitle.setText("My Patients (0)");
                        return;
                    }
                    tvPatientsTitle.setText("My Patients (" + arr.length() + ")");
                    LayoutInflater inflater = LayoutInflater.from(this);

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject p = arr.optJSONObject(i);
                        if (p == null) continue;

                        View card = inflater.inflate(
                                R.layout.item_patient_card,
                                containerCards,
                                false
                        );

                        ((TextView) card.findViewById(R.id.tvPatientName))
                                .setText(p.optString("name"));
                        ((TextView) card.findViewById(R.id.tvPatientEmail))
                                .setText(p.optString("email"));

                        String pid = p.optString("patient_id");

                        card.setOnClickListener(v -> {
                            Intent i1 = new Intent(this, MedicalRecordsActivity.class);
                            i1.putExtra("patient_id", pid);
                            startActivity(i1);
                        });

                        containerCards.addView(card);
                    }
                });

            } catch (Exception ignored) {}
        }).start();
    }
}
