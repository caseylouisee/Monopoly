package com.example.caseylouisee.monopolyversion1;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import us.dicepl.android.sdk.BluetoothManipulator;
import us.dicepl.android.sdk.DiceConnectionListener;
import us.dicepl.android.sdk.DiceController;
import us.dicepl.android.sdk.DiceResponseAdapter;
import us.dicepl.android.sdk.DiceResponseListener;
import us.dicepl.android.sdk.DiceScanningListener;
import us.dicepl.android.sdk.Die;
import us.dicepl.android.sdk.responsedata.RollData;

public class MainActivity extends AppCompatActivity {

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

//    TextToSpeech textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
//        @Override
//        public void onInit(int status) {
//            if (status == TextToSpeech.SUCCESS) {
//                int result = textToSpeech.setLanguage(Locale.US);
//                if (result == TextToSpeech.LANG_MISSING_DATA
//                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                    Log.e("error", "This Language is not supported");
//                } else {
//                    Log.d("onInit", "TextToSpeech initialized");
//                }
//            } else {
//                Log.e("error", "Initilization Failed!");
//            }
//        }
//    });
//
//    private void convertTextToSpeech(String text) {
//        if (null == text || "".equals(text)) {
//            text = "Please give some input.";
//        }
//        //textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "speech");
//        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
//    }


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

                    Log.d(method, "Name:" + current.getName());
                    Log.d(method, "Current Position:" + pos + ", " + board.getSquare(pos).getName());

                    currentPosition = (TextView) findViewById(R.id.currentPosition);
                    currentPosition.setText("Current Position: " + pos + ", " + board.getSquare(pos).getName());

                    int newPos = pos+face;
                    if(newPos >= 40){
                        newPos = newPos-40;
                    }

                    current.setCurrentPosition(newPos);

                    updatedPosition = (TextView) findViewById(R.id.updatedPosition);
                    updatedPosition.setText("Updated Position:" + newPos + ", " + board.getSquare(newPos).getName());


                    locationType = (TextView) findViewById(R.id.locationType);

                    Square location = board.getSquare(newPos);
                    //String posType = board.getSquare(newPos).getClass().getName();
                    if(location instanceof PropertySquare){
                        locationType.setText("Landed on property");
                        //buyProperty(current, location);
                    } else if(location instanceof CardSquare){
                        locationType.setText("Take a Card");
                    } else if(location instanceof SpecialSquare){
                        locationType.setText("Landed on special square");
                    }

                    currentTurn++;
                    //convertTextToSpeech(players.get(currentTurn) + "please roll the Dice");
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

        Log.d(method, "Name:" + current.getName());
        Log.d(method, "Current Position:" + pos + ", " + board.getSquare(pos).getName());

        currentPosition = (TextView) findViewById(R.id.currentPosition);
        currentPosition.setText("Current Position: " + pos + ", " + board.getSquare(pos).getName());
        //convertTextToSpeech(players.get(currentTurn) + "please roll the Dice");


        // Initiating
        BluetoothManipulator.initiate(this);
        DiceController.initiate(developerKey);

        // Every time the application starts it will search for a dice
        BluetoothManipulator.registerDiceScanningListener(scanningListener);
        DiceController.registerDiceConnectionListener(connectionListener);
        DiceController.registerDiceResponseListener(responseListener);

        BluetoothManipulator.startScan();

    }
}
