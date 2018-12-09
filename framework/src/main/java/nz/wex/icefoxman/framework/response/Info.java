package nz.wex.icefoxman.framework.response;

/**
 * Created by max user on 09.11.2017.
 */
public class Info {
    private int decimal_places;
    private double min_price;
    private double max_price;
    private double min_amount;
    private int hidden;
    private double fee;

    public int decimal_places() {
        return decimal_places;
    }

    public double min_price() {
        return min_price;
    }

    public double max_price() {
        return max_price;
    }

    public double min_amount() {
        return min_amount;
    }

    public int hidden() {
        return hidden;
    }

    public double fee() {
        return fee;
    }
}
