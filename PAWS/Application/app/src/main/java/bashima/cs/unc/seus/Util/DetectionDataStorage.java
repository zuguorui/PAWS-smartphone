package bashima.cs.unc.seus.Util;

/**
 * Created by stephen on 8/26/2017.
 * Used to store detection false positives and false negatives for loggin
 */

public class DetectionDataStorage {

    public long timestamp;
    public double[] signal;
    public double[] feature;

    public DetectionDataStorage(long timestamp, double[] signal, double[] feature) {
        this.timestamp = timestamp;
        this.signal = signal;
        this.feature = feature;
    }
}
