package nz.wex.icefoxman.trading.bot;

import nz.wex.icefoxman.framework.api.PublicApi;
import nz.wex.icefoxman.framework.response.Order;
import nz.wex.icefoxman.framework.response.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by max user on 25.12.2017.
 */
public class Bot2 extends BaseBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bot2.class);

    private State state = State.TO_BUY;
    private Trend trend = Trend.DOWN;

    private enum State {
        TO_BUY,
        BUY_ORDER,
        TO_SELL,
        SELL_ORDER
    }

    private enum Trend {
        UP,
        DOWN,
        PANIC
    }

    public Bot2(String pair, double amount) {
        LOGGER.info("Start trading with {} (pair {}, amount {})", this.getClass().getSimpleName(), pair, amount);
        this.pair = pair;
        this.decimals = PublicApi.info().get(pair).decimal_places();
        this.defaultAmountToBuy = amount;
        this.amountToBuy = defaultAmountToBuy;
        this.defaultAmountToSell = round(amountToBuy * (1 - FEE), decimals);
        this.amountToSell = defaultAmountToSell;
    }

    public void run() {
        this.setName(pair);

        Order buyOrder = findOrder("buy");
        Order sellOrder = findOrder("sell");

        while (true) {
            Ticker ticker;
            try {
                ticker = PublicApi.ticker(pair);
            } catch (Exception e) {
                e.printStackTrace();
                sleep();
                continue;
            }

            if (ticker.last() > ticker.avg() * 1.03) {
                trend = Trend.UP;
            }
            if (ticker.last() < ticker.avg() * .97 && ticker.last() > ticker.avg() * .9) {
                trend = Trend.DOWN;
            }
            if (ticker.last() < ticker.avg() * .85) {
                trend = Trend.PANIC;
            }

            // !!! too big looses !!! better to invert the strategy
            Double spread = round((ticker.high() - ticker.low()) / 6, decimals);
            Double priceToBuy = trend.equals(Trend.UP) ?
                    round(ticker.high() - spread, decimals) :
                    round(ticker.low() + spread, decimals);
            Double priceToSell = round(Math.max(priceToBuy + spread, priceToBuy * 1.02), decimals);
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
                buyOrder = createOrder("buy", priceToBuy);
                if (buyOrder == null) {
                    state = State.TO_SELL;
                } else {
                    state = State.BUY_ORDER;
                }
            }

            if (state.equals(State.BUY_ORDER)) {
                if (buyOrder == null || buyOrder.status() == 1) {
                    state = State.TO_SELL;
                    Double price = buyOrder == null ? priceToBuy : buyOrder.rate();
                    logDeal("buy", defaultAmountToBuy, price);
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
                    Double price = sellOrder == null ? priceToSell : sellOrder.rate();
                    logDeal("sell", defaultAmountToSell, price);
                    sellOrder = null;
                } else if (sellOrder.amount() > .01) {
                    sellOrder = updateOrder(sellOrder, priceToSell);
                }
            }

            LOGGER.debug("[{}] state {}", pair, state);
            LOGGER.debug("____________________");
            sleep();
        }
    }

}
