package com.example.kickoff;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class Home_Page extends AppCompatActivity {

    private FirebaseAuth mAuth;


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), Main_Page.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        mAuth = FirebaseAuth.getInstance();

    }

    public void goToRegister(View view) {
        Intent intent = new Intent(getApplicationContext(), Register_Page.class);
        startActivity(intent);
    }
    public void goToLogin(View view) {
        Intent intent = new Intent(getApplicationContext(), Login_Page.class);
        startActivity(intent);
    }
}