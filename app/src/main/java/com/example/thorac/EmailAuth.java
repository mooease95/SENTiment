package com.example.thorac;

import androidx.appcompat.app.AppCompatActivity;
import co.chatsdk.ui.main.BaseActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class EmailAuth extends MainActivity implements View.OnClickListener {

    private static final String TAG = "EmailAuth";

    private TextView mstatusTextView;
    private TextView mDetailTextField;
    private EditText mEmailField;
    private EditText mPasswordField;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_auth);



    }

    @Override
    public void onClick(View view) {

    }

}
