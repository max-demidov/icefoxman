package nz.wex.icefoxman.framework.response;

import java.util.Map;

/**
 * Created by max user on 08.11.2017.
 */
public class Trade {
    private Double received;
    private Double remains;
    private int order_id;
    private Map<String, Double> funds;

    public Double received() {
        return received;
    }

    public Double remains() {
        return remains;
    }

    public int order_id() {
        return order_id;
    }

    public Map<String, Double> funds() {
        return funds;
    }
}
