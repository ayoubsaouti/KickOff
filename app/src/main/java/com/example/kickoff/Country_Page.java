package com.example.kickoff;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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


public class Country_Page extends AppCompatActivity {

    private RequestQueue queue;
    private String apiKey, url;
    private ListView myListOfCountry;
    private SearchView searchBar;
    private HashMap<Integer, String> myHashMap;
    private Set<String> mySetOfCountry;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_page);
        myListOfCountry = findViewById(R.id.myCountryList_id);
        searchBar = findViewById(R.id.searchBar);

        apiKey = getResources().getString(R.string.key_football_api);
        url = "https://apiv2.allsportsapi.com/football/?met=Leagues&APIkey=" + apiKey;
        queue  = Volley.newRequestQueue(this); // Instantiate the RequestQueue.

        myHashMap = new HashMap<>();
        mySetOfCountry = new TreeSet<>();


        generateCountry();
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

    public void generateShortToast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    public void generateCountry(){
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jObject = new JSONObject(response);
                            JSONArray countryArray = jObject.getJSONArray("result");


                            for (int i = 0; i < countryArray.length(); i++) {
                                JSONObject country = countryArray.getJSONObject(i);
                                String countryName = country.getString("country_name");
                                Integer countryId = country.getInt("country_key");
                                mySetOfCountry.add(countryName);
                                myHashMap.put(countryId, countryName);
                            }

                            List<String> sortedListOfCountry = new ArrayList<>(mySetOfCountry);

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                                    android.R.layout.simple_list_item_1, android.R.id.text1, sortedListOfCountry);


                            myListOfCountry.setAdapter(adapter);

                            setupSearchBar(adapter);

                            myListOfCountry.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    String item = (String) parent.getItemAtPosition(position);
                                    generateShortToast(item + " selected");
                                    //chatgpt
                                    //Creer un flux d'entré de la hashmap encapsule dans un object de type map.entry et filtre
                                    final Integer idCountrySelected = myHashMap.entrySet().stream().filter(entry -> item.equals(entry.getValue()))
                                            .map(Map.Entry::getKey) // extraction de la key
                                            .findFirst() // prend le premier
                                            .orElse(null); // sinon renvoie null
                                    //fin chatgpt
                                    Intent intent = new Intent(getApplicationContext(), League_Page.class);
                                    intent.putExtra("idCountrySelected", idCountrySelected);
                                    intent.putExtra("countrySelected", item);
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