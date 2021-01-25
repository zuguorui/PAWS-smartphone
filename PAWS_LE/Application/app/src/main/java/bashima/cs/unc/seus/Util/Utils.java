package bashima.cs.unc.seus.Util;

import android.util.Log;

import bashima.cs.unc.seus.constant.Constant;
import bashima.cs.unc.seus.featuer.Matrix;

/**
 * Created by stephen on 8/18/2018.
 * Utility functions
 */

public class Utils {

    /**
     * Localizes sound source given the relative delays of each microphone with respect to a reference microphone
     * and the position of the microphones. The returned array contains the estimated (x, y) from
     * the user, who is assumed to be standing at position (0,0).
     * @return
     */
    public static double[] localizeSoundSource(double[][] mic_positions, double[] relative_delays) {

        int num_microphones = mic_positions.length;

        double[][] A = new double[num_microphones - 1][3];
        double[][] b = new double[num_microphones - 1][1];

        // Set up Linear equations
        for(int i = 1; i < num_microphones; i++) {
            A[i - 1][0] = (2 * mic_positions[i][0]) - (2 * mic_positions[0][0]);
            A[i - 1][1] = (2 * mic_positions[i][1]) - (2 * mic_positions[0][1]);
            A[i - 1][2] = 2 * (Constant.sound_speed * (relative_delays[i - 1] / Constant.asic_sampling_rate));

            b[i - 1][0] = (mic_positions[i][0] * mic_positions[i][0]) + (mic_positions[i][1] * mic_positions[i][1]) - (mic_positions[0][0] * mic_positions[0][0]) - (mic_positions[0][1] * mic_positions[0][1]) - (Constant.sound_speed * Constant.sound_speed * relative_delays[i - 1] * relative_delays[i - 1] / (Constant.asic_sampling_rate * Constant.asic_sampling_rate));
            Log.d("Debug", "localizeSoundSource " + String.valueOf(i) + ": " + "A - " + String.valueOf(A[i - 1][2]) + ", relative delays: " + String.valueOf(relative_delays[i - 1]));
            Log.d("Debug", "localizeSoundSource " + String.valueOf(i) + ": " + "b - " + String.valueOf(b[i - 1][0]) + ", relative delays: " + String.valueOf(relative_delays[i - 1]));
        }

        // Solve
        Matrix A_input = new Matrix(A);
        Matrix b_input = new Matrix(b);
        Log.d("Dimensions of A", String.valueOf(A_input.getRowDimension()) + "x" + String.valueOf(A_input.getColumnDimension()));
        Log.d("Dimensions of b", String.valueOf(b_input.getRowDimension()) + "x" + String.valueOf(b_input.getColumnDimension()));
        /*for(int i = 0; i < A_input.getRowDimension(); i++) {
            String temp = "";
            for(int j = 0; j < A_input.getColumnDimension(); j++) {
                temp = temp + String.valueOf(A_input.get(i, j) + ", ");
            }
            Log.d("A[" + String.valueOf(i) + "]", temp);
        }
        for(int i = 0; i < b_input.getRowDimension(); i++) {
            String temp = "";
            for(int j = 0; j < b_input.getColumnDimension(); j++) {
                temp = temp + String.valueOf(b_input.get(i, j) + ", ");
            }
            Log.d("b[" + String.valueOf(i) + "]", temp);
        }*/
        /*Matrix A_temp = new Matrix(A);
        Matrix A_input = A_temp.transpose().times(A_temp);
        Matrix b_input = A_temp.transpose().times(new Matrix(b));*/
        LUDecomposition solver = new LUDecomposition(A_input);
        Matrix x = solver.solve(b_input);

        //Log.d("Debug", "Num Rows of Location: " + String.valueOf(x.getRowDimension()));
        //Log.d("Debug", "Num Cols of Location: " + String.valueOf(x.getColumnDimension()));
        double[] x_array = new double[2];
        x_array[0] = x.get(0, 0);
        x_array[1] = x.get(1, 0);
        return x_array;
    }

