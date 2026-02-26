package com.saveetha.myjoints;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.saveetha.network.RetrofitClient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PatientLoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ImageView backBtn;
    private TextView tvSignUp, tvForgotPassword;

    private static final String PREFS_NAME = "patient_prefs";
    private static final String KEY_PATIENT_ID = "patient_id";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_PATIENT_NAME = "patient_name";
    private static final String KEY_PATIENT_EMAIL = "patient_email";

    private static final String LOGIN_URL =
            RetrofitClient.BASE_URL + "patient_login_api.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        backBtn = findViewById(R.id.back_btn);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        backBtn.setOnClickListener(v -> onBackPressed());

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(
                    PatientLoginActivity.this,
                    ForgotPasswordActivity.class
            );
            intent.putExtra("USER_TYPE", "patient");
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> handleLogin());

        tvSignUp.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                PatientLoginActivity.this,
                                PatientSignupActivity.class
                        )
                )
        );
    }

    private void handleLogin() {

        String patientId = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(patientId)) {
            etUsername.setError("Patient ID is required");
            return;
        }

        if (!patientId.matches("(?i)^pat_\\d{4}$")) {
            etUsername.setError("Patient ID must be like pat_1001");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        // ❌ NO PASSWORD FORMAT VALIDATION DURING LOGIN

        loginPatientOnServer(patientId, password);
    }

    private void loginPatientOnServer(String patientId, String password) {

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(LOGIN_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                JSONObject body = new JSONObject();
                body.put("patient_id", patientId);
                body.put("password", password);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = conn.getResponseCode();
                InputStream is =
                        (responseCode >= 200 && responseCode < 400)
                                ? conn.getInputStream()
                                : conn.getErrorStream();

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(is));

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                runOnUiThread(() ->
                        handleLoginResponse(sb.toString(), patientId, password)
                );

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(
                                PatientLoginActivity.this,
                                "Server connection failed",
                                Toast.LENGTH_LONG
                        ).show()
                );
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private void handleLoginResponse(
            String response,
            String patientId,
            String password
    ) {
        try {
            JSONObject json = new JSONObject(response);
            boolean success = json.getBoolean("success");

            if (success) {
                String patientName = json.optString("name");
                String patientEmail = json.optString("email");

                SharedPreferences prefs =
                        getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

                prefs.edit()
                        .putString(KEY_PATIENT_ID, patientId)
                        .putString(KEY_PASSWORD, password)
                        .putString(KEY_PATIENT_NAME, patientName)
                        .putString(KEY_PATIENT_EMAIL, patientEmail)
                        .apply();

                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

                Intent intent =
                        new Intent(this, PatientDashboardActivity.class);
                intent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_NEW_TASK
                );
                startActivity(intent);
                finish();

            } else {
                Toast.makeText(
                        this,
                        json.optString("message", "Login failed"),
                        Toast.LENGTH_LONG
                ).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(
                    this,
                    "Invalid server response",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
