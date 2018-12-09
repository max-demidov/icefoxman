package nz.wex.icefoxman.framework.response;

import java.util.Map;

/**
 * Created by max user on 08.11.2017.
 */
public class GetInfo {
    private Map<String, Double> funds;
    private Map<String, Integer> rights;
    private int transaction_count;
    private int open_orders;
    private long server_time;

    public Map<String, Double> funds() {
        return funds;
    }

    public Map<String, Integer> rights() {
        return rights;
    }

    public int transaction_count() {
        return transaction_count;
    }

    public int open_orders() {
        return open_orders;
    }

    public long server_time() {
        return server_time;
    }
}
