package com.saveetha.myjoints;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * PatientDashboardActivity
 * Uses activity_patient_dashboard.xml and item_card.xml.
 */
public class PatientDashboardActivity extends AppCompatActivity {

    private LinearLayout containerCards;
    private ImageView btnBack;
    private ImageView btnLogout;
    private ImageView btnDeleteAccount;

    private TextView tvGreeting;
    private TextView tvSubtitle;
    private TextView tvProfileName;
    private TextView tvProfileEmail;

    private final String[] labels = {
            "Complaints",
            "Comorbidities",
            "Daily Assessment",
            "Medications",
            "Investigations",
            "Treatments",
            "Diet",
            "Exercises",
            "Consult Now"
    };

    private final int[] icons = {
            R.drawable.ic_complaints,
            R.drawable.ic_comorbidities,
            R.drawable.ic_daily_assessment,
            R.drawable.ic_medications,
            R.drawable.ic_investigations,
            R.drawable.ic_treatments,
            R.drawable.ic_diet,
            R.drawable.ic_exercises,
            R.drawable.ic_consult_now
    };

    private static final String PREFS_NAME = "patient_prefs";
    private static final String KEY_PATIENT_ID = "patient_id";
    private static final String KEY_PATIENT_NAME = "patient_name";
    private static final String KEY_PATIENT_EMAIL = "patient_email";

    // 🔹 DELETE ACCOUNT API
    private static final String DELETE_ACCOUNT_URL =
            "http://14.139.187.229:8081/aug_batch2025/myjoints/delete_patient_account.php";

    String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dashboard);

        containerCards = findViewById(R.id.containerCards);
        btnBack = findViewById(R.id.btnBack);
        btnLogout = findViewById(R.id.btnLogout);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        tvGreeting = findViewById(R.id.tvGreeting);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);

        SharedPreferences prefs =
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        patientId = prefs.getString(KEY_PATIENT_ID, "");
        String patientName = prefs.getString(KEY_PATIENT_NAME, "");
        String patientEmail = prefs.getString(KEY_PATIENT_EMAIL, "");

        Intent intent = getIntent();
        if (intent != null) {
            String extraName = intent.getStringExtra("patient_name");
            String extraEmail = intent.getStringExtra("patient_email");
            if (!TextUtils.isEmpty(extraName)) patientName = extraName;
            if (!TextUtils.isEmpty(extraEmail)) patientEmail = extraEmail;
        }

        if (TextUtils.isEmpty(patientName)) patientName = "Patient";
        if (TextUtils.isEmpty(patientEmail)) patientEmail = "patient@example.com";

        tvGreeting.setText("Hi, " + patientName);
        tvSubtitle.setText(patientEmail);
        tvProfileName.setText(patientName);
        tvProfileEmail.setText(patientEmail);

        btnBack.setOnClickListener(v -> onBackPressed());

        // 🔹 Logout confirmation (UNCHANGED)
        btnLogout.setOnClickListener(v -> showLogoutConfirmation(prefs));

        // 🔹 Delete account confirmation (UPDATED)
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmation(prefs));

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < labels.length; i++) {
            final String label = labels[i];
            final int iconRes = icons[i];

            View card = inflater.inflate(
                    R.layout.item_card,
                    containerCards,
                    false
            );

            ImageView cardImage = card.findViewById(R.id.cardImage);
            TextView cardText = card.findViewById(R.id.cardText);

            cardText.setText(label);
            cardImage.setImageResource(iconRes);

            card.setOnClickListener(v -> {
                switch (label) {
                    case "Complaints":
                        startActivity(new Intent(this, ComplaintsActivity.class));
                        break;
                    case "Comorbidities":
                        startActivity(new Intent(this, ComorbiditiesActivity.class));
                        break;
                    case "Daily Assessment":
                        Intent da = new Intent(this, DailySelfAssessmentActivity.class);
                        da.putExtra("patient_id", patientId);
                        startActivity(da);
                        break;
                    case "Medications":
                        startActivity(new Intent(this, MedicationsActivity.class));
                        break;
                    case "Investigations":
                        startActivity(new Intent(this, InvestigationsActivity.class));
                        break;
                    case "Treatments":
                        startActivity(new Intent(this, TreatmentsActivity.class));
                        break;
                    case "Diet":
                        startActivity(new Intent(this, DietRecommendationsActivity.class));
                        break;
                    case "Exercises":
                        startActivity(new Intent(this, ExerciseRecommendationsActivity.class));
                        break;
                    case "Consult Now":
                        startActivity(new Intent(this, ConsultNowActivity.class));
                        break;
                    default:
                        Toast.makeText(this, label, Toast.LENGTH_SHORT).show();
                }
            });

            containerCards.addView(card);
        }
    }

    // =========================================================
    // LOGOUT CONFIRMATION
    // =========================================================
    private void showLogoutConfirmation(SharedPreferences prefs) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    prefs.edit().clear().apply();
                    Intent i = new Intent(
                            PatientDashboardActivity.this,
                            PatientLoginActivity.class
                    );
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    // =========================================================
    // DELETE ACCOUNT (SERVER-SIDE)
    // =========================================================
    private void showDeleteAccountConfirmation(SharedPreferences prefs) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete the account?")
                .setPositiveButton("Yes", (dialog, which) ->
                        deleteAccountFromServer(prefs)
                )
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteAccountFromServer(SharedPreferences prefs) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(DELETE_ACCOUNT_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                JSONObject body = new JSONObject();
                body.put("patient_id", patientId);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.flush();
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
                        Toast.makeText(this,
                                "Account deleted successfully",
                                Toast.LENGTH_LONG).show();
                        Intent i = new Intent(
                                PatientDashboardActivity.this,
                                PatientLoginActivity.class
                        );
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
                                Toast.LENGTH_LONG).show()
                );
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}
