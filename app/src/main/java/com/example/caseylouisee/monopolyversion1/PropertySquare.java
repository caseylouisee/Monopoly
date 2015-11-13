package com.example.caseylouisee.monopolyversion1;

/**
 * Created by Casey Denner on 26/10/2015.
 */
public class PropertySquare extends Square {

    private final String name;
    private final int place;
    private final int price;
    private final String group;
    private Boolean owned;
    private String ownedBy;
    private int numHouses;

    public PropertySquare(String name, int place, int price, String group){
        super(name, place);
        this.name = name;
        this.place = place;
        this.price = price;
        this.group = group;
        // By default on creation no properties are owned by any player.
        owned = false;
        // No properties have houses at the start of the game.
        numHouses = 0;
    }

    public void addHouse() {
        numHouses++;
    }

    public void setOwnedBy(String playerName) {
        ownedBy = playerName;
        owned = true;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getGroup() {
        return group;
    }

    public Boolean getOwned() {
        return owned;
    }

    public int getNumHouses() {
        return numHouses;
    }

    public String getOwnedBy() {
        return ownedBy;
    }

}
