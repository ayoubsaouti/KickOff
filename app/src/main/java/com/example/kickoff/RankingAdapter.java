package com.example.kickoff;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RankingAdapter extends ArrayAdapter<ClubData> {

    private String allMatch, winMatch, drawMatch, lostMatch, rank,point;
    //https://www.youtube.com/watch?v=TRfDbqmhSDQ&t=5s + CHATGPT
    // Constructeur de l'adaptateur
    public RankingAdapter(@NonNull Context context, ArrayList<ClubData> dataArrayList) {
        super(context, R.layout.list_item_ranking, dataArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        // Récupérer les données du club à la position donnée
        ClubData clubdata = getItem(position);

        // Si la vue est nulle, inflate la mise en page du liste item ranking
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_ranking, parent, false);
        }

        // Récupérer les vues de la mise en page
        TextView listRank = view.findViewById(R.id.rankingPosition_id);
        ImageView listLogo = view.findViewById(R.id.clubLogo_id);
        TextView listName = view.findViewById(R.id.clubName_id);
        TextView listAllMatch = view.findViewById(R.id.allMatch_id);
        TextView listMatchWon = view.findViewById(R.id.matchWon_id);
        TextView listMatchDraws = view.findViewById(R.id.matchesDraws_id);
        TextView listMatchLost = view.findViewById(R.id.matchesLost_id);
        TextView listPoint = view.findViewById(R.id.points_id);

        point = "Point : " + clubdata.point;
        listPoint.setText(point);

        // Convertir le rang en chaîne et définir le texte du rang
        rank = String.valueOf(clubdata.rank) + ".";
        listRank.setText(rank);

        // Charger l'image du club avec Picasso
        Picasso.get().load(clubdata.logo).into(listLogo);

        // Vérifier si une image a été chargée avec succès, sinon définir une image par défaut
        if (listLogo.getDrawable() == null) {
            listLogo.setImageResource(R.drawable.logo_team);
        }

        // Construire les chaînes de texte pour les statistiques du club
        allMatch = "Matches Played : " + clubdata.allMatch;
        winMatch = "Matches Won : " + clubdata.winMatch;
        drawMatch = "Matches Draws : " + clubdata.drawMatch;
        lostMatch = "Matches Lost : " + clubdata.lostMatch;

        // Définir le texte des vues avec les statistiques du club
        listName.setText(clubdata.name);
        listAllMatch.setText(allMatch);
        listMatchWon.setText(winMatch);
        listMatchDraws.setText(drawMatch);
        listMatchLost.setText(lostMatch);

        // Retourner la vue
        return view;
    }
}