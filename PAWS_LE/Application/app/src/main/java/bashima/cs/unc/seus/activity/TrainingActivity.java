/*
 * MIT License
 *
 * Copyright (c) 2018, Stephen Xia, Columbia Intelligent and Connected Systems Lab (ICSL), Columbia University
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */ 
package bashima.cs.unc.seus.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.classification.tree.RandomForest;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import bashima.cs.unc.seus.constant.Constant;
import bashima.cs.unc.seus.featuer.GenericCC;
import bashima.cs.unc.seus.featuer.MFCCFeatureExtract;
import bashima.cs.unc.seus.featuer.WindowFeature;
import libsvm.LibSVM;
import seus.bashima.cs.unc.seus.R;

public class TrainingActivity extends AppCompatActivity {

    Button btTrainCSV;
    Button btTrain;
    Button btFeature;
    Button btTrainDir;
    Button btConvert;
    Button btTest;
    Button btDirTest;
    TextView tvStatus;
    public byte audio[];
    Dataset data;

    Dataset datah;
    Context context;
    public LibSVM libSVM;
    public RandomForest randomForestDetection;
    public RandomForest randomForestCar;
    //public KNearestNeighbors randomForestCar;
    public RandomForest randomForestHorn;
    public RandomForest randomForestCarDist;
    public RandomForest randomForestHornDist;
    String modelName = "model.ser";

    private GenericCC genericCC_features;

    private static final String TAG = "TrainingActivity";
    private static final String genericCCFolderName = "GenericCC/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        context = this;

        android.support.v7.app.ActionBar menu = getSupportActionBar();
        if (menu != null) {
            menu.setDisplayHomeAsUpEnabled(true);
            menu.setDisplayShowHomeEnabled(true);
            menu.setLogo(R.mipmap.ic_launcher);
            menu.setDisplayUseLogoEnabled(true);
        }

        tvStatus = (TextView) findViewById(R.id.tv_status);
        btTrainDir = (Button) findViewById(R.id.bt_train_dir);
        btTrainDir.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                trainCarDirModel();
                //trainHornDirModel();
                //trainCarDistModel();
                //trainHornDistModel();
                Toast.makeText(context, "Direction Calculated", Toast.LENGTH_LONG).show();
            }
        });

        // Train Detection Classifier from Features generated
        btTrain = (Button) findViewById(R.id.bt_train);
        btTrain.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //tvStatus.setText("Calculating MFCC");
                //readMFCC();
                trainGCCModel();
            }
        });

        // Train detection classifier from csv files
        btTrainCSV = (Button) findViewById(R.id.bt_train_csv);
        btTrainCSV.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //tvStatus.setText("Calculating MFCC");
                //readMFCC();
                trainGCCModelCSV();
            }
        });



        // Generate Features From .wav Files stored.
        btFeature = (Button) findViewById(R.id.bt_calculate_feature);
        btFeature.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                for (int i = 1000; i < 10000; i = i + 100) {
//                    Constant.a = i;
//                    mfccCalcutation();
//                    Log.e("Output", "MFCC Calculated for " + Constant.a);
//                }
                //mfccCalcutation();
                genericCC_features = new GenericCC(Constant.SAMPLE_RATE, Constant.Wl, Constant.Ts, Constant.n_frames_per_window, Constant.B_GCC, Constant.a_GCC, Constant.b_GCC);
                generateGCCFeaturesFromDataset();
            }
        });

        // Converting feature docs to human readable text
        btConvert = (Button) findViewById(R.id.bt_convert);
        btConvert.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                convertDetectionFeaturestoHumanReadable();
            }
        });

        // test classifier button
        btTest = (Button) findViewById(R.id.bt_test);
        btTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                testDetectionClassifier();
            }
        });

        // test direction classifier button
        btDirTest = (Button) findViewById(R.id.bt_dir_test);
        btDirTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                testDirectionClassifier();
            }
        });

        data = new DefaultDataset();
