package com.example.thorac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class HomeScreen extends AppCompatActivity implements View.OnClickListener {

    private static final int SIGN_IN_REQUEST_CODE = 0;

    private static final String TAG = "HomeScreen";

    private String sender;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            if (user.getDisplayName() != null) {
                sender = user.getDisplayName();
            }
        }


        db = FirebaseFirestore.getInstance();

        Log.d(TAG, "Reached onCreate");

        //populateListOfThreads();

    }

    private void populateListOfThreads() {
        /*
        All threads have threadContact as a field now
        TODO: Find in what format the .get() method below returns ALL THE DOCUMENTS
        TODO: Convert whatever it returns to an array
        TODO: Use ArrayAdapter to show list of threads in HomeScreen
        TODO: Show list of threads in HomeScreen.
         */
        String[] listOfThreads;


        db.collection("users").document(sender)
                .collection("threads")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                Timber.d("%s: Fetching all Threads.", TAG);
                                Map<String, Object> threadMap = documentSnapshot.getData();
                                //listOfThreads[0] = threadMap.get("threadContact").toString();

                            }
                        }

                    }
                });

    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.createNewThread) {
            Log.d(TAG, "createNewThread clicked");
            Timber.d("%s: createNewThread clicked.", TAG);
            startActivity(new Intent(HomeScreen.this, NewThread.class));
        }
    }
}
