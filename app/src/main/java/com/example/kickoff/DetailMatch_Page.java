package com.example.kickoff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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

import java.text.SimpleDateFormat;

import java.util.Date;


import java.util.Locale;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import java.text.ParseException;


public class DetailMatch_Page extends AppCompatActivity implements OnMapReadyCallback {

    private RequestQueue queue;
    private int idMatchSelected;
    private String matchId,url,urlMaps, apiKey;
    private TextView dateTimeMatch,finalResult,homeTeam,awayTeam,stadium,statut;
    private ImageView homeTeamLogo,awayTeamLogo;
    private SimpleDateFormat inputFormat, outputFormat;
    private SupportMapFragment mapFragment;
    private double latitude, longitude;
    private Button backToHome;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_match_page);

        apiKey = getResources().getString(R.string.key_football_api);
        queue = Volley.newRequestQueue(this); // Instantiate the RequestQueue.

        idMatchSelected = getIntent().getIntExtra("idMatchSelected", -1); // retourne -1 si le id n'est pas trouve


        matchId = "&matchId=" + idMatchSelected;
        url = "https://apiv2.allsportsapi.com/football/?met=Fixtures&APIkey=" + apiKey + matchId;
        System.out.println(url);

        dateTimeMatch = findViewById(R.id.dateTimeMatch);
        homeTeamLogo = findViewById(R.id.homeTeamLogo);
        finalResult = findViewById(R.id.finalResult);
        awayTeamLogo = findViewById(R.id.awayTeamLogo);
        homeTeam = findViewById(R.id.homeTeam);
        awayTeam = findViewById(R.id.awayTeam);
        stadium = findViewById(R.id.stadium);
        statut = findViewById(R.id.statut);
        backToHome = findViewById(R.id.bt_home);

        //CHATGPT
        // Locale.getDefault() est utilisé pour obtenir la configuration régionale par défaut.
        // interpréter la date et l'heure dans le format spécifié "AAAA-MM-JJ HH:MM".
        inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        // formater la date et l'heure dans le format souhaité "JJ-MM-AAAA 'at' HH:MM".
        outputFormat = new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm", Locale.getDefault());
        // FIN CHATGPT

        generateDetailMatch();

    }

    public void generateDetailMatch(){
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jObject = new JSONObject(response);
                            JSONArray detailMatchArray = jObject.getJSONArray("result");

                                JSONObject detailMatch = detailMatchArray.getJSONObject(0);
                                // Extraire la date et l'heure du détail du match
                                String eventDate = detailMatch.getString("event_date");
                                String eventTime = detailMatch.getString("event_time");

                                //CHATGPT
                                // input et output du Formatage de la date et l'heure
                                Date dateTime = inputFormat.parse(eventDate + " " + eventTime);
                                String formattedDateTime = outputFormat.format(dateTime);
                                //fin CHATGPT

                                dateTimeMatch.setText(formattedDateTime);
                                finalResult.setText(detailMatch.getString("event_final_result"));
                                homeTeam.setText(detailMatch.getString("event_home_team"));

                                awayTeam.setText(detailMatch.getString("event_away_team"));
                                statut.setText(detailMatch.getString("event_status"));
                                stadium.setText(detailMatch.getString("event_stadium"));
                                Picasso.get().load(detailMatch.getString("home_team_logo")).into(homeTeamLogo);
                                Picasso.get().load(detailMatch.getString("away_team_logo")).into(awayTeamLogo);

                                if (homeTeamLogo.getDrawable() == null) {
                                    // homeTeamLogo n'a pas d'image
                                    homeTeamLogo.setImageResource(R.drawable.logo_team);
                                }
                                if (awayTeamLogo.getDrawable() == null) {
                                    // awayTeamLogo n'a pas d'image
                                    awayTeamLogo.setImageResource(R.drawable.logo_team);
                                }
                                if(TextUtils.isEmpty(stadium.getText())){
                                    stadium.setText("NO STADIUM ASSIGNED YET");
                                }

                                urlMaps = "https://maps.googleapis.com/maps/api/geocode/json?key=AIzaSyB1kHsHujdwKvUAfaSAs0a6KUf1oZQI2Gs&address=" + stadium.getText();
                                System.out.println(urlMaps);
                                generateLocation();
                                mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);


                        } catch (JSONException | ParseException e) {
                            e.printStackTrace();
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

    public void generateShortToast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    public void generateLocation(){
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlMaps,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jObject = new JSONObject(response);
                            JSONObject stadiumObject = jObject.getJSONArray("results").getJSONObject(0);
                            JSONObject geometryObject = stadiumObject.getJSONObject("geometry");
                            JSONObject locationObject = geometryObject.getJSONObject("location");

                            latitude = locationObject.getDouble("lat");
                            longitude = locationObject.getDouble("lng");
                            System.out.println("avant = " + latitude);
                            System.out.println("avant = " + longitude);
                            mapFragment.getMapAsync(DetailMatch_Page.this); // initialisation de la map

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                generateShortToast("Adress not found");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        System.out.println("apr = " + latitude);
        System.out.println("apr = " + longitude);

        LatLng location = new LatLng(latitude,longitude);
        googleMap.addMarker(new MarkerOptions().position(location).title((String) stadium.getText()));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,12));
    }

    public void goToHome(View view){
        Intent intent = new Intent(getApplicationContext(), Home_Page.class);
        startActivity(intent);
        finish();
    }
}