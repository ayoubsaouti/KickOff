package com.example.kickoff;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Groupe_Page extends AppCompatActivity {

    private TextView groupName,myScore;
    private String name,userId, username,score;
    private ListView groupUser;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private List<String> usernames;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_groupe_page);
        groupName = findViewById(R.id.groupName);
        groupUser = findViewById(R.id.listOfUsers);
        myScore = findViewById(R.id.point);

        name = getIntent().getStringExtra("groupName");
        // Récupérer l'utilisateur actuel
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        usernames = new ArrayList<>();

        groupName.setText(name);

        printUsers();
        getCurrentPoint();
    }

    public void getCurrentPoint(){
        userRef = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db)).getReference("groups").child("name").child(name).child("score");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot dataSnapshot) {
                score = String.valueOf(dataSnapshot.getValue());
                if(score.equals("null")){
                    score = "0";
                }
                myScore.setText("Point : " + score);
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                // Gestion des erreurs de base de données
                generateShortToast("Error : " + error);
            }
        });

    }
    public void printUsers(){
        // Référence à l'emplacement de tout les utilisateur dans la base de données
        userRef = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db)).getReference("users");

        // Écoute les modifications apportées aux données de la base de données
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Efface la liste actuelle des noms d'utilisateur
                usernames.clear();

                // Parcourt chaque enfant de l'emplacement de la base de données
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Récupère le nom d'utilisateur et l'ajoute à la liste
                    System.out.println("NAME = " + name);
                    System.out.println("GROUP = " + snapshot.child("group").getValue(String.class));

                    if(name.equals(snapshot.child("group").getValue(String.class))){
                        username = snapshot.child("username").getValue(String.class); //specifie le type entre ()
                        usernames.add(username);
                    }
                }

                // Crée un ArrayAdapter pour afficher les noms d'utilisateur dans la ListView
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                        android.R.layout.simple_list_item_1, usernames);

                // Associe l'ArrayAdapter à la ListView
                groupUser.setAdapter(adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Gestion des erreurs de base de données
                System.out.println("Error : " + databaseError);
            }
        });
    }


    public void leaveGroup(View view) {
        //CHATGPT

        userId = currentUser.getUid();

        // Référence à l'emplacement de l'utilisateur dans la base de données
        userRef = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db)).getReference("users").child(userId);

        // Supprimer la valeur du groupe pour l'utilisateur
        userRef.child("group").removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        leaveTableGroup();
                        // La valeur du groupe a été supprimée avec succès
                        generateShortToast("You have left the group");
                        // Rediriger l'utilisateur vers une autre activité ou effectuer d'autres actions
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Une erreur s'est produite lors de la suppression de la valeur du groupe
                        generateShortToast("Error :  " + e.getMessage());
                    }
                });
        //FINCHATGPT
    }
    public void leaveTableGroup(){
        userRef = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db)).getReference("groups").child("name").child(name).child("users");

        userRef.child(userId).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        System.out.println("Table group left");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@androidx.annotation.NonNull Exception e) {
                        System.out.println("Table group Error :  " + e.getMessage());

                    }
                });
    }
    public void generateShortToast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    public void goToLeaderboardPage(View view){
        Intent intent = new Intent(getApplicationContext(), Leaderboard_Page.class);
        startActivity(intent);
    }
    public void goToMatchdayPage(View view){
        Intent intent = new Intent(getApplicationContext(), MatchOfTheDay_Page.class);
        intent.putExtra("groupName",name);
        startActivity(intent);
    }
}