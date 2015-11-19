package com.example.caseylouisee.monopolyversion1;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

import us.dicepl.android.sdk.BluetoothManipulator;
import us.dicepl.android.sdk.DiceConnectionListener;
import us.dicepl.android.sdk.DiceController;
import us.dicepl.android.sdk.DiceResponseAdapter;
import us.dicepl.android.sdk.DiceResponseListener;
import us.dicepl.android.sdk.DiceScanningListener;
import us.dicepl.android.sdk.Die;
import us.dicepl.android.sdk.responsedata.RollData;

import static android.speech.SpeechRecognizer.createSpeechRecognizer;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    public static final int[] developerKey = new int[]
            {0x5e, 0x77, 0x68, 0xd3, 0xc6, 0xa0, 0x17, 0x0a};
    private Die dicePlus;
    private static final String TAG = "DICEPlus";
    TextView player;
    TextView rollResult;
    TextView currentPosition;
    TextView updatedPosition;
    TextView locationType;

    int currentTurn;
    ArrayList<Player> players;
    int numPlayers;
    Board board;
    private TextToSpeech tts;

    private void convertTextToSpeech(String text) {
        if (null == text || "".equals(text)) {
            text = "Please give some input.";
        }
        tts.speak(text, TextToSpeech.QUEUE_ADD, null);
    }

    DiceScanningListener scanningListener = new DiceScanningListener() {
        @Override
        public void onNewDie(Die die) {
            Log.d(TAG, "New DICE+ found");
            dicePlus = die;
            DiceController.connect(dicePlus);
        }

        @Override
        public void onScanStarted() {
            Log.d(TAG, "Scan Started");
        }

        @Override
        public void onScanFailed() {
            Log.d(TAG, "Scan Failed");
            BluetoothManipulator.startScan();
        }

        @Override
        public void onScanFinished() {
            Log.d(TAG, "Scan Finished");
            if(dicePlus == null){
                BluetoothManipulator.startScan();
            }
        }
    };

    DiceConnectionListener connectionListener = new DiceConnectionListener() {
        @Override
        public void onConnectionEstablished(Die die) {
            Log.d(TAG, "DICE+ Connected");
            DiceController.subscribeRolls(dicePlus);
        }

        @Override
        public void onConnectionFailed(Die die, Exception e) {
            Log.d(TAG, "Connection failed", e);
            dicePlus = null;
            BluetoothManipulator.startScan();
        }

        @Override
        public void onConnectionLost(Die die) {
            Log.d(TAG, "Connection lost");
            dicePlus = null;
            BluetoothManipulator.startScan();
        }
    };

    /*
    Here you can use the DiceResponseAdapter as it already has methods implemented unlike the
    DiceResponseListener Class.
    */
    DiceResponseListener responseListener = new DiceResponseAdapter(){

        @Override
        public void onRoll(Die die, RollData rolls, Exception exception) {
            super.onRoll(die, rolls, exception);

            Log.d(TAG, "Roll: " + rolls.face);

            final int face = rolls.face;

            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String method = "onRoll";
                    rollResult = (TextView) findViewById(R.id.rollResult);
                    rollResult.setText(""+face);
                    if(currentTurn >= numPlayers){
                        currentTurn = 0;
                    }

                    Player current = players.get(currentTurn);
                    int pos = current.getCurrentPosition();
                    player = (TextView) findViewById(R.id.player);
                    player.setText(current.getName());
                    Log.d(method, "*****IT IS " + current.getName().toUpperCase() + "'S TURN*****");
                    Log.d(method, "Current Position:" + pos + ", " + board.getSquare(pos).getName());

                    currentPosition = (TextView) findViewById(R.id.currentPosition);
                    currentPosition.setText("Position before dice roll " + pos + ", " +
                            board.getSquare(pos).getName());
                    convertTextToSpeech("Position before dice roll" + pos +
                            board.getSquare(pos).getName());

                    int newPos = pos+face;
                    if(newPos >= 40){
                        newPos = newPos-40;
                    }

                    current.setCurrentPosition(newPos);
                    convertTextToSpeech("You rolled a" + face);

                    updatedPosition = (TextView) findViewById(R.id.updatedPosition);
                    updatedPosition.setText("Updated Position:" + newPos + ", " +
                            board.getSquare(newPos).getName());
                    convertTextToSpeech("Position after dice roll" + newPos +
                            board.getSquare(newPos).getName());

                    locationType = (TextView) findViewById(R.id.locationType);

                    Square location = board.getSquare(newPos);
                    //String posType = board.getSquare(newPos).getClass().getName();
                    if(location instanceof PropertySquare){
                        locationType.setText("Landed on property");
                        buyProperty(current, location);
                    } else if(location instanceof CardSquare){
                        locationType.setText("Take a Card");
                    } else if(location instanceof SpecialSquare){
                        locationType.setText("Landed on special square");
                    }

                    currentTurn++;
                    if(currentTurn >= numPlayers){
                        currentTurn = 0;
                    }
                    convertTextToSpeech((players.get(currentTurn)).getName() +
                            "please roll the Dice");
                }
            });
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        game();
        //Game game = new Game();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");

        BluetoothManipulator.unregisterDiceScanningListener(scanningListener);
        DiceController.unregisterDiceConnectionListener(connectionListener);
        DiceController.unregisterDiceResponseListener(responseListener);

        DiceController.disconnectDie(dicePlus);
        dicePlus = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tts = new TextToSpeech(this, this);
    }

    public void game() {

        String method = "game";
        board = new Board();
        players = new ArrayList<>();
        currentTurn = 0;

        Player player1 = new Player("Casey");
        Player player2 = new Player("Jon");
        players.add(player1);
        players.add(player2);
        numPlayers = players.size();

        Log.d(method, "*****START GAME*****");
        Log.d(method, "numPlayers: " + numPlayers);

        Player current = players.get(currentTurn);
        int pos = current.getCurrentPosition();
        player = (TextView) findViewById(R.id.player);
        player.setText(current.getName());
        Log.d(method, "*****IT IS " + current.getName().toUpperCase() + "'S TURN*****");
        Log.d(method, "Current Position:" + pos + ", " + board.getSquare(pos).getName());

        currentPosition = (TextView) findViewById(R.id.currentPosition);
        currentPosition.setText("Current Position: " + pos + ", " + board.getSquare(pos).getName());
        convertTextToSpeech("Start Game" + players.get(currentTurn) + "please roll the dice");

        // Initiating
        BluetoothManipulator.initiate(this);
        DiceController.initiate(developerKey);

        // Every time the application starts it will search for a dice
        BluetoothManipulator.registerDiceScanningListener(scanningListener);
        DiceController.registerDiceConnectionListener(connectionListener);
        DiceController.registerDiceResponseListener(responseListener);

        BluetoothManipulator.startScan();

    }


    private void buyProperty(Player current, Square location) {
        int price = ((PropertySquare)location).getPrice();
        convertTextToSpeech("Would you like to buy" + ((PropertySquare)location).getName() + "for"
            + price);
        // voice recognition feature needed here
        //createSpeechRecognizer(this);
//        if(in.equals("yes")){
//            current.subtractMoney(price);
//            ((PropertySquare)location).setOwnedBy(current.getName());
//            System.out.println("You now own" + location.getName());
//        }
    }

    @Override
    public void onInit(int code) {
        if (code==TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.getDefault());
        } else {
            tts = null;
            Toast.makeText(this, "Failed to initialize TTS engine.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (tts!=null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
