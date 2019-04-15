package com.example.sentiment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EmailAuth extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "EmailAuth";

    private EditText mUsernameField;
    private FirebaseFirestore db;

    private User currentUser;

    private final String NOTIFICATION_CHANNEL_ID = "sentiment0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_auth);

        mUsernameField = findViewById(R.id.usernameField);

        mUsernameField.setHint("Username");

        findViewById(R.id.signin).setOnClickListener(this);
        findViewById(R.id.register).setOnClickListener(this);

        db = FirebaseFirestore.getInstance();

        createNotificationChannel();
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

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.register) {
            createAccount(mUsernameField.getText().toString());
        } else if(i == R.id.signin) {
            signIn(mUsernameField.getText().toString());
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        String username = mUsernameField.getText().toString();
        if (TextUtils.isEmpty(username)) {
            mUsernameField.setError("Required.");
            valid = false;
        } else {
            mUsernameField.setError(null);
        }

        return valid;
    }

    private void instantiateUser(String username, int numberOfThreads) {
        currentUser = new User();
        currentUser.setUsername(username);
        if (numberOfThreads < 0) { //-1 sent from sign in, 0 sent from create account
            obtainNumberOfThreads(username);
        } else {
            currentUser.setNumberOfThreads(numberOfThreads); //should be 0 here all the time
        }
        currentUser.setRepresentationPreference("Text");
    }

    private void obtainNumberOfThreads(String username) {
        DocumentReference documentReference = db.collection("users").document(username);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    int numberOfThreads = Integer.parseInt(documentSnapshot.get("numberOfThreads").toString());
                    currentUser.setNumberOfThreads(numberOfThreads);
                    startNextActivity();
                }
            }
        });
    }


    private void createAccount(String username) {
        instantiateUser(username, 0);

        addToCloud(username);

        startNextActivity();

    }

    private void addToCloud(String username) {
        db.collection("users").document(username).set(currentUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    private void signIn(String username) {
        if(!validateForm()) {
            return;
        }

        //If user does not exist, display a different page

        instantiateUser(username, -1);

        //Fetch the number of threads ///This now happens when instantiating
        //obtainNumberOfThreads(username);

        //startNextActivity();
    }

    private void startNextActivity() {
        Intent intent = new Intent(EmailAuth.this, ThreadLists.class);
        intent.putExtra("systemUser", currentUser.getUsername());
        intent.putExtra("numberOfThreads", currentUser.getNumberOfThreads());

        startActivity(intent);
    }
}
