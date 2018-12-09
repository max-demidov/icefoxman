package nz.wex.icefoxman.framework.response;

/**
 * Created by max user on 08.11.2017.
 */
public class TradeHistory {
    private int id;
    private String pair;
    private String type;
    private double amount;
    private double rate;
    private int order_id;
    private int is_your_order;
    private long timestamp;

    public int id() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String pair() {
        return pair;
    }

    public String type() {
        return type;
    }

    public double amount() {
        return amount;
    }

    public double rate() {
        return rate;
    }

    public int order_id() {
        return order_id;
    }

    public int is_your_order() {
        return is_your_order;
    }

    public long timestamp() {
        return timestamp;
    }
}
