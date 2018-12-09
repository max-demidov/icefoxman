package nz.wex.icefoxman.framework.response;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by max user on 06.11.2017.
 */
public class Depth {

    private List<List<Double>> asks;
    private List<List<Double>> bids;

    public List<Item> asks() {
        List<Item> items = new ArrayList<>(asks.size());
        for (List<Double> ask : asks) {
            items.add(new Item(ask.get(0), ask.get(1)));
        }
        return items;
    }

    public List<Item> bids() {
        List<Item> items = new ArrayList<>(bids.size());
        for (List<Double> bid : bids) {
            items.add(new Item(bid.get(0), bid.get(1)));
        }
        return items;
    }

    public class Item {
        private Double price;
        private Double amount;

        private Item(Double price, Double amount) {
            this.price = price;
            this.amount = amount;
        }

        public Double amount() {
            return amount;
        }

        public Double price() {
            return price;
        }
    }

}
