package com.example.thorac;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class HomeScreen extends AppCompatActivity {

    FirebaseAuth thoracAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        //FirebaseUser thoracUser = thoracAuth.getCurrentUser();

        //TextView showEmail = findViewById(R.id.showEmail);
        //showEmail.setText(thoracUser.getEmail());


    }
}
