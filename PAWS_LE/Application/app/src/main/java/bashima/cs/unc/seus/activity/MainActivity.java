package bashima.cs.unc.seus.activity;

import android.Manifest;
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
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import net.sf.javaml.classification.tree.RandomForest;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import bashima.cs.unc.seus.Object.BTData;
import bashima.cs.unc.seus.Service.UartService;
import bashima.cs.unc.seus.Util.DetectionDataStorage;
import bashima.cs.unc.seus.Util.Utils;
import bashima.cs.unc.seus.constant.Constant;
import bashima.cs.unc.seus.featuer.GenericCC;
import bashima.cs.unc.seus.featuer.MFCCFeatureExtract;
import bashima.cs.unc.seus.featuer.WindowFeature;
//import bashima.cs.unc.seus.view.MyPlotView;
import bashima.cs.unc.seus.view.MyPolarView;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;
import libsvm.LibSVM;
import seus.bashima.cs.unc.seus.R;

import static bashima.cs.unc.seus.constant.Constant.CAR_CLASS;
import static bashima.cs.unc.seus.constant.Constant.DELIM;
import static bashima.cs.unc.seus.constant.Constant.HORN_CLASS;
import static bashima.cs.unc.seus.constant.Constant.REQUEST_SELECT_DEVICE;
import static bashima.cs.unc.seus.constant.Constant.UART_PROFILE_CONNECTED;
import static bashima.cs.unc.seus.constant.Constant.UART_PROFILE_DISCONNECTED;
import static bashima.cs.unc.seus.constant.Constant.classifierCarDirection;
import static bashima.cs.unc.seus.constant.Constant.classifierCarDistance;
import static bashima.cs.unc.seus.constant.Constant.classifierHornDirection;
import static bashima.cs.unc.seus.constant.Constant.classifierHornDistance;
//import static bashima.cs.unc.seus.constant.Constant.classifierHornDirection;

public class MainActivity extends AppCompatActivity {
    // Decalring Variables
    // Detection
    public String valueFile = "";
    public String valueHornFile = "";
    String deviceName;
    boolean isRunning = false;
    ToggleButton btStart = null;
    public byte audioData[] = null;
    private int audioBufferSize;
    private AudioRecord recorder;
    private Thread readAudioThread;
    BlockingQueue<byte[]> audioSharedQueue = null;
//    LibSVM libsvm;
    int detected = Constant.NONE;
    int quardrant = 20;
    int distance = 0;
    double xPoint = Double.NEGATIVE_INFINITY;
    double yPoint = Double.NEGATIVE_INFINITY;

    // UI
    //MyPlotView myPlotView = null;
    private double audioPlotValue = 0;
    TextView tvDeviceName;
    private PieChartView chart;
    private PieChartData data;
    SliceValue sliceValue1, sliceValue2;
    MyPolarView mpv = null;
    SoundPool sp;
//    SoundPool sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);



    // Car Detection and Localization
    private Thread carDetecLocal = null;

    //Localization
    private Button btnDetectionToggle;
    private boolean detectionEnabled = true;
    public boolean btState = false;
    private BluetoothAdapter mBtAdapter = null;
    private Button btnConnectDisconnect;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    public int mState = UART_PROFILE_DISCONNECTED;
    public ArrayList<BTData> btDataArrayList = new ArrayList<>();
//    Classifier classifierCarDirection;
//    Classifier classifierCarDistance;
//    Classifier classifierHornDirection;
//    Classifier classifierHornDistance;

    public int counter = 0;
    private static boolean delim_found = false;
    private static int delim_index = 0;
    // Keep track of bytes found in data reconstruction
    private static byte[] byte_data = new byte[DELIM[DELIM.length - 1]];
    // Keep track of where we are in data reconstruction
    private static int data_index = 0;
    private static boolean printed = false;
    public boolean isConnected = false;
    public String cls = "";


    //application
    public Context context = null;
    public MainActivity mainActivity = null;

    int soundIds[] = new int[10];

    // Logging
    private Button log_detection_falsepositive;
    private Button log_detection_falsenegative;
    private static int log_false_positive_flag = 0;
    private static int log_false_negative_flag = 0;
    private static Queue<DetectionDataStorage> falsePositiveDataStorageQueue;
    private static Queue<DetectionDataStorage> falseNegativeDataStorageQueue;
    private static OutputStreamWriter feature_classifier_log_writer;
    private static FileOutputStream feature_classifier_log_stream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        context = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, 0);

        }


        AudioAttributes attrs = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
           sp = new SoundPool.Builder()
                    .setMaxStreams(10)
                    .setAudioAttributes(attrs)
                    .build();
