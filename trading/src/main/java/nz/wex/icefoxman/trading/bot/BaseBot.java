package nz.wex.icefoxman.trading.bot;

import nz.wex.icefoxman.framework.api.TradeApi;
import nz.wex.icefoxman.framework.csv.writer.CsvWriter;
import nz.wex.icefoxman.framework.response.Order;
import nz.wex.icefoxman.framework.response.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

/**
 * Created by max user on 02.01.2018.
 */
public class BaseBot extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseBot.class);
    private static final String SERIALIZATION_FOLDER = "log\\serialization";
    protected static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    protected static final Double FEE = .002;

    protected String pair;
    protected Double defaultAmountToBuy;
    protected Double amountToBuy;
    protected Double defaultAmountToSell;
    protected Double amountToSell;
    protected int decimals;

    protected Order findOrder(String type) {
        String filename = String.format("%s%s%s.%s.ser", SERIALIZATION_FOLDER, File.separator, pair, type);
        Order order = deserialize(filename);
        if (order != null) {
            LOGGER.info("Active {} order deserialized: {}", type, order);
            return order;
        }
        Map<Integer, Order> orders = TradeApi.activeOrders(pair);
        for (Order o : orders.values()) {
            if (o.type().equals(type) && o.amount() <= amountToBuy) {
                LOGGER.info("Active {} order found: {}", type, o);
                return o;
            }
        }
        return null;
    }

    private Order deserialize(String filename) {
        try {
            FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream oin = new ObjectInputStream(fis);
            return (Order) oin.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.warn("Could not deserialize Order:", e);
        }
        return null;
    }

    protected Order updateOrder(Order order, Double price) {
        if (order.rate() == price) {
            return order;
        }
        cancelOrder(order);
        return createOrder(order.type(), price);
    }

    protected Order getOrder(int id) {
        Order order = TradeApi.orderInfo(id);
        if (order == null) {
            sleep();
            order = TradeApi.orderInfo(id);
        }
        return order;
    }

    protected Order createOrder(String type, Double price) {
        Double amount = "buy".equals(type) ? amountToBuy : amountToSell;
        Trade trade = TradeApi.trade(pair, type, price, amount);
        LOGGER.debug("Order created: [{} {} for ${}]", type, pair, price);
        if (trade.remains() == 0) {
            logDeal(type, amount, price);
            return null;
        }
        Order order = TradeApi.orderInfo(trade.order_id());
        serialize(order);
        return order;
    }

    private void serialize(Order order) {
        if (order == null) {
            LOGGER.warn("Cannot serialize NULL object");
            return;
        }
        String filename = String.format("%s%s%s.%s.ser", SERIALIZATION_FOLDER, File.separator, pair, order.type());
        try {
            File file = new File(filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(order);
            oos.close();
            fos.close();
        } catch (IOException e) {
            LOGGER.warn("Serialization of [{}] failed: ", order, e);
        }
    }

    protected void cancelOrder(Order order) {
        LOGGER.debug("Cancel {}", order);
        try {
            TradeApi.cancelOrder(order.id());
        } finally {
            deleteSerialization(order.pair(), order.type());
        }
    }

    protected double round(double value, int decimals) {
        long factor = (long) Math.pow(10, decimals);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    protected void logDeal(String type, Double amount, Double price) {
        LOGGER.info("Order [{} {} for ${}] executed completely", type, pair, price);
        String time = DATE_FORMAT.format(Calendar.getInstance().getTime());
        Double sum = round(amount * price, decimals);
        //              "Time","Type","Pair","Amount","Price","Sum","Profit"
        Object[] line = {time, type, pair, amount, price, sum, ""};
        new CsvWriter(pair).append(line);
        deleteSerialization(pair, type);
    }

    protected void deleteSerialization(String pair, String type) {
        String filename = String.format("%s%s%s.%s.ser", SERIALIZATION_FOLDER, File.separator, pair, type);
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
    }

    protected void sleep() {
        try {
            sleep(120_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
