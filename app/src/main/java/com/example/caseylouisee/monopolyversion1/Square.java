package com.example.caseylouisee.monopolyversion1;

/**
 * Created by Casey Denner on 26/10/2015.
 */
public abstract class Square {

    String name;
    int place;

    public Square(String name, int place) {
        this.name = name;
        this.place = place;
    }

    public String getName() {
        return name;
    }

    public int getPlace() {
        return place;
    }

    //public abstract void doAction(Player player, Board board);
}