//        datac = new DefaultDataset();
        datah = new DefaultDataset();

        randomForestHorn = new RandomForest(50);

        // Detection Classifier
        //libSVM = new LibSVM();
        randomForestDetection = new RandomForest(50);
    }

    public void mfccCalcutation() {
        tvStatus.setText("Calculating MFCC");
        File folder = new File(Constant.FILE_PATH, Constant.FOLDER_NAME + "Audio/");
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
            } else {
                String fileName = fileEntry.getName();
                String extention = "";
                int pos = fileName.lastIndexOf(".");
                if (pos > 0) {
                    extention = fileName.substring(pos + 1);
                    fileName = fileName.substring(0, pos);

                }
                if (extention.equals("wav")) {
                    File newFile = new File(fileName + "_1.txt");
                    Log.d("mfcc exists???", fileName + "_1.txt");
                    if (newFile.exists()) {
                        Log.d("nofile", "NO FILE");
                    } else {
                        calculateMFCC(fileEntry.getName());
                    }
                }
            }
        }

    }


    public void calculateMFCC(String wavFileName) {
        File fileIn = new File(Constant.FILE_PATH + Constant.FOLDER_NAME + "Audio/", wavFileName);
        Log.i("current mfcc wav file", wavFileName);
        int fileSize = (int) fileIn.length();
        String[] nameWOExtension = wavFileName.split("\\.");
//        Log.i("no", nameWOExtension.length + "");
//        Log.i("ne", nameWOExtension[1]);

        audio = new byte[(int) fileIn.length()];//size & length of the file
        InputStream inputStream = null;
        int subArrayNumber = (int) Math.floor(fileSize / Constant.BUFFERSIZE);
        if (subArrayNumber < 1)
            return;


        try {
            inputStream = new FileInputStream(fileIn);

            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, Constant.SAMPLE_RATE);
            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);      //  Create a DataInputStream to read the audio data from the saved file

            int i = 0;   //  Read the file into the "audio" array
            while (dataInputStream.available() > 0)

            {
                audio[i] = dataInputStream.readByte();     //  This assignment does not reverse the order
                i++;
            }

            dataInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < subArrayNumber; i++) {
//        for (int i = 0; i < 1; i++) {
            MFCCFeatureExtract mfccFeatures = null;
            List<WindowFeature> windowFeatureList = null;
            byte[] temp = new byte[Constant.BUFFERSIZE];
            System.arraycopy(audio, i * Constant.BUFFERSIZE, temp, 0, Constant.BUFFERSIZE);
            double[] inputSignal = Constant.convertSignalToDouble(temp);
//            Log.d("imput signal length", inputSignal.length + "");
            mfccFeatures = new MFCCFeatureExtract(inputSignal, Constant.Tw, Constant.Ts, Constant.SAMPLE_RATE, Constant.Wl);
            windowFeatureList = mfccFeatures.getListOfWindowFeature();
            double[] feature = MFCCFeatureExtract.generateDataSet(windowFeatureList);

            Log.d("before write", doubleToString(feature));
            try {
//                Log.v("full name", Constant.FILE_PATH + Constant.FOLDER_NAME + nameWOExtension[1] + "_" + (i + 1) + ".ser");
//                Log.d("only name", nameWOExtension[0] + "_" + (i + 1) + ".ser");
                if (Constant.prnt) {
                    FileOutputStream fout = new FileOutputStream(Constant.FILE_PATH + Constant.FOLDER_NAME + "MFCC/" + nameWOExtension[0] + "_" + (i + 1) + ".txt");
                    ObjectOutputStream oos = new ObjectOutputStream(fout);
                    oos.writeObject(feature);
                    oos.close();
                    System.out.println("Done");
                }

                String className = "";
                if (wavFileName.contains("car")) {
                    className = Constant.CAR_CLASS;
                } else if (wavFileName.contains("horn")) {
                    className = Constant.HORN_CLASS;
                } else {
                    className = Constant.NONE_CLASS;
                }


//                String name = "Data_" + ((int) Constant.a) + "_" + ((int) Constant.b) + ".csv";
//                String data = doubleToString(feature) + "," + className + "\n";
//
//
//                FileWriter pw = new FileWriter(Constant.FILE_PATH + Constant.FOLDER_NAME + "A_Var/"+name, true);
//
//                pw.append(data);
//
//                pw.flush();
//                pw.close();


                System.out.println("Done Data");


            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        Log.v("finish", "mfcc calculated");
        tvStatus.setText("MFCC Calculated");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void readMFCC() {
        tvStatus.setText("Building Detection Model");
        File folder = new File(Constant.FILE_PATH, Constant.FOLDER_NAME + "MFCC/");
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
            } else {
                String fileName = fileEntry.getName();
                String extention = "";
                int pos = fileName.lastIndexOf(".");
                if (pos > 0) {
                    extention = fileName.substring(pos + 1);
                }
                if (extention.equals("txt")) {
                    try {

                        FileInputStream fin = new FileInputStream(fileEntry);
                        ObjectInputStream ois = new ObjectInputStream(fin);
                        double[] feature = (double[]) ois.readObject();
                        ois.close();
                        String className = "";
                        if (fileName.contains("car")) {
                            className = Constant.CAR_CLASS;
                        } else if (fileName.contains("horn")) {
                            className = Constant.HORN_CLASS;
                        } else {
                            className = Constant.NONE_CLASS;
                        }
                        Instance instance = new DenseInstance(feature, className);
                        data.add(instance);
                        Log.d("after read", doubleToString(feature));

                    } catch (Exception ex) {
                        ex.printStackTrace();

                    }
                }
            }
        }
        libSVM.buildClassifier(data);
        FileOutputStream fout = null;
        File f = new File(getCacheDir() + modelName);
//        try {
//
//            InputStream is = getAssets().open(detectionModelName);
//            int size = is.available();
//            byte[] buffer = new byte[size];
//            is.read(buffer);
//            is.close();
//
//
//            FileOutputStream fos = new FileOutputStream(f);
//            fos.write(buffer);
//            fos.close();
//        } catch (Exception e) { throw new RuntimeException(e); }
        try {
            Log.d("classifier", "classifier built");
            fout = new FileOutputStream(Constant.FILE_PATH + Constant.FOLDER_NAME + modelName);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(libSVM);
            oos.close();
            Log.d("classifier", "classifier built done");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        tvStatus.setText("Detection Model Build");
        Toast.makeText(context, "Classifier Build", Toast.LENGTH_LONG).show();


    }

    public String doubleToString(double[] doub) {
        String temp = doub[0] + "";

        for (int i = 1; i < doub.length; i++) {
            temp = temp + "," + doub[i];
        }

        return temp;
    }

    public double[] stringToDouble(String[] doub) {
        double[] temp = new double[doub.length];
        for (int i = 0; i < doub.length; i++) {
            temp[i] = Double.parseDouble(doub[i]);
        }
        return temp;
    }

    public double[] arrayToDouble(ArrayList<double[]> doub) {
        int i = 0;
        int size = doub.size() * 10;
        Log.d("SIZE", size + "");
        double[] target = new double[size];
        for (double[] doubles : doub) {
            for (; i < 10; i++) {
                target[i] = doubles[i];
            }
        }

        return target;
    }

    public void printArraylist(ArrayList<double[]> doub) {
        int i = 0;
        int size = doub.size() * 10;
        Log.d("SIZE", size + "");
        for (double[] doubles : doub) {
            for (; i < doubles.length; i++) {
                Log.d("ArrayPrint", i + "");
            }
        }
    }

    public void printArray(double[] doub) {
        for (double i : doub
                ) {
            Log.d("Double: ", i + "");

        }
    }

    public double[] doubArr2Doub(ArrayList doub) {
        double[] temp = new double[doub.size()];
        for (int i = 0; i < doub.size(); i++) {
            temp[i] = (double) doub.get(i);
            Log.d("CONVERTION", temp[i] + "");
        }
        return temp;
    }

    public void trainCarDirModel() {
        tvStatus.setText("Building Car Direction Model");
//        File folder = new File(Constant.FILE_PATH, Constant.FOLDER_NAME + "CarDir/");
//        Log.d("FILE NAME", folder.getAbsolutePath());
        Dataset dataset = new DefaultDataset();
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader(Constant.FILE_PATH + Constant.FOLDER_NAME + "carWeka.csv"));
            while ((line = br.readLine()) != null) {

                // Stephen: Only use two ccrs (mic1 - mic2 and mic1 - mic3)
                int num_features = 3;
                double[] value = new double[num_features];
                // use comma as separator
                String[] data = line.split(cvsSplitBy);
                for (int i = 0; i < num_features; i++) {
                    value[i] = Double.parseDouble(data[i]);
                }
                //value[0] = Double.parseDouble(data[7]);
                //value[1] = Double.parseDouble(data[8]);
                Instance instance = new DenseInstance(value, data[3]);
//                Random rand = new Random();
//
//                int n = rand.nextInt(10) + 1;
//                if (n > 8) {
////					Data temp = new Data();
////					temp.value = value;
////					temp.classes = data[10];
//                    test.add(instance);
//                } else {
                dataset.add(instance);
//                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        System.out.println("Data read done");

//        for (final File fileEntry : folder.listFiles()) {
//            if (fileEntry.isDirectory()) {
//            } else {
//                String fileName = fileEntry.getName();
//                String extention = "";
//                int pos = fileName.lastIndexOf(".");
//                if (pos > 0) {
//                    extention = fileName.substring(pos + 1);
//                }
//
//                if (extention.equals("csv")) {
//                    try {
//                        String className = "c" + fileName.charAt(0);
//                        Log.e("Classname", className);
//                        String line = "";
//                        String cvsSplitBy = ",";
//                        BufferedReader br = new BufferedReader(new FileReader(Constant.FILE_PATH + Constant.FOLDER_NAME + "CarDir/" + fileName));
//                        while ((line = br.readLine()) != null) {
//
//                            // use comma as separator
//                            String[] splitting = line.split(cvsSplitBy);
//                            Log.e("READING", fileName + " " + className);
//
//                            double[] feature = stringToDouble(splitting);
////                            Log.d("data", doubleToString(feature));
//                            Instance instance = new DenseInstance(feature, className);
//
//                            datac.add(instance);
//                        }
//
//                    } catch (Exception ignored) {
//                    }
//                }
//            }
//
//        }
        randomForestCar = new RandomForest(50);
        //randomForestCar = new KNearestNeighbors(7);
        Log.d("Classifier Car", "Start Building Classifier Car Direction");
//        printDataset(datac);
        randomForestCar.buildClassifier(dataset);
        FileOutputStream fout = null;

        try {
            Log.d("classifier", "car classifier built");
            fout = new FileOutputStream(Constant.FILE_PATH + Constant.FOLDER_NAME + Constant.carDirModelName);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(randomForestCar);
            oos.close();
            fout.close();
            Log.d("classifier", "car direction classifier wrote");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(context, "Car Direction Classifier Build", Toast.LENGTH_LONG).show();
        tvStatus.setText("Building Car Direction Model Done");
    }


    public void trainHornDirModel() {
        tvStatus.setText("Building Horn Direction Model");
        File folder = new File(Constant.FILE_PATH, Constant.FOLDER_NAME + "HornDir/");
        Log.d("FILE NAME", folder.getAbsolutePath());
        Dataset dataset = new DefaultDataset();
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader(Constant.FILE_PATH + Constant.FOLDER_NAME + "hornWeka.csv"));
            while ((line = br.readLine()) != null) {

                double[] value = new double[10];
                // use comma as separator
                String[] data = line.split(cvsSplitBy);
                for (int i = 0; i < 10; i++) {
                    value[i] = Double.parseDouble(data[i]);
                }
                Instance instance = new DenseInstance(value, data[10]);
//                Random rand = new Random();
//
//                int n = rand.nextInt(10) + 1;
//                if (n > 8) {
////					Data temp = new Data();
////					temp.value = value;
////					temp.classes = data[10];
//                    test.add(instance);
//                } else {
                dataset.add(instance);
//                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        System.out.println("Data read done");


        randomForestHorn.buildClassifier(dataset);
//        Constant.classifierHornDirection = randomForestHorn;
        FileOutputStream fout = null;

        try {
            Log.d("classifier", "horn classifier built");
            fout = new FileOutputStream(Constant.FILE_PATH + Constant.FOLDER_NAME + Constant.hornDirModelName);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(randomForestHorn);
            oos.close();
            Log.d("classifier", "horn classifier wrote");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(context, "Classifier Build", Toast.LENGTH_LONG).show();
        tvStatus.setText("Build Horn Direction Model");

    }

    public void printDataset(Dataset dataset) {
        File file = new File(Constant.FILE_PATH + "\filename.txt");
        FileWriter fw = null;
        try {
            fw = new FileWriter(file.getAbsoluteFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter bw = new BufferedWriter(fw);
        // if file doesnt exists, then create it
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (Instance instance : dataset
                ) {
            String s = "";
            for (int i = 0; i < 10; i++) {
                s = s + " " + instance.value(i);
            }
            s = s + " " + instance.classValue() + "\n";


            try {

                bw.append(s);

            } catch (IOException e) {
                e.printStackTrace();
            }


//            System.out.println("Done");
        }
        try {
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void trainCarDistModel() {
        tvStatus.setText("Building Car Distance Model");
        File folder = new File(Constant.FILE_PATH, Constant.FOLDER_NAME + "CarDist/");
        Log.d("FILE NAME", folder.getAbsolutePath());
        Dataset dataset = new DefaultDataset();
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader(Constant.FILE_PATH + Constant.FOLDER_NAME + "carDistWeka.csv"));
            while ((line = br.readLine()) != null) {

                double[] value = new double[10];
                // use comma as separator
                String[] data = line.split(cvsSplitBy);
                for (int i = 0; i < 10; i++) {
                    value[i] = Double.parseDouble(data[i]);
                }
                Instance instance = new DenseInstance(value, data[10]);
//                Random rand = new Random();
//
//                int n = rand.nextInt(10) + 1;
//                if (n > 8) {
////					Data temp = new Data();
////					temp.value = value;
////					temp.classes = data[10];
//                    test.add(instance);
//                } else {
                dataset.add(instance);
//                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        System.out.println("Data read done");

        randomForestCarDist = new RandomForest(50);
        Log.d("Classifier Car", "Start Building Classifier Car Distance");
//        printDataset(datac);
        randomForestCarDist.buildClassifier(dataset);
        FileOutputStream fout = null;

        try {
            Log.d("classifier", "car distance classifier built");
            fout = new FileOutputStream(Constant.FILE_PATH + Constant.FOLDER_NAME + Constant.carDistModelName);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(randomForestCarDist);
            oos.close();
            fout.close();
            Log.d("classifier", "car distance classifier wrote");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(context, "Car Distance Classifier Build", Toast.LENGTH_LONG).show();
        tvStatus.setText("Building Car Distance Model Done");
    }

    public void trainHornDistModel() {
        tvStatus.setText("Building Horn Distance Model");
        File folder = new File(Constant.FILE_PATH, Constant.FOLDER_NAME + "HornDist/");
        Log.d("FILE NAME", folder.getAbsolutePath());
        Dataset dataset = new DefaultDataset();
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader(Constant.FILE_PATH + Constant.FOLDER_NAME + "hornDistWeka.csv"));
            while ((line = br.readLine()) != null) {

                double[] value = new double[10];
                // use comma as separator
                String[] data = line.split(cvsSplitBy);
                for (int i = 0; i < 10; i++) {
                    value[i] = Double.parseDouble(data[i]);
                }
                Instance instance = new DenseInstance(value, data[10]);
//                Random rand = new Random();
//
//                int n = rand.nextInt(10) + 1;
//                if (n > 8) {
////					Data temp = new Data();
////					temp.value = value;
////					temp.classes = data[10];
//                    test.add(instance);
//                } else {
                dataset.add(instance);
//                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        System.out.println("Data read done");


        randomForestHorn.buildClassifier(dataset);
//        Constant.classifierHornDirection = randomForestHorn;
        FileOutputStream fout = null;

        try {
            Log.d("classifier", "horn distance classifier built");
            fout = new FileOutputStream(Constant.FILE_PATH + Constant.FOLDER_NAME + Constant.hornDistModelName);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(randomForestHornDist);
            oos.close();
            Log.d("classifier", "horn distance classifier wrote");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(context, "Horn Distance Classifier Build", Toast.LENGTH_LONG).show();
        tvStatus.setText("Build Horn Distance Model");

    }

    private void generateGCCFeaturesFromDataset()
    {
        File folder = new File(Constant.FILE_PATH, Constant.FOLDER_NAME + "Audio/");
        int count = 0;

        tvStatus.setText("Generating Features from .wav Files....");
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
            } else {
                String fileName = fileEntry.getName();
                String extension = "";
                int pos = fileName.lastIndexOf(".");
                if (pos > 0) {
                    extension = fileName.substring(pos + 1);
                    fileName = fileName.substring(0, pos);

                }

                // Generate features from all .wav files in Audio/ and count the number of examples successfully generated.
                if (extension.equals("wav")) {
                    File newFile = new File(fileName + "_1.txt");
                    if (newFile.exists()) {
                        Log.d(TAG, fileName + "_1.txt already exists; skipping.");
                    } else {

                        // Generate and save features; count number of files where at least one example was generated
                        if(genericCCFeaturesFromFile(fileEntry.getName())) {
                            count = count + 1;
                        }
                    }
                }
            }
        }

        tvStatus.setText("Files processed for GenericCC features: " + String.valueOf(count));
    }

    /**
     * Computes genericCC features from an audio file in the Audio/ folder and saves it.
     * @param wavFileName
     * @return True if at least some features were successfully extracted; false otherwise
     */
    private boolean genericCCFeaturesFromFile(String wavFileName)
    {
        File fileIn = new File(Constant.FILE_PATH + Constant.FOLDER_NAME + "Audio/", wavFileName);
        Log.d(TAG, "Current File: " + wavFileName);
        int fileSize = (int) fileIn.length();
        String[] nameWOExtension = wavFileName.split("\\.");

        Log.d(TAG, "File length Long: " + Long.toString(fileIn.length()));
        Log.d(TAG, "File length int: " + Integer.toString(fileSize));

        // Check if the file has already been processed. If so, return.
        File featuresFile = new File(Constant.FILE_PATH + Constant.FOLDER_NAME + genericCCFolderName + nameWOExtension[0] + "_1" + ".txt");
        if(featuresFile.exists()) {
            Log.d(TAG, "File already processed; skipping.");
            return false;
        }

        audio = new byte[(int) fileIn.length()];//size & length of the file
        InputStream inputStream = null;
        int subArrayNumber = (int) Math.floor(fileSize / Constant.BUFFERSIZE);
        if (subArrayNumber < 1) {
            Log.d(TAG, "Not Enough Samples in File.");
            return false;
        }

        try {
            inputStream = new FileInputStream(fileIn);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, Constant.SAMPLE_RATE);
            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);      //  Create a DataInputStream to read the audio data from the saved file

            int i = 0;   //  Read the file into the "audio" array
            Log.d(TAG, "Data Bytes available: " + dataInputStream.available());
            while (dataInputStream.available() > 0)
            {
                audio[i] = dataInputStream.readByte();     //  This assignment does not reverse the order
                i++;
            }
            dataInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "Could not find file: " + wavFileName);
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Could not read file: " + wavFileName);
            return false;
        }

        // Check if the features folder exists. If not, create it.
        File gccFolder = new File(Constant.FILE_PATH + Constant.FOLDER_NAME + genericCCFolderName);
        if(!gccFolder.exists()) {
            Log.d(TAG, "Directory: " + Constant.FILE_PATH + Constant.FOLDER_NAME + genericCCFolderName + " - does not exist; creating....");
            if(gccFolder.mkdir()) {
                Log.d(TAG, "Directory: " + Constant.FILE_PATH + Constant.FOLDER_NAME + genericCCFolderName + " - successfully created.");
            } else {
                Log.e(TAG, "Unable to Create Directory: " + Constant.FILE_PATH + Constant.FOLDER_NAME + genericCCFolderName + " - exiting");
                return false;
            }
        }

        // Extract and save features
        boolean success = false;
        for (int i = 0; i < subArrayNumber; i++) {

            // Process input signal
            byte[] temp = new byte[Constant.BUFFERSIZE];
            System.arraycopy(audio, i * Constant.BUFFERSIZE, temp, 0, Constant.BUFFERSIZE);
            double[] inputSignal = Constant.convertSignalToDouble(temp);

            // Generate features
            double[] feature = genericCC_features.generate_features(inputSignal);

            Log.d(TAG, "GCC Features: " + doubleToString(feature));
            try {

                // Write Features to File
                FileOutputStream fout = new FileOutputStream(Constant.FILE_PATH + Constant.FOLDER_NAME + genericCCFolderName + nameWOExtension[0] + "_" + (i + 1) + ".txt");
                ObjectOutputStream oos = new ObjectOutputStream(fout);
                oos.writeObject(feature);
                oos.close();
                Log.d(TAG, "Features written to file.");

                String className = "";
                if (wavFileName.contains("car")) {
                    className = Constant.CAR_CLASS;
                } else if (wavFileName.contains("horn")) {
                    className = Constant.HORN_CLASS;
                } else {
                    className = Constant.NONE_CLASS;
                }

                // At least one feature extracted from file, so set to true.
                success = true;
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e(TAG, "Could not write features to file.");
            }
        }

        Log.d(TAG, "GCC features calculated.");
        return success;
        //tvStatus.setText("Features Generated");
    }

    /**
     * Trains the Detection model using Generic CC features from a csv file
     */
    private void trainGCCModelCSV() {

        Dataset dataset = new DefaultDataset();
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        int num_cars = 0;
        int num_noncars = 0;

        try {

            br = new BufferedReader(new FileReader(Constant.FILE_PATH + Constant.FOLDER_NAME + "detection_features.csv"));
            while ((line = br.readLine()) != null) {

                double[] value = new double[20];
                // use comma as separator
                String[] data = line.split(cvsSplitBy);
                for (int i = 0; i < data.length - 1; i++) {
                    value[i] = Double.parseDouble(data[i]);
                }

                String className = "";
                if (Double.parseDouble(data[data.length - 1]) > 0) {
                    className = Constant.CAR_CLASS;
                    num_cars = num_cars + 1;
                } else {
                    className = Constant.NONE_CLASS;
                    num_noncars = num_noncars + 1;
                }
                Instance instance = new DenseInstance(value, className);
//                Random rand = new Random();
//
//                int n = rand.nextInt(10) + 1;
//                if (n > 8) {
////					Data temp = new Data();
////					temp.value = value;
////					temp.classes = data[10];
//                    test.add(instance);
//                } else {
                dataset.add(instance);
//                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        System.out.println("Data read done");
        Log.d("trainGCCModelCSV", "Cars: " + String.valueOf(num_cars) + ", Non-cars: " + String.valueOf(num_noncars));


        Log.d(TAG, "Building detection classifier");
        //libSVM.buildClassifier(data);
        randomForestDetection.buildClassifier(dataset);
        Log.d(TAG, "Done building detection classifier");
        FileOutputStream fout = null;
        try {
            Log.d(TAG, "Saving detection classifier");
            fout = new FileOutputStream(Constant.FILE_PATH + Constant.FOLDER_NAME + modelName);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            //oos.writeObject(libSVM);
            oos.writeObject(randomForestDetection);
            oos.close();
            Log.d(TAG, "Done saving detection classifier");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "Could not find file: " + Constant.FILE_PATH + Constant.FOLDER_NAME + modelName);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Could not access file: " + Constant.FILE_PATH + Constant.FOLDER_NAME + modelName);
        }

        tvStatus.setText("Detection Model Built With " + String.valueOf(dataset.size()) + " Examples");
        Toast.makeText(context, "Detection Model Built", Toast.LENGTH_LONG).show();
    }

    /**
     * Trains the Detection model using Generic CC features generated and stored in .txt files in the GenericCC folder.
     */
    private void trainGCCModel() {
        File folder = new File(Constant.FILE_PATH, Constant.FOLDER_NAME + genericCCFolderName);
        int count = 0;

        // Collecting features from files to generate model
        tvStatus.setText("Building Detection Model....");
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
            } else {
                String fileName = fileEntry.getName();
                String extension = "";
                int pos = fileName.lastIndexOf(".");
                if (pos > 0) {
                    extension = fileName.substring(pos + 1);
                }

                // Process txt files only
                if (extension.equals("txt")) {
                    try {
                        FileInputStream fin = new FileInputStream(fileEntry);
                        ObjectInputStream ois = new ObjectInputStream(fin);
                        double[] feature = (double[]) ois.readObject();
                        ois.close();

                        Log.d(TAG, "GCC Features: " + doubleToString(feature));

                        // Class name determined by the label of the text file.
                        String className = "";
                        if (fileName.contains("car")) {
                            className = Constant.CAR_CLASS;
                        } else if (fileName.contains("horn")) {
                            className = Constant.HORN_CLASS;
                        } else {
                            className = Constant.NONE_CLASS;
                        }
                        Instance instance = new DenseInstance(feature, className);
                        data.add(instance);
                        Log.d(TAG, "Feature: " + doubleToString(feature));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Log.e(TAG, "Could not open file: " + fileEntry.getName());
                    }
                }
            }
        }

        // Build and Save Classifier
        Log.d(TAG, "Building detection classifier");
        //libSVM.buildClassifier(data);
        randomForestDetection.buildClassifier(data);
        Log.d(TAG, "Done building detection classifier");
        FileOutputStream fout = null;
        try {
            Log.d(TAG, "Saving detection classifier");
            fout = new FileOutputStream(Constant.FILE_PATH + Constant.FOLDER_NAME + modelName);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            //oos.writeObject(libSVM);
            oos.writeObject(randomForestDetection);
            oos.close();
            Log.d(TAG, "Done saving detection classifier");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "Could not find file: " + Constant.FILE_PATH + Constant.FOLDER_NAME + modelName);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Could not access file: " + Constant.FILE_PATH + Constant.FOLDER_NAME + modelName);
        }

        tvStatus.setText("Detection Model Built With " + String.valueOf(data.size()) + " Examples");
        Toast.makeText(context, "Detection Model Built", Toast.LENGTH_LONG).show();
    }

    /**
     * Converts generated features into human readable files
     */
    public void convertDetectionFeaturestoHumanReadable() {

        File folder = new File(Constant.FILE_PATH, Constant.FOLDER_NAME + genericCCFolderName);
        if(folder.exists()) {

            // Create folder for new files if it doesn't exist
            File folder_readable = new File(Constant.FILE_PATH, Constant.FOLDER_NAME + Constant.DETECTION_FEATURES_READABLE_FOLDER_NAME);
            if(!folder_readable.exists()) {
                folder_readable.mkdirs();
            }

            // Read features and convert to human readable form
            try {

                // Setup output writers
                FileOutputStream outputStreamCar = new FileOutputStream(Constant.FILE_PATH + Constant.FOLDER_NAME + Constant.DETECTION_FEATURES_READABLE_FOLDER_NAME + "car.csv");
                OutputStreamWriter outputStreamWriterCar = new OutputStreamWriter(outputStreamCar);
                FileOutputStream outputStreamNoCar = new FileOutputStream(Constant.FILE_PATH + Constant.FOLDER_NAME + Constant.DETECTION_FEATURES_READABLE_FOLDER_NAME + "negativeExamples.csv");
                OutputStreamWriter outputStreamWriterNoCar = new OutputStreamWriter(outputStreamNoCar);

                //for(int j = 1;j <= 282; j++) {
                //    String fileName = "";
                //    String extension = "txt";
                //    File fileEntry = new File(Constant.FILE_PATH, Constant.FOLDER_NAME + genericCCFolderName + "1_48000_test_" + Integer.toString(j) + ".txt");
                for (final File fileEntry : folder.listFiles()) {
                    if (fileEntry.isDirectory()) {
                    } else {
                        String fileName = fileEntry.getName();
                        String extension = "";
                        int pos = fileName.lastIndexOf(".");
                        if (pos > 0) {
                            extension = fileName.substring(pos + 1);
                        }


                        // Process txt files only
                        if (extension.equals("txt")) {

                            FileInputStream fin = new FileInputStream(fileEntry);
                            ObjectInputStream ois = new ObjectInputStream(fin);
                            double[] feature = (double[]) ois.readObject();
                            ois.close();

                            Log.d(TAG, "GCC Features: " + doubleToString(feature));

                            // Class name determined by the label of the text file.
                            int className = 0;
                            if (fileName.contains("car")) {
                                className = 1;
                            }

                            // Create readable string
                            String data = "";
                            for(int i = 0; i < feature.length; i++) {
                                data = data + Double.toString(feature[i]) + ",";
                            }
                            data = data + "\n";

                            // Write
                            if(className == 1) {
                                outputStreamWriterCar.write(data);
                            }
                            else {
                                outputStreamWriterNoCar.write(data);
                            }

                        }
                    }
                }

                // Cleanup
                outputStreamWriterCar.close();
                outputStreamWriterNoCar.close();
                outputStreamCar.close();
                outputStreamNoCar.close();

                tvStatus.setText("Done converting");
            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
        }
        else {
            tvStatus.setText("No data to convert");
        }
    }

    /**
     * Test Detection classifier using the features in the genericCC folder
     */
    public void testDetectionClassifier() {

        double[][] carData;
        double[][] noCarData;
        Dataset trainingDataset = new DefaultDataset();

        File folder = new File(Constant.FILE_PATH, Constant.FOLDER_NAME + genericCCFolderName);
        int num_car = 0;
        int num_noncar = 0;
        int feature_len = -1;

        Log.d("TAG", "Counting examples....");
        if(folder.exists()) {

            // Counting number of examples and finding feature length
            for (final File fileEntry : folder.listFiles()) {
                if (fileEntry.isDirectory()) {
                } else {
                    String fileName = fileEntry.getName();
                    String extension = "";
                    int pos = fileName.lastIndexOf(".");
                    if (pos > 0) {
                        extension = fileName.substring(pos + 1);
                    }

                    // Count number of car and noncar examples
                    if (extension.equals("txt") && fileName.contains("car")) {
                        num_car = num_car + 1;
                    }
                    else if(extension.equals("txt")) {
                        num_noncar = num_noncar + 1;
                    }

                    // Find feature length from first car file if not yet found
                    if(feature_len < 0 && extension.equals("txt")) {
                        try {
                            FileInputStream fin = new FileInputStream(fileEntry);
                            ObjectInputStream ois = new ObjectInputStream(fin);
                            double[] feature = (double[]) ois.readObject();
                            feature_len = feature.length;
                            ois.close();
                            fin.close();
                        } catch (Exception e) {
                            Log.d(TAG, e.toString());
                        }
                    }
                }
            }

            Log.d(TAG, "Analyzing counted samples....");

            // Create data storage containers
            if(num_car < 1 || num_noncar < 1) {
                tvStatus.setText("Not enough examples to test.");
                return;
            }
            carData = new double[num_car][feature_len];
            noCarData = new double[num_noncar][feature_len];

            // Read in data
            Log.d(TAG, "Reading in data....");
            try {
                int carDataIdx = 0;
                int nonCarDataIdx = 0;

                // Reading in data
                for (final File fileEntry : folder.listFiles()) {
                    if (fileEntry.isDirectory()) {
                    } else {
                        String fileName = fileEntry.getName();
                        String extension = "";
                        int pos = fileName.lastIndexOf(".");
                        if (pos > 0) {
                            extension = fileName.substring(pos + 1);
                        }

                        // Read in feature
                        if(extension.equals("txt")) {
                            FileInputStream fin = new FileInputStream(fileEntry);
                            ObjectInputStream ois = new ObjectInputStream(fin);
                            double[] feature = (double[]) ois.readObject();
                            ois.close();
                            fin.close();

                            // Determine class
                            String className = "";
                            if (fileName.contains("car")) {
                                className = Constant.CAR_CLASS;
                            } else if (fileName.contains("horn")) {
                                className = Constant.HORN_CLASS;
                            } else {
                                className = Constant.NONE_CLASS;
                            }

                            // Allocate to corresponding dataset
                            if(className.equals(Constant.CAR_CLASS)) {
                                carData[carDataIdx++] = feature;
                            } else if(className.equals(Constant.NONE_CLASS)) {
                                noCarData[nonCarDataIdx++] = feature;
                            }
                        }
                    }
                }

                // Build classifier using half of examples as training
                Log.d(TAG, "Building classifier....");
                for(int i = 0; i < 4 * num_car / 5; i++) {
                    Instance instance = new DenseInstance(carData[i], Constant.CAR_CLASS);
                    trainingDataset.add(instance);
                }
                for(int i = 0;i < 4 * num_noncar / 5; i++) {
                    Instance instance = new DenseInstance(noCarData[i], Constant.NONE_CLASS);
                    trainingDataset.add(instance);
                }
            } catch(Exception e) {
                Log.d(TAG, e.toString());
                tvStatus.setText("Error reading in features.");
            }

            RandomForest tempRandomForest = new RandomForest(50);
            tempRandomForest.buildClassifier(trainingDataset);

            // Testing using second half of data
            Log.d(TAG, "Beginning testing");

            int numCorrectlyClassifiedCars = 0;
            int numCorrectlyClassifiedNonCars = 0;
            for(int i = 4 * num_car / 5; i < num_car; i++) {
                Instance instance = new DenseInstance(carData[i]);
                Object predictedValue = tempRandomForest.classify(instance);

                if(predictedValue.equals(Constant.CAR_CLASS)) {
                    numCorrectlyClassifiedCars = numCorrectlyClassifiedCars + 1;
                }
            }
            for(int i = 4 * num_noncar / 5; i < num_noncar; i++) {
                Instance instance = new DenseInstance(noCarData[i]);
                Object predictedValue = tempRandomForest.classify(instance);

                if(predictedValue.equals(Constant.NONE_CLASS)) {
                    numCorrectlyClassifiedNonCars = numCorrectlyClassifiedNonCars + 1;
                }
            }

            // Display stats
            double truePositiveAccuracy = ((double)(numCorrectlyClassifiedCars)) / (num_car - (4 * num_car / 5));
            double trueNegativeAccuracy = ((double) (numCorrectlyClassifiedNonCars)) / (num_noncar - (4 * num_noncar / 5));
            double totalAccuracy = ((double) numCorrectlyClassifiedCars + numCorrectlyClassifiedNonCars) / (num_car + num_noncar - (4 * num_car / 5) - (4 * num_noncar / 5));
            tvStatus.setText("TP: " + Double.toString(truePositiveAccuracy) + ", TN: " + Double.toString(trueNegativeAccuracy) + ", Total: " + Double.toString(totalAccuracy));
            Log.i(TAG, "Total Number of car examples trained: " + Integer.toString((4 * num_car / 5)));
            Log.i(TAG, "Total Number of noncar examples trained: " + Integer.toString((4 * num_noncar / 5)));
            Log.i(TAG, "Total Number of car examples tested: " + Integer.toString(num_car - (4 * num_car / 5)));
            Log.i(TAG, "Total Number of noncar examples tested: " + Integer.toString(num_noncar - (4 * num_noncar / 5)));
            Log.i(TAG, "Total Number of correctly classified car examples: " + Integer.toString(numCorrectlyClassifiedCars));
            Log.i(TAG, "Total Number of correctly classified noncar examples: " + Integer.toString(numCorrectlyClassifiedNonCars));
        }
        else {
            tvStatus.setText("No data to test classifier");
        }
    }

    /**
     * For testing direction classifier
     */
    public void testDirectionClassifier() {

        File directionFile = new File(Constant.FILE_PATH + Constant.FOLDER_NAME + "carWeka.csv");

        try {

            // Check if the weka file (contains examples used for training direction) exists.
            if (directionFile.exists()) {

                FileInputStream fileInputStream = new FileInputStream(directionFile);
                InputStreamReader fileInputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader fileBufferedReader = new BufferedReader(fileInputStreamReader);

                // Read in features and store
                Log.d(TAG, "Extracting direction dataset....");
                ArrayList<double[]> dataList = new ArrayList<>();
                ArrayList<String> dataLabels = new ArrayList<>();
                String csvLine = "";
                while((csvLine = fileBufferedReader.readLine()) != null) {
                    String[] row = csvLine.split(",");

                    // Parse and store feature and label
                    double[] row_double = new double[row.length - 1];
                    for(int i = 0; i < row.length - 1; i++) {
                        Log.d(TAG, "Value: " + row[i]);

                        row_double[i] = Double.parseDouble(row[i]);
                    }
                    dataList.add(row_double);
                    dataLabels.add(row[row.length - 1]);
                    Log.d(TAG, "label: " + dataLabels.get(dataLabels.size() - 1));
                }

                // Close readers
                fileBufferedReader.close();
                fileInputStreamReader.close();
                fileInputStream.close();

                // Split into training and test set after collecting all labels
                ArrayList<ArrayList<double[]>> organizedData = new ArrayList<>();
                ArrayList<String> classLabels = new ArrayList<>();
                for(int i = 0; i < dataList.size(); i++) {

                    // See if this label is already seen
                    boolean found = false;
                    int foundIndex = -1;
                    for(int j = 0; j < classLabels.size(); j++) {
                        if(classLabels.get(j).equals(dataLabels.get(i))) {
                            found = true;
                            foundIndex = j;
                        }
                    }

                    // If class already seen, add it to the pertinent list, otherwise create a new list for storing.
                    // Otherwise create a new list
                    if(found) {
                        organizedData.get(foundIndex).add(dataList.get(i));
                    }
                    else {
                        ArrayList<double[]> newClass = new ArrayList<>();
                        newClass.add(dataList.get(i));
                        organizedData.add(newClass);
                        classLabels.add(dataLabels.get(i));
                    }
                }


                // Build training and obtain test set
                Log.d(TAG, "Building direction training and testing set....");
                Dataset trainingDataset = new DefaultDataset();
                ArrayList<double []> testingData = new ArrayList<>();
                ArrayList<String> testingLabels = new ArrayList<>();
                RandomForest dirClassifier = new RandomForest(50);
                for(int i = 0; i < organizedData.size(); i++) {

                    ArrayList<double []> currentClassData = organizedData.get(i);
                    String currentClassLabel = classLabels.get(i);
                    int indexSplit = currentClassData.size() * 3 / 4;    // Train using half and test using half

                    // Training dataset
                    for(int j = 0; j < indexSplit; j++) {
                        Instance instance = new DenseInstance(currentClassData.get(j), currentClassLabel);
                        trainingDataset.add(instance);
                    }

                    // Test dataset
                    for(int j = indexSplit; j < currentClassData.size() ; j++) {
                        testingData.add(currentClassData.get(j));
                        testingLabels.add(currentClassLabel);
                    }
                }
                dirClassifier.buildClassifier(trainingDataset);

                // Testing
                Log.d(TAG, "Testing direction classifier....");
                double totalAccuracy = 0;
                for(int i = 0; i < testingData.size(); i++) {
                    Instance instance = new DenseInstance(testingData.get(i));
                    Object result = dirClassifier.classify(instance);

                    Log.d(TAG, "1. True Label: " + dataLabels.get(i));
                    Log.d(TAG, "2. Test Result Labels: " + result.toString());
                    if(result.equals(dataLabels.get(i))) {
                        totalAccuracy = totalAccuracy + 1;
                    }
                }

                Log.d(TAG, "Num Correctly classified: " + Double.toString(totalAccuracy));
                totalAccuracy = totalAccuracy / (testingData.size());

                Log.d(TAG, "Total Number Test Examples = " + Integer.toString(testingData.size()));
                Log.d(TAG, "Total Accuracy = " + Double.toString(totalAccuracy));
                tvStatus.setText("Total Number Test Examples: " + Integer.toString(testingData.size()) + ", Total Accuracy: " + Double.toString(totalAccuracy));

            } else {
                Log.d(TAG, "Direction Feature File not found.");
                tvStatus.setText("Direction Feature File not found.");
            }
        }
        catch (Exception e) {
            Log.d(TAG, e.toString());
            tvStatus.setText(e.toString());
        }
    }
}
