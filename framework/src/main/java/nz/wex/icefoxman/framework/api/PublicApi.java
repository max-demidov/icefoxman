package nz.wex.icefoxman.framework.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import nz.wex.icefoxman.framework.response.Depth;
import nz.wex.icefoxman.framework.response.Info;
import nz.wex.icefoxman.framework.response.Ticker;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Documentation: https://wex.nz/api/3/docs
 * <p>
 * Created by max user on 06.11.2017.
 */
public class PublicApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublicApi.class);
    private static final String WEX_PUBLIC_API_URL = "https://wex.nz/api/3";

    /**
     * his method provides all the information about currently active pairs, such as the maximum number
     * of digits after the decimal point, the minimum price, the maximum price, the minimum transaction size,
     * whether the pair is hidden, the commission for each pair.
     * <p>
     * Sample request:
     * https://wex.nz/api/3/info
     * <p>
     * Sample response:
     * {
     * "server_time":1370814956,
     * "pairs":{
     * "btc_usd":{
     * "decimal_places":3,
     * "min_price":0.1,
     * "max_price":400,
     * "min_amount":0.01,
     * "hidden":0,
     * "fee":0.2
     * }
     * ...
     * }
     * }
     * <p>
     * decimal_places: number of decimals allowed during trading.
     * min_price: minimum price allowed during trading.
     * max_price: maximum price allowed during trading.
     * min_amount: minimum sell / buy transaction size.
     * hidden: whether the pair is hidden, 0 or 1.
     * fee: commission for this pair.
     * <p>
     * A hidden pair (hidden=1) remains active but is not displayed in the list of pairs on the main page.
     * The Commission is displayed for all users, it will not change even if it was reduced on your account
     * in case of promotional pricing.
     * If one of the pairs is disabled, it will simply disappear from the list.
     *
     */
    public static Map<String, Info> info() {
        JsonObject json = sendRequest("/info");
        Type type = new TypeToken<Map<String, Info>>() {
        }.getType();
        return new Gson().fromJson(json.getAsJsonObject("pairs"), type);
    }

    /**
     * This method provides all the information about currently active pairs, such as: the maximum price,
     * the minimum price, average price, trade volume, trade volume in currency, the last trade, Buy and Sell price.
     * All information is provided over the past 24 hours.
     * <p>
     * Sample request:
     * https://wex.nz/api/3/ticker/btc_usd
     * <p>
     * Sample response:
     * {
     * "btc_usd":{
     * "high":109.88,
     * "low":91.14,
     * "avg":100.51,
     * "vol":1632898.2249,
     * "vol_cur":16541.51969,
     * "last":101.773,
     * "buy":101.9,
     * "sell":101.773,
     * "updated":1370816308
     * }
     * ...
     * }
     * <p>
     * high: maximum price.
     * low: minimum price.
     * avg: average price.
     * vol: trade volume.
     * vol_cur: trade volume in currency.
     * last: the price of the last trade.
     * buy: buy price.
     * sell: sell price.
     * updated: last update of cache.
     *
     */
    public static Ticker ticker(String pair) {
        JsonObject json = sendRequest("/ticker/" + pair);
        return new Gson().fromJson(json.getAsJsonObject(pair), Ticker.class);
    }

    /**
     * This method provides the information about active orders on the pair.
     * <p>
     * Additionally it accepts an optional GET-parameter limit,
     * which indicates how many orders should be displayed (150 by default).
     * Is set to less than 5000.
     * <p>
     * Sample request:
     * https://wex.nz/api/3/depth/btc_usd
     * <p>
     * Sample response:
     * {
     * "btc_usd":{
     * "asks":[
     * [103.426,0.01],
     * [103.5,15],
     * [103.504,0.425],
     * [103.505,0.1],
     * ...
     * ],
     * "bids":[
     * [103.2,2.48502251],
     * [103.082,0.46540304],
     * [102.91,0.99007913],
     * [102.83,0.07832332],
     * ...
     * ]
     * }
     * ...
     * }
     * <p>
     * asks: Sell orders.
     * bids: Buy orders.
     *
     */
    public static Depth depth(String pair, int limit) {
        JsonObject json = sendRequest("/depth/" + pair + "?limit=" + limit);
        return new Gson().fromJson(json.getAsJsonObject(pair), Depth.class);
    }

    /**
     * This method provides the information about the last trades.
     * <p>
     * Additionally it accepts an optional GET-parameter limit,
     * which indicates how many orders should be displayed (150 by default).
     * The maximum allowable value is 5000.
     * <p>
     * Sample request:
     * https://wex.nz/api/3/trades/btc_usd
     * <p>
     * Sample response:
     * {
     * "btc_usd":[
     * {
     * "type":"ask",
     * "price":103.6,
     * "amount":0.101,
     * "tid":4861261,
     * "timestamp":1370818007
     * },
     * {
     * "type":"bid",
     * "price":103.989,
     * "amount":1.51414,
     * "tid":4861254,
     * "timestamp":1370817960
     * },
     * ...
     * ]
     * ...
     * }
     * <p>
     * type: ask – Sell, bid – Buy.
     * price: Buy price/Sell price.
     * amount: the amount of asset bought/sold.
     * tid: trade ID.
     * timestamp: UNIX time of the trade.
     *
     */
    public static JsonObject trades(String pair, int limit) {
        return sendRequest("/trades/" + pair + "?limit=" + limit);
    }

    /* internal methods */

    private static synchronized JsonObject sendRequest(String urlPath) {
        LOGGER.debug("Send GET request to: {}", WEX_PUBLIC_API_URL + urlPath);
        String message = null;
        JsonObject result = null;

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(WEX_PUBLIC_API_URL + urlPath);

        try {
            HttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();
            message = EntityUtils.toString(entity);
            result = new JsonParser().parse(message).getAsJsonObject();
        } catch (Exception e) {
            LOGGER.error("Failed to perform GET request", e);
            Toolkit.getDefaultToolkit().beep();
        }

        LOGGER.debug("Got response: {}", message);
        return result;
    }

}
