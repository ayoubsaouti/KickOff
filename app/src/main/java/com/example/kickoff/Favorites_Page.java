package com.example.kickoff;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Favorites_Page extends AppCompatActivity {
    private ListView listFav;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;
    private String userId, favRef, item, key;
    private List<String> favoritesValue,favoritesKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites_page);

        listFav = findViewById(R.id.listOfFavorites);

        favRef = "favorites";

        // Récupérer l'utilisateur actuel
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentUser.getUid();
        // Référence à l'emplacement de tout les utilisateur dans la base de données
        userRef = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db))
                .getReference("users").child(userId).child(favRef);

        favoritesValue = new ArrayList<>();
        favoritesKey = new ArrayList<>();


        printFav();

    }

    public void printFav(){
        // Écoute les modifications apportées aux données de la base de données
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Efface la liste actuelle des noms d'utilisateur
                favoritesValue.clear();

                // Parcourt chaque enfant de l'emplacement de la base de données
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Récupère le nom d'utilisateur et l'ajoute à la liste
                    favoritesValue.add(snapshot.getValue(String.class));
                    favoritesKey.add(snapshot.getKey());
                }

                // Crée un ArrayAdapter pour afficher les noms d'utilisateur dans la ListView
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                        android.R.layout.simple_list_item_1, favoritesValue);

                // Associe l'ArrayAdapter à la ListView
                listFav.setAdapter(adapter);

                listFav.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        item = favoritesValue.get(position);
                        key = favoritesKey.get(position);

                        generateShortToast(item + " selected");
                        Intent intent = new Intent(getApplicationContext(),Match_Page.class);
                        intent.putExtra("leagueSelected", item);
                        intent.putExtra("idLeagueSelected", Integer.valueOf(key));
                        startActivity(intent);

                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Gestion des erreurs de base de données
                System.out.println("Error : " + databaseError);
            }
        });
    }
    public void generateShortToast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}