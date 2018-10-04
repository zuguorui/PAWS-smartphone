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

import java.util.Arrays;

public class PreProcess {
	
	/** 
	 * get frames from an array of signal
	 * @param vec input signal (array of samples)
	 * @param Nw frame width (samples)
	 * @param Ns frame shift or overlap (samples)
	 * @return frames 2d array; each row represents a frame
	 */
	public static double[][] vec2frames(double[] vec,int Nw, int Ns){			
		int L = vec.length;							// length of the input vector
		int M = 0;
		if(Ns==0)
			M = (int) Math.floor(L/Nw);
		else
			M = (int) Math.floor((L-Nw)/Ns+1);             // number of frames
	    // figure out if the input vector can be divided into frames exactly
        int E = (L-((M-1)*Ns+Nw));
        // see if padding is actually needed
        double[] padded_vec = null;
       
        if( E>0 ) {
        	// how much padding will be needed to complete the last frame?
        	int P = Nw-E;
        	padded_vec = Arrays.copyOf(vec, (int) (L+P));
        }
        else{
        	padded_vec = vec;
        }
        // update number of frames after padding
        L = padded_vec.length;
        
        M = (int) Math.floor((L-Nw)/Ns+1);	//number of frames
        
        // overlapping
        double[][] frames = new double[M][Nw];
    	//System.out.println(Arrays.toString(padded_vec));
    	
    	int i = 0;
    	int j = 0;
    	int k = 0;
    	while(k < M){
    		double[] f = new double[Nw];
    		j = Ns*k;
    		for(i = 0; i < Nw; i++){
    			f[i] = padded_vec[j+i];
    		}
    		
    		frames[k] = f;
    		
    		j = j + Ns;
    		i = j;
    		k++;
    	}
    	
    	//windowing
		WindowFunction window = new WindowFunction();
		window.setWindowType("Hamming");
		double[] win = window.generate(Nw);		//generate window samples
		for(int m = 0; m < M; m++){
			if(win.length == Nw){
				for(int s = 0; s < Nw; s++){
					frames[m][s] =frames[m][s]*win[s];
				}

			}
		
		}
		return frames;
		
	}
	
	// for no shift in phone
	public static double[][] vec2frames(double[] vec, int Nw){
		int L = vec.length;
		int M = (int) Math.ceil((double)L/Nw);
		int remaining = M*Nw-L;
		double[] padded_vec = null;
		//if(remaining >0)
			padded_vec = Arrays.copyOf(vec, (int) (L+remaining));
		
//		System.out.println(Arrays.toString(padded_vec));
		double[][] frames = new double[M][Nw];
		
		int fi=0;
		while(fi<M){
			for(int si = 0; si < Nw; si++){
				frames[fi][si] = padded_vec[fi*Nw+si];	
			}
			fi++;
		}
		
		return frames;
	}

	
	public static double log2( double a){
		return Math.log(a) / Math.log(2);
	}
	
	public static double nextpow2(double num){
		return Math.ceil(log2(num));
	}
	
	
//	private static void main(String[] args) {
//		double[] input = new double[]{1,2,3,4,5,6,7,8};
//		double[][] result = vec2frames(input,5,2);
//
//	}
}

	
