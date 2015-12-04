package com.example.harrymorgan.minioncontrol;

import android.annotation.TargetApi;
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

import java.util.List;
import android.media.AudioManager;

public class MusicIntentReceiver extends BroadcastReceiver implements BluetoothProfile.ServiceListener {

    //AudioManager.
    // final AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);

    public static enum VOICE { DISCONNECTED, CONNECTING, CONNECTED };

    private VOICE state = VOICE.DISCONNECTED;
    private BluetoothHeadset bluetoothHeadset = null;
    private Context appContext = null;

    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
        if(profile == BluetoothProfile.HEADSET) {
            bluetoothHeadset = (BluetoothHeadset)proxy;
        }
    }

    @Override
    public void onServiceDisconnected(int profile) {
        if(profile == BluetoothProfile.HEADSET) {
            bluetoothHeadset = null;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)) {
            Object state = intent.getExtras().get(BluetoothHeadset.EXTRA_STATE);
            if(state.equals(BluetoothHeadset.STATE_AUDIO_CONNECTING)) {
                state = VOICE.CONNECTING;
            } else if(state.equals(BluetoothHeadset.STATE_AUDIO_CONNECTED)) {
                state = VOICE.CONNECTED;
                Toast.makeText(context, "Enter User Name",
                        Toast.LENGTH_LONG).show();
                Intent btState = new Intent();
                btState.putExtra("bluetooth_connected", true);
                context.sendBroadcast(btState);
            } else {
                state = VOICE.DISCONNECTED;
                Intent btState = new Intent();
                btState.putExtra("bluetooth_connected", false);
                context.sendBroadcast(btState);
            }
        }
    }

    public void setContext(Context context) {
        appContext = context;
    }

    public boolean obtainProxy() {
        if(appContext != null) {
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            return btAdapter.getProfileProxy(appContext, this, BluetoothProfile.HEADSET);
        } else {
            return false;
        }
    }

    public void releaseProxy() throws Exception {
        if(appContext != null) {
            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            btAdapter.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothHeadset);
        }
    }

    public void startVoiceRecognition() {
        //BluetoothDevice btDevice = bluetoothHeadset.getConnectedDevices().get(0);
        //List<BluetoothDevice> list =
        bluetoothHeadset.getConnectedDevices();
        /*if(list != null) {
            Toast.makeText(appContext, "Number of devices" + Integer.toString(bluetoothHeadset.getConnectedDevices().size()),
                    Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(appContext, "Number of device is null",
                    Toast.LENGTH_LONG).show();
        }*/
        //bluetoothHeadset.stopVoiceRecognition(btDevice);
        //bluetoothHeadset.startVoiceRecognition(btDevice);
    }

    public void stopVoiceRecognition() {
        if (bluetoothHeadset.getConnectedDevices().size() > 0) {
            BluetoothDevice btDevice = bluetoothHeadset.getConnectedDevices().get(0);
            bluetoothHeadset.stopVoiceRecognition(btDevice);
        }
    }

    public boolean isEnabled() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    public boolean isAvailable() {
        if(isEnabled()) {
            return bluetoothHeadset != null && bluetoothHeadset.getConnectedDevices().size() > 0;
        } else {
            return false;
        }
    }

    public VOICE getVoiceState() {
        return state;
    }

}

