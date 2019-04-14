package com.example.sentiment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class InstantFeedback extends AppCompatActivity implements View.OnClickListener {

    private String username;
    private int numberOfThreads;

    private EditText feedbackAns1;
    private EditText feedbackAns2;
    private EditText feedbackAns3;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instant_feedback);

        feedbackAns1 = findViewById(R.id.feedbackA1);
        feedbackAns2 = findViewById(R.id.feedbackA2);
        feedbackAns3 = findViewById(R.id.feedbackA3);

        findViewById(R.id.submit_feedback).setOnClickListener(this);

        username = getIntent().getStringExtra("systemUser");
        numberOfThreads = getIntent().getIntExtra("numberOfThreads", 0);

        db = FirebaseFirestore.getInstance();

    }

    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.submit_feedback) {
            submitFeedback(feedbackAns1.getText().toString(), feedbackAns2.getText().toString(),
                    feedbackAns3.getText().toString());
        }
    }

    private void submitFeedback(String ans1, String ans2, String ans3) {
        Map<String, Object> feedbackMap = new HashMap<>();
        feedbackMap.put("ans1", ans1);
        feedbackMap.put("ans2", ans2);
        feedbackMap.put("ans3", ans3);


        db.collection("users").document(username)
                .collection("Feedback").document()
                .set(feedbackMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        startNextActivity();
                    }
                });
    }

    private void startNextActivity() {
        Intent intent = new Intent(InstantFeedback.this, ThreadLists.class);

        intent.putExtra("systemUser", username);
        intent.putExtra("numberOfThreads", numberOfThreads);

        startActivity(intent);
    }
}
