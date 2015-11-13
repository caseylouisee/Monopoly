package com.example.caseylouisee.monopolyversion1;

import android.app.Activity;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Casey Denner on 26/10/2015.
 */
public class Game extends Activity{

    public Game() {

        Board board = new Board();
        ArrayList<Player> players = new ArrayList<Player>();
        int currentTurn = 0;

//        System.out.println("How many players?");
//        Scanner in = new Scanner(System.in);
//        int num = Integer.parseInt(in.next());
//
//        for(int i = 1; i<=num; i++){
//            System.out.println("Enter player" + i + "name");
//            // TO-DO fix here - name entry..what if space types? in.nextLine() needed.
//            Player temp = new Player(in.next());
//            players.add(temp);
//        }

        Player player1 = new Player("jon");
        Player player2 = new Player("casey");
        players.add(player1);
        players.add(player2);
        int numPlayers = players.size();

        System.out.println("*****START GAME*****");
        System.out.println("numPlayers: " + numPlayers);
        playGame(currentTurn, numPlayers, players, board);

    }

    public void playGame(int currentTurn, int numPlayers, ArrayList<Player> players, Board board){

        if(currentTurn >= numPlayers){
            currentTurn = 0;
        }

        Player current = players.get(currentTurn);
        int pos = current.getCurrentPosition();

        TextView player = (TextView) findViewById(R.id.player);
        player.setText(current.getName());
        System.out.println("*****IT IS " + current.getName().toUpperCase() + "'S TURN*****");

        System.out.println("Name:" + current.getName());
        System.out.println("Current Position:" + pos + ", " + board.getSquare(pos).getName());

        System.out.println("Would you like to roll?");
        Scanner in = new Scanner(System.in);

        if(in.next().equals("yes")){
            int roll = getRoll();
            current.setCurrentPosition(pos + roll);
            int newPos = pos+roll;
            System.out.println("You rolled a " + roll);

            //loops around the board if player goes past square 40
            if(newPos > 40){
                newPos = newPos-40;
            }

            System.out.println("Updated Position:" + newPos + ", " + board.getSquare(newPos).getName());
            Square location = board.getSquare(newPos);
            //String posType = board.getSquare(newPos).getClass().getName();
            if(location instanceof PropertySquare){
                System.out.println("Landed on property");
                //buyProperty(current, location);
                playGame(currentTurn, numPlayers, players, board);
            } else if(location instanceof CardSquare){
                System.out.println("Take a Card");
                playGame(currentTurn, numPlayers, players, board);
            } else if(location instanceof SpecialSquare){
                System.out.println("Landed on special square");
                playGame(currentTurn, numPlayers, players, board);
            }
        }
    }

    private static void buyProperty(Player current, Square location) {
        int price = ((PropertySquare)location).getPrice();
        System.out.println("Would you like to buy " + location.getName() + "for " + price);
        Scanner in = new Scanner(System.in);
        if(in.equals("yes")){
            current.subtractMoney(price);
            ((PropertySquare)location).setOwnedBy(current.getName());
            System.out.println("You now own" + location.getName());
        }
    }

    public static int getRoll() {
        int dice = (int)(Math.random()*6) + 1;
        return dice;
    }

}