//            setVolumeControlStream(AudioManager.STREAM_MUSIC);


        } else
        {
            sp = new SoundPool(10, AudioManager.STREAM_ALARM, 0);
        }

        soundIds[0] = sp.load(context, R.raw.alert, 1);

        android.support.v7.app.ActionBar menu = getSupportActionBar();
//        menu.setDisplayHomeAsUpEnabled(true);
        menu.setDisplayShowHomeEnabled(true);
        menu.setLogo(R.mipmap.ic_launcher);
        menu.setDisplayUseLogoEnabled(true);

        //UI
        //myPlotView = (MyPlotView) findViewById(R.id.plotviewid);
        mpv = (MyPolarView) findViewById(R.id.polarviewid);
        tvDeviceName = (TextView) findViewById(R.id.tv_connected_device);

//        chart = (PieChartView) findViewById(R.id.chart);
//        chart.setChartRotationEnabled(false);

        sliceValue1 = new SliceValue((float)  15, getResources().getColor(R.color.primary));
        sliceValue2 = new SliceValue((float)  15, getResources().getColor(R.color.divider));

        mpv.whichPie = 20;
//        generatePi(0);


//        Log.d("ReadModel", "Start");
//        readModel();
//        Log.d("ReadModel", "End");

        // Setup detection button
        btnDetectionToggle = (Button)findViewById(R.id.detection_toggle_btn);
        btnDetectionToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(detectionEnabled) {
                    detectionEnabled = false;
                    btnDetectionToggle.setText("Enable Detection");
                }
                else {
                    detectionEnabled = true;
                    btnDetectionToggle.setText("Disable Detection");
                }
            }
        });

        //setup audio buffer
        try {
            audioBufferSize = AudioRecord
                    .getMinBufferSize(Constant.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);
            //recalculate audioBufferSize
            Log.d(Constant.TAG, "audioBufferSize: " + audioBufferSize);
            int n = (int) Math.ceil(Constant.DETECTION_WINDOW_SIZE / audioBufferSize);
            audioBufferSize = n * audioBufferSize;
            Constant.BUFFERSIZE = Constant.DETECTION_WINDOW_SIZE;
            audioData = new byte[Constant.BUFFERSIZE];
            Log.d(Constant.TAG, "n: " + n);

            Log.d(Constant.TAG, "Buffer Size: " + Constant.BUFFERSIZE);
            Log.d(Constant.TAG, "Frame_len: " + Integer.toString((int)Math.floor(Constant.SAMPLE_RATE * Constant.Wl)));
        } catch (Exception e) {
            android.util.Log.e("TrackingFlow", "Exception", e);
        }


        //Bluetooth
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        btnConnectDisconnect = (Button) findViewById(R.id.btn_select);

        service_init();
        // Handle Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {

                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);

                    }
                    Log.i(Constant.TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, Constant.REQUEST_ENABLE_BT);
                } else {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {

                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);

                    }
                    if (btnConnectDisconnect.getText().equals("Connect")) {

                        //Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices

                        Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                        startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
//                        service_init();
                    } else {
                        //Disconnect button pressed
                        if (mDevice != null) {
                            mService.disconnect();
                            isConnected = false;
                        }
                    }
                }
            }
        });

        startButtonListener();

        // Set up Logging if enabled
        if(Constant.LOGGING_FLAG > 0) {

            // Log False Positive Button
            log_detection_falsepositive = (Button) findViewById(R.id.log_fp);
            log_detection_falsepositive.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    // Create directory if it doesn't already exist
                    File directory = new File(Constant.FP_FOLDER);
                    if(!directory.exists()) {
                        Log.d(Constant.TAG, "False positive folder does not exist; creating....");
                        directory.mkdirs();
                    }

                    // Write files
                    try {
                        int queue_size = falsePositiveDataStorageQueue.size();
                        long timestamp = System.currentTimeMillis() / 1000;
                        for (int i = 0; i < queue_size; i++) {
                            String signal_filename = Constant.FP_FOLDER + Long.toString(timestamp) + "_signal_" + Integer.toString(i) + ".txt";
                            String feature_filename = Constant.FP_FOLDER + Long.toString(timestamp) + "_feature_" + Integer.toString(i) + ".txt";


                            FileOutputStream signal_file = new FileOutputStream(signal_filename);
                            FileOutputStream feature_file = new FileOutputStream(feature_filename);
                            OutputStreamWriter signal_stream = new OutputStreamWriter(signal_file);
                            OutputStreamWriter feature_stream = new OutputStreamWriter(feature_file);

                            // Write
                            DetectionDataStorage entry = falsePositiveDataStorageQueue.poll();
                            double[] entry_signal = entry.signal;
                            double[] entry_feature = entry.feature;
                            for(int j = 0; j < entry_signal.length; j++) {
                                signal_stream.write(Double.toString(entry_signal[j]) + "\n");
                            }
                            for(int j = 0; j < entry_feature.length; j++) {
                                feature_stream.write(Double.toString(entry_feature[j]) + "\n");
                            }

                            // Close after finishing
                            signal_stream.close();
                            feature_stream.close();
                            signal_file.close();
                            feature_file.close();
                        }
                    } catch(Exception e) {
                        Log.e(Constant.TAG, e.toString());
                    }
                }
            });

            // Log False Negative Button
            log_detection_falsenegative = (Button) findViewById(R.id.log_fn);
            log_detection_falsenegative.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    // Create directory if it doesn't already exist
                    File directory = new File(Constant.FN_FOLDER);
                    if(!directory.exists()) {
                        Log.d(Constant.TAG, "False negative folder does not exist; creating....");
                        directory.mkdirs();
                    }

                    // Write files
                    try {
                        int queue_size = falseNegativeDataStorageQueue.size();
                        long timestamp = System.currentTimeMillis() / 1000;
                        for (int i = 0; i < queue_size; i++) {
                            String signal_filename = Constant.FN_FOLDER + Long.toString(timestamp) + "_signal_" + Integer.toString(i) + ".txt";
                            String feature_filename = Constant.FN_FOLDER + Long.toString(timestamp) + "_feature_" + Integer.toString(i) + ".txt";

                            FileOutputStream signal_file = new FileOutputStream(signal_filename);
                            FileOutputStream feature_file = new FileOutputStream(feature_filename);
                            OutputStreamWriter signal_stream = new OutputStreamWriter(signal_file);
                            OutputStreamWriter feature_stream = new OutputStreamWriter(feature_file);

                            // Write
                            DetectionDataStorage entry = falseNegativeDataStorageQueue.poll();
                            double[] entry_signal = entry.signal;
                            double[] entry_feature = entry.feature;
                            for(int j = 0; j < entry_signal.length; j++) {
                                signal_stream.write(Double.toString(entry_signal[j]) + "\n");
                            }
                            for(int j = 0; j < entry_feature.length; j++) {
                                feature_stream.write(Double.toString(entry_feature[j]) + "\n");
                            }

                            // Close after finishing
                            signal_stream.close();
                            feature_stream.close();
                            signal_file.close();
                            feature_file.close();
                        }
                    } catch(Exception e) {
                        Log.e(Constant.TAG, e.toString());
                    }
                }
            });

            // Set up storage queues
            falsePositiveDataStorageQueue = new PriorityQueue<>(Constant.NUM_FP_TO_LOG, new Comparator<DetectionDataStorage>() {
                @Override
                public int compare(DetectionDataStorage o1, DetectionDataStorage o2) {

                    // Returns the 'lowest' values first; in this case, we want the smaller timestamp to be returned first
                    // because we want past windows to get dumped first
                    return o1.timestamp - o2.timestamp >0 ? 1: -1;
                }
            });
            falseNegativeDataStorageQueue = new PriorityQueue<>(Constant.NUM_FN_TO_LOG, new Comparator<DetectionDataStorage>() {
                @Override
                public int compare(DetectionDataStorage o1, DetectionDataStorage o2) {

                    // Returns the 'lowest' values first; in this case, we want the smaller timestamp to be returned first
                    // because we want past windows to get dumped first
                    return o1.timestamp - o2.timestamp >0 ? 1: -1;
                }
            });
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(Constant.TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(Constant.TAG, "Unable to initialize Bluetooth");
                finish();
            }

        }


        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            isConnected = false;
            mService = null;
        }
    };

    private Handler mHandler = new Handler() {
        @Override

        //Handler events that received from UART service
        public void handleMessage(Message msg) {

        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(Constant.TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    deviceName = mDevice.getName();
                    tvDeviceName.setText(mDevice.getName()+ " - connecting");
                    mService.connect(deviceAddress);
                    isConnected = true;
                }
                break;
            case Constant.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(Constant.TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(Constant.TAG, "wrong request code");
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.train:
                Intent trainIntent = new Intent(mainActivity, TrainingActivity.class);
                mainActivity.startActivity(trainIntent);
                return true;
            case R.id.about:
                Intent aboutIntent = new Intent(mainActivity, AboutActivity.class);
                mainActivity.startActivity(aboutIntent);
                return true;
            case R.id.setting:
                Intent settingIntent = new Intent(mainActivity, SettingActivity.class);
                mainActivity.startActivity(settingIntent);
                return true;
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    void startButtonListener() {
        btStart = (ToggleButton) findViewById(R.id.toggleButton);
        btStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    {

                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

                    }

                    // Logging Setup
                    if(Constant.LOGGING_FLAG > 0) {

                        // Logging Directory Setup; create folder if it doesn't exist
                        File directory = new File(Constant.FEATURE_CLASSIFICATION_LOG_FOLDER);
                        if(!directory.exists()) {
                            Log.d(Constant.TAG, "Feature/Classifier logging directory does not exist; creating....");
                            directory.mkdirs();
                        }

                        // Create File and Setup logging stream
                        try {
                            String feature_classifier_filename = Constant.FEATURE_CLASSIFICATION_LOG_FOLDER + Long.toString(System.currentTimeMillis() / 1000) + ".csv";
                            feature_classifier_log_stream = new FileOutputStream(feature_classifier_filename);
                            feature_classifier_log_writer = new OutputStreamWriter(feature_classifier_log_stream);
                        } catch(Exception e) {
                            Log.e(Constant.TAG, e.toString());
                        }
                    }

                    isRunning = true;
                    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, Constant.SAMPLE_RATE,
                            AudioFormat.CHANNEL_IN_DEFAULT,
                            AudioFormat.ENCODING_PCM_16BIT, audioBufferSize * 2);
                    audioSharedQueue = new LinkedBlockingQueue<>();
                    recorder.startRecording();
                    readAudioThread = new Thread(new Runnable() {
                        public void run() {
                            while (readAudioThread != null && !readAudioThread.isInterrupted()) {
                                readAudioBuffer();
                                try {
                                    readAudioThread.sleep(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        updatePlotView(audioPlotValue);
                                    }
                                });
                            }
                        }
                    });
                    readAudioThread.start();
                    carDetecLocal = new Thread(new CarDetectionLocalization());
                    carDetecLocal.start();
                    if(isConnected){
                    String message = "SEUSS";
                    byte[] value;
                    try {
                        //send data to service
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }}

                } else {
                    for(int i = 0; i<btDataArrayList.size();i++) {
                        btDataArrayList.remove(i);
                    }
                    quardrant = 20;
                    Log.d("Valuefile", valueFile);
                    Log.d("ValueHornfile", valueHornFile);
                    try(  PrintWriter out = new PrintWriter( Constant.FILE_PATH+Constant.FOLDER_NAME+"hello.txt" )  ){
                        out.println( valueFile );
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try(  PrintWriter out = new PrintWriter( Constant.FILE_PATH+Constant.FOLDER_NAME+"helloHorn.txt" )  ){
                        out.println( valueHornFile );
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    valueFile = "";
                    valueHornFile = "";
                    isRunning = false;
                    readAudioThread.interrupt();
                    readAudioThread = null;
                    carDetecLocal.interrupt();
                    try {
                        if (recorder != null) {
                            recorder.stop();
                            recorder.release();
                            recorder = null;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (isConnected) {
                        String message = "SEUSE";
                        byte[] value;
                        try {
                            //send data to service
                            value = message.getBytes("UTF-8");
                            mService.writeRXCharacteristic(value);
                            //Update the log with time stamp
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }

                    // Logging Deinitialization
                    if(Constant.LOGGING_FLAG > 0) {
                        try {
                            feature_classifier_log_writer.close();
                            feature_classifier_log_stream.close();
                        } catch(Exception e) {
                            Log.e(Constant.TAG, e.toString());
                        }
                    }
                }
            }
        });
    }

    private void readAudioBuffer() {
        int bufferReadResult;
        try {
            if (recorder != null) {
                while (isRunning) {
                    bufferReadResult = recorder.read(audioData, 0, audioBufferSize);
                    if (bufferReadResult != AudioRecord.ERROR_INVALID_OPERATION) {
                        audioSharedQueue.put(audioData);
                        double sumLevel = 0;
                        for (int i = 0; i < bufferReadResult; i++) {
                            sumLevel += audioData[i];
                        }
                        audioPlotValue = Math.abs((sumLevel / bufferReadResult));
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                updatePlotView(audioPlotValue);
                            }
                        });
                    }
                }
                recorder.stop();
                recorder.release();
                recorder = null;
                readAudioThread = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class CarDetectionLocalization implements Runnable {
        MFCCFeatureExtract mfccFeatures;
        List<WindowFeature> windowFeaturelst;

        // GenericCC parameters
        GenericCC genericCC_features = new GenericCC(Constant.SAMPLE_RATE, Constant.Wl, Constant.Ts, Constant.n_frames_per_window, Constant.B_GCC, Constant.a_GCC, Constant.b_GCC);

        CarDetectionLocalization() {
            mfccFeatures = null;
            windowFeaturelst = null;
        }

        @Override
        public void run() {
            BTData temBt = null;
            int carPredict = 0;
            double[] feature = null;
            while (isRunning) {
                try {
                    Log.e("Detection", "Detection");
                    //audio feature extract
                    byte[] windowSizeSamples = audioSharedQueue.take();
                    if (windowSizeSamples.length != 0) {
                        double[] inputSignal = Constant.convertSignalToDouble(windowSizeSamples);

                        //mfccFeatures = new MFCCFeatureExtract(inputSignal, Constant.Tw, Constant.Ts, Constant.SAMPLE_RATE, Constant.Wl);
                        //windowFeaturelst = mfccFeatures.getListOfWindowFeature();
                        //double[] feature = MFCCFeatureExtract.generateDataSet(windowFeaturelst);

                        // New GenericCC feature
                        feature = genericCC_features.generate_features(inputSignal);
                        //Log.d("features", Arrays.toString(feature));

                        //classification instance create
                        Instance instance = new DenseInstance(feature);

                        //classify detection
                        //Object predictedClassValue = Constant.libsvm.classify(instance);
                        Object predictedClassValue = Constant.classifierDetection.classify(instance);
                        Log.e("Predicted Value", predictedClassValue.toString());
                        //predictedClassValue = Constant.CAR_CLASS;
                        if (predictedClassValue.equals(CAR_CLASS) || !detectionEnabled) {

                            carPredict++;
                            if (carPredict > 3) {
                                detected = Constant.CAR;
                                if(!isConnected)
                                {
                                    //Alert();
                                }
                                quardrant = 1;
                                distance = 1;
                            }

                            xPoint = 0;
                            yPoint = 0;
                            Log.d("btDataArrayListSize", String.valueOf(btDataArrayList.size()));
                            if (isConnected && btDataArrayList.size() > 0) {
//                                int quard = 0;
//                                int []train = new int[8];
//                                for(int i= 0; i<20; i++) {
                                temBt = btDataArrayList.get(0);
                                double[] btFeature = temBt.FeatureGen();
//                                Log.d("CCR CLASSIFY", btFeature[7]+ " "+ btFeature[8]+" "+btFeature[9]);
                                String tempString = temBt.genString();
                                valueFile = valueFile+tempString;

//                                Log.d("Feature Test", tempString);
                                // Stephen: Only use ccr.
                                /*Instance localInstance_dist = new DenseInstance(btFeature);
                                btFeature = new double[3];
                                btFeature[0] = temBt.ccr[0];
                                btFeature[1] = temBt.ccr[1];
                                btFeature[2] = temBt.ccr[2];
                                Instance localInstance = new DenseInstance(btFeature);
                                Object carDistVal = classifierCarDistance.classify(localInstance_dist);
                                //Object carDirVal = classifierCarDirection.classify(localInstance);
                                //char ct  = carDirVal.toString().charAt(0);
                                int vt = (int) ct;
                                //vt = (int)'h';
                                int r = vt - 96;
                                cls = " "+ r;

                                char ct_d  = carDistVal.toString().charAt(0);
                                int vt_d = ((int) ct_d);
                                int r_d = vt_d - 96;

                                btDataArrayList.remove(0);
//                                    quard = Integer.parseInt((carDirVal.toString().charAt(1) + ""));
//                                    train[quard]++;
//                                }
//                                int max = train[0];
//                                int max_i = 0;
//
//                                for (int i = 1; i < 8; i++) {
//                                    if (train[i] > max) {
//                                        max = train[i];
//                                        max_i = i;
//                                    }
//                                }
//                                if(r%2 ==0)
//                                {
//                                    r = r-1;
//                                }
//                                quardrant = (9 - (r));
//                                generatePi(max_i);
                                quardrant = r;
                                distance = r_d;
                                //Alert();
                                Log.d("Debug: ", String.valueOf(r));*/

                                long timing =  System.currentTimeMillis();

                                // Geometric method
                                double[] bt_ccr = new double[3];
                                bt_ccr[0] = temBt.ccr[0];
                                bt_ccr[1] = temBt.ccr[1];
                                bt_ccr[2] = temBt.ccr[2];
                                //Log.d("Debug", "bt_ccr: " + String.valueOf(bt_ccr[0][0]) + ", " + String.valueOf(bt_ccr[1][0]) + ", " + String.valueOf(bt_ccr[2][0]));
                                //Log.d("Debug", "temBt: " + String.valueOf(temBt.ccr[0]) + ", " + String.valueOf(temBt.ccr[1]) + ", " + String.valueOf(temBt.ccr[2]));
                                double[] estimated_loc = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
                                try {
                                    estimated_loc = Utils.localizeSoundSource2(Constant.mic_positions, bt_ccr);
                                }
                                catch(Exception e) {
                                    Log.d("Debug", e.getMessage());
                                }
                                xPoint = estimated_loc[0];
                                yPoint = estimated_loc[1];   // Angle

                                // Compute distance via regression, by finding value of maximum coefficient
                                double max_coefficient = Utils.findMaxVal(feature);
                                xPoint = Utils.distanceRegression(max_coefficient, Constant.distance_eqn_parameters);

                                Log.d("Localization Time", String.valueOf(System.currentTimeMillis() - timing));

                                btDataArrayList.remove(0);
                                Log.d("Debug", "Estimated Location: " + String.valueOf(xPoint) + ", " + String.valueOf(yPoint));
                                //Alert();
                            }

                            // For Logging Detection False Positives
                            if(Constant.LOGGING_FLAG > 0) {

                                Long timestamp = new Long(System.currentTimeMillis() / 1000);
                                DetectionDataStorage newWindow = new DetectionDataStorage(timestamp, inputSignal, feature);

                                // See if queue is full; if so, remove the oldest entry
                                if(falsePositiveDataStorageQueue.size() == Constant.NUM_FP_TO_LOG) {
                                    falsePositiveDataStorageQueue.poll();
                                }
                                falsePositiveDataStorageQueue.offer(newWindow);

                            }
                        }
                        /*else if(predictedClassValue.equals(HORN_CLASS)) {
                            detected = Constant.HORN;
                            carPredict++;
                            if(!isConnected) {
                                //Alert();
                            }

                            if (isConnected && btDataArrayList.size() > 0) {
//                                int []train1 = new int[8];
//                                for(int i=0; i<8; i++) {
                                temBt = btDataArrayList.get(0);
                                double[] btFeature = temBt.FeatureGen();
                                String tempString = temBt.genString();
                                valueHornFile = valueHornFile+tempString;
                                Instance localInstance = new DenseInstance(btFeature);
//                            Object hornDistVal = classifierHornDistance.classify(localInstance);



//                                Log.d("CCR CLASSIFY", btFeature[7]+ " "+ btFeature[8]+" "+btFeature[9]);



//                                Log.d("Feature Test", tempString);
                                Object hornDistVal = classifierHornDistance.classify(localInstance);
                                Object hornDirVal = classifierHornDirection.classify(localInstance);
                                char ct  = hornDirVal.toString().charAt(0);
                                int vt = (int) ct;
                                int r = vt - 96;
                                cls = " "+ r;

                                char ct_d  = hornDistVal.toString().charAt(0);
                                int vt_d = (int) ct_d;
                                int r_t = vt_d - 96;



                                btDataArrayList.remove(0);


//                                    int quard1 = Integer.parseInt((hornDirVal.toString().charAt(1) + ""));
//                                    train1[quard1]++;
//                                }
//                                generatePi(quard);
//                                int max = train1[0];
//                                int max_i = 0;
//
//                                for (int i = 1; i < 8; i++) {
//                                    if (train1[i] > max) {
//                                        max = train1[i];
//                                        max_i = i;
//                                    }
//                                }
//                                char ct  = hornDirVal.toString().charAt(0);
//                                int vt = (int) ct;
//                                int r = vt - 96;
//                                cls = " "+ r;
////                                quardrant = (9 - (max_i+1))+8;
                                quardrant = r+4;
                                distance = r_t;
                                //Alert();
//                                generatePi(max_i);
                            }



                        }*/
                        else {
                            carPredict = 0;
                            detected = Constant.NONE;
                            /*quardrant = 20;
                            distance = 0;*/
                            xPoint = Double.NEGATIVE_INFINITY;
                            yPoint = Double.NEGATIVE_INFINITY;
                            temBt = null;

                            // For Logging Detection False Negatives
                            if(Constant.LOGGING_FLAG > 0) {

                                Long timestamp = new Long(System.currentTimeMillis() / 1000);
                                DetectionDataStorage newWindow = new DetectionDataStorage(timestamp, inputSignal, feature);

                                // See if queue is full; if so, remove the oldest entry
                                if(falseNegativeDataStorageQueue.size() == Constant.NUM_FN_TO_LOG) {
                                    falseNegativeDataStorageQueue.poll();
                                }
                                falseNegativeDataStorageQueue.offer(newWindow);

                            }

                        }
                    }

                    // Logging features and classifier results
                    // Order of features written: zcr, power, ccr, detection class, direction class, distance class, detection features
                    try {

                        if (Constant.LOGGING_FLAG > 0) {

                            String timestamp_str = String.valueOf(System.currentTimeMillis()) + ",";

                            // Convert detection features to string
                            String detection_feature_string = "";
                            if(feature == null) {
                                for(int i = 0; i < Constant.B_GCC; i++) {
                                    detection_feature_string = detection_feature_string + "-Inf,";
                                }
                            }
                            else{
                                for(int i = 0; i < feature.length; i++) {
                                    detection_feature_string = detection_feature_string + Double.toString(feature[i]) + ",";
                                }
                                feature = null;
                            }

                            String curr_headset_features_string = "";
                            // Convert headset features to string
                            if ((temBt == null && btDataArrayList.size() < 1) || !isConnected) {

                                // Case where headset is not connected or if a car/honk is not detected.
                                curr_headset_features_string = "-Inf,-Inf,-Inf,-Inf,-Inf,-Inf,-Inf,-Inf,-Inf,-Inf,";
                            }
                            else {
                                BTData bt_feature_to_log = temBt;

                                if(bt_feature_to_log == null) {
                                    bt_feature_to_log = btDataArrayList.get(0);
                                }

                                // Case where the headset is connected or if a car/honk was detected
                                double[] curr_headset_features = bt_feature_to_log.FeatureGen();
                                for(int i = 0; i < curr_headset_features.length; i++) {
                                    curr_headset_features_string = curr_headset_features_string +
                                                                   Double.toString(curr_headset_features[i]) +
                                                                   ",";
                                }
                            }

                            String xPoint_str = Double.toString(xPoint);
                            String yPoint_str = Double.toString(yPoint);
                            if(xPoint == Double.NEGATIVE_INFINITY) {
                                xPoint_str = "-Inf";
                            }
                            if(yPoint == Double.NEGATIVE_INFINITY) {
                                yPoint_str = "-Inf";
                            }

                            // Write
                            feature_classifier_log_writer.write(timestamp_str +
                                                                curr_headset_features_string +
                                                                Integer.toString(detected) + "," +
                                                                Integer.toString(quardrant) + "," +
                                                                Integer.toString(distance) + "," +
                                                                xPoint_str + "," +
                                                                yPoint_str + "," +
                                                                detection_feature_string + "\n");
                        }
                    } catch(Exception e) {
                        Log.e(Constant.TAG, e.toString());
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            carDetecLocal = null;
        }
    }

    //Bluetooth service initialization
    private void service_init() {
        Log.e("SERVICEINTIT", "serviceinit......................");

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




    void updatePlotView(double val) {
        //mpv.whichPie = quardrant;
        //mpv.distance = distance;
        //myPlotView.insertPoint(val, detected, distance);
        mpv.xPoint = xPoint;
        mpv.yPoint = yPoint;
        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            public void run() {
                //myPlotView.invalidate();
                mpv.invalidate();
            }
        });
    }

    public final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(Constant.TAG, "UART_CONNECT_MSG");
                        btnConnectDisconnect.setText(R.string.disconnect);
                        deviceName = mDevice.getName();
                        tvDeviceName.setText(mDevice.getName() + R.string.connected);
                        mState = UART_PROFILE_CONNECTED;
//                        fopen();
                        counter = 0;
                    }

                });
            }
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        btnConnectDisconnect.setText(R.string.connect);
                        deviceName = "";
                        tvDeviceName.setText(R.string.none_connect);
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
//                        file_close();

                        // Deinitialize everything
                        delim_found = false;
                        delim_index = 0;
                        data_index = 0;
                        counter = 0;
                    }
                });
            }

            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }

            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                Log.d("Recv bytes raw:", "" + txValue.length);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            System.out.println(txValue.length);
                            for (byte aTxValue : txValue) {
                                BTData data = new BTData();
                                int index =0;

                                // Case where delimiter is already found; parse data
                                if (delim_found) {

                                    // Keep adding bytes to the byte array until we have read all windows.
                                    byte_data[data_index++] = aTxValue;

                                    // If all bytes have been read, then save them to file
                                    if (data_index >= byte_data.length) {

                                        int b1, b2, b3, b4;

                                        // Write delays
                                        data.ccr[0] = byte_data[0];
                                        data.ccr[1] = byte_data[1];
                                        data.ccr[2] = byte_data[2];

                                        Log.d("CCR",data.ccr[0]+ " "+data.ccr[1]+" "+ data.ccr[2]);

                                        // Write powers; skip first four MSB bytes since
                                        // We don't need that much precision
                                        b1 = byte_data[7] & 0xFF;
                                        b2 = byte_data[8] & 0xFF;
                                        b3 = byte_data[9] & 0xFF;
                                        b4 = byte_data[10] & 0xFF;
                                        data.pwr[0] = ((b1 << 24) | (b2 << 16) | (b3 << 8) | b4);

                                        b1 = byte_data[15] & 0xFF;
                                        b2 = byte_data[16] & 0xFF;
                                        b3 = byte_data[17] & 0xFF;
                                        b4 = byte_data[18] & 0xFF;
                                        data.pwr[1] = ((b1 << 24) | (b2 << 16) | (b3 << 8) | b4);

                                        b1 = byte_data[23] & 0xFF;
                                        b2 = byte_data[24] & 0xFF;
                                        b3 = byte_data[25] & 0xFF;
                                        b4 = byte_data[26] & 0xFF;
                                        data.pwr[2] = ((b1 << 24) | (b2 << 16) | (b3 << 8) | b4);

                                        // Write out ZCR
                                        b1 = byte_data[27] & 0xFF;
                                        b2 = byte_data[28] & 0xFF;
                                        data.zcr[0] = ((b1 << 8) | b2);

                                        b1 = byte_data[29] & 0xFF;
                                        b2 = byte_data[30] & 0xFF;
                                        data.zcr[1] = ((b1 << 8) | b2);

                                        b1 = byte_data[31] & 0xFF;
                                        b2 = byte_data[32] & 0xFF;
                                        data.zcr[2] = ((b1 << 8) | b2);

                                        b1 = byte_data[33] & 0xFF;
                                        b2 = byte_data[34] & 0xFF;
                                        data.zcr[3] = ((b1 << 8) | b2);
                                        data.name = index;
                                        data.FeatureGen();
                                        Log.d("BLUETOOTH", data.toString());

                                        btDataArrayList.add(data);
                                        counter++;
                                        if (btDataArrayList.size() > 2) {
                                            btDataArrayList.remove(0);
                                        }
                                        // All bytes found, so need to find next window
                                        delim_found = false;
                                        delim_index = 0;
                                        data_index = 0;
                                    }
                                }

                                // Case where delimiter not found
                                else {

                                    // The byte is part of the delimiter we are looking for
                                    if (aTxValue == DELIM[delim_index] || delim_index == DELIM.length - 2) {

                                        // Just found the delimiter, so now we start reading data
                                        if (++delim_index >= DELIM.length) {
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
                            if (counter % 10 == 0 && !printed) {

                                tvDeviceName.setText(deviceName + ", " + counter + cls);
                                printed = true;
                            } else {
                                printed = false;
                            }

                        } catch (Exception e) {
                            Log.e(Constant.TAG, e.toString());
                        }
                    }
                });
            }
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                Toast.makeText(context, "Device doesn't support UART. Disconnecting", Toast.LENGTH_LONG).show();
                mService.disconnect();
                isConnected = false;
            }


        }
    };

    public void Alert() {
        if (Constant.sound) {

            try {
                if(quardrant== 1 || quardrant ==3 || quardrant ==5 || quardrant==7) {
                    sp.play(soundIds[0], 1, 1, 1, 0, (float) 1.0);
                }
                else if(quardrant ==2 || quardrant==6){
                    sp.play(soundIds[0], 0, 1, 1, 0, (float) 1.0);
                }
                else if(quardrant ==4 || quardrant==8){
                    sp.play(soundIds[0], 1, 0, 1, 0, (float) 1.0);
                }
                else{
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (Constant.vibrate) {

            Vibrator vibrator;
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(500);
        }
    }


    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
//            showMessage("nRFUART's running in background.\n             Disconnect to exit");
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
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(Constant.TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(Constant.TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;
        sp.release();

    }

}
