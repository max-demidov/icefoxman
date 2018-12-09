package nz.wex.icefoxman.trading.bot;

import nz.wex.icefoxman.framework.api.PublicApi;
import nz.wex.icefoxman.framework.response.Order;
import nz.wex.icefoxman.framework.response.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by max user on 28.12.2017.
 */
public class Bot4 extends BaseBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bot4.class);

    private State state = State.TO_BUY;
    private Trend trend = Trend.NORMAL;

    private enum State {
        TO_BUY,
        BUY_ORDER,
//        HOLD,
        TO_SELL,
        SELL_ORDER
    }

    private enum Trend {
        NORMAL,
        PANIC
    }

    public Bot4(String pair, double amount) {
        LOGGER.info("Start trading with {} (pair {}, amount {})", this.getClass().getSimpleName(), pair, amount);
        this.pair = pair;
        this.decimals = PublicApi.info().get(pair).decimal_places();
        this.defaultAmountToBuy = amount;
        this.amountToBuy = defaultAmountToBuy;
        this.defaultAmountToSell = round(amountToBuy * (1 - FEE), decimals);
        this.amountToSell = defaultAmountToSell;
    }

    public Bot4(String pair, int funds, double part) {
        LOGGER.info("Start trading with {} (pair {}, funds ${})", this.getClass().getSimpleName(), pair, funds * part);
        this.pair = pair;
        this.decimals = PublicApi.info().get(pair).decimal_places();
        double price = PublicApi.ticker(pair).last();
        this.defaultAmountToBuy = round(funds * part / price, 2);
        this.amountToBuy = defaultAmountToBuy;
        this.defaultAmountToSell = round(amountToBuy * (1 - FEE), decimals);
        this.amountToSell = defaultAmountToSell;
    }

    public void run() {
        this.setName(pair);
//        Double prevHigh = PublicApi.ticker(pair).high();

        Order buyOrder = findOrder("buy");
        Order sellOrder = findOrder("sell");

        if (buyOrder != null && sellOrder != null) {
            if (buyOrder.timestamp_created() > sellOrder.timestamp_created()) {
                deleteSerialization(pair, "sell");
                sellOrder = null;
            } else {
                deleteSerialization(pair, "buy");
                buyOrder = null;
            }
        }

        while (true) {
            Ticker ticker;
            try {
                ticker = PublicApi.ticker(pair);
            } catch (Exception e) {
                e.printStackTrace();
                sleep();
                continue;
            }

            if (ticker.last() > ticker.avg() * .95) {
                trend = Trend.NORMAL;
            }
            if (ticker.last() < ticker.avg() * .9) {
                trend = Trend.PANIC;
            }

            // buy for (avg - low) / 2, sell for (high - avg) / 2
//            Double spread = round( .05 * (ticker.high() - ticker.low()), decimals);
//            Double priceToBuy = round((ticker.avg() + ticker.low()) / 2  + spread, decimals);
//            Double priceToSell = round(Math.max((ticker.high() + ticker.avg()) / 2 - spread, priceToBuy * 1.02), decimals);
            // commented 2018.01.21

            // buy for 1.01 of low, sell for .99 of high
            Double priceToBuy = round(1.01 * ticker.low(), decimals);
            Double priceToSell = round(Math.max(.99 * ticker.high(), priceToBuy * 1.02), decimals);
            LOGGER.debug("[{}] {} trend, price to buy ${}, price to sell ${}", pair, trend, priceToBuy, priceToSell);

            if (buyOrder != null && buyOrder.status() < 1) {
                state = State.BUY_ORDER;
                buyOrder = getOrder(buyOrder.id());
                this.amountToBuy = buyOrder.amount();
            }
            if (sellOrder != null && sellOrder.status() < 1) {
                state = State.SELL_ORDER;
                sellOrder = getOrder(sellOrder.id());
                this.amountToSell = sellOrder.amount();
            }

            if (state.equals(State.TO_BUY)) {
                if (trend.equals(Trend.PANIC)) {
                    sleep();
                    continue;
                }
                this.amountToBuy = defaultAmountToBuy;

                // buy & hold strategy
//                if (ticker.last() > prevHigh) {
//                    createOrder("buy", round(ticker.last() * 1.05, decimals));
//                    state = State.HOLD;
//                    prevHigh = ticker.high();
//                    sleep();
//                    continue;
//                }

                // between min & max trading
                buyOrder = createOrder("buy", priceToBuy);
                if (buyOrder == null) {
                    state = State.TO_SELL;
                } else {
                    state = State.BUY_ORDER;
                }
            }

            if (state.equals(State.BUY_ORDER)) {
                // buy & hold strategy
//                if (ticker.last() > prevHigh) {
//                    if (buyOrder != null) {
//                        cancelOrder(buyOrder);
//                        buyOrder = null;
//                    }
//                    createOrder("buy", round(ticker.last() * 1.05, decimals));
//                    state = State.HOLD;
//                    prevHigh = ticker.high();
//                    sleep();
//                    continue;
//                }

                if (buyOrder == null || buyOrder.status() == 1) {
                    state = State.TO_SELL;
                    Double amount = buyOrder == null ? defaultAmountToBuy : buyOrder.start_amount();
                    Double price = buyOrder == null ? priceToBuy : buyOrder.rate();
                    logDeal("buy", amount, price);
                    buyOrder = null;
                } else if (buyOrder.amount() > .01) {
                    buyOrder = updateOrder(buyOrder, priceToBuy);
                }
            }

            if (state.equals(State.TO_SELL)) {
                this.amountToSell = defaultAmountToSell;
                sellOrder = createOrder("sell", priceToSell);
                if (sellOrder == null) {
                    state = State.TO_BUY;
                } else {
                    state = State.SELL_ORDER;
                }
            }

            if (state.equals(State.SELL_ORDER)) {
                if (sellOrder == null || sellOrder.status() == 1) {
                    state = State.TO_BUY;
                    Double amount = sellOrder == null ? defaultAmountToSell : sellOrder.start_amount();
                    Double price = sellOrder == null ? priceToSell : sellOrder.rate();
                    logDeal("sell", amount, price);
                    sellOrder = null;
                } else if (sellOrder.amount() > .01) {
                    sellOrder = updateOrder(sellOrder, priceToSell);
                }
            }

//            if (state.equals(State.HOLD)) {
//                Double signalRate = round( (ticker.high() + ticker.avg()) / 2, decimals);
//                LOGGER.debug("Signal price to sell ${}", signalRate);
//                if (ticker.last() < signalRate) {
//                    createOrder("sell", round(ticker.last() * .9, decimals));
//                    state = State.TO_BUY;
//                }
//            }

            LOGGER.debug("[{}] state {}", pair, state);
            LOGGER.debug("____________________");
//            prevHigh = ticker.high();
            sleep();
        }
    }

}
