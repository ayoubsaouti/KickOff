package com.example.kickoff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import android.widget.Button;
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


import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;

public class MatchOfTheDay_Page extends AppCompatActivity {
    private RequestQueue queue;

    private NotificationManager notificationManager;
    private String apiKey, url, dateFormatted, debutUrl, idOfMatch, finalResult, teamHome,
            teamAway, groupName, userId, currentVote, voteTeamA, voteTeamB, textNoVote, currentId,
            myFinalTeam, score, winner,chanelId,eventDateStr;
    private Calendar calendarForNextDay;
    private Date dateDuLendemain, eventDate, currentDate;
    private SimpleDateFormat sdf;
    private TextView homeTeam, awayTeam, voteHome, voteAway, myVote, noVote;
    private Button btHome, btAway;
    private Random random;
    private int randomRaw, matchId, matchIdInt, cptHome, cptAway, cptNoVote, randomVote,
            scoreTeamA, scoreTeamB, point,dayAdded;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef, usersRef;
    private String[] scores;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_of_the_day_page);

        queue = Volley.newRequestQueue(this); // Instantiate the RequestQueue.
        random = new Random();
        apiKey = getResources().getString(R.string.key_football_api);
        //https://apiv2.allsportsapi.com/football/?met=Fixtures&APIkey=e78d8f0873248f7d9be0d24043eb567e6b5f918e179a76356a02cd67ec1b4d7a&from=2024-05-18&to=2024-05-18
        debutUrl = "https://apiv2.allsportsapi.com/football/?met=Fixtures&APIkey=";
        groupName = getIntent().getStringExtra("groupName");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentUser.getUid();
        voteTeamA = "You have voted for Team : 1";
        voteTeamB = "You have voted for Team : 2";
        chanelId = "CHANNEL_ID_MATCHOFTHEDAY";
        point = 10;
        dayAdded = 2;


        homeTeam = findViewById(R.id.homeTeam);
        awayTeam = findViewById(R.id.awayTeam);
        voteHome = findViewById(R.id.score_team_a);
        voteAway = findViewById(R.id.score_team_b);
        btHome = findViewById(R.id.bt_team_a);
        btAway = findViewById(R.id.bt_team_b);
        myVote = findViewById(R.id.my_vote);
        noVote = findViewById(R.id.no_vote);

        //CHATGPT + https://www.youtube.com/watch?v=vyt20Gg2Ckg&t=672s
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            // Vérifie si l'appareil exécute Android 9.0 (Pie) ou une version ultérieure
            if(ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){

                // Vérifie si la permission POST_NOTIFICATIONS n'est pas accordée
                // Demande la permission POST_NOTIFICATIONS si elle n'est pas accordée
                ActivityCompat.requestPermissions(MatchOfTheDay_Page.this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        //FIN

        getMatchIdFromDatabase();
        getCurrentVote();


    }

    public String getNextDayDate() {
        //CHATGPT

        // Obtenir la date actuelle
        calendarForNextDay = Calendar.getInstance();

        // Ajouter un jour à la date actuelle
        calendarForNextDay.add(Calendar.DAY_OF_YEAR, 1);

        // Obtenir la date du lendemain
        dateDuLendemain = calendarForNextDay.getTime();

        // Format la date pour le JSON
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        dateFormatted = sdf.format(dateDuLendemain);

        return dateFormatted;
    }

    public void getRandomMatch() {
        url = debutUrl + apiKey + "&from=" + getNextDayDate() + "&to=" +  getNextDayDate();
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jObject = new JSONObject(response);
                            JSONArray matchArray = jObject.getJSONArray("result");

                            System.out.println("LENGHT : " + matchArray.length());
                            randomRaw = random.nextInt(matchArray.length());

                            JSONObject match = matchArray.getJSONObject(randomRaw);
                            matchIdInt = match.getInt("event_key");
                            matchId = match.getInt("event_key");
                            idOfMatch = String.valueOf(matchId);

                            teamHome = match.getString("event_home_team");
                            teamAway = match.getString("event_away_team");
                            updateTextView(teamHome, teamAway);


                            addMatchInDb();

                        } catch (JSONException e) {
                            generateShortToast("Erreur dans l'analyse de la réponse JSON : " + e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                generateShortToast("Error : " + error);
            }

        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void addMatchInDb() {
        dbRef = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db)).getReference("groups").child("matchOfTheDay");

        dbRef.setValue(idOfMatch)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        System.out.println("idOfMatch success");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("idOfMatch error :" + e.getMessage());
                    }
                });
    }

    public void getMatchIdFromDatabase() {
        dbRef = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db)).getReference("groups").child("matchOfTheDay");

        // Ajouter un écouteur pour écouter les changements de données dans la base de données
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Vérifier si les données existent
                if (dataSnapshot.getValue() == null) {
                    getRandomMatch();
                } else {
                    idOfMatch = String.valueOf(dataSnapshot.getValue());
                    getFinalResult(idOfMatch);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Gérer les erreurs de lecture de la base de données
                System.out.println("Error :" + databaseError);
            }
        });
    }

    public void removeMatchInDb() {
        dbRef = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db)).getReference("groups").child("matchOfTheDay");

        dbRef.removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        generateShortToast("Un nouveau match va apparaitre");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        generateShortToast("Error : " + e);
                    }
                });
    }
    public boolean verifCloseVote(String date){
        try {

            // Date récupérée depuis votre JSON
            sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            eventDate = sdf.parse(date);

            // Crée une nouvelle instance de calendarForNextDay avec la date du jour
            calendarForNextDay = Calendar.getInstance();
            //stocker la date du jour
            currentDate = calendarForNextDay.getTime();
            //set avec celle du JSON
            calendarForNextDay.setTime(eventDate);
            // Ajouter x jour à la date du json
            calendarForNextDay.add(Calendar.DAY_OF_YEAR, dayAdded);

            // Obtenir la date du lendemain ( x jour)
            dateDuLendemain = calendarForNextDay.getTime();

            System.out.println(dateDuLendemain);

            System.out.println(currentDate);

            System.out.println(currentDate.compareTo(dateDuLendemain));

            // Vérifier si la date actuelle est le lendemain ou plus tard que la date récupérée
            return currentDate.compareTo(dateDuLendemain) > 0;

        } catch (ParseException e) {
            // Gestion de l'exception ParseException
            System.out.println("ERROR PARSE :" + e);
        }
        return false;
    }

    public void getFinalResult(String idOfMatch) {
        url = debutUrl + apiKey + "&matchId=" + idOfMatch;
        System.out.println(url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jObject = new JSONObject(response);
                            JSONArray matchArray = jObject.getJSONArray("result");
                            JSONObject match = matchArray.getJSONObject(0);

                            matchIdInt = match.getInt("event_key");
                            finalResult = match.getString("event_status");
                            eventDateStr = match.getString("event_date");

                            //eventDateStr = "27-05-2024";
                            if(verifCloseVote(eventDateStr)){
                                score = match.getString("event_final_result");
                                //score = "2-0"; // AJOUTER POUR LE TEST
                                // Diviser la chaîne en fonction du séparateur "-"
                                scores = score.split("-");

                                // Convertir les scores en entiers .trim pour supp les espaces
                                scoreTeamA = Integer.parseInt(scores[0].trim());
                                scoreTeamB = Integer.parseInt(scores[1].trim());
                                if (myFinalTeam != null) {
                                    resultat(scoreTeamA, scoreTeamB, myFinalTeam);
                                }
                                //METTRE EN COM POUR LE TEST
                                removeMatchInDb();
                                resetVote();
                                finish();
                            } else {
                                teamHome = match.getString("event_home_team");
                                teamAway = match.getString("event_away_team");
                                updateTextView(teamHome, teamAway);

                            }

                        } catch (JSONException e) {
                            generateShortToast("Erreur dans l'analyse de la réponse JSON : " + e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                generateShortToast("Error : " + error);
            }

        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void getCurrentVote() {
        cptAway = 0;
        cptHome = 0;
        cptNoVote = 0;
        //ECOUTE LA DB
        dbRef = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db)).getReference("groups")
                .child("name").child(groupName).child("users");

        // Ajouter un écouteur pour écouter les changements de données dans la base de données
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    // Vérifier si l'utilisateur a voté
                    if (userSnapshot.child("vote").exists()) {
                        // Récupérer le vote de l'utilisateur
                        currentVote = userSnapshot.child("vote").getValue(String.class);
                        // incrementer le compteur en fonction du vote de l'utilisateur

                        if (currentVote.equals("A")) {
                            if (userSnapshot.getKey().equals(userId)) {
                                myVote.setText(voteTeamA);
                            }
                            cptHome++;
                        } else if (currentVote.equals("B")) {
                            if (userSnapshot.getKey().equals(userId)) {
                                myVote.setText(voteTeamB);
                            }
                            cptAway++;
                        }
                    } else {
                        // L'utilisateur n'a pas encore voté (vote == false)
                        cptNoVote++;
                        System.out.println(cptNoVote);
                    }

                    if (cptNoVote != 0) {
                        textNoVote = "Number of people who have not yet voted : " + String.valueOf(cptNoVote);
                        noVote.setText(textNoVote);
                    } else {
                        //TOUT LE MONDE A VOTER
                        //CHOIX DEQUIPE
                        lockVote();
                        noVote.setText("");
                        myFinalTeam = lockTeam(cptHome, cptAway);
                    }

                }

                updateVote(String.valueOf(cptHome), String.valueOf(cptAway));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Gérer les erreurs de lecture de la base de données
                System.out.println("Error :" + databaseError);
            }
        });

    }

    public void updateVote(String cptA, String cptB) {
        voteHome.setText(cptA);
        voteAway.setText(cptB);
    }

    public void addVoteInDb(String equipe) {
        dbRef = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db)).getReference("groups")
                .child("name").child(groupName).child("users").child(userId).child("vote");
        //SETVALUE IN DB
        dbRef.setValue(equipe)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        System.out.println("Vote success");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Vote error :" + e.getMessage());
                    }
                });
    }

    public void lockVote() {
        btHome.setEnabled(false);
        btAway.setEnabled(false);
    }

    public void resetVote() {
        usersRef = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db)).getReference("groups")
                .child("name").child(groupName).child("users");

        // Récupérer une référence à tous les utilisateurs du groupe
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Parcourir tous les utilisateurs
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    // Récupérer l'ID de l'utilisateur
                    currentId = userSnapshot.getKey();

                    usersRef.child(currentId).setValue(false)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    System.out.println("resetVote success");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("resetVote error :" + e.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Gérer les erreurs de lecture de la base de données
                System.out.println("Erreur lors de la lecture des utilisateurs du groupe: " + databaseError.getMessage());
            }
        });

    }

    public String lockTeam(int voteA, int voteB) {
        if (voteA == 0 && voteB == 0) {
            return null;
        }
        if (voteA > voteB) {
            myVote.setText("Votre équipe a choisis de voter pour l'équipe : A");
            return "A";
        } else if (voteB > voteA) {
            myVote.setText("Votre équipe a choisis de voter pour l'équipe : B");
            return "B";
        } else {
            randomVote = random.nextInt(2) + 1;

            // Utiliser le résultat du nombre aléatoire pour choisir une option
            if (randomVote == 1) {
                myVote.setText("Votre équipe a choisi de voter pour l'équipe A");
                return "A";
            } else {
                myVote.setText("Votre équipe a choisi de voter pour l'équipe B");
                return "B";
            }
        }
    }

    public void resultat(int teamA, int teamB, String teamVoted) {
        if (teamA > teamB) {
            winner = "A";
        } else if (teamB > teamA) {
            winner = "B";
        } else {
            winner = "Match nul";
        }
        if (teamVoted == winner) {
            //NOTIF

            notifWon();
            updateScoreDb();
        } else {
            //NOTIF
            notifLost();

        }
    }
    private void createNotificationChannel() {
        //CHATGPT + https://www.youtube.com/watch?v=vyt20Gg2Ckg&t=672s
        // Obtenir le gestionnaire de notification
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Vérifier si le périphérique exécute Android Oreo (version 8.0) ou ultérieure
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Récupérer le canal de notification avec l'identifiant spécifié
            NotificationChannel notificationChannel =
                    notificationManager.getNotificationChannel(chanelId);

            // Vérifier si le canal de notification n'existe pas
            if (notificationChannel == null) {
                // Définir l'importance du canal
                int importance = NotificationManager.IMPORTANCE_HIGH;

                // Créer un nouveau canal de notification
                notificationChannel = new NotificationChannel(chanelId, "MATCH OF THE DAY", importance);

                // Définir la couleur de la lumière de notification
                notificationChannel.setLightColor(Color.RED);

                // Activer la vibration pour ce canal de notification
                notificationChannel.enableVibration(true);

                // Ajouter le canal de notification au gestionnaire de notification
                notificationManager.createNotificationChannel(notificationChannel);
                //
            }
        }
    }

    public void notifLost() {
        //CHATGPT + https://www.youtube.com/watch?v=vyt20Gg2Ckg&t=672s
        // Créer un Intent pour l'activité que vous souhaitez ouvrir lorsque la notification est cliquée
        Intent intent = new Intent(getApplicationContext(), Groupe_Page.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Créer un constructeur de notification avec le canal spécifié
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), chanelId);

        // Définir le titre, le texte, l'icône et d'autres propriétés de la notification
        builder.setContentTitle("MATCH OF THE DAY")
                .setContentText("YOU LOST ! You haven't earned any points")
                .setSmallIcon(R.drawable.notifications_active)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Créer le canal de notification
        createNotificationChannel();

        // Afficher la notification
        notificationManager.notify(0, builder.build());
        //
    }

    public void notifWon() {
        //CHATGPT + https://www.youtube.com/watch?v=vyt20Gg2Ckg&t=672s
        // Créer un Intent pour l'activité que vous souhaitez ouvrir lorsque la notification est cliquée
        Intent intent = new Intent(getApplicationContext(), Groupe_Page.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Créer un constructeur de notification avec le canal spécifié
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), chanelId);

        // Définir le titre, le texte, l'icône et d'autres propriétés de la notification
        builder.setContentTitle("MATCH OF THE DAY");
        builder.setContentText("CONGRATULATIONS YOU WON !! Your group has just earned 10 points");
        builder.setSmallIcon(R.drawable.notifications_active);
        builder.setAutoCancel(true);

        // Créer le canal de notification
        createNotificationChannel();

        // Afficher la notification
        notificationManager.notify(0, builder.build());
        //
    }
    public void updateScoreDb(){
        dbRef = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db)).getReference("groups")
                .child("name").child(groupName);

        // Ajouter un écouteur pour écouter les changements de données dans la base de données
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("score").exists()){

                    point += dataSnapshot.child("score").getValue(Integer.class);
                    addScoreInDb();
                }
                else{
                    addScoreInDb();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Gérer les erreurs de lecture de la base de données
                System.out.println("Error :" + databaseError);
            }
        });
    }
    public void addScoreInDb(){
        dbRef = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db)).getReference("groups")
                .child("name").child(groupName).child("score");
        dbRef.setValue(point)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        System.out.println("point added ");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("point error :" + e.getMessage());
                    }
                });
    }
    public void voteEquipeA(View view){
        addVoteInDb("A");
        getCurrentVote();
        myVote.setText(voteTeamA);
    }
    public void voteEquipeB(View view){
        addVoteInDb("B");
        getCurrentVote();
        myVote.setText(voteTeamB);
    }
    public void updateTextView(String teamA, String teamB){
        homeTeam.setText(teamA);
        awayTeam.setText(teamB);
    }
    public void goToDetailMatch(View view){
        Intent intent = new Intent(getApplicationContext(), DetailMatch_Page.class);
        intent.putExtra("idMatchSelected", matchIdInt);
        startActivity(intent);
    }

    public void generateShortToast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}