package com.example.kickoff;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class League_Page extends AppCompatActivity {

    private RequestQueue queue;
    private int idCountrySelected;
    private TextView countrySelect;
    private String countryId, url,country,apiKey;
    private SearchView searchBar;
    private ListView myListOfLeagues;
    private HashMap<Integer, String> myHashMap;
    private Set<String> mySetOfLeagues;
    private List<String> sortedListOfLeagues;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_league_page);
        searchBar = findViewById(R.id.searchBar);
        myListOfLeagues = findViewById(R.id.myLeagueList_id);
        countrySelect = findViewById(R.id.countrySelected);

        apiKey = getResources().getString(R.string.key_football_api);
        queue = Volley.newRequestQueue(this); // Instantiate the RequestQueue.

        country = getIntent().getStringExtra("countrySelected");
        idCountrySelected = getIntent().getIntExtra("idCountrySelected", -1); // retourne -1 si le id n'est pas trouve
        countryId = "&countryId=" + idCountrySelected;
        url = "https://apiv2.allsportsapi.com/football/?met=Leagues&APIkey=" + apiKey + countryId;
        System.out.println(url);


        myHashMap = new HashMap<>();
        mySetOfLeagues = new TreeSet<>();

        System.out.println(country);
        countrySelect.setText(country);

        generateLeagues();
    }

    //https://www.youtube.com/watch?v=hOSa885ed9k
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
    //FIN https://www.youtube.com/watch?v=hOSa885ed9k
    public void generateShortToast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    public void generateLeagues(){
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jObject = new JSONObject(response);
                            JSONArray leaguesArray = jObject.getJSONArray("result");


                            for (int i = 0; i < leaguesArray.length(); i++) {
                                JSONObject league = leaguesArray.getJSONObject(i);
                                Integer leagueId = league.getInt("league_key");
                                String leagueName = league.getString("league_name");
                                mySetOfLeagues.add(leagueName);
                                myHashMap.put(leagueId, leagueName);

                            }

                            sortedListOfLeagues = new ArrayList<>(mySetOfLeagues);

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                                    android.R.layout.simple_list_item_1, android.R.id.text1, sortedListOfLeagues);

                            myListOfLeagues.setAdapter(adapter);

                            setupSearchBar(adapter);

                            myListOfLeagues.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    String item = (String) parent.getItemAtPosition(position);
                                    generateShortToast(item + " selected");
                                    //chatgpt
                                    //Creer un flux d'entré de la hashmap encapsule dans un object de type map.entry et filtre
                                    final Integer idLeagueSelected = myHashMap.entrySet().stream().filter(entry -> item.equals(entry.getValue()))
                                            .map(Map.Entry::getKey) // extraction de la key
                                            .findFirst() // prend le premier
                                            .orElse(null); // sinon renvoie null
                                    //fin chatgpt
                                    Intent intent = new Intent(getApplicationContext(),Match_Page.class);
                                    intent.putExtra("leagueSelected", item);
                                    intent.putExtra("idLeagueSelected", idLeagueSelected);
                                    startActivity(intent);
                                }
                            });

                        } catch (JSONException e) {
                            generateShortToast("Erreur dans l'analyse de la réponse JSON");
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
}