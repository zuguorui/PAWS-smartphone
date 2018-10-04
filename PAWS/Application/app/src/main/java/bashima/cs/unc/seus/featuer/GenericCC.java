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
package bashima.cs.unc.seus.featuer;

import android.icu.text.StringPrepParseException;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by stephen on 3/27/2017.
 */

public class GenericCC {
    private final static String TAG = "GenericCC";
    private double sampling_freq;
    private double window_len;
    private double time_shift;
    private int B_self;
    private int a_self;
    private int b_self;
    private int frame_len;
    private int nfft;
    private FFT mfft;
    private int n, m;
    private int dt1, dt2;
    private int r_len, r1_len;
    private int n_frames_per_window_self;

    private String wintype = "HAMMING";
    private double[] window;

    /**
     * Constructor
     * @param fs is the sampling frequency (Hz)
     * @param wl is the window length (s)
     * @param ts is the analysis frame shift (ms)
     * @param n_frames_per_window number of frames to average per window
     * @param B
     * @param a
     * @param b
     */
    public GenericCC(double fs, double wl, double ts, int n_frames_per_window, int B, int a, int b)
    {
        sampling_freq = fs;
        window_len = wl;
        time_shift = ts;
        n_frames_per_window_self = n_frames_per_window;
        B_self = B;
        a_self = a;
        b_self = b;

        // Check for valid input parameters
        if(B <= b)
        {
            Log.e(TAG, "B <= b: invalid.");
        }

        // Compute number of samples per frame
        frame_len = (int) Math.floor(sampling_freq * window_len) / n_frames_per_window_self;
        if(frame_len < 1)
        {
            Log.e(TAG, "Invalid sampling frequency and/or window length.");
        } else {

            // Otherwise, generate window function
            WindowFunction window_func = new WindowFunction();
            window_func.setWindowType(wintype);
            window = window_func.generate(frame_len);

            // Compute nfft and initialize fft
            nfft = (int) Math.pow(2, PreProcess.nextpow2(frame_len));
            mfft = new FFT(nfft);

            // Compute parameters
            n = (nfft / 2) + 1;
            m = (int) Math.max(1, Math.floor(((double) a_self) * n / 100));
            dt1 = (int) Math.max(Math.floor(((double) m) / b_self), 1);
            dt2 = (int) Math.max(Math.floor(((double)(n - m)) / (B_self - b_self)), 1);

            // Error checking dt1 and dt2
            if(!(dt2 > 0 && dt1 > 0))
            {
                Log.d(TAG, "dt1 and dt2 invalid.");
            }

            // Compute accumulation vector params
            r1_len = (b_self) * dt1;
            r_len = r1_len + ((B_self - b_self) * dt2);
        }
    }

    /**
     * @param audio_samples: audio signal
     * @return
     */
    public double[] generate_features(double[] audio_samples)
    {
        int frame_count;
        double[][] psd;
        double[] mean_psd, features;
        int i, j, k, counter;

        // Power spectral density
        psd = power_spectral_density(audio_samples, r_len);
        frame_count = psd.length;
        //Log.d(TAG, Arrays.toString(psd[0]));

        for(i = 0; i < frame_count; i++) {

            // Scaling the first values by dt1.
            for(j = 0; j < r1_len; j++) {
                psd[i][j] = psd[i][j] * dt1;
            }

            // Scaling the rest by dt2
            for(j = r1_len; j < r_len; j++) {
                psd[i][j] = psd[i][j] * dt2;
            }
        }

        // Compute Mean
        mean_psd = new double[r_len];
        for(j = 0; j < r_len; j++)
        {
            for(i = 0; i < frame_count; i++)
            {
                mean_psd[j] = mean_psd[j] + psd[i][j];
            }
            mean_psd[j] = mean_psd[j] / frame_count;
        }

        // Accumulation to create feature bins
        features = new double[B_self];
        i = 0;
        for(k = 0; k < b_self; k++)
        {
            for(j = 0; j < dt1; j++)
            {
                features[k] = features[k] + mean_psd[i];
                i = i + 1;
            }
        }

        for(k = b_self; k < B_self; k++)
        {
            for(j = 0; j < dt2; j++)
            {
                features[k] = features[k] + mean_psd[i];
                i = i + 1;
            }
        }

        // dB scale
        for(k = 0; k < B_self; k++) {
            features[k] = 10 * Math.log10(features[k]);
        }

        //Log.d(TAG, Arrays.toString(features));
        return features;
    }

    /**
     * Computes power spectral density after partitioning data into frames specified by the parameters
     * fed into the constructor
     * @param signal
     * @param nFreq: number of frequencies to return; should be less than equal to nfft.
     * @return psd: the power spectral density of the signal, returning only the number of bins requested
     */
    private double[][] power_spectral_density(double[] signal, int nFreq)
    {
        double[][] frames, paddedFrames, frames_re, frames_im, psd;
        int totalFrames;

        // Check if nfreq is less than equal to nfft
        if(nFreq > nfft)
        {
            Log.e(TAG, "Number of freq bins requested should be less than time window");
            return null;
        }

        // Partition by windows
        frames = PreProcess.vec2frames(signal, (int) frame_len);
        totalFrames = frames.length;
        Log.d("hi", "Total Frames: " + totalFrames);

        // Padding
        paddedFrames = new double[totalFrames][nfft];
        for(int i = 0; i < totalFrames; i++)
        {
            // Apply the window before padding
            for(int j = 0; j < frame_len; j++)
            {
                frames[i][j] = frames[i][j] * window[j];
            }
            paddedFrames[i] = Arrays.copyOf(frames[i], nfft);
        }

        // Compute power spectral density
        frames_re = paddedFrames;
        frames_im = new double[totalFrames][nfft];
        psd = new double[totalFrames][nFreq];
        for(int i = 0; i < totalFrames; i++)
        {
            // Take fft of window
            mfft.fft(frames_re[i], frames_im[i]);

            // Compute power spectral density
            psd[i][0] = ((1 / ((double) sampling_freq * nfft)) * (Math.pow(frames_re[i][0], 2) + Math.pow(frames_im[i][0], 2)));
            for(int j = 1; j < nFreq; j++)
            {
                // Multiple all frequencies besides dc to account for positive and negative frequencies.
                psd[i][j] = (2 * (1 / ((double) sampling_freq * nfft)) * (Math.pow(frames_re[i][j], 2) + Math.pow(frames_im[i][j], 2)));

                // Normalizing psd by sampling frequency
                //psd[i][j] = (2 * (1 / ((double) sampling_freq * nfft)) * (Math.pow(frames_re[i][j], 2) + Math.pow(frames_im[i][j], 2)));
            }
        }

        return psd;
    }

}
