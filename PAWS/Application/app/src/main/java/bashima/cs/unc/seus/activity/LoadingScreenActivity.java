package bashima.cs.unc.seus.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.classification.tree.RandomForest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import bashima.cs.unc.seus.constant.Constant;
import libsvm.LibSVM;
import seus.bashima.cs.unc.seus.R;

import static bashima.cs.unc.seus.constant.Constant.classifierCarDistance;
import static bashima.cs.unc.seus.constant.Constant.classifierHornDistance;

public class LoadingScreenActivity extends Activity {

    public LoadingScreenActivity loadActivity = null;
    //A ProgressDialog object
    private ProgressDialog progressDialog;
    public Context context;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        context = this;
        loadActivity = this;
        //Initialize a LoadViewTask object and call the execute() method

        new LoadViewTask().execute();

    }

    //To use the AsyncTask, it must be subclassed
    private class LoadViewTask extends AsyncTask<Void, Integer, Void> {
        //Before running code in separate thread
        @Override

        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(LoadingScreenActivity.this, "Loading...",
                    "Loading models, please wait...", false, false);
        }

        //The code to be executed in a background thread.
        @Override
        protected Void doInBackground(Void... params) {
            /* This is just a code that delays the thread execution 4 times,
             * during 850 milliseconds and updates the current progress. This
             * is where the code that is going to be executed on a background
             * thread must be placed.
             */
            //Get the current thread's token
            synchronized (this) {
                Log.d("Read model", "start");
                readModel();
                Log.d("Read Model", "end");
//                Intent i = new Intent(loadActivity, MainActivity.class);
//                loadActivity.startActivity(i);
//                Log.d("HERE", "HERE");
                publishProgress();

            }
            return null;
        }

        //Update the progress
        @Override
        protected void onProgressUpdate(Integer... values) {
            //set the current progress of the progress dialog
//            progressDialog.setProgress(values[0]);
        }

        //after executing the code in the thread
        @Override
        protected void onPostExecute(Void result) {
            //close the progress dialog
            progressDialog.dismiss();
            //initialize the View
//            setContentView(R.layout.activity_main);

        }


        public void readModel() {
//        ReadingModel
            Log.e("Read MOdel", Constant.isColumbia + "");

            if (Constant.isColumbia) {
                File detectModelFile = new File(getCacheDir() + Constant.detectionModelName);
                try {
                    InputStream detecModelReadInputStream = getAssets().open(Constant.detectionModelName);
                    int detectModelReadBufferSize = detecModelReadInputStream.available();
                    byte[] detectModelReadBuffer = new byte[detectModelReadBufferSize];
                    detecModelReadInputStream.read(detectModelReadBuffer);
                    detecModelReadInputStream.close();

                    FileOutputStream detectModelFileOutputStream = new FileOutputStream(detectModelFile);
                    detectModelFileOutputStream.write(detectModelReadBuffer);
                    detectModelFileOutputStream.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                File carDistModelFile = new File(getCacheDir() + Constant.carDistModelName);
                try {
                    InputStream carDistModelReadInputStream = getAssets().open(Constant.carDistModelName);
                    int carDistModelReadBufferSize = carDistModelReadInputStream.available();
                    byte[] carDistModelReadBuffer = new byte[carDistModelReadBufferSize];
                    carDistModelReadInputStream.read(carDistModelReadBuffer);
                    carDistModelReadInputStream.close();

                    FileOutputStream carDistModelFileOutputStream = new FileOutputStream(carDistModelFile);
                    carDistModelFileOutputStream.write(carDistModelReadBuffer);
                    carDistModelFileOutputStream.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                File hornDistModelFile = new File(getCacheDir() + Constant.hornDistModelName);
                try {
                    InputStream hornDistModelReadInputStream = getAssets().open(Constant.hornDistModelName);
                    int hornDistModelReadBufferSize = hornDistModelReadInputStream.available();
                    byte[] hornDistModelReadBuffer = new byte[hornDistModelReadBufferSize];
                    hornDistModelReadInputStream.read(hornDistModelReadBuffer);
                    hornDistModelReadInputStream.close();

                    FileOutputStream hornDistModelFileOutputStream = new FileOutputStream(hornDistModelFile);
                    hornDistModelFileOutputStream.write(hornDistModelReadBuffer);
                    hornDistModelFileOutputStream.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }


                File hornDirModelFile = new File(getCacheDir() + Constant.hornDirModelName);
                try {
                    InputStream hornDirModelReadInputStream = getAssets().open(Constant.hornDirModelName);
                    int hornDirModelReadBufferSize = hornDirModelReadInputStream.available();
                    byte[] hornDirModelReadBuffer = new byte[hornDirModelReadBufferSize];
                    hornDirModelReadInputStream.read(hornDirModelReadBuffer);
                    hornDirModelReadInputStream.close();

                    FileOutputStream hornDirModelFileOutputStream = new FileOutputStream(hornDirModelFile);
                    hornDirModelFileOutputStream.write(hornDirModelReadBuffer);
                    hornDirModelFileOutputStream.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                File carDirModelFile = new File(getCacheDir() + Constant.carDirModelName);
                try {
                    InputStream carDirModelReadInputStream = getAssets().open(Constant.carDirModelName);
                    int carDirModelReadBufferSize = carDirModelReadInputStream.available();
                    byte[] carDirModelReadBuffer = new byte[carDirModelReadBufferSize];
                    carDirModelReadInputStream.read(carDirModelReadBuffer);
                    carDirModelReadInputStream.close();

                    FileOutputStream carDirModelFileOutputStream = new FileOutputStream(carDirModelFile);
                    carDirModelFileOutputStream.write(carDirModelReadBuffer);
                    carDirModelFileOutputStream.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                FileInputStream detectFileInputStream, carDistFileInputStream, carDirFileInputStream, hornDistFileInputStream, hornDirFileInputStream;
                if (Constant.isColumbia) {
                    detectFileInputStream = new FileInputStream(getCacheDir() + Constant.detectionModelName);
                    carDistFileInputStream = new FileInputStream(getCacheDir() + Constant.carDistModelName);
                    carDirFileInputStream = new FileInputStream(getCacheDir() + Constant.carDirModelName);
                    hornDistFileInputStream = new FileInputStream(getCacheDir() + Constant.hornDistModelName);
                    hornDirFileInputStream = new FileInputStream(getCacheDir() + Constant.hornDirModelName);
                } else {
                    Log.e("Inside", "Inside");
                    detectFileInputStream = new FileInputStream(Constant.FILE_PATH + Constant.FOLDER_NAME + Constant.detectionModelName);
                    carDistFileInputStream = new FileInputStream(Constant.FILE_PATH + Constant.FOLDER_NAME + Constant.carDistModelName);
                    carDirFileInputStream = new FileInputStream(Constant.FILE_PATH + Constant.FOLDER_NAME + Constant.carDirModelName);
                    hornDistFileInputStream = new FileInputStream(Constant.FILE_PATH + Constant.FOLDER_NAME + Constant.hornDistModelName);
                    hornDirFileInputStream = new FileInputStream(Constant.FILE_PATH + Constant.FOLDER_NAME + Constant.hornDirModelName);
                }
                ObjectInputStream ois = new ObjectInputStream(detectFileInputStream);
                //Constant.libsvm = (LibSVM) ois.readObject();
                Constant.classifierDetection = (RandomForest) ois.readObject();
                if (Constant.classifierDetection != null) {
                    Log.e("Loading", "RandomForest Detection Model Read.");
                }
                ois.close();
                Log.e("Model File", Constant.FILE_PATH + Constant.FOLDER_NAME + Constant.carDistModelName);
                ObjectInputStream cDistOis = new ObjectInputStream(carDistFileInputStream);
                classifierCarDistance = (RandomForest) cDistOis.readObject();
                cDistOis.close();

                Log.e("Model File", Constant.FILE_PATH + Constant.FOLDER_NAME + Constant.carDirModelName);
                ObjectInputStream cDirOis = new ObjectInputStream(carDirFileInputStream);
                Constant.classifierCarDirection = (RandomForest) cDirOis.readObject();
                cDirOis.close();

                Log.e("Model File", Constant.FILE_PATH + Constant.FOLDER_NAME + Constant.hornDistModelName);
                ObjectInputStream hDistOis = new ObjectInputStream(hornDistFileInputStream);
                classifierHornDistance = (RandomForest) hDistOis.readObject();
                hDistOis.close();

                Log.e("Model File", Constant.FILE_PATH + Constant.FOLDER_NAME + Constant.hornDirModelName);
                ObjectInputStream hDirOis = new ObjectInputStream(hornDirFileInputStream);
                Constant.classifierHornDirection = (RandomForest) hDirOis.readObject();
                hDirOis.close();
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }


            Intent i = new Intent(loadActivity, MainActivity.class);
            loadActivity.startActivity(i);
            finish();
        }
    }
}