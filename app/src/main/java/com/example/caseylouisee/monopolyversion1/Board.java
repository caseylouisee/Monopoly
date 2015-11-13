package com.example.caseylouisee.monopolyversion1;

import java.util.ArrayList;

/**
 * Created by Casey Denner on 26/10/2015.
 */
public class Board {

    private static final ArrayList<Square> squares = new ArrayList<Square>(40);


    public Board() {

        initializeSquares();

    }

    public void initializeSquares() {

        squares.add(0, new SpecialSquare("Go", 0));

        squares.add(1, new PropertySquare("Old Kent Road", 1, 60, "Brown"));
        squares.add(2, new CardSquare("Community Chest", 2));
        squares.add(3, new PropertySquare("Whitechapel Road", 3, 50, "Brown"));
        squares.add(4, new SpecialSquare("Income Tax", 4));
        squares.add(5, new PropertySquare("Kings Cross Station", 5, 200, "Station"));
        squares.add(6, new PropertySquare("The Angel Islington", 6, 100, "Blue"));
        squares.add(7, new CardSquare("Chance", 7));
        squares.add(8, new PropertySquare("Euston Road", 8, 100, "Blue"));
        squares.add(9, new PropertySquare("Pentonville Road", 9, 120, "Blue"));
        squares.add(10, new SpecialSquare("Jail", 10));
        squares.add(11, new PropertySquare("Pall Mall", 11, 140, "Pink"));
        squares.add(12, new PropertySquare("Electric Company", 12, 150, "Utilities"));
        squares.add(13, new PropertySquare("Whitehall", 13, 140, "Pink"));
        squares.add(14, new PropertySquare("Northumberland Avenue", 14, 160, "Pink"));
        squares.add(15, new PropertySquare("Marylebone Station", 15, 200, "Station"));
        squares.add(16, new PropertySquare("Bow Street", 16, 180, "Orange"));
        squares.add(17, new CardSquare("Community Chest", 17));
        squares.add(18, new PropertySquare("Marlborough Street", 18, 180, "Orange"));
        squares.add(19, new PropertySquare("Vine Street", 19, 200, "Orange"));
        squares.add(20, new SpecialSquare("Free Parking", 20));
        squares.add(21, new PropertySquare("Strand", 21, 220, "Red"));
        squares.add(22, new CardSquare("Chance", 22));
        squares.add(23, new PropertySquare("Fleet Street", 23, 220, "Red"));
        squares.add(24, new PropertySquare("Trafalgar Square", 24, 240, "Red"));
        squares.add(25, new PropertySquare("Fenchurch Street Station", 25, 200, "Station"));
        squares.add(26, new PropertySquare("Leicester Square", 26, 260, "Yellow"));
        squares.add(27, new PropertySquare("Coventry Streey", 27, 260, "Yellow"));
        squares.add(28, new PropertySquare("Water Works", 28, 150, "Utilities"));
        squares.add(29, new PropertySquare("Picadilly", 29, 280, "Yellow"));
        squares.add(30, new SpecialSquare("Go To Jail", 30));
        squares.add(31, new PropertySquare("Regent Street", 31, 300, "Green"));
        squares.add(32, new PropertySquare("Oxford Street", 32, 300, "Green"));
        squares.add(33, new CardSquare("Community Chest", 33));
        squares.add(34, new PropertySquare("Bond Street", 34, 320, "Green"));
        squares.add(35, new PropertySquare("Liverpool Street Station", 35, 200, "Station"));
        squares.add(36, new CardSquare("Chance", 36));
        squares.add(37, new PropertySquare("Park Lane", 37, 350, "Purple"));
        squares.add(38, new SpecialSquare("Super Tax", 38));
        squares.add(39, new PropertySquare("Mayfair", 39, 400, "Purple"));

    }

    public Square getSquare(int pos){
        return squares.get(pos);
    }

}
