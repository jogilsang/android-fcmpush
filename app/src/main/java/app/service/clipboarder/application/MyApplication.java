package app.service.clipboarder.application;

import android.app.Application;

import com.google.firebase.FirebaseApp;

import app.service.clipboarder.R;


public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);

    }

}