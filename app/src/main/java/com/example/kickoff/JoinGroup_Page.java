package com.example.kickoff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JoinGroup_Page extends AppCompatActivity {

    private ListView listGroup;
    private FirebaseUser currentUser;
    private DatabaseReference userRef, usersRef;
    private Set<String> sortedGroups;
    private String group,userId;
    private List<String> usernames;
    private Boolean vote;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group_page);

        listGroup = findViewById(R.id.listOfGroup);

        vote = false;
        // Récupérer l'utilisateur actuel
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        userId = currentUser.getUid();

        sortedGroups = new HashSet<>();

        printGroup();

    }


    public void printGroup(){
        // Référence à l'emplacement de tout les utilisateur dans la base de données
        usersRef = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db)).getReference("groups").child("name");

        //CHATGPT
        // Écouteur d'événements pour récupérer les données des groups
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    group = groupSnapshot.getKey();
                    // Ajouter le nom du groupe à la liste
                    sortedGroups.add(group);
                }
                // met la set dans la arraylist pour l'adapter (prend uniquement des arraylist en param)
                usernames = new ArrayList<>(sortedGroups);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_list_item_1, android.R.id.text1, usernames);
                //mis a jour de la listview
                listGroup.setAdapter(adapter);

                joinGroup();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Gestion des erreurs de base de données
                generateShortToast("Error : " + error);
            }
        });
    }


    public void joinGroup(){
        listGroup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                generateShortToast(item + " selected");

                userRef = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db)).getReference("users").child(userId);

                userRef.child("group").setValue(item)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                joinTableGroup(item);
                                generateShortToast("You have joined " + item);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                generateShortToast("Error : " + e);
                            }
                        });
            }
        });
    }
    public void joinTableGroup(String name){
        System.out.println("ITEM " + name);
        usersRef = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db)).getReference("groups").child("name").child(name).child("users").child(userId);

        usersRef.setValue(vote)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            System.out.println("Table group success");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println("Table group error : " + e.getMessage());
                        }
                    });
    }

    public void generateShortToast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}