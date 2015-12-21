package com.example.caseylouisee.monopolyversion1;

/**
 * Created by Casey Denner on 26/10/2015.
 */
public class Player {

    private final String name;
    private int currentPosition;
    private int money = 1500;
    private Boolean jail = false;

    public Player (String name){
        this.name = name;
        // When a new player is created they start at go(0).
        currentPosition = 0;
        money = 1500;
    }

    public void subtractMoney(int value){
        money = money-value;
    }

    public void addMoney(int value){
        money = money + value;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public String getName() {
        return name;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public int getMoney() {
        return money;
    }

    public void setJail(Boolean bool){
        jail = bool;
    }

    public Boolean getJail() {
        return jail;
    }
}
