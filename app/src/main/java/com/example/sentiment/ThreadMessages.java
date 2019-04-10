package com.example.sentiment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private RequestQueue requestQueue;

    FirebaseFirestore db;

    List<UserMessage> messagesList;
    List<String> messagesListString;
    List<String> receivedMessagesList;

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
        System.out.println(TAG + "username - " + username);
        System.out.println(TAG + "threadContact - " + threadContact);

        currentUser = new User();
        currentUser.setUsername(username);

        messagesList = new ArrayList<>();
        messagesListString = new ArrayList<>();
        receivedMessagesList = new ArrayList<>();

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
                        receivedMessagesList.clear();

                        //List<String> realtimeMessageListString = new ArrayList<>();
                        int indexAll = 0;
                        int indexReceived = 0;

                        for (QueryDocumentSnapshot doc: queryDocumentSnapshots) {
                            if (doc.get("message") != null) {
                                //Here sort between username and threadContact
                                String message = doc.getString("message");
                                String sender = doc.getString("sender");
                                if (sender.equals(username)) {
                                    String messageToDisplay = "You: " + message;
                                    messagesListString.add(indexAll, messageToDisplay);
                                    indexAll++;
                                } else if (sender.equals(threadContact)){
                                    receivedMessagesList.add(indexReceived, message);
                                    String messageToDisplay = threadContact + ": " + message;
                                    messagesListString.add(indexAll, messageToDisplay);
                                    indexAll++;
                                    indexReceived++;
                                }
                                //messagesListString.add(doc.getString("message"));
                            }
                        }
                        //sentimentIndexCheck();
                        if (checkSize()) {
                            sentimentIndexCheck();
                        } else {
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

    private boolean checkSize() {
        boolean sizeIsValid = false;

        if (receivedMessagesList.size() != 0 && receivedMessagesList.size() % 5 == 0) { //for every 5 message received
            sizeIsValid = true;
            System.out.println(TAG + "receivedMessagesList size - " + receivedMessagesList.size()); //this increases for no apparent reason.
            System.out.println(TAG + "Size of receivedMessagesList is valid: " + receivedMessagesList.size());
        }
        return sizeIsValid;
    }

    private void sentimentIndexCheck() {
        String sentimentMessage = getSentimentMessage();
        doSentimentCheck(sentimentMessage);
    }

//    private void populateReceivedMessagesList() {
//        int index = 0;
//        for (int i = messagesListString.size(); i > 0; i--) {
//            while (index < 5) {
//                String sender =
//            }
//        }
//    }

    private String getSentimentMessage() {
        int size = receivedMessagesList.size();
        String sentimentMessage = "";
        for (int i = size - 5; i < size; i++) {
            System.out.println(TAG + "getSentimentMessage - value of i is - " + i);
            System.out.println(TAG + "getSentimentMessage - message to append - " + receivedMessagesList.get(i));
            sentimentMessage = sentimentMessage + receivedMessagesList.get(i); //change it to StringBuilder?
        }
        System.out.println(TAG + "sentimentMessage - " + sentimentMessage);
        return sentimentMessage;
    }

    private void doSentimentCheck(String sentimentMessage) {

        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();

        String url = "https://apiv2.indico.io/emotion";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        addToList(response);
                        createRecyclerView();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(TAG + error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("api_key", "d802adc8f6210a1af5e8aa5e790dfef8");
                params.put("data", sentimentMessage);

                return params;
            }

        };

        requestQueue.add(postRequest);
    }

    private void addToList(String response) {
        System.out.println(TAG + "Response is - " + response);
        int size = messagesListString.size();
        System.out.println(TAG + "messagesListString size - " + messagesListString.size());
        messagesListString.add(size, "SENTiment: " + response);
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