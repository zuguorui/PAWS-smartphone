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
package bashima.cs.unc.seus.Object;

/**
 * Created by bashimaislam on 10/21/16.
 */

public class BTData {
    public double []ccr = new double[3];
    public double []pwr = new double[3];
    public double []zcr = new double[4];
    public double []feature = new double[10];
    public int name = 0;

    public double[] FeatureGen()
    {
        for (int i=0; i<3; i++)
        {
            feature[i] = zcr[i];
            feature[i+4] = pwr[i];
            feature[i+7] = ccr[i];
        }
        feature[3] = zcr[3];
        return feature;
    }

    public String genString(){
        String str = "";
        for (int i =0; i<10; i++) {
            str = str + "," + feature[i];
        }
        str = str + "\n";
        return str;
    }
}
