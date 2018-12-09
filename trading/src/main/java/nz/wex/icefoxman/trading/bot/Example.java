package nz.wex.icefoxman.trading.bot;

import nz.wex.icefoxman.framework.api.PublicApi;
import nz.wex.icefoxman.framework.api.TradeApi;
import nz.wex.icefoxman.framework.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by max user on 05.11.2017.
 */
public class Example {

    private static final Logger LOGGER = LoggerFactory.getLogger(Example.class);

    public static void main(String[] args) {
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.MINUTE, 0);
        endCalendar.set(Calendar.SECOND, 0);
        endCalendar.set(Calendar.MILLISECOND, 0);

        DateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        LOGGER.info(format.format(endCalendar.getTime()));

        Calendar sinceCalendar = (Calendar) endCalendar.clone();
        sinceCalendar.add(Calendar.DAY_OF_MONTH, -2);

        long since = sinceCalendar.getTimeInMillis() / 1000L;
        long end = endCalendar.getTimeInMillis() / 1000L;

        List<TradeHistory> tradeHistory = TradeApi.tradeHistory(since, end, "bch_usd");
        LOGGER.info("{}", tradeHistory.get(0).rate());

//        new Bot4("btc_usd", .003).start();
    }

    private static void examples() throws Exception {
        Map<String, Info> info = PublicApi.info();
        LOGGER.info("decimal places for btc_usd: {}", info.get("btc_usd").decimal_places());

        Ticker ticker = PublicApi.ticker("btc_usd");
        LOGGER.info("last {}, avg {}", ticker.last(), ticker.avg());

        Depth depth = PublicApi.depth("btc_usd", 5);
        LOGGER.info("{} - {}", depth.asks().get(0).price(), depth.bids().get(4).amount());
        PublicApi.trades("btc_usd", 3);

        GetInfo getInfo = TradeApi.getInfo();
        LOGGER.info("usd {}, btc {}", getInfo.funds().get("usd"), getInfo.funds().get("btc"));
        LOGGER.info("server_time {}", getInfo.server_time());

        Map<Integer, Order> activeOrders = TradeApi.activeOrders("btc_usd");
        LOGGER.info("btc_usd orders {}", activeOrders.get(44752093).amount());

        Trade trade = TradeApi.trade("ppc_usd", "buy", 1.25, 20);
        LOGGER.info("Order {} created, USD funds {}", trade.order_id(), trade.funds().get("usd"));

        Thread.sleep(2000);
        Map<String, Double> funds = TradeApi.cancelOrder(trade.order_id());
        LOGGER.info("Order {} cancelled, USD funds {}", trade.order_id(), funds.get("usd"));

    }
}
