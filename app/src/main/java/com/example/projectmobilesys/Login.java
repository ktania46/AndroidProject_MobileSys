package com.example.projectmobilesys;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.Executor;

public class Login extends AppCompatActivity {

    // UI View
    EditText email;
    EditText password;
    MaterialButton loginBtn;
    TextView createAccount;
    ProgressBar progressBar;
    private TextView button2;
    private ImageView authbtn;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private androidx.biometric.BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        createAccount = findViewById(R.id.createAccount);
        loginBtn = findViewById(R.id.loginBtn);
        progressBar = findViewById(R.id.progressbar);

        button2 = findViewById(R.id.button2);
        authbtn = findViewById(R.id.authbtn);

        loginBtn.setOnClickListener((v)-> loginUser());
        createAccount.setOnClickListener((v)->startActivity(new Intent(Login.this, UserDetails.class)));

        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate()){
            case BiometricManager.BIOMETRIC_SUCCESS:
                button2.setText("You can use fingerprint sensor to login");
                button2.setTextColor(Color.parseColor("#Fafafa"));
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                button2.setText("Device don't have fingerprint sensor");
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                button2.setText("Biometric sensor is currently unavailable");
                authbtn.setVisibility(View.GONE);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                button2.setText("Your device don't have any fingerprint saved, please check your security settings");
                authbtn.setVisibility(View.GONE);
                break;
        }

        // init bio metric

        executor = ContextCompat.getMainExecutor(this);

        androidx.biometric.BiometricPrompt biometricPrompt = new androidx.biometric.BiometricPrompt(Login.this, executor, new androidx.biometric.BiometricPrompt.AuthenticationCallback() {
            @Override // method called when there is error while authenticating
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override // method called when authentication is successful
            public void onAuthenticationSucceeded(@NonNull androidx.biometric.BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(), "Login successful !", Toast.LENGTH_SHORT).show();
            }

            @Override // method called when there is failure to authenticate
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        // Biometric dialogue box
        androidx.biometric.BiometricPrompt.PromptInfo promptInfo = new androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                .setTitle("Login")
                .setDescription("Use your fingerprint to login to app")
                .setNegativeButtonText("Cancel")
                .build();
        authbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                biometricPrompt.authenticate(promptInfo);
            }
        });
        // handle authBtn click, start authentication
        authbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
    void loginUser(){
        String email1 = email.getText().toString();
        String password1 = password.getText().toString();

        boolean isValidated = validateData(email1, password1);
        if(!isValidated){
            return;
        }
        loginAccountInFirebase(email1, password1);
    }

    void loginAccountInFirebase(String email1, String password1){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        changeInProgress(true);
        firebaseAuth.signInWithEmailAndPassword(email1, password1).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                changeInProgress(false);
                if(task.isSuccessful()) {
                    // login is success
                    if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                        startActivity(new Intent(Login.this, UserDetails.class));
                    } else {
                        // failure
                        Utility.showToast(Login.this, "Email not verified, please verify your email.");
                    }
                }else {
                    // login is failed
                    Utility.showToast(Login.this, task.getException().getLocalizedMessage());
                }
            }
        });

    }

    void changeInProgress(boolean inProgress){
        if(inProgress){
            progressBar.setVisibility(View.VISIBLE);
            loginBtn.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            loginBtn.setVisibility(View.VISIBLE);
        }
    }

    boolean validateData(String email1, String password1){
        // User data validation
        if(!Patterns.EMAIL_ADDRESS.matcher(email1).matches()){
            email.setError("Email is invalid");
            return false;
        }
        if(password1.length()<6){
            password.setError("Password length too short");
            return  false;
        }

        return true;
    }

}
