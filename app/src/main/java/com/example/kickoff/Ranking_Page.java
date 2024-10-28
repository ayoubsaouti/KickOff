package com.example.kickoff;

import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;

import android.widget.ArrayAdapter;
import android.widget.ListView;
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

public class Ranking_Page extends AppCompatActivity {


    private String leagueId,url,apiKey,teamName,winMatch,lostMatch,drawMatch,allMatch,logo,point;
    private ListView rankingList;
    private RequestQueue queue;
    private RankingAdapter adapter;
    private ArrayList<ClubData> dataArrayList;
    private ClubData listData;
    private int rank;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking_page);

        rankingList = findViewById(R.id.rankingList_id);
        apiKey = getResources().getString(R.string.key_football_api);
        leagueId = getIntent().getStringExtra("leagueId");
        url = "https://apiv2.allsportsapi.com/football/?&met=Standings&leagueId=" + leagueId + "&APIkey=" + apiKey;
        queue = Volley.newRequestQueue(this); // Instantiate the RequestQueue.
        dataArrayList = new ArrayList<>();

        generateRanking();
    }


    public void generateRanking(){

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jObject = new JSONObject(response);
                                JSONArray totalArray = jObject.getJSONObject("result").getJSONArray("total");

                                for (int i = 0; i < totalArray.length(); i++) {
                                    JSONObject club = totalArray.getJSONObject(i);
                                    rank = i+1;
                                    teamName = club.getString("standing_team");
                                    allMatch = club.getString("standing_P");
                                    winMatch = club.getString("standing_W");
                                    drawMatch = club.getString("standing_D");
                                    lostMatch = club.getString("standing_L");
                                    logo =  club.getString("team_logo");
                                    point = club.getString("standing_PTS");

                                    listData = new ClubData(rank,logo,teamName,allMatch,winMatch,drawMatch,lostMatch,point);
                                    dataArrayList.add(listData);

                                }
                                adapter = new RankingAdapter(getApplicationContext(),dataArrayList);
                                rankingList.setAdapter(adapter);

                                if (adapter == null || adapter.getCount() == 0) {
                                    // Si l'adaptateur est nul ou vide
                                    // Créez un adaptateur simple avec le message "Classement pas encore disponible"
                                    String[] message = {"Classement pas encore disponible"};
                                    ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, message);

                                    // Définissez l'adaptateur sur la ListView
                                    rankingList.setAdapter(emptyAdapter);
                                }


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

    public void generateShortToast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}