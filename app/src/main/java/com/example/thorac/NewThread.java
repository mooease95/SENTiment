package com.example.thorac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NewThread extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "NewThread";

    private EditText mRecipient;
    private EditText mMessage;

    private String sender;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_thread);

        mRecipient = findViewById(R.id.newThreadRecipient);
        mMessage = findViewById(R.id.newThreadMessage);

        findViewById(R.id.newThreadSendMessage).setOnClickListener(this);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            if (user.getDisplayName() != null) {
                sender = user.getDisplayName();
            }
        }

        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.newThreadSendMessage) {
            validateAndSend(mRecipient.getText().toString(), mMessage.getText().toString());
        }
    }

    public void validateAndSend(String recipient, String message) {
        DocumentReference documentReference = db.collection("users").document(recipient);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        Timber.d("%s: Username is valid.", TAG);
                        String timestamp = getTimestamp();
                        createThread(recipient);
                        populateMessagesSent(documentReference, recipient, message, timestamp);
                        populateMessagesReceived(documentReference, recipient, message, timestamp);
                    } else {
                        Timber.d("%s: Username not found.", TAG);
                        showErrorMessage();
                    }
                }
            }
        });
    }

    public String getTimestamp() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        String timestamp = simpleDateFormat.format(new Date());
        return timestamp;
    }

    public void createThread(String threadContact) {
        Map<String, Object> threadMap = new HashMap<>();

        threadMap.put("threadContact", threadContact);

        db.collection("users").document(sender)
                .collection("threads").document(threadContact)
                .set(threadMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Timber.d("%s: Created thread: %s", TAG, threadContact);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Timber.d("%s: Failed to create threadContact %s for sender %s", TAG, threadContact, sender);
                    }
                });

    }

    public void populateMessagesSent(DocumentReference documentReference, String recipient, String message, String timestamp) {
        Map<String, Object> messageMap = new HashMap<>();

        messageMap.put("message", message);
        messageMap.put("timestamp", timestamp);

        db.collection("users").document(sender)
                .collection("threads").document(recipient)
                .collection("messagesSent").document(timestamp)
                .set(messageMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Timber.d("%s: Message populated in messageSent for %s.", TAG, sender);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Timber.d("%s: Failed to populate messagesSent for %s.", TAG, sender);
                    }
                });
    }

    public void populateMessagesReceived(DocumentReference documentReference, String recipient, String message, String timestamp) {
        Map<String, Object> messageMap = new HashMap<>();

        //messageMap.put("sender", sender);
        messageMap.put("message", message);
        messageMap.put("timestamp", timestamp);
        messageMap.put("emotion", "");

        db.collection("users").document(recipient)
                .collection("threads").document(sender)
                .collection("messagesReceived").document(timestamp)
                .set(messageMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Timber.d("%s: Message populated in messageReceived for %s.", TAG, recipient);
                        startActivity(new Intent(NewThread.this, ViewThread.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Timber.d("%s: Failed to populate messagesReceived for %s.", TAG, recipient);
                    }
                });

    }

    public void showErrorMessage() {

    }




}
