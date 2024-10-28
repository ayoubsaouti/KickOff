package com.example.kickoff;

import android.widget.ImageView;

public class ClubData {
    int rank;
    String name,allMatch,winMatch,drawMatch,lostMatch,logo,point;


    public ClubData(int rank, String logo, String name, String allMatch, String winMatch, String drawMatch, String lostMatch, String point) {
        this.rank = rank;
        this.logo = logo;
        this.name = name;
        this.allMatch = allMatch;
        this.winMatch = winMatch;
        this.drawMatch = drawMatch;
        this.lostMatch = lostMatch;
        this.point = point;
    }

}