    /**
     * Localizes sound source: method involving learning spatial features
     * return_array[0] = the index of the circle equation used
     * return_array[1] = angle computed in degrees
     *
     * If there is an error, both values in return_array will be negative infinity
     * @return
     */
    public static double[] localizeSoundSource2(double[][] mic_positions, double[] relative_delays) {

        Matrix relative_delays_mat = new Matrix(new double[][]{
                relative_delays
        });

        // Project onto two dimensions with highest variance
        Matrix result_mat = Constant.projection_matrix_3d_to_2d.transpose().times(relative_delays_mat.transpose());

        // Reflect across y axis
        result_mat.set(0, 0, result_mat.get(0, 0) * -1);

        // Subtract center offset learned from training
        result_mat.set(0, 0, result_mat.get(0, 0) - Constant.center_2d[0]);
        result_mat.set(1, 0, result_mat.get(1, 0) - Constant.center_2d[1]);

        // Rotate axis such that 0 degrees is facing front
        result_mat = Constant.rotation_matrix.times(result_mat);

        // Figure out which quadrant the point is in
        double test_slope = result_mat.get(1, 0) / result_mat.get(0, 0);
        int circle_eqn_idx = -1;

        // First quadrant
        if(result_mat.get(0, 0) >= 0 && result_mat.get(1, 0) >= 0) {

            if(test_slope >= Constant.training_slopes[0] && test_slope <= Constant.training_slopes[1]) {
                circle_eqn_idx = 0;
            }
            else if(test_slope >= Constant.training_slopes[1]) {
                circle_eqn_idx = 1;
            }

        }

        // Second quadrant
        else if(result_mat.get(0, 0) <= 0 && result_mat.get(1, 0) >= 0) {

            if(test_slope <= Constant.training_slopes[2]) {
                circle_eqn_idx = 1;
            }
            else if(test_slope >= Constant.training_slopes[2] && test_slope <= Constant.training_slopes[3]) {
                circle_eqn_idx = 2;
            }
            else if(test_slope >= Constant.training_slopes[3]) {
                circle_eqn_idx = 3;
            }

        }

        // Third quadrant
        else if(result_mat.get(0, 0) <= 0 && result_mat.get(1, 0) <= 0) {

            if(test_slope <= Constant.training_slopes[4]) {
                circle_eqn_idx = 3;
            }
            else if(test_slope >= Constant.training_slopes[4] && test_slope <= Constant.training_slopes[5]) {
                circle_eqn_idx = 4;
            }
            else if(test_slope >= Constant.training_slopes[5]) {
                circle_eqn_idx = 5;
            }

        }

        // Fourth quadrant
        else if(result_mat.get(0, 0) >= 0 && result_mat.get(1, 0) <= 0) {

            if(test_slope <= Constant.training_slopes[6]) {
                circle_eqn_idx = 5;
            }
            else if(test_slope >= Constant.training_slopes[6] && test_slope <= Constant.training_slopes[7]) {
                circle_eqn_idx = 6;
            }
            else if(test_slope >= Constant.training_slopes[7] && test_slope <= Constant.training_slopes[0]) {
                circle_eqn_idx = 7;
            }
            else if(test_slope >= Constant.training_slopes[0]) {
                circle_eqn_idx = 0;
            }
        }

        // See if we have picked a circle or not
        double[] return_array = new double[] {
                Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY
        };
        if(circle_eqn_idx < 0) {
            Log.d("localizeSoundSource2", "could not find circle_eqn_idx of " + String.valueOf(relative_delays[0]) + ", " + String.valueOf(relative_delays[1]) + ", " + String.valueOf(relative_delays[2]));
            return return_array;
        }

        // Find where the circle segment and the new point intersect
        double x, y;
        x = Constant.interp_equations[circle_eqn_idx][7] / (test_slope - Constant.interp_equations[circle_eqn_idx][6]);
        y = x * test_slope;

        // Compute angle via interpolation
        double distance = Math.sqrt(Math.pow(x - Constant.interp_equations[circle_eqn_idx][2], 2) + Math.pow(y - Constant.interp_equations[circle_eqn_idx][3], 2));
        double angle = ((distance / Constant.interp_equations[circle_eqn_idx][8]) * (Constant.interp_equations[circle_eqn_idx][1] - Constant.interp_equations[circle_eqn_idx][0])) + Constant.interp_equations[circle_eqn_idx][0];

        return_array[0] = circle_eqn_idx;
        return_array[1] = angle;
        return return_array;
    }

    /**
     * Returns distance via regression
     * @param max_coefficient - Point to find distance for
     * @param eqn_parameters - two entry array with equation parameters to do regression
     * @return
     */
    public static double distanceRegression(double max_coefficient, double[] eqn_parameters) {

        double offset = eqn_parameters[0];
        double slope = eqn_parameters[1];

        return (slope * max_coefficient) + offset;
    }

    /**
     * Finds and returns maximum value in an array
     * @param array
     * @return
     */
    public static double findMaxVal(double[] array) {

        double max_val = Double.NEGATIVE_INFINITY;
        for(int i = 0; i < array.length; i++) {
            if(array[i] > max_val) {
                max_val = array[i];
            }
        }

        return max_val;
    }
}
