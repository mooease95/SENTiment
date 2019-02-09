package com.example.thorac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import co.chatsdk.ui.main.BaseActivity;
import timber.log.Timber;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseUser;

public class EmailAuth extends MainActivity implements View.OnClickListener {

    private static final String TAG = "EmailAuth";

    private EditText mEmailField;
    private EditText mPasswordField;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_auth);

        mEmailField = findViewById(R.id.emailField);
        mPasswordField = findViewById(R.id.passwordField);

        findViewById(R.id.signin).setOnClickListener(this);
        findViewById(R.id.register).setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void createAccount(String email, String password) {
        Timber.d("%s: createAccount: %s", TAG, email);
        if(!validateForm()) {
            return;
        }

        showProgressDialog();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(EmailAuth.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    private void signIn(String email, String password) {
        Timber.d("%s: signIn: %s", TAG, email);
        if(!validateForm()) {
            return;
        }

        showProgressDialog();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(EmailAuth.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
//                        if (!task.isSuccessful()) {
//                            mStatusTextView.setText(R.string.auth_failed);
//                        }
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if(user != null) {
            findViewById(R.id.signin).setVisibility(View.GONE);
            findViewById(R.id.register).setVisibility(View.GONE);
            findViewById(R.id.emailField).setVisibility(View.GONE);
            findViewById(R.id.passwordField).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.signin).setVisibility(View.VISIBLE);
            findViewById(R.id.register).setVisibility(View.VISIBLE);
            findViewById(R.id.emailField).setVisibility(View.GONE);
            findViewById(R.id.passwordField).setVisibility(View.GONE);
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }


    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.register) {
            createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if(i == R.id.signin) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
    }

}
