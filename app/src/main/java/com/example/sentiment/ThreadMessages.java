package com.example.sentiment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
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
import java.util.Collections;
import java.util.Comparator;
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
    private int numberOfThreads;

    int reachedIndex = 0;

    private RequestQueue requestQueue;

    FirebaseFirestore db;

    List<UserMessage> messagesList;
    List<String> messagesListString;
    List<String> receivedMessagesList;

    Boolean firstListener = true;

    private final String NOTIFICATION_CHANNEL_ID = "sentiment0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_messages);

        System.out.println(TAG + "Started");

        mNewMessage = findViewById(R.id.threadMessages_messageField);
        findViewById(R.id.threadMessages_sendButton).setOnClickListener(this);

        username = getIntent().getStringExtra("systemUser");
        threadContact = getIntent().getStringExtra("threadContact");
        numberOfThreads = getIntent().getIntExtra("numberOfThreads", 0);

        System.out.println(TAG + "Inherited from previous activity - ");
        System.out.println(TAG + "username - " + username);
        System.out.println(TAG + "threadContact - " + threadContact);

        currentUser = new User();
        currentUser.setUsername(username);

        messagesList = new ArrayList<>();
        messagesListString = new ArrayList<>();
        receivedMessagesList = new ArrayList<>();

        db = FirebaseFirestore.getInstance();

        populateMessageList();

        //populateRealtimeMessagesList();

        //listenForRealtime();

    }

    private void populateMessageList() {
        db.collection("users").document(username)
                .collection("threads").document(threadContact)
                .collection("allMessages")
                .orderBy("serverTimestamp", Query.Direction.ASCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    int indexAll = 0;
                    int indexReceived = 0;

                    for (QueryDocumentSnapshot doc: task.getResult()) {
                        if (doc.get("message") != null) {
                            String message = doc.getString("message");
                            String sender = doc.getString("sender");
                            if (sender.equals(username)) {
                                String messageToDisplay = "You: " + message;
                                messagesListString.add(indexAll, messageToDisplay);
                                indexAll++;
                            } else if (sender.equals(threadContact)) {
                                receivedMessagesList.add(indexReceived, message);
                                String messageToDisplay = threadContact + ": " + message;
                                messagesListString.add(indexAll, messageToDisplay);
                                indexAll++;
                                indexReceived++;
                            }
                        }
                    }
                    createRecyclerView();
                    recyclerView.smoothScrollToPosition(mAdapter.getItemCount());
                    listenForRealtime();
                }
            }
        });
    }

    private void listenForRealtime() {
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
                        int indexAll = 0;
                        int indexReceived = 0;

                        if (queryDocumentSnapshots != null) {
                            QueryDocumentSnapshot document = queryDocumentSnapshots.getDocumentChanges().get(indexAll).getDocument();

                            if (!document.getMetadata().hasPendingWrites()) {
                                String message = document.getString("message");
                                String sender = document.getString("sender");
                                if (sender.equals(username)) {
                                    String messageToDisplay = "You: " + message;
                                    if (firstListener == false) {
                                        messagesListString.add(messageToDisplay);
                                        indexAll++;
                                    } else {
                                        firstListener = false;
                                    }
                                    //indexAll++;
                                } else if (sender.equals(threadContact)) {
                                    receivedMessagesList.add(indexReceived, message);
                                    String messageToDisplay = threadContact + ": " + message;
                                    if (firstListener == false) {
                                        messagesListString.add(messageToDisplay);
                                        indexAll++;
                                        indexReceived++; //Should they be here or outside the if-else statement?
                                    } else {
                                        firstListener = false;
                                    }
                                    sendNotification(message, username);
//                                    indexAll++;
//                                    indexReceived++;
                                }
                            }
                        }
                        if (checkSize()) {
                            sentimentIndexCheck();
                        } else {
                            mAdapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(mAdapter.getItemCount());
                        }
                    }
                });
    }

    private void sendNotification(String message, String nameOfUser) {
        PendingIntent pendingResultIntent = getResultIntent(nameOfUser);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.sentimentlogoweb)
                .setContentTitle(threadContact)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        createNotificationChannel();

        builder.setContentIntent(pendingResultIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, builder.build());
    }

    private PendingIntent getResultIntent(String nameOfUser) {
        Intent resultIntent = new Intent(this, ThreadMessages.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);

        Intent threadListsIntent = stackBuilder.editIntentAt(1);
        threadListsIntent.putExtra("systemUser", nameOfUser);
        threadListsIntent.putExtra("numberOfThreads", numberOfThreads);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        stackBuilder.editIntentAt(1);
        return resultPendingIntent;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
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

    private String getSentimentMessage() {
        int size = receivedMessagesList.size();
        String sentimentMessage = "";
        for (int i = 4; i > 0; i--) { //changing this because received message is now getting added to the beginning
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
                        mAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(mAdapter.getItemCount());
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
        Map.Entry<String, Double> maxEntry = parseResponse(response);

        String affectiveStateToDisplay = getDisplayState(maxEntry);

        System.out.println(TAG + affectiveStateToDisplay);
        int size = messagesListString.size();
        System.out.println(TAG + "messagesListString size - " + messagesListString.size());
        messagesListString.add(size, affectiveStateToDisplay);
    }

    /*
    response format:
    {"results": {"anger": <value>, "fear": <value>, "joy": <value>, "sadness": <value>, "surprise": <value>}}
     */
    private Map.Entry<String, Double> parseResponse(String response) {
        String delimiter = "[{]+"; //first parse by [{] to divide into two
        String[] parse1 = response.split(delimiter); //there should be 3
        String outputValues = parse1[2]; //0: empty, 1: results, 2: the outputs

        delimiter = "[}]+"; //now get rid of the last two parentheses
        String[] parse2 = outputValues.split(delimiter);
        outputValues = parse2[0];

        delimiter = "[,]+"; //now get each state and value
        String[] parse3 = outputValues.split(delimiter);

        /*
        0: "anger":_<value>
        1: _"fear":_<value>
        2: _"joy":_<value>
        3: _"sadness":_<value>
        4: _"surprise":_<value>
         */

        String[] affectiveStateNames = new String[parse3.length];
        Double[] affectiveStateValues = new Double[parse3.length];

        delimiter = "[:]"; //separate the values out by colon(:)
        for (int i = 0; i < parse3.length; i++) {
            outputValues = parse3[i];
            String[] parse4 = outputValues.split(delimiter); //should have 2,
            delimiter = "[ ]"; //now get rid of the spaces
            String[] parse5 = parse4[0].split(delimiter);
            String[] parse6 = parse4[1].split(delimiter);
            affectiveStateNames[i] = parse5[parse5.length-1]; //anger doesn't have any, others will
            affectiveStateValues[i] = Double.parseDouble(parse6[1]); //first one will be empty
            delimiter = "[:]"; //set delimiter back for next iteration
        }

        Map<String, Double> affectiveStatesMap = new HashMap<>();

        delimiter = "[\"]"; //Get rid of the first quotation mark in the affective state name
        for (int i = 0; i < affectiveStateNames.length; i++) {
            outputValues = affectiveStateNames[i];
            String parse5[] = outputValues.split(delimiter); //0: empty, 1: anger
            String key = parse5[1];
            Double value = affectiveStateValues[i];
            affectiveStatesMap.put(key, value);
        }

        Map.Entry<String, Double> maxEntry = Collections.max(affectiveStatesMap.entrySet(), new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        return maxEntry;
    }

    private String getDisplayState(Map.Entry<String, Double> maxEntry) {
        String intensity = "";
        String message = "";
        String affectiveState = maxEntry.getKey();
        Double affectiveStateValue = maxEntry.getValue();

        if (affectiveStateValue < 0.5) {
            intensity = " feeling slightly";
        }

        switch (affectiveState) {
            case "anger":
                message = threadContact + " is" + intensity + " angry";
                break;
            case "fear":
                message = threadContact + " is" + intensity + " fearful";
                break;
            case "joy":
                message = threadContact + " is" + intensity + " joyful";
                break;
            case "sadness":
                message = threadContact + " is" + intensity + " sad";
                break;
            case "surprise":
                message = threadContact + " is" + intensity + " surprised";
                break;
            default:
                break;
        }

        return message;

    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.threadMessages_sendButton) {
            if (mNewMessage.getText().toString() != null) {
                validateAndSend(mNewMessage.getText().toString());
                mNewMessage.getText().clear();
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
                        System.out.println(TAG + "Adding document " + recipientReference.getId());
                        String timestamp = getTimestamp();

                        //Create the userMessage with sender as current user and thread contact as recipient
                        //Because this user has clicked on Send
                        //UserMessage userMessage = createUserMessage(username, threadContact, newMessage, timestamp);

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