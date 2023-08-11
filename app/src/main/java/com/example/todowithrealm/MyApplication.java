package com.example.todowithrealm;


import android.app.Application;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);

        // Set up a default configuration for the Realm database
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("myrealm.realm")
                .schemaVersion(1) // Increment this when you make changes to your data models
                .allowWritesOnUiThread(true) // Enable write transactions on the UI thread
                .build();

        Realm.setDefaultConfiguration(config);
    }
}
