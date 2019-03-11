package com.example.thorac;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ThreadMessages extends AppCompatActivity {

    ListView messagesSentListview;
    ListView messagesReceivedListview;

    private static final String TAG = "ThreadMessages";

    private int numberOfThreads;

    private String sender;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_messages);

        TextView threadName = findViewById(R.id.threadContactName);
        String threadContact = getIntent().getStringExtra("threadUser");
        threadName.setText(threadContact);
        System.out.println("ThreadMessages class got: " + threadContact);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            if (user.getDisplayName() != null) {
                sender = user.getDisplayName();
            }
        }

        db = FirebaseFirestore.getInstance();
    }
}
