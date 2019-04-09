package com.example.thorac;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Objects;

public class ThreadLists extends AppCompatActivity implements ThreadListsAdapter.ItemClickListener {

    private static final String TAG = "ThreadLists";

    ThreadListsAdapter threadListsAdapter;

    private RecyclerView recyclerView;
    private ThreadListsAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private User currentUser;
    private String username;
    private int numberOfThreads;

    private String sender;

    FirebaseFirestore db;

    ListView listView;
    String[] threadContacts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_lists);

        System.out.println("Started ThreadLists.");

        username = getIntent().getStringExtra("systemUser");
        numberOfThreads = getIntent().getIntExtra("numberOfThreads", 0);

        System.out.println("Inherited from previous activity: " + username + ", " + numberOfThreads);

        currentUser = new User();
        currentUser.setUsername(username);
        currentUser.setNumberOfThreads(numberOfThreads);

        System.out.println(TAG + ": Inherited currentUser - " + currentUser.getUsername());

        threadContacts = new String[numberOfThreads];

        db = FirebaseFirestore.getInstance();

        if (numberOfThreads > 0) {
            getListOfThreads();
        } else {
            createRecyclerView();
        }

    }

    public void getListOfThreads() {
        db.collection("users").document(username).collection("threads")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Timber.d("%s: Getting List of Threads.", TAG);
                            int index = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String contact = document.getData().get("threadContact").toString();
                                threadContacts[index] = contact;
                                System.out.println("ThreadLists fetched: " + threadContacts[index]);
                                System.out.println("Should have got: " + contact);
                                index++;
                            }
                            createRecyclerView();
                        }
                    }
                });
    }

    private void createRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_threadLists);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new ThreadListsAdapter(threadContacts);
        mAdapter.setClickListener(this);

        recyclerView.setAdapter(mAdapter);

        //findViewById(R.id.createNewThread).setOnClickListener(this);
    }

    @Override
    public void onItemClick(View view, int position) {
        String threadName = mAdapter.getItem(position);

        Intent intent = new Intent(ThreadLists.this, ThreadMessages.class);
        intent.putExtra("systemUser", username);
        intent.putExtra("threadContact", threadName);

        startActivity(intent);

    }

    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.createNewThread) {
            startNextActivity();
        }
    }

    private void startNextActivity() {
        Intent intent = new Intent(ThreadLists.this, NewThread.class);
        intent.putExtra("systemUser", username);

        startActivity(intent);
    }

}
