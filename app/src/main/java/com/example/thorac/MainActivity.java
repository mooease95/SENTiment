package com.example.thorac;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;
import co.chatsdk.core.error.ChatSDKException;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configuration;
import co.chatsdk.core.session.InterfaceManager;
import co.chatsdk.firebase.FirebaseNetworkAdapter;
import co.chatsdk.firebase.file_storage.FirebaseFileStorageModule;
import co.chatsdk.ui.manager.BaseInterfaceAdapter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context context = getApplicationContext();

        Configuration.Builder builder = new Configuration.Builder(context);

        builder.firebaseRootPath("test");

        try {
            ChatSDK.initialize(builder.build(), new BaseInterfaceAdapter(context),
                    new FirebaseNetworkAdapter());
        } catch(ChatSDKException e) {
            System.err.println("I don't know what I'm doing.");
        }

        FirebaseFileStorageModule.activate();
        //FirebasePushModule.activate();

        InterfaceManager.shared().a.startLoginActivity(context, true);

    }

//    public void goToRegistration(View view) {
//        //Intent registerIntent = new Intent(this, Register.class);
//        //startActivity(registerIntent);
//
//        Context context = getApplicationContext();
//
//        Configuration.Builder builder = new Configuration.Builder(context);
//
//        builder.firebaseRootPath("test");
//
//        try {
//            ChatSDK.initialize(builder.build(), new BaseInterfaceAdapter(context),
//                    new FirebaseNetworkAdapter());
//        } catch(ChatSDKException e) {
//            System.err.println("I don't know what I'm doing.");
//        }
//
//        FirebaseFileStorageModule.activate();
//        //FirebasePushModule.activate();
//
//        InterfaceManager.shared().a.startLoginActivity(context, true);
//
//    }


}
