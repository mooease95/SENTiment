package com.example.sentiment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class ThreadMessages extends AppCompatActivity implements View.OnClickListener {

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

        //populateMessageList();

        populateRealtimeMessagesList();

    }

    private void populateRealtimeMessagesList() {
        db.collection("users").document(username)
                .collection("threads").document(threadContact)
                .collection("allMessages")
                .orderBy("serverTimestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            System.out.println(TAG + "Listen failed." + e);
                            return;
                        }

                        //remove all past messages
                        messagesListString.clear();

                        //List<String> realtimeMessageListString = new ArrayList<>();
                        int index = 0;

                        for (QueryDocumentSnapshot doc: queryDocumentSnapshots) {
                            if (doc.get("message") != null) {
                                //Here sort between username and threadContact
                                String message = doc.getString("message");
                                String sender = doc.getString("sender");
                                if (sender.equals(username)) {
                                    String messageToDisplay = "You: " + message;
                                    messagesListString.add(index, messageToDisplay);
                                    index++;
                                } else if (sender.equals(threadContact)){
                                    String messageToDisplay = threadContact + ": " + message;
                                    messagesListString.add(index, messageToDisplay);
                                    index++;
                                }
                                //messagesListString.add(doc.getString("message"));
                            }
                        }
                        createRecyclerView();
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
//        int indexOfLastMessage = messagesListString.size();
//        messagesListString.add(indexOfLastMessage, newMessage);

        System.out.println(TAG + "Size of list now is - " + messagesListString.size());
        for (int elements = 0; elements < messagesListString.size(); elements++) {
            System.out.println(TAG + "messageListString(" + elements + "): " + messagesListString.get(elements));
        }

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

                        //Create the userMessage with sender as current user and thread contact as recipient
                        //Because this user has clicked on Send
                        UserMessage userMessage = createUserMessage(username, threadContact, newMessage, timestamp);

//                        //Populate in Firestore for current user: username and threadContact only for directory
//                        populateAllMessages(senderReference, username, threadContact, userMessage);
//
//                        //Populate in Firestore for other interlocutor: username and threadContact only for directory
//                        populateAllMessages(recipientReference, threadContact, username, userMessage);

                        populateAllMessages(senderReference, username, threadContact, threadContact, username, newMessage, timestamp);
                        populateAllMessages(recipientReference, username, threadContact, username, threadContact, newMessage, timestamp);
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

    private UserMessage createUserMessage(String sender, String recipient, String newMessage, String timestamp) {
        UserMessage newUserMessage = new UserMessage();
        newUserMessage.setSender(sender);
        newUserMessage.setRecipient(recipient);
        newUserMessage.setMessage(newMessage);
        newUserMessage.setServerTimestamp(FieldValue.serverTimestamp());
        return newUserMessage;
    }

    private void populateAllMessages(DocumentReference documentReference, String sender, String recipient,
                                     String firestoreUser, String firestoreThread,
                                     String message, String timestamp) {

        UserMessage userMessage = new UserMessage();
        userMessage.setSender(sender);
        userMessage.setRecipient(recipient);
        userMessage.setMessage(message);
        userMessage.setTimestamp(timestamp);
        userMessage.setEmotion("");
        userMessage.setServerTimestamp(FieldValue.serverTimestamp());

        System.out.println(TAG + "Adding a new message sent - From " + firestoreUser + " To " + firestoreThread + ": '" + message + "'");

//        db.collection("users").document(firestoreUser)
//                .collection("threads").document(firestoreThread)
//                .collection("allMessages").document(timestamp)


//        db.collection("users").document(firestoreUser)
//                .collection("threads").document(firestoreThread)
//                .collection("allMessages").document(timestamp)
//                .set(newUserMessage)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {


        db.collection("users").document(firestoreUser)
                .collection("threads").document(firestoreThread)
                .collection("allMessages").document(timestamp)
                .set(userMessage)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println(TAG + "Added " + message + " to "
                                + db.collection("users").document(firestoreUser)
                                .collection("threads").document(firestoreThread)
                                .collection("allMessages").document(timestamp).getId());
                    }
                });

    }


}
