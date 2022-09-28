package com.example.projectmobilesys;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {

    EditText name;
    EditText studentId;
    EditText email;
    EditText password;
    EditText confirmPassword;
    ProgressBar progressBar;
    MaterialButton registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name =findViewById(R.id.name);
        studentId = findViewById(R.id.stdid);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmpassword);
        registerBtn = findViewById(R.id.registerbtn);
        progressBar = findViewById(R.id.progressbar);

        registerBtn.setOnClickListener(v-> createAccount());
        //startActivity(new Intent(getApplication(), MainActivity.class));
    }

    void createAccount() {

        String name1 = name.getText().toString();
        String studentId1 = studentId.getText().toString();
        String email1 = email.getText().toString();
        String password1 = password.getText().toString();
        String confirmPassword1 = confirmPassword.getText().toString();

        boolean isValidated = validateData(email1, password1, confirmPassword1);
        if(!isValidated){
            return;
        }
        createAccountInFirebase(email1, password1);

    }

    void createAccountInFirebase(String email1, String password1) {
        changeInProgress(true);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email1, password1).addOnCompleteListener(Register.this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // creating account is done
                            Utility.showToast(Register.this, "Successfully created account, check email to verify");
                            firebaseAuth.getCurrentUser().sendEmailVerification();
                            firebaseAuth.signOut();
                            finish();
                        } else {
                            // failure
                            Utility.showToast(Register.this, task.getException().getLocalizedMessage());
                        }
                    }
                });

    }
    void changeInProgress(boolean inProgress){
        if(inProgress){
            progressBar.setVisibility(View.VISIBLE);
            registerBtn.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            registerBtn.setVisibility(View.VISIBLE);
        }
    }

    boolean validateData(String email1, String password1, String confirmPassword1){
        // User data validation
        if(!Patterns.EMAIL_ADDRESS.matcher(email1).matches()){
            email.setError("Email is invalid");
            return false;
        }
        if(password1.length()<6){
            password.setError("Password length too short");
            return  false;
        }
        if(!password1.equals(confirmPassword1)){
            confirmPassword.setError("Password not matched");
            return false;
        }
        return true;
    }
}