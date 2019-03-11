package com.example.thorac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ThreadLists extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ThreadLists";

    private int numberOfThreads;

    private String sender;

    FirebaseFirestore db;

    ListView listView;
    String threadContacts[];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_lists);

        findViewById(R.id.createNewThread).setOnClickListener(this);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            if (user.getDisplayName() != null) {
                sender = user.getDisplayName();
            }
        }

        db = FirebaseFirestore.getInstance();

        getNumberOfThreads();


        getListOfThreads();

    }

    public void getNumberOfThreads() {
        db.collection("users").document(sender)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String threadCount = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).get("NumberOfThreads")).toString();
                numberOfThreads = Integer.parseInt(threadCount);
                threadContacts = new String[numberOfThreads];
            }
        });
    }

    public void getListOfThreads() {
        db.collection("users").document(sender).collection("threads")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Timber.d("%s: Getting List of Threads.", TAG);
                            int index = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String contact = Objects.requireNonNull(document.getData().get("threadContact")).toString();
                                threadContacts[index] = contact;
                                index++;
                            }
                            viewListOfThreads();
                        }
                    }
                });
    }

    private void viewListOfThreads() {
        CustomListAdapter customListAdapter = new CustomListAdapter(this,
                threadContacts);
        listView = (ListView) findViewById(R.id.threadListview);
        listView.setAdapter(customListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ThreadLists.this, ThreadMessages.class);
                String threadContact = threadContacts[position];
                intent.putExtra("threadUser", threadContact);
                System.out.println("ThreadLists class got: " + threadContact);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.createNewThread) {
            startActivity(new Intent(ThreadLists.this, NewThread.class));
        }
    }

}
