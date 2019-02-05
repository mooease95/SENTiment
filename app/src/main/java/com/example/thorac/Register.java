package com.example.thorac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity {

    private FirebaseAuth thoracAuth;

    private static final String TAG = "Registration";

    private EditText userEmail;
    private EditText userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirebaseApp.initializeApp(this);

        thoracAuth = FirebaseAuth.getInstance();
    }

    private void createNewUser(String email, String password) {
        Log.d(TAG, "createNewUser: " + email);

        if(!validateInfo()) {
            return;
        }

        thoracAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            //FirebaseUser user = thoracAuth.getCurrentUser();
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(Register.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    public void submitButton(View view) {
        userEmail = findViewById(R.id.email);
        userPassword = findViewById(R.id.password);

        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        createNewUser(email, password);

        Intent homeScreenIntent = new Intent(this, HomeScreen.class);
        startActivity(homeScreenIntent);

    }

    private boolean validateInfo() {
        boolean valid = true;

        String email = userEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            userEmail.setError("Required.");
            valid = false;
        } else {
            userEmail.setError(null);
        }

        String password = userPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            userPassword.setError("Required.");
            valid = false;
        } else {
            userPassword.setError(null);
        }

        return valid;
    }

}
