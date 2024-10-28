package com.example.kickoff;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



public class Main_Page extends AppCompatActivity {
    private static FirebaseAuth auth;
    private static FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if(user == null){
            Intent intent = new Intent(getApplicationContext(), Home_Page.class);
            startActivity(intent);
            finish();
        }
    }
    public void goToCountryPage(View view) {
        Intent intent = new Intent(getApplicationContext(), Country_Page.class);
        startActivity(intent);
    }
    public void goToProfilePage(View view){
        Intent intent = new Intent(getApplicationContext(), Profile_Page.class);
        intent.putExtra("username", user.getEmail());
        startActivity(intent);
    }

    public void goToCreateJoinGroupPage(View view){
        Intent intent = new Intent(getApplicationContext(), CreateJoinGroup_Page.class);
        startActivity(intent);
    }

    public void goToFavoritesPage(View view){
        Intent intent = new Intent(getApplicationContext(), Favorites_Page.class);
        startActivity(intent);
    }
}
