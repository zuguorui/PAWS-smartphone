package bashima.cs.unc.seus.constant;

import android.os.Environment;

import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.classification.tree.RandomForest;

import bashima.cs.unc.seus.featuer.Matrix;
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

    // Geometric Method for localization
    // Position of microphones in meters
    public static final double[][] mic_positions = new double [][]{
            {0.1, 0},
            {-0.1, 0},
            {0, -0.1},
            {0, 0.1}
    };
    public static final double asic_sampling_rate = 100000; // Sampling rate used in ASIC chip
    public static final double sound_speed = 343; // Speed of sound in m/s
    public static final double max_distance = 30; // Maximum distance away from user displayed for detected cars in meters

    // Geometric method 2 variables: Used in localizing source
    public static final Matrix rotation_matrix = new Matrix(new double[][] {
            {-0.2638, -0.9646},
            {0.9646, -0.2638}
    });
    public static final double[] center_2d = new double[]{10.5559, -14.3915};
    public static final Matrix projection_matrix_3d_to_2d = new Matrix(new double[][] {
            {0.4055, 0.7158},
            {0.3877, -0.6979},
            {0.8278, -0.0238}
    });

    // Order of columns:
    // 0 - begin_angle
    // 1 - end_angle
    // 2 - begin_x
    // 3 - begin_y
    // 4 - end_x
    // 5 - end_y
    // 6 - slope
    // 7 - offset
    // 8 - length
    public static final double[][] interp_equations = new double[][] {
            {0, 45, 42.7592, -1.9039e-14, 3.5997, 93.2615, -2.3816, 101.8344, 101.1493},
            {45, 90, 3.5997, 93.2615, -17.5302, 105.6907, -0.5882, 95.3790, 24.5144},
            {90, 135, -17.5302, 105.6907, -56.5135, 95.6043, 0.2587, 110.2265, 40.2671},
            {135, 180, -56.5135, 95.6043, -61.8755, -20.1848, 21.5945, 1.3160e03, 115.9132},
            {180, 225, -61.8755, -20.1848, -5.8730, -93.2974, -1.3055, -100.9647, 92.0963},
            {225, 270, -5.8730, -93.2974, 38.6305, -102.2651, -0.2015, -94.4808, 45.3980},
            {270, 315, 38.6305, -102.2651, 56.8029, -78.8092, 1.2907, -152.1273, 29.6719},
            {315, 360, 56.8029, -78.8092, 42.7592 , -1.9039e-14, -5.6117, 239.9524, 80.0507},
    };

    // Training points for the different angles; here we trained 8 angles in 45 degree increments starting from 0 degrees
    // Since we only need to know the slope of the line crossing the origin and the point, we store only the slope
    public static final double[] training_slopes = new double[] {
            -4.4525e-16,
            25.9084,
            -6.0291,
            -1.6917,
            0.3262,
            15.8859,
            -2.6473,
            -1.3874
    };

    // Regression distance parameters
    public static final double[] distance_eqn_parameters = new double[] {
            80.5719,
            -1.1866
    };

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
