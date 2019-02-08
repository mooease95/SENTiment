package com.example.thorac;

import androidx.appcompat.app.AppCompatActivity;
import co.chatsdk.core.error.ChatSDKException;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configuration;
import co.chatsdk.core.session.InterfaceManager;
import co.chatsdk.firebase.FirebaseNetworkAdapter;
import co.chatsdk.firebase.file_storage.FirebaseFileStorageModule;
import co.chatsdk.ui.manager.BaseInterfaceAdapter;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class HomeScreen extends AppCompatActivity {

    FirebaseAuth thoracAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

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



        //FirebaseUser thoracUser = thoracAuth.getCurrentUser();

        //TextView showEmail = findViewById(R.id.showEmail);
        //showEmail.setText(thoracUser.getEmail());


    }
}
