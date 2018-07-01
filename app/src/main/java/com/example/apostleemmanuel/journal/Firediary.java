package com.example.apostleemmanuel.journal;

import android.app.Application;

import com.firebase.client.Firebase;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by nithun on 11/21/16.
 */

public class Firediary extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Firebase.setAndroidContext(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);



    }
}
