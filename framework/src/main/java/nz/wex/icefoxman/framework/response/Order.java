package nz.wex.icefoxman.framework.response;

import java.io.Serializable;

/**
 * Created by max user on 08.11.2017.
 */
public class Order implements Serializable {
    private int id;
    private String pair;
    private String type;
    private double start_amount;
    private double amount;
    private double rate;
    private long timestamp_created;
    private int status;

    public int id() {
        return id;
    }

    public String pair() {
        return pair;
    }

    public String type() {
        return type;
    }

    public double start_amount() {
        return start_amount;
    }

    public double amount() {
        return amount;
    }

    public double rate() {
        return rate;
    }

    public long timestamp_created() {
        return timestamp_created;
    }

    public int status() {
        return status;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override public String toString() {
        return "Order{" +
                "id=" + id +
                ", pair='" + pair + '\'' +
                ", type='" + type + '\'' +
                ", start_amount=" + start_amount +
                ", amount=" + amount +
                ", rate=" + rate +
                '}';
    }
}
