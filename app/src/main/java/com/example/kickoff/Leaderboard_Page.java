package com.example.kickoff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Pair;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Leaderboard_Page extends AppCompatActivity {

    private ListView leaderboard;
    private DatabaseReference groupsRef;
    private String groupName;
    private int groupScore;
    private List<Pair<String, Integer>> teamScores;
    private List<String> teamsSorted;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard_page);

        leaderboard = findViewById(R.id.listLeaderboard);


        printGroup();
    }

    public void printGroup(){
        // instanciation d'une liste pour stocker les paires (nom d'équipe, score)
        teamScores = new ArrayList<>();
        // instanciation d'une liste pour stocker les noms d'équipes triés
        teamsSorted = new ArrayList<>();

        // Référence à l'emplacement de tout les utilisateur dans la base de données
        groupsRef = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db)).getReference("groups").child("name");

        //CHATGPT
        // Écouteur d'événements pour récupérer les données des groups
        groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {

                    //si le score existe
                    if(groupSnapshot.child("score").exists()){
                        groupScore = groupSnapshot.child("score").getValue(Integer.class);
                        groupName = groupSnapshot.getKey() + " : " + groupSnapshot.child("score").getValue(Integer.class) + " points";

                        // Ajout de la paire (nom d'équipe, score) à la liste
                        teamScores.add(new Pair<>(groupName,groupScore));

                    }

                }
                // Tri de la liste en fonction des scores (décroissant)
                Collections.sort(teamScores, new Comparator<Pair<String, Integer>>() {
                    @Override
                    public int compare(Pair<String, Integer> team1, Pair<String, Integer> team2) {
                        // Trie en fonction des scores en ordre décroissant
                        return Integer.compare(team2.second, team1.second);
                    }
                });

                for (Pair<String, Integer> team : teamScores) {
                    String teamName = team.first;
                    teamsSorted.add(teamName);
                }


                adapter = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_list_item_1, android.R.id.text1, teamsSorted);

                //mis a jour de la listview
                leaderboard.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Gestion des erreurs de base de données
                generateShortToast("Error : " + error);
            }
        });
    }
    public void generateShortToast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}