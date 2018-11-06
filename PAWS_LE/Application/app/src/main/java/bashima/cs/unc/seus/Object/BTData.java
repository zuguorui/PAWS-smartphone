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
