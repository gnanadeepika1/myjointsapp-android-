package com.saveetha.myjoints;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.saveetha.network.RetrofitClient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ImageView backBtn;
    private EditText etEmail, etNewPassword, etConfirmPassword;
    private MaterialButton btnSubmit;

    // 🔹 SAME PASSWORD RULE AS SIGNUP / LOGIN
    private static final String PASSWORD_PATTERN =
            "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/])[A-Za-z\\d!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/]{6,10}$";

    // 🔹 SAME EMAIL RULE AS SIGNUP
    private static final String EMAIL_PATTERN =
            "^[A-Za-z][A-Za-z0-9]*@gmail\\.com$";

    private static final String FORGOT_PASSWORD_URL =
            RetrofitClient.BASE_URL + "forgot_password.php";

    private String userType; // doctor or patient

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        backBtn = findViewById(R.id.back_btn);
        etEmail = findViewById(R.id.etEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSubmit = findViewById(R.id.btnSubmit);

        userType = getIntent().getStringExtra("USER_TYPE");
        if (userType == null) {
            Toast.makeText(this, "User type missing", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        backBtn.setOnClickListener(v -> onBackPressed());
        btnSubmit.setOnClickListener(v -> handleSubmit());
    }

    private void handleSubmit() {

        String email = etEmail.getText().toString().trim();
        String newPass = etNewPassword.getText().toString();
        String confPass = etConfirmPassword.getText().toString();

        // 🔹 Email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }
        if (!email.matches(EMAIL_PATTERN)) {
            etEmail.setError("Email must be abc123@gmail.com");
            return;
        }

        // 🔹 New Password
        if (TextUtils.isEmpty(newPass)) {
            etNewPassword.setError("New password is required");
            return;
        }
        if (!newPass.matches(PASSWORD_PATTERN)) {
            etNewPassword.setError(
                    "Password must be 6–10 chars with 1 capital, 1 digit & 1 special character"
            );
            return;
        }

        // 🔹 Confirm Password
        if (TextUtils.isEmpty(confPass)) {
            etConfirmPassword.setError("Confirm your new password");
            return;
        }
        if (!newPass.equals(confPass)) {
            etConfirmPassword.setError("Passwords do not match");
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        sendDataToServer(email, newPass);
    }

    private void sendDataToServer(String email, String newPass) {

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                JSONObject body = new JSONObject();
                body.put("email", email);
                body.put("new_password", newPass);
                body.put("user_type", userType); // doctor / patient

                URL url = new URL(FORGOT_PASSWORD_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.close();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONObject response = new JSONObject(sb.toString());

                runOnUiThread(() -> {
                    Toast.makeText(
                            this,
                            response.optString("message"),
                            Toast.LENGTH_LONG
                    ).show();

                    if (response.optBoolean("success")) {
                        finish(); // back to login
                    }
                });

            } catch (Exception e) {
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
