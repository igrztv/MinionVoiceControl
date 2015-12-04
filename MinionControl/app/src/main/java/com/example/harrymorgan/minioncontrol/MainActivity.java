package com.example.harrymorgan.minioncontrol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import android.content.ServiceConnection;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import android.speech.SpeechRecognizer;

import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

    static final int SocketServerPORT = 53000;

    LinearLayout loginPanel, chatPanel;

    EditText editTextUserName, editTextAddress;
    Button buttonConnect;
    TextView chatMsg, textPort;

    EditText editTextSay;
    Button buttonSend;
    Button buttonDisconnect;

    String msgLog = "";

    ChatClientThread chatClientThread = null;

    private static final int REQUEST_CODE = 1234;
    Button Start;
    TextView Speech;
    Dialog match_text_dialog;
    ListView textlist;
    ListView textlist2;
    ArrayList<String> matches_text;

    private static final String TAG = "BluetoothSCO";
    private static final boolean DEBUG = true;
    private AudioManager mAudioManager = null;
    private Context mContext = null;
    private boolean created = false;
    private boolean listening = false;
    CheckBox checkBox;
    CheckBox checkRus;

    private void recognizing()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "listening");
        if(checkRus.isChecked())
        {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
        }else{
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        }
        //
        intent.putExtra(RecognizerIntent.EXTRA_SECURE, true);
        startActivityForResult(intent, REQUEST_CODE);
    }

    /* Broadcast receiver for the SCO State broadcast intent.*/
    private final BroadcastReceiver mSCOHeadsetAudioState = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            //if(DEBUG)
            // Log.e(TAG, " mSCOHeadsetAudioState--->onReceive");
            int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
            if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                DisplayToast("BT SCO Music is now enabled. Play song in Media Player");
            } else if (state == AudioManager.SCO_AUDIO_STATE_DISCONNECTED) {
                if (checkBox.isChecked() && created) {
                    if (DEBUG)
                        Log.e(TAG, "BTSCOApp: unexpected reset ");
                    mAudioManager.setBluetoothScoOn(true);
                    mAudioManager.startBluetoothSco();
                    recognizing();
                }else {
                    DisplayToast("BT SCO Music is now disabled");
                }
            }
                /*if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                    if (!checkBox.isChecked()) {
                        mAudioManager.setBluetoothScoOn(false);
                        mAudioManager.stopBluetoothSco();
                    }
                    DisplayToast("BT SCO Music is now enabled. Play song in Media Player");
                } else if (state == AudioManager.SCO_AUDIO_STATE_DISCONNECTED) {
                    if (checkBox.isChecked() && created) {
                        if (DEBUG)
                            Log.e(TAG, "BTSCOApp: unexpected reset ");
                        mAudioManager.setBluetoothScoOn(true);
                        mAudioManager.startBluetoothSco();
                        recognizing();
                    } else {
                        DisplayToast("BT SCO Music is now disabled");
                    }
                }*/
        }
    };

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Start = (Button)findViewById(R.id.start_reg);
        //Speech = (TextView)findViewById(R.id.speech);
        Start.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                recognizing();
                /*Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "listening");

                //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
                intent.putExtra(RecognizerIntent.EXTRA_SECURE, true);
                startActivityForResult(intent, REQUEST_CODE);*/
            }
        });

        loginPanel = (LinearLayout)findViewById(R.id.loginpanel);
        chatPanel = (LinearLayout)findViewById(R.id.chatpanel);

        editTextUserName = (EditText) findViewById(R.id.username);
        editTextUserName.setText("Grushki");
        editTextAddress = (EditText) findViewById(R.id.address);
        editTextAddress.setText("77.220.132.233");
        textPort = (TextView) findViewById(R.id.port);
        textPort.setText("port: " + SocketServerPORT);
        buttonConnect = (Button) findViewById(R.id.connect);
        buttonDisconnect = (Button) findViewById(R.id.disconnect);
        chatMsg = (TextView) findViewById(R.id.chatmsg);

        buttonConnect.setOnClickListener(buttonConnectOnClickListener);
        buttonDisconnect.setOnClickListener(buttonDisconnectOnClickListener);

        editTextSay = (EditText)findViewById(R.id.say);
        buttonSend = (Button)findViewById(R.id.send);

        buttonSend.setOnClickListener(buttonSendOnClickListener);

        /*MusicIntentReceiver m = new MusicIntentReceiver();
        m.setContext(MainActivity.this);
        if(!m.isAvailable()) {
            if(!m.obtainProxy())
            {
                Toast.makeText(MainActivity.this, "BT not available",
                        Toast.LENGTH_LONG).show();
            }else{
                //m.startVoiceRecognition();
            }
        }else{
            m.startVoiceRecognition();
        }*/

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Bluetooth is not available");
            finish();
            return;
        }
        mContext = this;
        IntentFilter newintent = new IntentFilter();
        newintent.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED);
        mContext.registerReceiver(mSCOHeadsetAudioState, newintent);
        // Check whether BT is enabled
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth is not enabled", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Bluetooth is not enabled");
            finish();
            return;
        }
        // get the Audio Service context
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (mAudioManager == null){
            Log.e(TAG, "mAudiomanager is null");
            finish();
            return;
        }
        // Android 2.2 onwards supports BT SCO for non-voice call use case
        // Check the Android version whether it supports or not.
        if(!mAudioManager.isBluetoothScoAvailableOffCall()) {
            Toast.makeText(this, "Platform does not support use of SCO for off call", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // Check list of bonded devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                Log.e(TAG, "BT Device :"+device.getName()+ " , BD_ADDR:" + device.getAddress());
            }
            // To do:
            // Need to check from the paired devices which supports BT HF profile
            // and take action based on that.
        } else {
            Toast.makeText(this, "No Paired Headset, Pair and connect to phone audio", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // Check whether BT A2DP (media) is connected
        // If yes, ask user to disconnect
        if(mAudioManager.isBluetoothA2dpOn ()){
            Toast.makeText(this, "Disconnect A2DP (media audio) to headset from Bluetooth Settings", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Toast.makeText(this, "Make sure:Device is connected to Headset & Connected to Phone Audio only!", Toast.LENGTH_LONG).show();
        checkBox = (CheckBox) findViewById(R.id.CheckBox01);
        checkBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox)v).isChecked()) {
                    if(DEBUG)
                        Log.e(TAG, "BTSCOApp: Checkbox Checked ");
                    mAudioManager.setBluetoothScoOn(true);
                    mAudioManager.startBluetoothSco();
                } else {
                    if(DEBUG)
                        Log.e(TAG, "BTSCOApp Checkbox Unchecked ");
                    mAudioManager.setBluetoothScoOn(false);
                    mAudioManager.stopBluetoothSco();
                }
            }
        });

        created = true;

        checkRus = (CheckBox) findViewById(R.id.language);
        checkRus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((CheckBox)v).isChecked()) {
                } else {
                }
            }
        });

    }

    private void DisplayToast(String msg)
    {
        Toast.makeText(getBaseContext(), msg,
                Toast.LENGTH_SHORT).show();
    }

    OnClickListener buttonDisconnectOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if(chatClientThread==null){
                return;
            }
            chatClientThread.disconnect();
        }

    };

    OnClickListener buttonSendOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {/////SEND DATA
            Log.e(TAG, "send button\n");
            if (editTextSay.getText().toString().equals("")) {
                return;
            }

            if(chatClientThread==null){
                return;
            }

            chatClientThread.sendMsg(editTextSay.getText().toString() + "\n");
            Log.e(TAG, "send button end\n");
        }

    };

    OnClickListener buttonConnectOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            String textUserName = editTextUserName.getText().toString();
            if (textUserName.equals("")) {
                Toast.makeText(MainActivity.this, "Enter User Name",
                        Toast.LENGTH_LONG).show();
                return;
            }

            String textAddress = editTextAddress.getText().toString();
            if (textAddress.equals("")) {
                Toast.makeText(MainActivity.this, "Enter Addresse",
                        Toast.LENGTH_LONG).show();
                return;
            }

            msgLog = "";
            chatMsg.setText(msgLog);
            loginPanel.setVisibility(View.GONE);
            chatPanel.setVisibility(View.VISIBLE);

            chatClientThread = new ChatClientThread(
                    textUserName, textAddress, SocketServerPORT);
            chatClientThread.start();
        }

    };

    private class ChatClientThread extends Thread {

        String name;
        String dstAddress;
        int dstPort;

        String msgToSend = "";
        boolean goOut = false;

        ChatClientThread(String name, String address, int port) {
            this.name = name;
            dstAddress = address;
            dstPort = port;
        }

        @Override
        public void run() {
            Socket socket = null;
            DataOutputStream dataOutputStream = null;
            DataInputStream dataInputStream = null;

            try {
                socket = new Socket(dstAddress, dstPort);
                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream.writeUTF(name);
                dataOutputStream.flush();
                Log.e(TAG, "while (!goOut) {\n");
                while (!goOut) {
                    if (dataInputStream.available() > 0) {
                        Log.e(TAG, "input stream available\n");
                        msgLog += dataInputStream.readUTF();
                        Log.e(TAG, "input stream read\n");
                        MainActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                chatMsg.setText(msgLog);
                            }
                        });
                    }

                    if(!msgToSend.equals("")){
                        Log.e(TAG, "dataOutputStream.writeUTF(msgToSend);\n");
                        dataOutputStream.writeUTF(msgToSend);
                        dataOutputStream.flush();
                        Log.e(TAG, "dataOutputStream.flush();\n");
                        msgToSend = "";
                    }
                }
                Log.e(TAG, "while (!goOut) }\n");

            } catch (UnknownHostException e) {
                e.printStackTrace();
                final String eString = e.toString();
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Log.e(TAG, "UnknownHostException\n");
                        Toast.makeText(MainActivity.this, eString, Toast.LENGTH_LONG).show();
                    }

                });
            } catch (IOException e) {
                e.printStackTrace();
                final String eString = e.toString();
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Log.e(TAG, "IOException\n");
                        Toast.makeText(MainActivity.this, eString, Toast.LENGTH_LONG).show();
                    }

                });
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        loginPanel.setVisibility(View.VISIBLE);
                        chatPanel.setVisibility(View.GONE);
                    }

                });
            }

        }

        private void sendMsg(String msg){

            Log.e(TAG, "sendMsg: " + msg + "\n");
            msgToSend = msg + "\n";
        }

        private void disconnect(){
            goOut = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            ArrayList<String> commands = new ArrayList<String>();
            if(checkRus.isChecked())
            {
                commands.add("привет робот");
                commands.add("проверить системы");
                commands.add("желтая команда");
                commands.add("зеленая команда");
                commands.add("привет");
            }else{
                commands.add("hello robot");
                commands.add("check settings");
                commands.add("play yellow team");
                commands.add("play green team");
                commands.add("hello");
            }

            textlist2 = (ListView)findViewById(R.id.list2);
            matches_text = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, matches_text);
            textlist2.setAdapter(adapter);

            boolean parsed = false;
            for (int i = 0; i < matches_text.size(); i++) {
                for(int j = 0; j < commands.size(); j++)
                {
                    if(commands.get(j).equals(matches_text.get(i)))
                    {
                        parsed = true;
                        Log.e(TAG, "cmd" + Integer.toString(j) + "\n");
                        if(chatClientThread != null) {
                            chatClientThread.sendMsg("cmd" + Integer.toString(j) + "\n");
                        }
                    }else{
                        //Log.e(TAG, "1" + matches_text.get(i) + commands.get(j) + "2");
                    }
                }
            }
            if(parsed == false)
            {
                Log.e(TAG, "unparsed, (i,j)" + Integer.toString(matches_text.size()) + Integer.toString(commands.size()));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}