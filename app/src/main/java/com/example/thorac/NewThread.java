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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NewThread extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "NewThread";

    private EditText mRecipient;
    private EditText mMessage;

    private String username; //Remove since now we have username as a field?

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_thread);

        username = getIntent().getStringExtra("systemUser");
        System.out.println(TAG + ": Inherited currentUser - " + username);

        mRecipient = findViewById(R.id.newThreadRecipient);
        mMessage = findViewById(R.id.newThreadMessage);

        findViewById(R.id.newThreadSendMessage).setOnClickListener(this);

        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.newThreadSendMessage) {
            validateAndSend(mRecipient.getText().toString(), mMessage.getText().toString());
        }
    }

    //Check if recipient already exists as a thread
    //If they do,
    // populate messagesSent and populate messagesReceived
    //If they don't,
    // Create thread for username and recipient
    // Increment numberOfThreads for username and recipient
    // populate messagesSent and populate messagesReceived
    public void validateAndSend(String recipient, String message) {
        DocumentReference senderReference = db.collection("users").document(username);
        DocumentReference recipientReference = db.collection("users").document(recipient);

        System.out.println(TAG + ": senderReference - " + senderReference.getId());
        System.out.println(TAG + ": recipientReference - " + recipientReference.getId());

        recipientReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot userSnapshot = task.getResult();
                if (userSnapshot.exists()) { //Checking that this is a valid user
                    System.out.println(TAG + ": username is valid.");
                    Timber.d("%s: Username is valid.", TAG);
                    String timestamp = getTimestamp();
                    checkForThread(senderReference, recipient);
                    //populateMessagesSent(recipientReference, recipient, message, timestamp);
                    //populateMessagesReceived(recipientReference, recipient, message, timestamp);
                    updateThreadCount(username);
                    updateThreadCount(recipient);
                    populateAllMessages(recipientReference, username, recipient, message, timestamp);
                    populateAllMessages(senderReference, recipient, username, message, timestamp);
                } else {
                    Timber.d("%s: Username not found.", TAG);
                    showErrorMessage();
                }
            }
        });
    }

    public String getTimestamp() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        String timestamp = simpleDateFormat.format(new Date());
        return timestamp;
    }

    public void checkForThread(DocumentReference senderReference, String recipient) {
        senderReference.collection("threads").document(recipient).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot threadSnapshot = task.getResult();
                if (!threadSnapshot.exists()) { //No current thread between the two users
                    createThread(username, recipient);
                    createThread(recipient, username);
                }
            }
        });
    }

    public void createThread(String user, String threadContact) {
        Map<String, Object> threadMap = new HashMap<>();

        threadMap.put("threadContact", threadContact);

        db.collection("users").document(user)
                .collection("threads").document(threadContact)
                .set(threadMap)
                .addOnSuccessListener(aVoid -> {
                    Timber.d("%s: Created thread: %s", TAG, threadContact);
                    //updateThreadCount(user);
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Timber.d("%s: Failed to create threadContact %s for username %s", TAG, threadContact, username);
                    }
                });

    }

    public void updateThreadCount(String user) {
        DocumentReference documentReference = db.collection("users").document(user);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Timber.d("%s: Document exists.", TAG);
                        Map<String, Object> userMap = new HashMap<>();
                        userMap = document.getData();
                        String numberOfThreads = Objects.requireNonNull(Objects.requireNonNull(userMap).get("numberOfThreads")).toString();
                        int threadCount = Integer.parseInt(numberOfThreads);
                        String updatedNumberOfThreads = Integer.toString(threadCount+1);
                        userMap.put("numberOfThreads", updatedNumberOfThreads);
                        db.collection("users").document(user)
                                .update(userMap);
                        Timber.d("%s: NumberOfThreads updated successfully from %s to %s.", TAG,
                                numberOfThreads, updatedNumberOfThreads);
                    } else {
                        Timber.d("%s: Document does not exist.", TAG);
                    }
                }
            }
        });
    }

    public void populateAllMessages(DocumentReference documentReference, String sender, String recipient, String message, String timestamp) {

        UserMessage userMessage = new UserMessage();
        userMessage.setSender(sender);
        userMessage.setRecipient(recipient);
        userMessage.setMessage(message);
        userMessage.setTimestamp(timestamp);
        userMessage.setEmotion(getEmotion(message));


//        Map<String, Object> messageMap = new HashMap<>();
//
//        messageMap.put("username", username);
//        messageMap.put("recipient", recipient);
//        messageMap.put("message", message);
//        messageMap.put("timestamp", timestamp);
//        messageMap.put("emotion", "");

        db.collection("users").document(recipient)
                .collection("threads").document(sender)
                .collection("allMessages").document(timestamp)
                .set(userMessage)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Timber.d("%s: Message populated in allMessages for %s.", TAG, recipient);
                        startNextActivity(recipient);
                        //startActivity(new Intent(NewThread.this, ThreadMessages.class)); //should send threadContact
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Timber.d("%s: Failed to populate allMessages for %s.", TAG, recipient);
                    }
                });
    }

    private void startNextActivity(String recipient) {
        Intent intent = new Intent(NewThread.this, ThreadMessages.class);
        intent.putExtra("systemUser", username);
        intent.putExtra("threadContact", recipient);

        //System.out.println("EmailAuth numberOfThreads: " + currentUser.getNumberOfThreads());

        startActivity(intent);
    }

    private String getEmotion(String message) {
        return "";
    }

    public void showErrorMessage() {

    }





    public void populateMessagesSent(DocumentReference documentReference, String recipient, String message, String timestamp) {
        Map<String, Object> messageMap = new HashMap<>();

        messageMap.put("username", username);
        messageMap.put("recipient", recipient);
        messageMap.put("message", message);
        messageMap.put("timestamp", timestamp);

        db.collection("users").document(username)
                .collection("threads").document(recipient)
                .collection("messagesSent").document(timestamp)
                .set(messageMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Timber.d("%s: Message populated in messageSent for %s.", TAG, username);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Timber.d("%s: Failed to populate messagesSent for %s.", TAG, username);
                    }
                });
    }

    public void populateMessagesReceived(DocumentReference documentReference, String recipient, String message, String timestamp) {
        Map<String, Object> messageMap = new HashMap<>();

        messageMap.put("username", username);
        messageMap.put("recipient", recipient);
        messageMap.put("message", message);
        messageMap.put("timestamp", timestamp);
        messageMap.put("emotion", "");

        db.collection("users").document(recipient)
                .collection("threads").document(username)
                .collection("messagesReceived").document(timestamp)
                .set(messageMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Timber.d("%s: Message populated in messageReceived for %s.", TAG, recipient);
                        startActivity(new Intent(NewThread.this, ThreadMessages.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Timber.d("%s: Failed to populate messagesReceived for %s.", TAG, recipient);
                    }
                });

    }


}
