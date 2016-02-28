package com.example.antoine.testapp.classes;

/**
 * Created by guillaumebrosse on 16/02/16.
 */
public class ClubClass {
    private String idClub;
    private String idLeague;
    private String name;
    private String iconLink;
    private String marketvalue;

    public ClubClass(String idClub, String idLeague, String name, String iconLink, String marketValue) {
        this.idClub = idClub;
        this.idLeague = idLeague;
        this.name = name;
        this.iconLink = iconLink;
        this.marketValue = marketValue;
    }

    public String getMarketValue() {

        return marketValue;
    }

    public void setMarketValue(String marketValue) {
        this.marketValue = marketValue;
    }

    public String getIdClub() {
        return idClub;
    }

    public void setIdClub(String idClub) {
        this.idClub = idClub;
    }

    public String getIdLeague() {
        return idLeague;
    }

    public void setIdLeague(String idLeague) {
        this.idLeague = idLeague;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconLink() {
        return iconLink;
    }

    public void setIconLink(String iconLink) {
        this.iconLink = iconLink;
    }

    private String marketValue;
}
