package com.example.kickoff;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class Profile_Page extends AppCompatActivity {

    private FirebaseAuth auth;
    private Button btLogout;
    private TextView textView;
    private String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        auth = FirebaseAuth.getInstance();

        btLogout = findViewById(R.id.logout);
        textView = findViewById(R.id.users_details);

        username = "Welcome\n" + getIntent().getStringExtra("username");
        textView.setText(username);

        //LOGOUT
        btLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Home_Page.class);
                //CHAPT
                //empeche l'utilisateur de revenir en arriere en effacant les activités précédentes de la pile
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                //fin CHATGPT
                startActivity(intent);
                finish();
            }
        });
    }
}