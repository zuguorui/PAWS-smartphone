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
package bashima.cs.unc.seus.constant;

import android.os.Environment;

import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.classification.tree.RandomForest;

import libsvm.LibSVM;

/**
 * Created by bashimaislam on 9/14/16.
 */
public class Constant {

    public static boolean prnt = false;

    public static double a = 2595.0;
    public static double b = 700.0;
    public static boolean sound = true;
    public static boolean vibrate = true;
    public static LibSVM libsvm;
//    public static LibSVM classifierCarDirection;
//    public static LibSVM classifierHornDirection;
    public static RandomForest classifierDetection;
    public static RandomForest classifierCarDirection;
    //public static KNearestNeighbors classifierCarDirection;
    public static RandomForest classifierCarDistance;
    public static RandomForest classifierHornDirection;
    public static RandomForest classifierHornDistance;

    public static String CAR_CLASS = "car";
    public static String HORN_CLASS = "horn";
    public static String NONE_CLASS = "none";
    public static String detectionModelName = "model.ser";
    public static final int CAR = 1;
    public static final int NONE = 0;
    public static final int HORN = 2;
    public static final int DIST1 = 1;
    public static final int DIST2 = 2;
    public static final int DIST3 = 3;
    public static final int SAMPLE_RATE = 48000;
    public static final int SAMPLE_DELAY = 0;
    public static final String FOLDER_NAME = "/SEUS/";
    public static final int DETECTION_WINDOW_SIZE = 40960;    //byte size (10 frames; 250 ms/frame; 2048 sample/frame)
    public static double Tw = 22;                // analysis frame duration (ms)
    public static double Ts = 22;                // analysis frame shift (ms)
    public static double Wl = 20480.0 / 48000.0;                   // window duration (second) //0.45
    public static int n_frames_per_window = 10;  // Number of frames to use per window
    public static String FILE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
    public static int BUFFERSIZE;
    public static final int REQUEST_SELECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 2;
    public static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    public static final int UART_PROFILE_CONNECTED = 20;
    public static final int UART_PROFILE_DISCONNECTED = 21;
    public static boolean isColumbia = false;

    // Constants for generating GenericCC features
    public static final int B_GCC = 20;
    public static final int a_GCC = 30;
    public static final int b_GCC = 18;


    // Start message to write to BLE module to begin transmission
    public static final byte[] START_MSG = new byte[]{(byte) 83, (byte) 69, (byte) 85, (byte) 83, (byte) 83};  //"SEUSS"

    // Delimiter between windows; final byte is the byte data length; Second to last byte is the microphone flag byte
    public static final int[] DELIM = {83, 69, 85, 83, 35};
    public static java.lang.String carDistModelName = "carDistModel.ser";
    public static java.lang.String hornDistModelName = "hornDistModel.ser";
    public static java.lang.String hornDirModelName = "hornDirModel.ser";
    public static java.lang.String carDirModelName = "carDirModel.ser";

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    public static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 3;
    public static final int MY_PERMISSIONS_REQUEST_READ_LOCATION = 4;

    // Logging
    public static final int LOGGING_FLAG = 1;   // 1 to enable; 0 to disable
    public static final int NUM_FP_TO_LOG = 3;  // Maximum number of iwndows to log for false positives
    public static final int NUM_FN_TO_LOG = 5;  // Maximum number of windows to log for false negatives
    public static final String FP_FOLDER = FILE_PATH + FOLDER_NAME + "FP_Logging/";  // Where to log false positives
    public static final String FN_FOLDER = FILE_PATH + FOLDER_NAME + "FN_Logging/";  // Where to log false negatives
    public static final String FEATURE_CLASSIFICATION_LOG_FOLDER = FILE_PATH + FOLDER_NAME + "Feature_classifier_logging/";
    public static final String DETECTION_FEATURES_READABLE_FOLDER_NAME = "GenericCC_Readable/";

    public static double[] convertSignalToDouble(byte[] byteData) {
        int bytePerSample = 2;
        int numSamples = byteData.length / bytePerSample;
        double[] amplitudes = new double[numSamples];

        int pointer = 0;
        for (int i = 0; i < numSamples; i++) {
            short amplitude = 0;
            for (int byteNumber = 0; byteNumber < bytePerSample; byteNumber++) {
                // little endian
                amplitude |= (short) ((byteData[pointer++] & 0xFF) << (byteNumber * 8));
            }
            amplitudes[i] = amplitude;
        }

        return amplitudes;
    }
}
