package com.example.kickoff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;



public class Match_Page extends AppCompatActivity {

    private RequestQueue queue;
    private int idLeagueSelected;
    private String leagueId,startDateVisual,endDateVisual,debutUrl,url, leagueSelected, userId, userRef,favRev,apiKey;
    private ListView myListOfMatch;
    private HashMap<Integer, String> hashMapOfIdAndTeam;
    private Set<String> mySetOfMatch;
    private MaterialButton dateSelector;
    private TextView dateSelected;
    private String startDate, endDate;
    private Pair<Long, Long> initialSelection;
    private SearchView searchBar;
    private ArrayAdapter<String> adapter;
    private Switch leagueFav;
    private FirebaseUser currentUser;
    private DatabaseReference leagueRef;
    private Button ranking;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_page);
        myListOfMatch = findViewById(R.id.myMatchList_id);
        dateSelector = findViewById(R.id.dateSelector);
        dateSelected = findViewById(R.id.dateSelected);
        searchBar = findViewById(R.id.searchBar);
        leagueFav = findViewById(R.id.sw_leagueSelected);
        ranking = findViewById(R.id.bt_ranking);

        apiKey = getResources().getString(R.string.key_football_api);
        queue = Volley.newRequestQueue(this); // Instantiate the RequestQueue.

        hashMapOfIdAndTeam = new HashMap<>();
        mySetOfMatch = new TreeSet<>();
        // Créer une paire de dates initiale avec la date actuelle
        initialSelection = new Pair<>(MaterialDatePicker.todayInUtcMilliseconds(), MaterialDatePicker.todayInUtcMilliseconds());

        leagueSelected = getIntent().getStringExtra("leagueSelected");
        idLeagueSelected = getIntent().getIntExtra("idLeagueSelected", -1); // retourne -1 si le id n'est pas trouve
        leagueId = "&leagueId=" + idLeagueSelected;
        debutUrl = "https://apiv2.allsportsapi.com/football/?met=Fixtures&APIkey=" + apiKey + leagueId;
        favRev = "favorites";
        userRef = "users";

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentUser.getUid();
        leagueRef = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db)).getReference(userRef).child(userId).child(favRev);


        leagueFav.setText(leagueSelected);
        checkAndSetSwitch(leagueSelected);

        dateSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateDataDate();
            }
        });


    }
    private void checkAndSetSwitch(String league) {
        getValueFromDb().thenAccept(value -> {
            // Faire quelque chose avec la valeur récupérée
            if (league.equals(value)) {
                // Si oui, activer le switch
                leagueFav.setChecked(true);
            } else {
                // Sinon, désactiver le switch
                leagueFav.setChecked(false);
            }
        }).exceptionally(e -> {
            // Gérer les erreurs éventuelles
            generateShortToast("Error: " + e.getMessage());
            return null;
        });
    }

    public void setupSearchBar(ArrayAdapter<String> adapter){
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }
    public void generateDataDate() {
        //CHATGPT + https://www.youtube.com/watch?v=DjjfDdz9wjk

        // Créer le MaterialDatePicker pour sélectionner une plage de dates
        MaterialDatePicker<Pair<Long, Long>> materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("SELECT DATES")
                .setSelection(initialSelection) // Définir la sélection initiale
                .build();
        // Ajouter un écouteur de clic pour le bouton "Save" du MaterialDatePicker
        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long, Long> selection) {
                // Récupérer les dates de début et de fin sélectionnées par l'utilisateur
                long startDateMillis = selection.first;
                long endDateMillis = selection.second;

                // Formater les dates pour l'affichage
                startDateVisual = new SimpleDateFormat("dd-MM-yyy", Locale.getDefault()).format(new Date(startDateMillis));
                endDateVisual = new SimpleDateFormat("dd-MM-yyy", Locale.getDefault()).format(new Date(endDateMillis));
                dateSelected.setText("FROM " + startDateVisual + "\nTO " + endDateVisual);

                // Formater les dates pour le json
                startDate = new SimpleDateFormat("yyy-MM-dd", Locale.getDefault()).format(new Date(startDateMillis));
                endDate = new SimpleDateFormat("yyy-MM-dd", Locale.getDefault()).format(new Date(endDateMillis));

                if( (startDate != null ) && (endDate != null ) ){
                    url = debutUrl + "&from=" + startDate + "&to=" + endDate;
                    System.out.println(url);
                    generateMatch();
                }
            }
        });
        // Afficher le MaterialDatePicker
        materialDatePicker.show(getSupportFragmentManager(), "date_range_picker");
        //fin CHATGPT + https://www.youtube.com/watch?v=DjjfDdz9wjk
    }

    public void generateShortToast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    public void generateMatch(){
        // Vérifier si l'ensemble contient des données
        if (!mySetOfMatch.isEmpty()) {
            // Effacer les éléments de l'ensemble
            mySetOfMatch.clear();

            if(adapter != null){
                // Effacer les données de l'adaptateur
                adapter.clear();
                adapter.notifyDataSetChanged(); //  mise à jour de l'affichage de la liste associée à cet adaptateur
            }
        }


        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jObject = new JSONObject(response);
                            JSONArray matchArray = jObject.getJSONArray("result");

                            for (int i = 0; i < matchArray.length(); i++) {
                                JSONObject match = matchArray.getJSONObject(i);
                                Integer matchID = match.getInt("event_key");
                                String homeTeam = match.getString("event_home_team");
                                String awayTeam = match.getString("event_away_team");
                                mySetOfMatch.add(homeTeam + " VS " + awayTeam);
                                hashMapOfIdAndTeam.put(matchID, homeTeam + " VS " + awayTeam);

                            }
                            List<String> sortedListOfMatch = new ArrayList<>(mySetOfMatch);

                            adapter = new ArrayAdapter<String>(getApplicationContext(),
                                    android.R.layout.simple_list_item_1, android.R.id.text1, sortedListOfMatch);

                            myListOfMatch.setAdapter(adapter);
                            setupSearchBar(adapter);

                            myListOfMatch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    String item = (String) parent.getItemAtPosition(position);
                                    generateShortToast(item + " selected");
                                    //chatgpt
                                    //Creer un flux d'entré de la hashmap encapsule dans un object de type map.entry et filtre
                                    final Integer idMatchSelected = hashMapOfIdAndTeam.entrySet().stream().filter(entry -> item.equals(entry.getValue()))
                                            .map(Map.Entry::getKey) // extraction de la key
                                            .findFirst() // prend le premier
                                            .orElse(null); // sinon renvoie null
                                    //fin chatgpt
                                    Intent intent = new Intent(getApplicationContext(),DetailMatch_Page.class);
                                    intent.putExtra("idMatchSelected", idMatchSelected);
                                    startActivity(intent);
                                }
                            });
                        } catch (JSONException e) {
                            generateShortToast("No match found");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                generateShortToast("That didn't work!");
            }

        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    public void setSwitch(View view){
        if (leagueFav.isChecked()) {  // Vérifier si le switch est activé
            getValueFromDb().thenApply(value -> {   // Traiter la valeur récupérée de la base de données
                // Vérifie si la valeur récupérée de la base de données est null
                if (value == null) {
                    // on ajoute car elle existe pas dans la db
                    addInDb(leagueSelected);
                }
                // Retourne la valeur récupérée pour la prochaine étape
                return value;
            }).thenAccept(value -> {  // Effectuer une action une fois que la valeur a été traitée
                // Si la valeur existe déjà dans la base de données, afficher un message
                if (value.equals(leagueSelected)) {
                    generateShortToast("Already in your favorites");
                } else {
                    addInDb(leagueSelected);  // Sinon, ajouter la valeur dans la base de données
                }
            }).exceptionally(ex -> {
                // En cas d'erreur lors de la récupération des données, afficher un message d'erreur
                System.out.println("Error retrieving data : " + ex.getMessage());
                return null;
            });
        } else {
            // Récupérer la valeur de la base de données
            getValueFromDb().thenAccept(value -> {
                // Si la valeur existe déjà dans la base de données, retirer de la db
                if (value.equals(leagueSelected)) {
                    removeInDb(leagueSelected);
                } else {
                    // Sinon, afficher un message
                    generateShortToast("Already deleted from your favorites");
                }
            }).exceptionally(ex -> {
                // En cas d'erreur lors de la récupération des données, afficher un message d'erreur
                System.out.println("Error retrieving data : " + ex.getMessage());
                return null;
            });
        }
    }

    public CompletableFuture<String> getValueFromDb() {
        // source : CHATGPT pour les co routine
        // Méthode pour vérifier si la valeur "league" existe dans la base de données

        // Création d'un objet CompletableFuture pour représenter le résultat futur
        CompletableFuture<String> future = new CompletableFuture<>();

        // Ajout d'un ValueEventListener à la référence de la base de données
        leagueRef.child(String.valueOf(idLeagueSelected)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);

                // Compléter le CompletableFuture avec la valeur récupérée
                future.complete(value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // En cas d'annulation de la requête ou d'erreur
                // Compléter le CompletableFuture avec une exception indiquant l'erreur
                future.completeExceptionally(error.toException());
            }
        });
        // Retourner l'objet CompletableFuture, qui sera complété ultérieurement avec la valeur de la base de données
        return future;
    }
    public void addInDb(String league){
        leagueRef.child(String.valueOf(idLeagueSelected)).setValue(league)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        generateShortToast(league + " added to your favorites");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        generateShortToast("Error : " + e);
                    }
                });
    }
    public void removeInDb(String league){
        leagueRef.child(String.valueOf(idLeagueSelected)).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        generateShortToast(league + " is removed from your favorites");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        generateShortToast("Error : " + e);
                    }
                });
    }

    public void goToRanking(View view){
        Intent intent = new Intent(getApplicationContext(),Ranking_Page.class);
        intent.putExtra("leagueId", String.valueOf(idLeagueSelected));
        startActivity(intent);
    }
}