package com.example.kickoff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CreateGroup_Page extends AppCompatActivity {

    private TextInputEditText groupName;
    private String name;
    private FirebaseUser currentUser;
    private DatabaseReference usersRef;
    private String userId;
    private boolean exists, vote;
    private Set<String> allGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_group_page);
        groupName = findViewById(R.id.name);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        userId = currentUser.getUid();

        allGroup = new HashSet<>();

        vote = false;

    }
    public CompletableFuture<Boolean> checkGrpInDb(){
        // source : CHATGPT pour les co routine
        // Méthode pour vérifier si la valeur "league" existe dans la base de données

        // Création d'un objet CompletableFuture pour représenter le résultat futur
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        // Référence à l'emplacement de tout les utilisateur dans la base de données
        usersRef = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db)).getReference("users");
        //CHATGPT
        // Écouteur d'événements pour récupérer les données des utilisateurs
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Parcourir toutes les entrées de données (utilisateurs)
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    if ((userSnapshot.child("group").getValue(String.class)) != null) {
                        allGroup.add(userSnapshot.child("group").getValue(String.class));
                    }
                }
                // Vérifier si la valeur recherchée existe dans le HashSet
                exists = allGroup.contains(name);
                // Compléter le CompletableFuture avec le résultat de la vérification
                future.complete(exists);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Gestion des erreurs de base de données
                future.completeExceptionally(error.toException());
            }
        });
        // Retourner l'objet CompletableFuture, qui sera complété ultérieurement avec la valeur de la base de données
        return future;
    }

    public void createGroup(View view){
        name = String.valueOf(groupName.getText());

        if(TextUtils.isEmpty(name)){
            generateShortToast("Name empty");
            return;
        }

        checkGrpInDb().thenAccept(groupExists  -> {
            // Si le groupe existe déjà dans la base de données, affichez un message
            if (groupExists) {
                generateShortToast("the name already exist");

            } else {
                usersRef = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db)).getReference("users").child(userId).child("group");

                usersRef.setValue(name)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                generateShortToast("Group created");
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                generateShortToast("Group Failed");
                            }});
                createTableGroup(name);
            }
        }).exceptionally(ex -> {
            // En cas d'erreur lors de la récupération des données, afficher un message d'erreur
            System.out.println("Error retrieving data : " + ex.getMessage());
            return null;
        });
    }

    public void generateShortToast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    public void createTableGroup(String nameOfGroup){
        usersRef = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db)).getReference("groups").child("name").child(nameOfGroup).child("users").child(userId);

        usersRef.setValue(vote)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        System.out.println("Table group success");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Table group error :" + e.getMessage());
                    }
                });
    }
}