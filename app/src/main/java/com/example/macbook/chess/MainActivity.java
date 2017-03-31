/*
* Android Chess + Chat App
* by: Ryan Nichols and Douglas McKinley
* This application allows two players to play chess and chat with each other
* over a groupcast server
 */

package com.example.macbook.chess;

        import android.app.Activity;
        import android.os.AsyncTask;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.inputmethod.InputMethodManager;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ListView;
        import android.widget.Toast;

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStreamReader;
        import java.io.PrintWriter;
        import java.net.Socket;
        import java.net.UnknownHostException;
        import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    // TAG for logging
    private static final String TAG = "ChessActivity";

    // server to connect to
    protected static final int GROUPCAST_PORT = 20000;
    protected static final String GROUPCAST_SERVER = "52.41.197.210"; // AWS ubuntu server public IP

    // networking
    Socket socket = null;
    BufferedReader in = null;
    PrintWriter out = null;
    boolean connected = false;

    // UI elements
    Button board[][] = new Button[8][8];
    Button bConnect = null;
    EditText etName = null;
    Button sendButton = null;
    EditText sendText = null;

    //game logic variables
    String[][] boardState = new String[8][8]; // initialize in setupBoard()
    String mName; // my name - is set, but not used
    String mOpponent; // opponent's name - not set or used
    String mGroup; // groupcast group name of current game
    String mColor; // my chess piece color
    String mOppColor; // opponent chess piece color
    int fromX, fromY, toX, toY; // piece moves [fromX,fromY]->[toX,toY]
    boolean firstPress = false; // moves involve two button press sequence
    boolean mKingAlive = true; // for determining game over
    boolean oppKingAlive = true; // for determining game over


    // stuff for messaging
    ArrayList messages;
    ArrayAdapter adapter;
    ListView listView;

    // to be used only in onProgressUpdate() for purpose of joining
    // a groupcast group with name @game1, @game2, ..., @game<gameNum>
    static int gameNum = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // stuff for messaging
        messages = new ArrayList<String>();

        // second parameter is row layout,
        adapter = new ArrayAdapter<String>(this,R.layout.simple_listview,messages);
        listView = (ListView) findViewById(R.id.display);
        listView.setAdapter(adapter);

        // find UI elements defined in xml
        sendButton = (Button) this.findViewById(R.id.sendButton);
        sendText = (EditText) this.findViewById(R.id.sendText);

        bConnect = (Button) this.findViewById(R.id.bConnect);
        etName = (EditText) this.findViewById(R.id.etName);
        board[0][0] = (Button) this.findViewById(R.id.b00);
        board[0][1] = (Button) this.findViewById(R.id.b01);
        board[0][2] = (Button) this.findViewById(R.id.b02);
        board[0][3] = (Button) this.findViewById(R.id.b03);
        board[0][4] = (Button) this.findViewById(R.id.b04);
        board[0][5] = (Button) this.findViewById(R.id.b05);
        board[0][6] = (Button) this.findViewById(R.id.b06);
        board[0][7] = (Button) this.findViewById(R.id.b07);

        board[1][0] = (Button) this.findViewById(R.id.b10);
        board[1][1] = (Button) this.findViewById(R.id.b11);
        board[1][2] = (Button) this.findViewById(R.id.b12);
        board[1][3] = (Button) this.findViewById(R.id.b13);
        board[1][4] = (Button) this.findViewById(R.id.b14);
        board[1][5] = (Button) this.findViewById(R.id.b15);
        board[1][6] = (Button) this.findViewById(R.id.b16);
        board[1][7] = (Button) this.findViewById(R.id.b17);

        board[2][0] = (Button) this.findViewById(R.id.b20);
        board[2][1] = (Button) this.findViewById(R.id.b21);
        board[2][2] = (Button) this.findViewById(R.id.b22);
        board[2][3] = (Button) this.findViewById(R.id.b23);
        board[2][4] = (Button) this.findViewById(R.id.b24);
        board[2][5] = (Button) this.findViewById(R.id.b25);
        board[2][6] = (Button) this.findViewById(R.id.b26);
        board[2][7] = (Button) this.findViewById(R.id.b27);

        board[3][0] = (Button) this.findViewById(R.id.b30);
        board[3][1] = (Button) this.findViewById(R.id.b31);
        board[3][2] = (Button) this.findViewById(R.id.b32);
        board[3][3] = (Button) this.findViewById(R.id.b33);
        board[3][4] = (Button) this.findViewById(R.id.b34);
        board[3][5] = (Button) this.findViewById(R.id.b35);
        board[3][6] = (Button) this.findViewById(R.id.b36);
        board[3][7] = (Button) this.findViewById(R.id.b37);

        board[4][0] = (Button) this.findViewById(R.id.b40);
        board[4][1] = (Button) this.findViewById(R.id.b41);
        board[4][2] = (Button) this.findViewById(R.id.b42);
        board[4][3] = (Button) this.findViewById(R.id.b43);
        board[4][4] = (Button) this.findViewById(R.id.b44);
        board[4][5] = (Button) this.findViewById(R.id.b45);
        board[4][6] = (Button) this.findViewById(R.id.b46);
        board[4][7] = (Button) this.findViewById(R.id.b47);

        board[5][0] = (Button) this.findViewById(R.id.b50);
        board[5][1] = (Button) this.findViewById(R.id.b51);
        board[5][2] = (Button) this.findViewById(R.id.b52);
        board[5][3] = (Button) this.findViewById(R.id.b53);
        board[5][4] = (Button) this.findViewById(R.id.b54);
        board[5][5] = (Button) this.findViewById(R.id.b55);
        board[5][6] = (Button) this.findViewById(R.id.b56);
        board[5][7] = (Button) this.findViewById(R.id.b57);

        board[6][0] = (Button) this.findViewById(R.id.b60);
        board[6][1] = (Button) this.findViewById(R.id.b61);
        board[6][2] = (Button) this.findViewById(R.id.b62);
        board[6][3] = (Button) this.findViewById(R.id.b63);
        board[6][4] = (Button) this.findViewById(R.id.b64);
        board[6][5] = (Button) this.findViewById(R.id.b65);
        board[6][6] = (Button) this.findViewById(R.id.b66);
        board[6][7] = (Button) this.findViewById(R.id.b67);

        board[7][0] = (Button) this.findViewById(R.id.b70);
        board[7][1] = (Button) this.findViewById(R.id.b71);
        board[7][2] = (Button) this.findViewById(R.id.b72);
        board[7][3] = (Button) this.findViewById(R.id.b73);
        board[7][4] = (Button) this.findViewById(R.id.b74);
        board[7][5] = (Button) this.findViewById(R.id.b75);
        board[7][6] = (Button) this.findViewById(R.id.b76);
        board[7][7] = (Button) this.findViewById(R.id.b77);


        //TODO hide chat
        hideChat();

        // hide login controls
        hideLoginControls();

        // make the board non-clickable
        disableBoardClick();

        // hide the board
        hideBoard();

        // assign OnClickListener to connect button
        bConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String name = etName.getText().toString();
                // sanity check: make sure that the name does not start with an @ character
                if (name == null || name.startsWith("@")) {
                    Toast.makeText(getApplicationContext(), "Invalid name",
                            Toast.LENGTH_SHORT).show();
                } else {
                    mName = etName.getText().toString();
                    send("NAME,"+etName.getText());
                }
            }
        });


        // assign a common OnClickListener to all board buttons
        View.OnClickListener boardClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int x, y;
                switch (v.getId()) {
                    case R.id.b00:
                        x = 0;
                        y = 0;
                        break;
                    case R.id.b01:
                        x = 0;
                        y = 1;
                        break;
                    case R.id.b02:
                        x = 0;
                        y = 2;
                        break;
                    case R.id.b03:
                        x = 0;
                        y = 3;
                        break;
                    case R.id.b04:
                        x = 0;
                        y = 4;
                        break;
                    case R.id.b05:
                        x = 0;
                        y = 5;
                        break;
                    case R.id.b06:
                        x = 0;
                        y = 6;
                        break;
                    case R.id.b07:
                        x = 0;
                        y = 7;
                        break;

                    case R.id.b10:
                        x = 1;
                        y = 0;
                        break;
                    case R.id.b11:
                        x = 1;
                        y = 1;
                        break;
                    case R.id.b12:
                        x = 1;
                        y = 2;
                        break;
                    case R.id.b13:
                        x = 1;
                        y = 3;
                        break;
                    case R.id.b14:
                        x = 1;
                        y = 4;
                        break;
                    case R.id.b15:
                        x = 1;
                        y = 5;
                        break;
                    case R.id.b16:
                        x = 1;
                        y = 6;
                        break;
                    case R.id.b17:
                        x = 1;
                        y = 7;
                        break;

                    case R.id.b20:
                        x = 2;
                        y = 0;
                        break;
                    case R.id.b21:
                        x = 2;
                        y = 1;
                        break;
                    case R.id.b22:
                        x = 2;
                        y = 2;
                        break;
                    case R.id.b23:
                        x = 2;
                        y = 3;
                        break;
                    case R.id.b24:
                        x = 2;
                        y = 4;
                        break;
                    case R.id.b25:
                        x = 2;
                        y = 5;
                        break;
                    case R.id.b26:
                        x = 2;
                        y = 6;
                        break;
                    case R.id.b27:
                        x = 2;
                        y = 7;
                        break;

                    case R.id.b30:
                        x = 3;
                        y = 0;
                        break;
                    case R.id.b31:
                        x = 3;
                        y = 1;
                        break;
                    case R.id.b32:
                        x = 3;
                        y = 2;
                        break;
                    case R.id.b33:
                        x = 3;
                        y = 3;
                        break;
                    case R.id.b34:
                        x = 3;
                        y = 4;
                        break;
                    case R.id.b35:
                        x = 3;
                        y = 5;
                        break;
                    case R.id.b36:
                        x = 3;
                        y = 6;
                        break;
                    case R.id.b37:
                        x = 3;
                        y = 7;
                        break;

                    case R.id.b40:
                        x = 4;
                        y = 0;
                        break;
                    case R.id.b41:
                        x = 4;
                        y = 1;
                        break;
                    case R.id.b42:
                        x = 4;
                        y = 2;
                        break;
                    case R.id.b43:
                        x = 4;
                        y = 3;
                        break;
                    case R.id.b44:
                        x = 4;
                        y = 4;
                        break;
                    case R.id.b45:
                        x = 4;
                        y = 5;
                        break;
                    case R.id.b46:
                        x = 4;
                        y = 6;
                        break;
                    case R.id.b47:
                        x = 4;
                        y = 7;
                        break;

                    case R.id.b50:
                        x = 5;
                        y = 0;
                        break;
                    case R.id.b51:
                        x = 5;
                        y = 1;
                        break;
                    case R.id.b52:
                        x = 5;
                        y = 2;
                        break;
                    case R.id.b53:
                        x = 5;
                        y = 3;
                        break;
                    case R.id.b54:
                        x = 5;
                        y = 4;
                        break;
                    case R.id.b55:
                        x = 5;
                        y = 5;
                        break;
                    case R.id.b56:
                        x = 5;
                        y = 6;
                        break;
                    case R.id.b57:
                        x = 5;
                        y = 7;
                        break;

                    case R.id.b60:
                        x = 6;
                        y = 0;
                        break;
                    case R.id.b61:
                        x = 6;
                        y = 1;
                        break;
                    case R.id.b62:
                        x = 6;
                        y = 2;
                        break;
                    case R.id.b63:
                        x = 6;
                        y = 3;
                        break;
                    case R.id.b64:
                        x = 6;
                        y = 4;
                        break;
                    case R.id.b65:
                        x = 6;
                        y = 5;
                        break;
                    case R.id.b66:
                        x = 6;
                        y = 6;
                        break;
                    case R.id.b67:
                        x = 6;
                        y = 7;
                        break;

                    case R.id.b70:
                        x = 7;
                        y = 0;
                        break;
                    case R.id.b71:
                        x = 7;
                        y = 1;
                        break;
                    case R.id.b72:
                        x = 7;
                        y = 2;
                        break;
                    case R.id.b73:
                        x = 7;
                        y = 3;
                        break;
                    case R.id.b74:
                        x = 7;
                        y = 4;
                        break;
                    case R.id.b75:
                        x = 7;
                        y = 5;
                        break;
                    case R.id.b76:
                        x = 7;
                        y = 6;
                        break;
                    case R.id.b77:
                        x = 7;
                        y = 7;
                        break;

                    default: // shouldn't occur
                        x = 0;
                        y = 0;
                        break;
                }

                // check if piece is yours to move
                if (firstPress == false ) {
                    fromX = x;
                    fromY = y;
                    if (boardState[fromY][fromX].startsWith("m")){
                        firstPress = true;
                        // TODO highlight piece to move
                    }
                }
                // second press, attempt move
                else if (firstPress == true){
                    disableBoardClick();
                    firstPress = false;
                    toX = x;
                    toY = y;
                    if (fromX == toX && fromY == toY){ // cancel attempted move
                        enableBoardClick();
                    }
                    else if(attemptMove(fromX,fromY,toX,toY)){
                        checkGameOver();

                    }
                    else { // move failed, try another move
                        enableBoardClick();
                    }
                }

            }
        };

        // assign OnClickListeners to board buttons
        for (int x = 0; x < 8; x++)
            for (int y = 0; y < 8; y++)
                board[x][y].setOnClickListener(boardClickListener);

        // assign OnClickListener to send (message) button
        // when button is clicked, read send string from (EditText) sendText
        // and send message string to opponent
        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String message = sendText.getText().toString();
                sendText.setText("");
                //Hide Keyboard
                //InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                //imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,0);
                // sanitity check: make sure that the message isn't empty
                if (message == null) {
                    Toast.makeText(getApplicationContext(), "Invalid message",
                            Toast.LENGTH_SHORT).show();
                } else {
                    send("MSG,"+mGroup+",CHAT:"+message);
                    adapter.add("me: "+message);
                }
            }
        });

        // start the AsyncTask that connects to the server
        // and listens to whatever the server is sending to us
        connect();

    }


    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy called");
        disconnect();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle menu click events
        if (item.getItemId() == R.id.exit) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chess, menu);
        return true;
    }




    /***************************************************************************/
    /********* Networking ******************************************************/
    /***************************************************************************/

    /**
     * Connect to the server. This method is safe to call from the UI thread.
     */
    void connect() {

        new AsyncTask<Void, Void, String>() {

            String errorMsg = null;

            @Override
            protected String doInBackground(Void... args) {
                Log.i(TAG, "Connect task started");
                try {
                    connected = false;
                    socket = new Socket(GROUPCAST_SERVER, GROUPCAST_PORT);
                    Log.i(TAG, "Socket created");
                    in = new BufferedReader(new InputStreamReader(
                            socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream());

                    connected = true;
                    Log.i(TAG, "Input and output streams ready");

                } catch (UnknownHostException e1) {
                    errorMsg = e1.getMessage();
                } catch (IOException e1) {
                    errorMsg = e1.getMessage();
                    try {
                        if (out != null) {
                            out.close();
                        }
                        if (socket != null) {
                            socket.close();
                        }
                    } catch (IOException ignored) {
                    }
                }
                Log.i(TAG, "Connect task finished");
                return errorMsg;
            }

            @Override
            protected void onPostExecute(String errorMsg) {
                if (errorMsg == null) {
                    Toast.makeText(getApplicationContext(),
                            "Connected to server", Toast.LENGTH_SHORT).show();

                    hideConnectingText();
                    showLoginControls();

                    // start receiving
                    receive();

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                    // can't connect: close the activity
                    finish();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Start receiving one-line messages over the TCP connection. Received lines are
     * handled in the onProgressUpdate method which runs on the UI thread.
     * This method is automatically called after a connection has been established.
     */

    void receive() {
        new AsyncTask<Void, String, Void>() {

            @Override
            protected Void doInBackground(Void... args) {
                Log.i(TAG, "Receive task started");
                try {
                    while (connected) {

                        String msg = in.readLine();

                        if (msg == null) { // other side closed the
                            // connection
                            break;
                        }
                        publishProgress(msg);
                    }

                } catch (UnknownHostException e1) {
                    Log.i(TAG, "UnknownHostException in receive task");
                } catch (IOException e1) {
                    Log.i(TAG, "IOException in receive task");
                } finally {
                    connected = false;
                    try {
                        if (out != null)
                            out.close();
                        if (socket != null)
                            socket.close();
                    } catch (IOException e) {
                    }
                }
                Log.i(TAG, "Receive task finished");
                return null;
            }

            @Override
            protected void onProgressUpdate(String... lines) {
                // the message received from the server is
                // guaranteed to be not null
                String msg = lines[0];
                ListView display = (ListView) findViewById(R.id.display);

                // TODO: act on messages received from the server
                if(msg.startsWith("+OK,NAME")) {
                    hideLoginControls();
                    showBoard();
                    //Hide keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    //attempt group joins until available group found
                    send("JOIN,@game1,2");
                    return;
                }

                if(msg.startsWith("+ERROR,NAME")) {
                    Toast.makeText(getApplicationContext(), msg.substring("+ERROR,NAME,".length()), Toast.LENGTH_SHORT).show();
                    return;
                }
                //either creates new group, or joins existing group, both capacity 2
                if(msg.startsWith("+OK,JOIN")){
                    mGroup = "@game" + gameNum;
                    //process msg
                    String numInGroup = msg.substring(msg.indexOf("(")+1);
                    // if group has 1 person, wait for challenger
                    if (numInGroup.startsWith("1")){
                        mColor = "white";
                        mOppColor = "black";
                        setupBoard();
                        // user waits until someone joins group and sends move
                        return;
                    }
                    // if group has 2 person, start game
                    else if (numInGroup.startsWith("2")){
                        mColor = "black";
                        mOppColor = "white";
                        // TODO: send my name
                        send("MSG," + mGroup + ",NAME:" + mName);
                        showChat();
                        setupBoard();
                        //start your move... opponent will get message
                        enableBoardClick();
                        return;
                    }
                    return;
                }

                // this will happen only if group is full -> try next group
                if(msg.startsWith("+ERROR,JOIN")){
                    ++gameNum;
                    send("JOIN,@game" + gameNum + ",2");
                    return;
                }

                // opponent disconnected, game crashes
                if(msg.startsWith("+ERROR,MSG,")){
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                    return;
                }

                //message sent successfully
                if(msg.startsWith("+OK,MSG," + mGroup)){
                    // simply wait for turn again
                    return;
                }

                //received opponents move
                if(msg.startsWith("+MSG,")){
                    //split message into array to make stuff eaisier
                    String splitMsg[] = msg.split(",");
                    //are we a move or a chat?
                    Log.i(TAG, "split string 3: "+splitMsg[3]);
                    if(splitMsg[3].startsWith("CHAT:")){
                        //display recieved line in display textview
                        String receivedMsg = splitMsg[3].substring(5);
                        adapter.add("opponent: "+receivedMsg);
                        return;
                    }else if(splitMsg[3].startsWith("NAME:")){
                        // we know opponent has connected, show chat
                        String receivedMsg = splitMsg[3].substring(5);
                        mOpponent = receivedMsg;
                        // TODO show chat controls
                        showChat();
                    } else {
                        int fX = Character.getNumericValue(msg.charAt(msg.length() - 4));
                        int fY = Character.getNumericValue(msg.charAt(msg.length() - 3));
                        int tX = Character.getNumericValue(msg.charAt(msg.length() - 2));
                        int tY = Character.getNumericValue(msg.charAt(msg.length() - 1));

                        //update board
                        swapPieces(fX, fY, tX, tY);

                        //check for game win/loss
                        checkGameOver();
                    }

                    // my turn: turn on clicks, unless game is over
                    if (!checkGameOver()) {
                        enableBoardClick();
                    }
                    return;
                }

                // if we haven't returned yet, tell the user that we have an unhandled message
                Toast.makeText(getApplicationContext(), "Unhandled message: "+msg, Toast.LENGTH_SHORT).show();
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    /*
    setupBoard()
    * this fills out the boardState matrix with characters representing pieces
    * your pieces are "mR", "mP", etc. (rook, pawn)
    * opponents pieces are "oR", "oP", etc.
    * also, calls drawBoard to set up GUI chessboard
    *
    * note: y is row, x is column  in boardState[y][x] for simplicity
    * bottom left corner of board is (0,0), top right is (7,7)
    * e.g. top left would be boardState[0][7]
     */
    void setupBoard(){
        int x, y;

        // fill in middle rows with "-"
        for (y = 2; y < 6; y++) {
            for (x = 0; x < 8; x++) {
                boardState[y][x] = "-";
            }
        }

        // fill in pawns
        for (x = 0; x < 8; x++){
            boardState[1][x] = "mP";
            boardState[6][x] = "oP";
        }

        // fill in back rows -> queen/king position change based on color
        String[] myBackRow;
        String[] opponentBackRow;
        if (mColor == "white"){ // queen left of king viewed from white
            myBackRow = new String[]{"mR1","mN","mB","mQ","mK","mB","mN","mR2"};
            opponentBackRow = new String[]{"oR","oN","oB","oQ","oK","oB","oN","oR"};
        } else { // queen to right of king viewed from black
            myBackRow = new String[]{"mR1","mN","mB","mK","mQ","mB","mN","mR2"};
            opponentBackRow = new String[]{"oR","oN","oB","oK","oQ","oB","oN","oR"};
        }
        boardState[0] = myBackRow;
        boardState[7] = opponentBackRow;

        drawBoard();
    }

    /*
    drawBoard()
    * set images for every position on board
     */
    void drawBoard(){
        for (int y = 0; y < 8; y++){
            for (int x = 0; x < 8; x++){
                drawPiece(y, x);
            }
        }
    }

    /*
    drawPiece(int y, int x)
    * set image on button at row y, column x
     */
    void drawPiece(int y, int x){
        String piece = boardState[y][x];

        if (mColor == "white") {
            switch (piece) {
                case "-":
                    board[x][y].setBackgroundResource(android.R.drawable.btn_default);
                    break;
                case "mR1":
                case "mR2":
                    board[x][y].setBackgroundResource(R.drawable.rook_white);
                    break;
                case "mB":
                    board[x][y].setBackgroundResource(R.drawable.bishop_white);
                    break;
                case "mN":
                    board[x][y].setBackgroundResource(R.drawable.knight_white);
                    break;
                case "mK":
                    board[x][y].setBackgroundResource(R.drawable.king_white);
                    break;
                case "mQ":
                    board[x][y].setBackgroundResource(R.drawable.queen_white);
                    break;
                case "mP":
                    //board[x][y].setBackgroundColor(0xFF00FF00);
                    board[x][y].setBackgroundResource(R.drawable.pawn_white);
                    break;

                case "oR":
                    board[x][y].setBackgroundResource(R.drawable.rook_black);
                    break;
                case "oB":
                    board[x][y].setBackgroundResource(R.drawable.bishop_black);
                    break;
                case "oN":
                    board[x][y].setBackgroundResource(R.drawable.knight_black);
                    break;
                case "oK":
                    board[x][y].setBackgroundResource(R.drawable.king_black);
                    break;
                case "oQ":
                    board[x][y].setBackgroundResource(R.drawable.queen_black);
                    break;
                case "oP":
                    board[x][y].setBackgroundResource(R.drawable.pawn_black);
                    break;
                default:
                    break; // should not happen
            }
        } else {
            switch (piece) {
                case "-":
                    board[x][y].setBackgroundResource(android.R.drawable.btn_default);
                    break;
                case "mR1":
                case "mR2":
                    board[x][y].setBackgroundResource(R.drawable.rook_black);
                    break;
                case "mB":
                    board[x][y].setBackgroundResource(R.drawable.bishop_black);
                    break;
                case "mN":
                    board[x][y].setBackgroundResource(R.drawable.knight_black);
                    break;
                case "mK":
                    board[x][y].setBackgroundResource(R.drawable.king_black);
                    break;
                case "mQ":
                    board[x][y].setBackgroundResource(R.drawable.queen_black);
                    break;
                case "mP":
                    board[x][y].setBackgroundResource(R.drawable.pawn_black);
                    break;

                case "oR":
                    board[x][y].setBackgroundResource(R.drawable.rook_white);
                    break;
                case "oB":
                    board[x][y].setBackgroundResource(R.drawable.bishop_white);
                    break;
                case "oN":
                    board[x][y].setBackgroundResource(R.drawable.knight_white);
                    break;
                case "oK":
                    board[x][y].setBackgroundResource(R.drawable.king_white);
                    break;
                case "oQ":
                    board[x][y].setBackgroundResource(R.drawable.queen_white);
                    break;
                case "oP":
                    board[x][y].setBackgroundResource(R.drawable.pawn_white);
                    break;
                default:
                    break; // should not happen
            }
        }
    }

    /*
    attemptMove()
    * if move is possible, updates board state/gui and sends move to opponent
    *
     */
    boolean attemptMove(int fX,int fY,int tX,int tY){
        if (canMove(fX,fY,tX,tY)){
            //update board state and gui
            swapPieces(fX,fY,tX,tY);
            //align coordinates to opponent's board and send move
            int alignFX = 7 - fX;
            int alignFY = 7 - fY;
            int alignTX = 7 - tX;
            int alignTY = 7 - tY;
            send("MSG," + mGroup + "," + alignFX + alignFY + alignTX + alignTY);
            return true;
        }
        return false;
    }

    /*
    canMove()
    * checks if desired move is legal
    * assumes (fX,fY) refers to a piece of mine
    * assumes (tX,tY) does not refer to same piece i.e. (fX,fY)!=(tX,tY)
     */
    boolean canMove(int fX,int fY,int tX,int tY){
        String myPiece = boardState[fY][fX];
        boolean isLegal = false;
        int yMin = Math.min(fY,tY);
        int yMax = Math.max(fY,tY);
        int xMin = Math.min(fX,tX);
        int xMax = Math.max(fX,tX);
        int x; // for checking necessary spaces
        int y; // for checking necessary spaces

        boolean isHorizontal = (fY == tY);
        boolean isVertical = (fX == tX);
        boolean isDiagonal = (yMax-yMin)==(xMax-xMin);
        boolean isDiagonalUpRight = (isDiagonal && (tY-fY==tX-fX));
        boolean isDiagonalUpLeft = (isDiagonal && (tY-fY==fX-tX));

        if (boardState[tY][tX].startsWith("m")){
            // TODO edit to allow castling (swap rook/king); WARNING affects swapPieces!
            return false;
        }

        switch(myPiece){
            //rook
            case "mR1":
            case "mR2":
                if (!(isVertical || isHorizontal)){
                    isLegal = false;
                }
                else if (isVertical) {
                    x = fX;
                    for (y = yMin + 1; y < yMax; y++){
                        if (!boardState[y][x].equals("-")){
                            isLegal = false;
                            return isLegal;
                        }
                    }
                    isLegal = true;
                }
                else if (isHorizontal) {
                    y = fY;
                    for (x = xMin + 1; x < xMax; x++){
                        if (!boardState[y][x].equals("-")){
                            isLegal = false;
                            return isLegal;
                        }
                    }
                    isLegal = true;
                }
                break;

            //bishop
            case "mB":
                if (!isDiagonal){
                    isLegal = false;
                }
                else if(isDiagonalUpRight){
                    for (x = xMin + 1, y = yMin + 1; y < yMax; x++, y++){
                        if (!boardState[y][x].equals("-")){
                            isLegal = false;
                            return isLegal;
                        }
                    }
                    isLegal = true;
                }
                else if(isDiagonalUpLeft){
                    for (x = xMax - 1, y = yMin + 1; y < yMax; x--, y++){
                        if (!boardState[y][x].equals("-")){
                            isLegal = false;
                            return isLegal;
                        }
                    }
                    isLegal = true;
                }
                break;

            //knight
            case "mN":
                if (xMax - xMin == 2){
                    isLegal = (yMax - yMin == 1);
                } else if (yMax - yMin == 2){
                    isLegal = (xMax - xMin == 1);
                }
                else{
                    isLegal = false;
                }
                break;

            //queen
            case "mQ":
                if (!(isDiagonal || isHorizontal || isVertical)) {
                    isLegal = false;
                }
                else if (isVertical) {
                    x = fX;
                    for (y = yMin + 1; y < yMax; y++){
                        if (!boardState[y][x].equals("-")){
                            isLegal = false;
                            return isLegal;
                        }
                    }
                    isLegal = true;
                }
                else if (isHorizontal) {
                    y = fY;
                    for (x = xMin + 1; x < xMax; x++){
                        if (!boardState[y][x].equals("-")){
                            isLegal = false;
                            return isLegal;
                        }
                    }
                    isLegal = true;
                }
                else if(isDiagonalUpRight){
                    for (x = xMin + 1, y = yMin + 1; y < yMax; x++, y++){
                        if (!boardState[y][x].equals("-")){
                            isLegal = false;
                            return isLegal;
                        }
                    }
                    isLegal = true;
                }
                else if(isDiagonalUpLeft){
                    for (x = xMax - 1, y = yMin + 1; y < yMax; x--, y++){
                        if (!boardState[y][x].equals("-")){
                            isLegal = false;
                            return isLegal;
                        }
                    }
                    isLegal = true;
                }
                break;

            //king
            case "mK":
                isLegal = !((yMax-yMin > 1) || (xMax-xMin > 1));
                break;

            //pawn
            case "mP":
                if (fX == tX){ // stay in column
                    x = fX;
                    if (tY - fY == 1){// move one
                        isLegal = boardState[tY][x].equals("-");
                    }
                    else if (tY - fY == 2 && fY == 1){
                        isLegal = boardState[tY][x].equals("-") && boardState[tY-1][x].equals("-");
                    }
                    else{
                        isLegal = false;
                    }
                }
                else if (tY - fY == 1 && xMax - xMin == 1) {
                    isLegal = boardState[tY][tX].startsWith("o");
                }
                else{
                    isLegal = false;
                }
                break;

            default:
                isLegal = false;
                break;
        }



        return isLegal;
    }

    /*
    swapPieces()
    * swaps two pieces in boardState[y][x] and updates gui
    * note: if (tX,tY) refers to opponent piece, replace with empty space
    * TODO edit this and canMove() to allow castling
     */
    void swapPieces(int fX,int fY,int tX,int tY){
        if (boardState[tY][tX]=="oK"){
            oppKingAlive = false;
        }else if(boardState[tY][tX]=="mK"){
            mKingAlive = false;
        }
        boardState[tY][tX] = boardState[fY][fX];
        boardState[fY][fX] = "-";//temp;
        drawPiece(fY,fX);
        drawPiece(tY,tX);
    }

    /*
    *checkGameOver()
    * checks board state to see if someone has won
    * simply checks to see if either king is dead
    * TODO: expand to actual chess rules of check/checkmate
     */
    boolean checkGameOver(){
        boolean gameOver;
        if (oppKingAlive == false){
            Toast.makeText(getApplicationContext(), "You win",
                    Toast.LENGTH_SHORT).show();
            gameOver = true;
        } else if (mKingAlive == false) {
            Toast.makeText(getApplicationContext(), "You lose",
                    Toast.LENGTH_SHORT).show();
            gameOver = true;
        } else {
            gameOver = false;
        }
        return gameOver;
    }


    //-----------END of game logic helper functions ------------


    /**
     * Disconnect from the server
     */
    void disconnect() {
        new Thread() {
            @Override
            public void run() {
                if (connected) {
                    connected = false;
                }
                // make sure that we close the output, not the input
                if (out != null) {
                    out.print("BYE");
                    out.flush();
                    out.close();
                }
                // in some rare cases, out can be null, so we need to close the socket itself
                if (socket != null)
                    try { socket.close();} catch(IOException ignored) {}

                Log.i(TAG, "Disconnect task finished");
            }
        }.start();
    }

    /**
     * Send a one-line message to the server over the TCP connection. This
     * method is safe to call from the UI thread.
     *
     * @param msg
     *            The message to be sent.
     * @return true if sending was successful, false otherwise
     */
    boolean send(String msg) {
        if (!connected) {
            Log.i(TAG, "can't send: not connected");
            return false;
        }

        new AsyncTask<String, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(String... msg) {
                Log.i(TAG, "sending: " + msg[0]);
                out.println(msg[0]);
                return out.checkError();
            }

            @Override
            protected void onPostExecute(Boolean error) {
                if (!error) {
                    Toast.makeText(getApplicationContext(),
                            "Message sent to server", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Error sending message to server",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, msg);

        return true;
    }

    /***************************************************************************/
    /***** UI related methods **************************************************/
    /***************************************************************************/

    /**
     * Hide the "connecting to server" text
     */
    void hideConnectingText() {
        findViewById(R.id.tvConnecting).setVisibility(View.GONE);
    }

    /**
     * Show the "connecting to server" text
     */
    void showConnectingText() {
        findViewById(R.id.tvConnecting).setVisibility(View.VISIBLE);
    }

    /**
     * Hide the login controls
     */
    void hideLoginControls() {
        findViewById(R.id.llLoginControls).setVisibility(View.GONE);
    }

    /**
     * Show the login controls
     */
    void showLoginControls() {
        findViewById(R.id.llLoginControls).setVisibility(View.VISIBLE);
    }

    /**
     * Hide the tictactoe board
     */
    void hideBoard() {
        findViewById(R.id.llBoard).setVisibility(View.GONE);
    }

    /**
     * Show the tictactoe board
     */
    void showBoard() {
        findViewById(R.id.llBoard).setVisibility(View.VISIBLE);
    }

    void hideChat(){
        findViewById(R.id.sendButton).setVisibility(View.GONE);
        findViewById(R.id.sendText).setVisibility(View.GONE);
        findViewById(R.id.display).setVisibility(View.GONE);
    }

    void showChat(){
        findViewById(R.id.sendButton).setVisibility(View.VISIBLE);
        findViewById(R.id.sendText).setVisibility(View.VISIBLE);
        findViewById(R.id.display).setVisibility(View.VISIBLE);
    }


    /**
     * Make the chess board clickable
     */
    void enableBoardClick() {
        for (int x = 0; x < 8; x++)
            for (int y = 0; y < 8; y++)
                if ("".equals(board[x][y].getText().toString()))
                    board[x][y].setEnabled(true);
    }

    /**
     * Make the chess board non-clickable
     */
    void disableBoardClick() {
        for (int x = 0; x < 8; x++)
            for (int y = 0; y < 8; y++)
                board[x][y].setEnabled(false);
    }



}