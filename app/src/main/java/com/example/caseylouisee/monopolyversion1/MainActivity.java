package com.example.caseylouisee.monopolyversion1;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import us.dicepl.android.sdk.BluetoothManipulator;
import us.dicepl.android.sdk.DiceConnectionListener;
import us.dicepl.android.sdk.DiceController;
import us.dicepl.android.sdk.DiceResponseAdapter;
import us.dicepl.android.sdk.DiceResponseListener;
import us.dicepl.android.sdk.DiceScanningListener;
import us.dicepl.android.sdk.Die;
import us.dicepl.android.sdk.responsedata.RollData;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, RecognitionListener {

    public static final int[] developerKey = new int[]
            {0x5e, 0x77, 0x68, 0xd3, 0xc6, 0xa0, 0x17, 0x0a};
    private Die dicePlus;
    private static final String TAG = "DICEPlus";
    TextView player;
    TextView rollResult;
    TextView currentPosition;
    TextView updatedPosition;
    TextView locationType;
    TextView recognizedSpeech;
    TextView funds;

    int currentTurn;
    ArrayList<Player> players;
    int numPlayers;
    Board board;
    int jailCount;
    int freeParking;
    private TextToSpeech tts;

    private SpeechRecognizer speech = null;

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
            convertTextToSpeech("Dice+ Connected");
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
            convertTextToSpeech("Dice connection lost");
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

                    Player currentPlayer = players.get(currentTurn);
                    int pos = currentPlayer.getCurrentPosition();
                    player = (TextView) findViewById(R.id.player);
                    player.setText(currentPlayer.getName());
                    Log.d(method, "*****IT IS " + currentPlayer.getName().toUpperCase() + "'S TURN*****");
                    Log.d(method, "Current Position:" + pos + ", " + board.getSquare(pos).getName());
                    Log.d(method, "Current Funds: " + String.valueOf(currentPlayer.getMoney()));

                    currentPosition = (TextView) findViewById(R.id.currentPosition);
                    currentPosition.setText("Position before dice roll " + pos + ", " +
                            board.getSquare(pos).getName());

                    funds = (TextView) findViewById(R.id.funds);
                    funds.setText(String.valueOf(currentPlayer.getMoney()));

                    rollResult = (TextView) findViewById(R.id.rollResult);
                    rollResult.setText("" + face);

                    updatedPosition = (TextView) findViewById(R.id.updatedPosition);
                    updatedPosition.setText("Updated Position:");

                    locationType = (TextView) findViewById(R.id.locationType);
                    locationType.setText("");

                    currentPosition = (TextView) findViewById(R.id.currentPosition);
                    currentPosition.setText("Position before dice roll " + pos + ", " +
                            board.getSquare(pos).getName());

                    recognizedSpeech = (TextView) findViewById(R.id.recognizedSpeech);
                    recognizedSpeech.setText("");

                    if (currentPlayer.getJail() == true) {
                        if (face == 6) {
                            convertTextToSpeech("You rolled a" + face);
                            convertTextToSpeech("You are now free, resume normal play on next turn");
                            currentPlayer.setJail(false);
                            nextTurnRoll();
                        } else {
                            convertTextToSpeech("You rolled a" + face);
                            convertTextToSpeech("You did not roll a 6, serve your sentence");
                            jailCount++;
                            if(jailCount==3){
                                currentPlayer.setJail(false);
                                convertTextToSpeech("You have served your sentence. " +
                                        "Resume normal play on next turn");
                            }
                            nextTurnRoll();
                        }
                    } else {
                        convertTextToSpeech("Position before dice roll" + pos +
                                board.getSquare(pos).getName());

                        int newPos = pos + face;
                        if (newPos >= 40) {
                            newPos = newPos - 40;
                        }

                        currentPlayer.setCurrentPosition(newPos);

                        updatedPosition = (TextView) findViewById(R.id.updatedPosition);
                        updatedPosition.setText("Updated Position:" + newPos + ", " +
                                board.getSquare(newPos).getName());
                        convertTextToSpeech("You rolled a" + face);

                        convertTextToSpeech("Position after dice roll" + newPos +
                                board.getSquare(newPos).getName());

                        locationType = (TextView) findViewById(R.id.locationType);

                        Square location = board.getSquare(newPos);
                        //String posType = board.getSquare(newPos).getClass().getName();
                        if (location instanceof PropertySquare) {
                            locationType.setText("Landed on property");
                            buyProperty(currentPlayer, location);
                        } else if (location instanceof CardSquare) {
                            locationType.setText("Take a Card");
                            convertTextToSpeech("Take a card");
                            String locName = location.getName();
                            if(locName.equals("Chance")){
                                chance(currentPlayer);
                            } else {
                                communityChest(currentPlayer);
                            }
                            nextTurnRoll();
                        } else if (location instanceof SpecialSquare) {
                            locationType.setText("Landed on special square");
                            String locName = location.getName();
                            if(locName.equals("Go To Jail")){
                                setJail(currentPlayer);
                                nextTurnRoll();
                            } else if (locName.equals("Income Tax")){
                                currentPlayer.subtractMoney(100);
                                funds.setText(String.valueOf(currentPlayer.getMoney()));
                                convertTextToSpeech("100 has been deducted from your funds");
                                Log.d("income tax subtract 100", currentPlayer.getName() +
                                        String.valueOf(currentPlayer.getMoney()));
                                freeParking += 100;
                                Log.d("income tax", "Free parking:" + String.valueOf(freeParking));
                                nextTurnRoll();
                            } else if (locName.equals("Jail")){
                                convertTextToSpeech("Just visiting");
                                nextTurnRoll();
                            } else if (locName.equals("Super Tax")){
                                convertTextToSpeech("300 has been deducted from your funds");
                                currentPlayer.subtractMoney(300);
                                funds.setText(String.valueOf(currentPlayer.getMoney()));
                                Log.d("super tax subtract 300", currentPlayer.getName() +
                                        String.valueOf(currentPlayer.getMoney()));
                                freeParking += 300;
                                Log.d("super tax", "Free parking:" + String.valueOf(freeParking));
                                nextTurnRoll();
                            } else {
                                convertTextToSpeech("Special square");
                                nextTurnRoll();
                            }
                        }
                    }
                }
            });
        }
    };

    private void setJail(Player currentPlayer){
        currentPlayer.setCurrentPosition(10);
        currentPlayer.setJail(true);
    }

    private void chance(Player currentPlayer) {
        Random rand = new Random();
        int randomNum = rand.nextInt((3 - 1) + 1) + 1;

        if(randomNum == 1){
            convertTextToSpeech("Advance to Bond Street");
            currentPlayer.setCurrentPosition(34);
        }
        if(randomNum == 2){
            convertTextToSpeech("Unpaid charges. Go to jail");
            setJail(currentPlayer);
        }
        if(randomNum == 3){
            convertTextToSpeech("Build a rooftop swimming pool on your apartment, pay 300");
            currentPlayer.subtractMoney(300);
            funds.setText(String.valueOf(currentPlayer.getMoney()));
            freeParking += 300;
            Log.d("chance subtract 300", currentPlayer.getName() +
                    String.valueOf(currentPlayer.getMoney()));
            Log.d("chance free parking", String.valueOf(freeParking));
        }
    }

    private void communityChest(Player currentPlayer){
        Random rand = new Random();
        int randomNum = rand.nextInt((3 - 1) + 1) + 1;

        if(randomNum == 1){
            convertTextToSpeech("Your new business takes off, collect 200");

            Log.d("chance add 200", currentPlayer.getName() +
                    String.valueOf(currentPlayer.getMoney()));
            currentPlayer.addMoney(200);
            funds.setText(String.valueOf(currentPlayer.getMoney()));
            Log.d("chance add 200", currentPlayer.getName() +
                    String.valueOf(currentPlayer.getMoney()));
        }
        if(randomNum == 2){
            convertTextToSpeech("Your friend hires your villa for a week, " +
                    "collect 100 off the next player");

            Log.d("chance add 100", currentPlayer.getName() +
                    String.valueOf(currentPlayer.getMoney()));
            currentPlayer.addMoney(100);
            funds.setText(String.valueOf(currentPlayer.getMoney()));
            Log.d("chance add 100", currentPlayer.getName() +
                    String.valueOf(currentPlayer.getMoney()));

            Log.d("chance subtract 100", players.get(currentTurn+1).getName() +
                    String.valueOf(players.get(currentTurn+1).getMoney()));
            players.get(currentTurn+1).subtractMoney(100);
            Log.d("chance subtract 100", players.get(currentTurn + 1).getName() +
                    String.valueOf(players.get(currentTurn + 1).getMoney()));
        }
        if(randomNum == 3){
            convertTextToSpeech("You recieve a tax rebate, collect 300");

            Log.d("chance add 300", currentPlayer.getName() +
                    String.valueOf(currentPlayer.getMoney()));
            currentPlayer.addMoney(300);
            funds.setText(String.valueOf(currentPlayer.getMoney()));
            Log.d("chance add 300", currentPlayer.getName() +
                    String.valueOf(currentPlayer.getMoney()));
        }
    }

    private void nextTurnRoll() {
        Log.d("nextTurnRoll", "next player please roll");
        currentTurn++;
        if(currentTurn >= numPlayers){
            currentTurn = 0;
        }
        convertTextToSpeech(players.get(currentTurn).getName() + "please roll the dice");
    }

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
        convertTextToSpeech("Start Game" + players.get(currentTurn).getName() + "please roll the dice");

        // Initiating
        BluetoothManipulator.initiate(this);
        DiceController.initiate(developerKey);

        // Every time the application starts it will search for a dice
        BluetoothManipulator.registerDiceScanningListener(scanningListener);
        DiceController.registerDiceConnectionListener(connectionListener);
        DiceController.registerDiceResponseListener(responseListener);

        BluetoothManipulator.startScan();

    }

    private void buyProperty(Player currentPlayer, Square location) {
        String method = "buyProperty";
        recognizedSpeech = (TextView) findViewById(R.id.recognizedSpeech);
        funds = (TextView) findViewById(R.id.funds);
        PropertySquare square = (PropertySquare)location;

        recognizedSpeech.setText("");
        if(square.getOwned()==true){
            convertTextToSpeech(square.getOwnedBy() + "call rent");
            //rent is subtracted and added
            currentPlayer.subtractMoney(100);
            funds.setText(String.valueOf(currentPlayer.getMoney()));

            Log.d(method + "rent subtract money", currentPlayer.getName() +
                    String.valueOf(currentPlayer.getMoney()));

            String owner = square.getOwnedBy();
            for(int i = 0; i<numPlayers; i++){
                if(players.get(i).getName().equals(owner)){
                    players.get(i).addMoney(100);
                    Log.d(method + "rent add money", players.get(i).getName() +
                            String.valueOf(players.get(i).getMoney()));

                }
            }
            nextTurnRoll();

        } else {
            int price = ((PropertySquare) location).getPrice();
            convertTextToSpeech("Would you like to buy" + square.getName() + "for"
                    + price);
            Log.d(method, currentPlayer.getName() + String.valueOf(currentPlayer.getMoney()));

            while (tts.isSpeaking()) {
                speech = null;
            }
            speech = SpeechRecognizer.createSpeechRecognizer(this);
            speech.setRecognitionListener(this);
            speech.startListening(getIntent());

        }
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

    @Override
    public void onBeginningOfSpeech() {
        Log.i("SRL", "onBeginningOfSpeech");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i("SRL", "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i("SRL", "onEndOfSpeech");
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d("SRL", "FAILED " + errorMessage);
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        speech.startListening(getIntent());
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i("SRL", "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i("SRL", "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i("SRL", "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
        recognizedSpeech = (TextView) findViewById(R.id.recognizedSpeech);
        Log.i("SRL", "onResults");
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches)
            text += result + "\n";
        recognizedSpeech.setText(text);

        if (recognizedSpeech.getText().toString().contains("yes")) {
            PropertySquare square = (PropertySquare) (board.getSquare(players.get(currentTurn).getCurrentPosition()));
            players.get(currentTurn).subtractMoney(square.getPrice());
            square.setOwnedBy(players.get(currentTurn).getName());
            Log.d("buyProperty yes", square.getOwnedBy());
            Log.d("buyProperty yes", players.get(currentTurn).getName() +
                    String.valueOf(players.get(currentTurn).getMoney()));
            convertTextToSpeech("You now own" + square.getName());
            funds.setText(String.valueOf(players.get(currentTurn).getMoney()));

        }
        nextTurnRoll();
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i("SRL", "onRmsChanged: " + rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
}
