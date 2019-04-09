package com.example.thorac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EmailAuth extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "EmailAuth";

    private EditText mUsernameField;
    private FirebaseFirestore db;

    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_auth);

        mUsernameField = findViewById(R.id.usernameField);

        findViewById(R.id.signin).setOnClickListener(this);
        findViewById(R.id.register).setOnClickListener(this);

        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.register) {
            System.out.println("Just clicked register.");
            createAccount(mUsernameField.getText().toString());
        } else if(i == R.id.signin) {
            System.out.println("Just clicked signIn.");
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
    }

    private void obtainNumberOfThreads(String username) {
        System.out.println(TAG + ": at obtainNumberOfThreads for " + username);
        DocumentReference documentReference = db.collection("users").document(username);
        System.out.println(TAG + ": documentReference is " + documentReference.getId());

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                System.out.println(TAG + ": getting document snapshot");
                if (documentSnapshot.exists()) {
                    int numberOfThreads = Integer.parseInt(documentSnapshot.get("numberOfThreads").toString());
                    System.out.println(TAG + ": numberOfThreads is " + numberOfThreads);
                    currentUser.setNumberOfThreads(numberOfThreads);
                    startNextActivity();
                }
            }
        });
        System.out.println(TAG + ": Finishing obtainNumberOfThreads.");

    }


    private void createAccount(String username) {
        Timber.d("%s: createAccount: %s", TAG, username);

        instantiateUser(username, 0);

        addToCloud(username);

        startNextActivity();

    }

    private void addToCloud(String username) {

        System.out.println("Adding to Cloud: " + username);

        db.collection("users").document(username).set(currentUser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Timber.d("%s: Document added successfully.", TAG);
                        System.out.println("Document added successfully for: " + username);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Timber.d("%s: Error writing document.", TAG);
                        System.out.println("Error writing document for: " + username);
                    }
                });
        System.out.println("Finished adding to Cloud: " + username);
    }

    private void signIn(String username) {
        Log.d(TAG, "signIn");
        Timber.d("%s: signIn: %s", TAG, username);
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

        //System.out.println("EmailAuth numberOfThreads: " + currentUser.getNumberOfThreads());

        startActivity(intent);
    }
}
