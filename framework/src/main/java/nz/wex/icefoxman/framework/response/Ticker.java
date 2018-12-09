package nz.wex.icefoxman.framework.response;

/**
 * Created by max user on 06.11.2017.
 */
public class Ticker {

    private double high;
    private double low;
    private double avg;
    private double vol;
    private double vol_cur;
    private double last;
    private double buy;
    private double sell;
    private long updated;

    public double high() {
        return high;
    }

    public double low() {
        return low;
    }

    public double avg() {
        return avg;
    }

    public double vol() {
        return vol;
    }

    public double vol_cur() {
        return vol_cur;
    }

    public double last() {
        return last;
    }

    public double buy() {
        return buy;
    }

    public double sell() {
        return sell;
    }

    public long updated() {
        return updated;
    }

}
