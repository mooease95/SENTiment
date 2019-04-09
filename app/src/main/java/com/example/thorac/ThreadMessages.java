package com.example.thorac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class ThreadMessages extends AppCompatActivity implements View.OnClickListener {

    /*
    TODO: Read on how to lay out the messages
     */

    private static final String TAG = "ThreadMessages: ";


    private RecyclerView recyclerView;
    private ThreadMessagesAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private EditText mNewMessage;

    private User currentUser;
    private String username;

    private String threadContact;

    int reachedIndex = 0;



    FirebaseFirestore db;

    List<UserMessage> messagesList;
    List<String> messagesListString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_messages);

        System.out.println(TAG + "Started");

        mNewMessage = findViewById(R.id.threadMessages_messageField);
        findViewById(R.id.threadMessages_sendButton).setOnClickListener(this);

        username = getIntent().getStringExtra("systemUser");
        threadContact = getIntent().getStringExtra("threadContact");

        System.out.println(TAG + "Inherited from previous activity - ");
        System.out.println(TAG + "usermame - " + username);
        System.out.println(TAG + "threadContact - " + threadContact);

        currentUser = new User();
        currentUser.setUsername(username);

        messagesList = new ArrayList<>();
        messagesListString = new ArrayList<>();

        db = FirebaseFirestore.getInstance();

        populateMessageList();

        //populateRealtimeMessagesList();

    }

    private void populateRealtimeMessagesList() {
        db.collection("users")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            System.out.println(TAG + "Listen failed." + e);
                            return;
                        }


                    }
                });
    }


    public void populateMessageList() {
        db.collection("users").document(username)
                .collection("threads").document(threadContact)
                .collection("allMessages")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Timber.d("%s: Getting all messages for user %s with %s.", TAG, username, threadContact);
                            int index = 0;
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                String message = documentSnapshot.getData().get("message").toString();
                                messagesListString.add(index, message);
                                System.out.println(TAG + "Fetched message - " + message);
                                System.out.println(TAG + "Added message - " + messagesListString.get(index));

                                //we do not want these yet
//                                UserMessage userMessage = createUserMessage(documentSnapshot);
//                                messagesList.set(index, userMessage);
                                index++;
                            }
                            createRecyclerView();
                        }
                    }
                });
    }

    private void createRecyclerView() {
        System.out.println(TAG + "Started createRecyclerView, times here: " + reachedIndex);
        reachedIndex++;

        recyclerView = (RecyclerView) findViewById(R.id.threadMessages_recyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if (messagesListString.isEmpty()) {
            System.out.println(TAG + "Size of list is: " + messagesListString.size());
        }

        System.out.println(TAG + "first element in list - " + messagesListString.get(0));

        mAdapter = new ThreadMessagesAdapter(messagesListString);
        //mAdapter.setClickListener(this); //this doesn't work because we haven't added this method; shouldn't need it

        recyclerView.setAdapter(mAdapter);
    }

    public UserMessage createUserMessage(DocumentSnapshot documentSnapshot) {
        UserMessage userMessage = new UserMessage();
        String tempSender = documentSnapshot.getData().get("sender").toString();
        String tempRecipient = documentSnapshot.getData().get("recipient").toString();
        String message = documentSnapshot.getData().get("message").toString();
        String timestamp = documentSnapshot.getData().get("timestamp").toString();
        String emotion = documentSnapshot.getData().get("emotion").toString();
        userMessage.setSender(tempSender);
        userMessage.setRecipient(tempRecipient);
        userMessage.setMessage(message);
        userMessage.setTimestamp(timestamp);
        userMessage.setEmotion(emotion);

        return userMessage;
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.threadMessages_sendButton) {
            if (mNewMessage.getText().toString() != null) {
                validateAndSend(mNewMessage.getText().toString());
            }
        }
    }

    private void validateAndSend(String newMessage) {
        //first display it to screen? -> Remove after setting up realtime listeners
        int indexOfLastMessage = messagesListString.size();
        messagesListString.add(indexOfLastMessage, newMessage);

        DocumentReference senderReference = db.collection("users").document(username);
        DocumentReference recipientReference = db.collection("users").document(threadContact);

        System.out.println(TAG + "senderReference - " + senderReference.getId());
        System.out.println(TAG + "recipientReference - " + recipientReference.getId());

        senderReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        System.out.println(TAG + "Adding document" + recipientReference.getId());
                        String timestamp = getTimestamp();
                        populateAllMessages(senderReference, username, threadContact, newMessage, timestamp);
                        populateAllMessages(recipientReference, threadContact, username, newMessage, timestamp);
                    }
                }
            }
        });
    }

    private String getTimestamp() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        String timestamp = simpleDateFormat.format(new Date());
        return timestamp;
    }

    private void populateAllMessages(DocumentReference docRef, String sender, String recipient,
                                     String newMessage, String timestamp) {

        UserMessage newUserMessage = new UserMessage();
        newUserMessage.setSender(sender);
        newUserMessage.setRecipient(recipient);
        newUserMessage.setMessage(newMessage);
        newUserMessage.setTimestamp(timestamp);

        System.out.println(TAG + "Adding a new message sent - From " + sender + " To " + recipient + ": '" + newMessage + "'");

        DocumentReference reference = docRef.collection("threads").document(recipient);

//        db.collection("users").document(sender)
//                .collection("threads").document(recipient)
//                .collection("allMessages").document(timestamp)

            reference.collection("allMessages").document(timestamp)
                    .set(newUserMessage)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println(TAG + "Added " + newMessage + " to "
                                + reference.collection("allMessages").document(timestamp).getId());
                    }
                });

    }


}
