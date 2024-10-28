package com.example.kickoff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CreateJoinGroup_Page extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private String email;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_join_group_page);


        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        email = user.getEmail();

        //CHATGPT
        usersRef = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db)).getReference("users");
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // L'utilisateur a été trouvé dans la base de données
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        // Vérifiez si l'utilisateur a un groupe
                        if (userSnapshot.child("group").exists()) {
                            // L'utilisateur appartient à un groupe
                            Intent intent = new Intent(getApplicationContext(),Groupe_Page.class);
                            intent.putExtra("groupName",userSnapshot.child("group").getValue(String.class));
                            startActivity(intent);
                            finish(); // Termine l'activité actuelle
                        } else {
                            // L'utilisateur n'appartient à aucun groupe
                            generateShortToast("You dont have a group");
                        }
                    }
                } else {
                    // L'utilisateur n'a pas été trouvé dans la base de données
                    generateShortToast("The user dont exist in DB");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Gestion des erreurs de base de données
                generateShortToast("Error DB : " + databaseError.getMessage());
            }
        });
    }
    //fin CHATGPT

    public void goToCreateGroupPage(View view){
        Intent intent = new Intent(getApplicationContext(), CreateGroup_Page.class);
        startActivity(intent);
        finish();
    }

    public void goToJoinGroupPage(View view){
        Intent intent = new Intent(getApplicationContext(), JoinGroup_Page.class);
        startActivity(intent);
        finish();
    }

    public void generateShortToast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}