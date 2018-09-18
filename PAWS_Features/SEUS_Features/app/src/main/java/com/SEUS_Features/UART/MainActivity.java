
/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.SEUS_Features.UART;




import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;


import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

    // See if we found delimiter
    private static boolean delim_found = false;
    private static int delim_index = 0;

    // Start message to write to BLE module to begin transmission
    private static final byte [] START_MSG = new byte[] {(byte)83, (byte)69, (byte)85, (byte)83, (byte)83};  //"SEUSS"

    // Delimiter between windows; final byte is the byte data length; Second to last byte is the microphone flag byte
    private static final int[] DELIM = {83, 69, 85, 83, 35};

    // Keep track of bytes found in data reconstruction
    private static byte [] byte_data = new byte[DELIM[DELIM.length - 1]];
    // Keep track of where we are in data reconstruction
    private static int data_index = 0;

    // Used to prevent same packet count from printing twice
    private static boolean printed = false;

    public String filepath = "/mnt/sdcard/";
    public File file = new File(filepath, "HEADDATA/");
    public int counter = 0;
    public int frame_counter = 0;
    public String filename;
    public String ccr_filename;
    public String power_filename;
    public String zcr_filename;
    public FileOutputStream fccr;
    public FileOutputStream fpwr;
    public FileOutputStream fzcr;
    public FileOutputStream fos;
    public OutputStreamWriter osw;
    public OutputStreamWriter occr;
    public OutputStreamWriter opwr;
    public OutputStreamWriter ozcr;

    public int head = 0;
    public int micStatus = 0;
    public String mic1;
    public String mic2;
    public String mic3;
    public String mic4;

    public long[] zcr_1 = new long[10];
    public long[] zcr_2 = new long[10];
    public long[] zcr_3 = new long[10];
    public long[] zcr_4 = new long[10];

    public long[] ccr_1 = new long[10];
    public long[] ccr_2 = new long[10];
    public long[] ccr_3 = new long[10];

    public long[] pwr_1 = new long[10];
    public long[] pwr_2 = new long[10];
    public long[] pwr_3 = new long[10];

    public long ccr_1_sum = 0;
    public long ccr_2_sum = 0;
    public long ccr_3_sum = 0;

    public long pwr_1_sum = 0;
    public long pwr_2_sum = 0;
    public long pwr_3_sum = 0;

    public long zcr_1_sum = 0;
    public long zcr_2_sum = 0;
    public long zcr_3_sum = 0;
    public long zcr_4_sum = 0;

    // For measuring packet rate
    public long start_time = 0;
    public long end_time = 0;

    public void addCircular(long c1, long c2, long c3, long p1, long p2, long p3, long z1, long z2, long z3, long z4){
        if ((micStatus & 0x01) == 0){
            mic1 = "ON";
        } else {
            mic1 = "OFF";
        }

        if ((micStatus & 0x02) == 0){
            mic2 = "ON";
        } else {
            mic2 = "OFF";
        }

        if ((micStatus & 0x04) == 0){
            mic3 = "ON";
        } else {
            mic3 = "OFF";
        }

        if ((micStatus & 0x08) == 0){
            mic4 = "ON";
        } else {
            mic4 = "OFF";
        }
        ccr_1[head] = c1;
        ccr_1_sum += c1;
        ccr_2[head] = c2;
        ccr_2_sum += c2;
        ccr_3[head] = c3;
        ccr_3_sum += c3;

        pwr_1[head] = p1;
        pwr_1_sum += p1;
        pwr_2[head] = p2;
        pwr_2_sum += p2;
        pwr_3[head] = p3;
        pwr_3_sum += p3;

        zcr_1[head] = z1;
        zcr_1_sum += z1;
        zcr_2[head] = z2;
        zcr_2_sum += z2;
        zcr_3[head] = z3;
        zcr_3_sum += z3;
        zcr_4[head] = z4;
        zcr_4_sum += z4;

        if (head == 9) {
            listAdapter.clear();
            listAdapter.add("Mic 1:" + mic1);
            listAdapter.add("Mic 2:" + mic2);
            listAdapter.add("Mic 3:" + mic3);
            listAdapter.add("Mic 4:" + mic4);
            listAdapter.add("");
            listAdapter.add("Delay 2: " + String.valueOf(ccr_1_sum/10) );
            listAdapter.add("Delay 3: " + String.valueOf(ccr_2_sum/10) );
            listAdapter.add("Delay 4: " + String.valueOf(ccr_3_sum/10) );
            listAdapter.add("");
            listAdapter.add("Power 2: " + String.valueOf(pwr_1_sum/10) );
            listAdapter.add("Power 3: " + String.valueOf(pwr_2_sum/10) );
            listAdapter.add("Power 4: " + String.valueOf(pwr_3_sum/10) );
            listAdapter.add("");
            listAdapter.add("Zcr 1: " + String.valueOf(zcr_1_sum/10) );
            listAdapter.add("Zcr 2: " + String.valueOf(zcr_2_sum/10) );
            listAdapter.add("Zcr 3: " + String.valueOf(zcr_3_sum/10) );
            listAdapter.add("Zcr 4: " + String.valueOf(zcr_4_sum/10) );
            ccr_1_sum = 0;
            ccr_2_sum = 0;
            ccr_3_sum = 0;
            pwr_1_sum = 0;
            pwr_2_sum = 0;
            pwr_3_sum = 0;
            zcr_1_sum = 0;
            zcr_2_sum = 0;
            zcr_3_sum = 0;
            zcr_4_sum = 0;
        }
        head = head+1;
        head = head%10;
    }
    //    public java.lang.Long pwr_value=0L;
    public int pwr_value = 0;
    public void fopen(){
        try{
            filename = file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".txt";
            ccr_filename = file.getAbsolutePath() + "/" + System.currentTimeMillis() + "ccr" + ".txt";
            power_filename = file.getAbsolutePath() + "/" + System.currentTimeMillis() + "power" + ".txt";
            zcr_filename = file.getAbsolutePath() + "/" + System.currentTimeMillis() + "zcr" + ".txt";

            fos = new FileOutputStream(filename);
            fccr = new FileOutputStream(ccr_filename);
            fpwr = new FileOutputStream(power_filename);
            fzcr = new FileOutputStream(zcr_filename);
            osw  = new OutputStreamWriter(fos);
            occr  = new OutputStreamWriter(fccr);
            opwr  = new OutputStreamWriter(fpwr);
            ozcr  = new OutputStreamWriter(fzcr);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

    }
    public void file_close(){
        try{
            osw.close();
            occr.close();
            opwr.close();
            ozcr.close();

            fos.close();
            fccr.close();
            fpwr.close();
            fzcr.close();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

    }
    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect,btnSend;
    private EditText edtMessage;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        messageListView = (ListView) findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);
        btnConnectDisconnect=(Button) findViewById(R.id.btn_select);
        btnSend=(Button) findViewById(R.id.sendButton);
        edtMessage = (EditText) findViewById(R.id.sendText);
        service_init();

        // Create file folder if it doesn't already exist.
        if(!file.exists()) {
            Log.i(TAG, "Storage Folder Does Not Exist. Creating....");
            file.mkdirs();
        }
        else {
            Log.i(TAG, "Storage Folder Already Exists.");
        }

        // Handle Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {
                    if (btnConnectDisconnect.getText().equals("Connect")){

                        //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices

                        Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                    } else {
                        //Disconnect button pressed
                        if (mDevice!=null)
                        {
                            mService.disconnect();
                        }
                    }
                }
            }
        });
        // Handle Send button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.sendText);
                String message = editText.getText().toString();
                byte[] value;
                try {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });

        // Set initial UI state

    }
    public int[] bytearray2intarray(byte[] barray)
    {
        int[] iarray = new int[barray.length];
        int i = 0;
        for (byte b : barray)
            iarray[i++] = b & 0xff;
        return iarray;
    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

        }


        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };

    private Handler mHandler = new Handler() {
        @Override

        //Handler events that received from UART service
        public void handleMessage(Message msg) {

        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        btnConnectDisconnect.setText("Disconnect");
                        edtMessage.setEnabled(true);
                        btnSend.setEnabled(true);
                        ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - ready");
                        listAdapter.add("[" + currentDateTimeString + "] Connected to: " + mDevice.getName());
                        messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                        mState = UART_PROFILE_CONNECTED;

                        // Write to start transmission
                        //Log.d(TAG, "Writing RX start");
                        //mService.writeRXCharacteristic(START_MSG);
                        //Log.d(TAG, "Done writing RX start");

                        //Handler handshake_handle = new Handler();
                        //handshake_handle.postDelayed(null, 1000);

                        //Toast.makeText(this, "Handshake", Toast.LENGTH_LONG).show();


                        fopen();
                        counter =0;
                    }

                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                         end_time = System.currentTimeMillis();
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        btnConnectDisconnect.setText("Connect");
                        edtMessage.setEnabled(false);
                        btnSend.setEnabled(false);
                        ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                        listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        //setUiState();
                        listAdapter.add("packets: " + counter);
                        listAdapter.add("packet rate: " + (counter / (1.0 * (end_time - start_time) / 1000)) + " per second");
                        file_close();

                        // Deinitialize everything
                        delim_found = false;
                        delim_index = 0;
                        data_index = 0;
                        counter = 0;
                    }
                });
            }


            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                Log.d("Recv bytes raw:",""+txValue.length);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            int i = 0;

                            //String text = new String(txValue, "UTF-8");
                            System.out.println(txValue.length);

                            for(i = 0; i < txValue.length; i++) {

                                // Write all bytes received
                                osw.write("" + txValue[i] + "\n");

                                // Case where delimiter is already found; parse data
                                if(delim_found) {

                                    // Keep adding bytes to the byte array until we have read all windows.
                                    byte_data[data_index++] = txValue[i];

                                    // If all bytes have been read, then save them to file
                                    if(data_index >= byte_data.length) {
                                        int zcr1, zcr2, zcr3, zcr4, ccr1, ccr2, ccr3;
                                        long pwr2, pwr3, pwr4;
                                        long b1, b2, b3, b4, b5, b6, b7, b8;
                                        int c1, c2;

                                        // Increment packet counter
                                        counter++;

                                        // Begin counting packet rate after receiving first packet
                                        if(counter == 1) {
                                            start_time = System.currentTimeMillis();
                                        }

                                        // Write delays
                                        ccr1 = (byte_data[0]);
                                        occr.write("" + ccr1 + ",");

                                        ccr2 = (byte_data[1]);
                                        occr.write("" + ccr2 + ",");

                                        ccr3 = (byte_data[2]);
                                        occr.write("" + ccr3 + ",\n");

                                        // Write powers
                                        b1 = (byte_data[3] & 0xFF);
                                        b2 = (byte_data[4] & 0xFF);
                                        b3 = (byte_data[5] & 0xFF);
                                        b4 = (byte_data[6] & 0xFF);
                                        b5 = (byte_data[7] & 0xFF);
                                        b6 = (byte_data[8] & 0xFF);
                                        b7 = (byte_data[9] & 0xFF);
                                        b8 = (byte_data[10] & 0xFF);
                                        pwr2 = ((b1 << 56) | (b2 << 48) | (b3 << 40) | (b4 << 32) | (b5 << 24) | (b6 << 16) | (b7 << 8) | b8);
                                        //val = ((b1 << 56) | (b2 << 48) | (b3 << 40) | (b4 << 32) | (b5 << 24) | (b6 << 16) | (b7 << 8) | b8);
                                        System.out.println(pwr2);
                                        opwr.write("" + pwr2 + ",");

                                        b1 = (byte_data[11] & 0xFF);
                                        b2 = (byte_data[12] & 0xFF);
                                        b3 = (byte_data[13] & 0xFF);
                                        b4 = (byte_data[14] & 0xFF);
                                        b5 = (byte_data[15] & 0xFF);
                                        b6 = (byte_data[16] & 0xFF);
                                        b7 = (byte_data[17] & 0xFF);
                                        b8 = (byte_data[18] & 0xFF);
                                        pwr3 = ((b1 << 56) | (b2 << 48) | (b3 << 40) | (b4 << 32) | (b5 << 24) | (b6 << 16) | (b7 << 8) | b8);
                                        System.out.println(pwr3);
                                        opwr.write("" + pwr3 + ",");

                                        b1 = (byte_data[19] & 0xFF);
                                        b2 = (byte_data[20] & 0xFF);
                                        b3 = (byte_data[21] & 0xFF);
                                        b4 = (byte_data[22] & 0xFF);
                                        b5 = (byte_data[23] & 0xFF);
                                        b6 = (byte_data[24] & 0xFF);
                                        b7 = (byte_data[25] & 0xFF);
                                        b8 = (byte_data[26] & 0xFF);
                                        pwr4 = ((b1 << 56) | (b2 << 48) | (b3 << 40) | (b4 << 32) | (b5 << 24) | (b6 << 16) | (b7 << 8) | b8);
                                        System.out.println(pwr4);
                                        opwr.write("" + pwr4 + ",\n");

                                        // Write out ZCR
                                        c1 = (byte_data[27] & 0xFF);
                                        c2 = (byte_data[28] & 0xFF);
                                        zcr1 = ((c1 << 8) | c2);
                                        ozcr.write("" + zcr1 + ",");

                                        c1 = (byte_data[29] & 0xFF);
                                        c2 = (byte_data[30] & 0xFF);
                                        zcr2 = ((c1 << 8) | c2);
                                        ozcr.write("" + zcr2 + ",");

                                        c1 = (byte_data[31] & 0xFF);
                                        c2 = (byte_data[32] & 0xFF);
                                        zcr3 = ((c1 << 8) | c2);
                                        ozcr.write("" + zcr3 + ",");

                                        c1 = (byte_data[33] & 0xFF);
                                        c2 = (byte_data[34] & 0xFF);
                                        zcr4 = ((c1 << 8) | c2);
                                        ozcr.write("" + zcr4 + ",\n");

                                        // All bytes found, so need to find next window
                                        delim_found = false;
                                        delim_index = 0;
                                        data_index = 0;
                                        addCircular(ccr1, ccr2, ccr3, pwr2, pwr3, pwr4, zcr1, zcr2, zcr3, zcr4);
                                    }
                                }

                                // Case where delimiter not found
                                else {

                                    // The byte is part of the delimiter we are looking for
                                    if(txValue[i] == DELIM[delim_index] || delim_index == DELIM.length - 2) {
                                        if(delim_index == DELIM.length-2)
                                            micStatus = txValue[delim_index];
                                        // Just found the delimiter, so now we start reading data
                                        if(++delim_index >= DELIM.length) {
                                            delim_index = 0;
                                            delim_found = true;
                                        }
                                    }

                                    // Byte is not part of the delimiter we are looking for
                                    // Reset and start looking again
                                    else {
                                        delim_index = 0;
                                        delim_found = false;
                                    }
                                }
                            }

                            // Display packet count every second, which corresponds to 20 packets
                            // if sampling = 32 kHz, window size = 100 ms with 50 ms overlap
                            if(counter % 20 == 0 && !printed) {
                                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
//                                 listAdapter.clear();
//                                 listAdapter.add("[" + currentDateTimeString + "] RX: " + String.valueOf(counter) + " packets");

//                                 messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);

                                printed = true;
                            }
                            else {
                                printed = false;
                            }

                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }
            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }


        }
    };

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;

    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                    mService.connect(deviceAddress);


                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }


    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("nRFUART's running in background.\n             Disconnect to exit");
        }
        else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.popup_title)
                    .setMessage(R.string.popup_message)
                    .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.popup_no, null)
                    .show();
        }
    }
}
