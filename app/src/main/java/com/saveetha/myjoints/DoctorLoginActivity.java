package com.saveetha.myjoints;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.saveetha.myjoints.util.Static;
import com.saveetha.network.RetrofitClient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DoctorLoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ImageView backBtn;
    private TextView tvSignUp, tvForgotPassword;

    private static final String PREFS_NAME = "doctor_prefs";
    private static final String KEY_DOCTOR_ID = "doctor_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_SPECIAL = "specialization";
    private static final String KEY_PASSWORD = "password";

    private static final String LOGIN_URL =
            RetrofitClient.BASE_URL + "doctor_login.php";

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        backBtn = findViewById(R.id.back_btn);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        backBtn.setOnClickListener(v -> finish());

        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, DoctorSignupActivity.class))
        );

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            intent.putExtra("USER_TYPE", "doctor");
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {

        String doctorId = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (doctorId.isEmpty()) {
            etUsername.setError("Doctor ID is required");
            return;
        }

        if (!doctorId.matches("(?i)^doc_\\d{4}$")) {
            etUsername.setError("Doctor ID must be like doc_1001");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            return;
        }

        sendLoginRequest(doctorId, password);
    }

    private void sendLoginRequest(String doctorId, String password) {

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                JSONObject body = new JSONObject();
                body.put("doctor_id", doctorId);
                body.put("password", password);

                URL url = new URL(LOGIN_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.close();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject respJson = new JSONObject(sb.toString());
                boolean success = respJson.optBoolean("success");

                runOnUiThread(() -> {
                    if (success) {
                        prefs.edit()
                                .putString(KEY_DOCTOR_ID, respJson.optString("doctor_id"))
                                .putString(KEY_FULL_NAME, respJson.optString("full_name"))
                                .putString(KEY_EMAIL, respJson.optString("email"))
                                .putString(KEY_PHONE, respJson.optString("phone"))
                                .putString(KEY_SPECIAL, respJson.optString("specialization"))
                                .putString(KEY_PASSWORD, password)
                                .apply();

                        startActivity(new Intent(this, MyPatientsActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this,
                                respJson.optString("message", "Invalid Credentials"),
                                Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> Static.showError(this, e.getMessage()));
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}
