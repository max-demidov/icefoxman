package nz.wex.icefoxman.framework.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import nz.wex.icefoxman.framework.AuthData;
import nz.wex.icefoxman.framework.Encoder;
import nz.wex.icefoxman.framework.response.GetInfo;
import nz.wex.icefoxman.framework.response.Order;
import nz.wex.icefoxman.framework.response.Trade;
import nz.wex.icefoxman.framework.response.TradeHistory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.List;

/**
 * Documentation: https://wex.nz/tapi/docs
 * <p>
 * Created by max user on 05.11.2017.
 */
public class TradeApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(TradeApi.class);
    private static final String WEX_TRADE_API_URL = "https://wex.nz/tapi";

    private static long nonce = getActualNonce();

    /**
     * Returns information about the user’s current balance, API-key privileges, the number of open orders and Server Time.
     * To use this method you need a privilege of the key info.
     * Parameters:
     * None.
     * Sample response:
     * {
     * "success":1,
     * "return":{
     * "funds":{
     * "usd":325,
     * "btc":23.998,
     * "ltc":0,
     * ...
     * },
     * "rights":{
     * "info":1,
     * "trade":0,
     * "withdraw":0
     * },
     * "transaction_count":0,
     * "open_orders":1,
     * "server_time":1342123547
     * }
     * }
     * <p>
     * funds: Your account balance available for trading. Doesn’t include funds on your open orders.
     * rights: The privileges of the current API key. At this time the privilege to withdraw is not used anywhere.
     * transaction_count: Deprecated, is equal to 0.
     * open_orders: The number of your open orders.
     * server_time: Server time (MSK).
     *
     */
    public static synchronized GetInfo getInfo() {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("method", "getInfo"));

        JsonObject json = sendAuthorizedRequest(params);
        return new Gson().fromJson(json.getAsJsonObject("return"), GetInfo.class);
    }

    /**
     * The basic method that can be used for creating orders and trading on the exchange.
     * To use this method you need an API key privilege to trade.
     * <p>
     * You can only create limit orders using this method, but you can emulate market orders using rate parameters.
     * E.g. using rate=0.1 you can sell at the best market price.
     * Each pair has a different limit on the minimum / maximum amounts, the minimum amount and the number of digits
     * after the decimal point. All limitations can be obtained using the info method in PublicAPI v3.
     * Parameters:
     * Parameter	description	assumes value
     * pair	pair	btc_usd (example)
     * type	order type	buy or sell
     * rate	the rate at which you need to buy/sell	numerical
     * amount	the amount you need to buy / sell	numerical
     * <p>
     * You can get the list of pairs using the info method in PublicAPI v3.
     * Sample response:
     * {
     * "success":1,
     * "return":{
     * "received":0.1,
     * "remains":0,
     * "order_id":0,
     * "funds":{
     * "usd":325,
     * "btc":2.498,
     * "ltc":0,
     * ...
     * }
     * }
     * }
     * <p>
     * received: The amount of currency bought/sold.
     * remains: The remaining amount of currency to be bought/sold (and the initial order amount).
     * order_id: Is equal to 0 if the request was fully “matched” by the opposite orders,
     * otherwise the ID of the executed order will be returned.
     * funds: Balance after the request.
     *
     */
    public static synchronized Trade trade(String pair, String type, double rate, double amount) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("method", "Trade"));
        params.add(new BasicNameValuePair("pair", pair));
        params.add(new BasicNameValuePair("type", type));
        params.add(new BasicNameValuePair("rate", String.valueOf(rate)));
        params.add(new BasicNameValuePair("amount", String.valueOf(amount)));

        JsonObject json = sendAuthorizedRequest(params);
        return new Gson().fromJson(json.getAsJsonObject("return"), Trade.class);
    }

    /**
     * Returns the list of your active orders.
     * To use this method you need a privilege of the info key.
     * <p>
     * If the order disappears from the list, it was either executed or canceled.
     * Optional parameters:
     * Parameter	description	assumes value	standard value
     * pair	pair	btc_usd (example)	all pairs
     * <p>
     * You can get the list of pairs using the info method in PublicAPI v3.
     * Sample response:
     * {
     * "success":1,
     * "return":{
     * "343152":{
     * "pair":"btc_usd",
     * "type":"sell",
     * "amount":12.345,
     * "rate":485,
     * "timestamp_created":1342448420,
     * "status":0
     * },
     * ...
     * }
     * }
     * <p>
     * Array key : Order ID.
     * pair: The pair on which the order was created.
     * type: Order type, buy/sell.
     * amount: The amount of currency to be bought/sold.
     * rate: Sell/Buy price.
     * timestamp_created: The time when the order was created.
     * status: Deprecated, is always equal to 0.
     *
     */
    public static synchronized Map<Integer, Order> activeOrders(String pair) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("method", "ActiveOrders"));
        params.add(new BasicNameValuePair("pair", pair));

        JsonObject json = sendAuthorizedRequest(params);
        Type type = new TypeToken<Map<Integer, Order>>() {
        }.getType();
        Map<Integer, Order> orders = new Gson().fromJson(json.getAsJsonObject("return"), type);
        if (orders == null) {
            return new HashMap<>();
        }
        for (Map.Entry<Integer, Order> item : orders.entrySet()) {
            item.getValue().setId(item.getKey());
        }
        return orders;
    }

    /**
     * Returns the information on particular order.
     * To use this method you need a privilege of the info key.
     * Parameters:
     * Parameter	description	assumes value
     * order_id	order ID	numerical
     * Sample response:
     * {
     * "success":1,
     * "return":{
     * "343152":{
     * "pair":"btc_usd",
     * "type":"sell",
     * "start_amount":13.345,
     * "amount":12.345,
     * "rate":485,
     * "timestamp_created":1342448420,
     * "status":0
     * }
     * }
     * }
     * <p>
     * Array key: Order ID.
     * pair: The pair on which the order was created
     * type: Order type, buy/sell.
     * start_amount: The initial amount at the time of order creation.
     * amount: The remaining amount of currency to be bought/sold.
     * rate: Sell/Buy price.
     * timestamp_created: The time when the order was created.
     * status: 0 - active, 1 – executed order, 2 - canceled, 3 – canceled, but was partially executed.
     *
     */
    public static synchronized Order orderInfo(int order_id) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("method", "OrderInfo"));
        params.add(new BasicNameValuePair("order_id", String.valueOf(order_id)));

        JsonObject json = sendAuthorizedRequest(params);
        if (json == null) {
            return null;
        }
        Order order = new Gson()
                .fromJson(json.getAsJsonObject("return").getAsJsonObject(String.valueOf(order_id)), Order.class);
        order.setId(order_id);
        return order;
    }

    /**
     * This method is used for order cancelation.
     * To use this method you need a privilege of the trade key.
     * Parameters:
     * Parameter	description	assumes value
     * order_id	order ID	numerical
     * Sample response:
     * {
     * "success":1,
     * "return":{
     * "order_id":343154,
     * "funds":{
     * "usd":325,
     * "btc":24.998,
     * "ltc":0,
     * ...
     * }
     * }
     * }
     * <p>
     * order_id: The ID of canceled order.
     * funds: Balance upon request.
     *
     */
    public static synchronized Map<String, Double> cancelOrder(long order_id) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("method", "CancelOrder"));
        params.add(new BasicNameValuePair("order_id", String.valueOf(order_id)));

        JsonObject json = sendAuthorizedRequest(params);
        Type type = new TypeToken<Map<String, Double>>() {
        }.getType();
        return new Gson().fromJson(json.getAsJsonObject("return").getAsJsonObject("funds"), type);
    }

    public static synchronized List<TradeHistory> tradeHistory(long since, long end, String pair) {
        return tradeHistory(-1, -1, -1, -1, "ASC", since, end, pair);
    }

    /**
     * Returns trade history.
     * To use this method you need a privilege of the info key.
     * Optional parameters:
     * Parameter	description	assumes value	standard value
     * from	trade ID, from which the display starts	numerical	0
     * count	the number of trades for display	numerical	1000
     * from_id	trade ID, from which the display starts	numerical	0
     * end_id	trade ID on which the display ends	numerical	∞
     * order	Sorting	ASC or DESC	DESC
     * since	the time to start the display	UNIX time	0
     * end	the time to end the display	UNIX time	∞
     * pair	pair to be displayed	btc_usd (example)	all pairs
     * <p>
     * When using parameters since or end, the order parameter automatically assumes the value ASC.
     * When using the since parameter the maximum time that can displayed is 1 week.
     * Sample response:
     * {
     * "success":1,
     * "return":{
     * "166830":{
     * "pair":"btc_usd",
     * "type":"sell",
     * "amount":1,
     * "rate":450,
     * "order_id":343148,
     * "is_your_order":1,
     * "timestamp":1342445793
     * }
     * }
     * }
     * <p>
     * Array keys: Trade ID.
     * pair: The pair on which the trade was executed.
     * type: Trade type, buy/sell.
     * amount: The amount of currency was bought/sold.
     * rate: Sell/Buy price.
     * order_id: Order ID.
     * is_your_order: Is equal to 1 if order_id is your order, otherwise is equal to 0.
     * timestamp: Trade execution time.
     *
     */
    public static synchronized List<TradeHistory> tradeHistory(long from, long count, long from_id, long end_id,
            String order, long since, long end, String pair) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("method", "TradeHistory"));
        if (from > 0) {
            params.add(new BasicNameValuePair("from", String.valueOf(from)));
        }
        if (count > 0) {
            params.add(new BasicNameValuePair("count", String.valueOf(count)));
        }
        if (from_id > 0) {
            params.add(new BasicNameValuePair("from_id", String.valueOf(from_id)));
        }
        if (end_id > 0) {
            params.add(new BasicNameValuePair("end_id", String.valueOf(end_id)));
        }
        if (order != null) {
            params.add(new BasicNameValuePair("order", order));
        }
        params.add(new BasicNameValuePair("since", String.valueOf(since)));
        params.add(new BasicNameValuePair("end", String.valueOf(end)));
        params.add(new BasicNameValuePair("pair", pair));

        JsonObject json = sendAuthorizedRequest(params);
        if (json == null) {
            return null;
        }
        Type type = new TypeToken<Map<Integer, TradeHistory>>() {
        }.getType();
        LinkedHashMap<Integer, TradeHistory> tradeHistory = new Gson().fromJson(json.getAsJsonObject("return"), type);
        if (tradeHistory == null) {
            return new ArrayList<>();
        }
        for (Map.Entry<Integer, TradeHistory> item : tradeHistory.entrySet()) {
            item.getValue().setId(item.getKey());
        }
        return new ArrayList<>(tradeHistory.values());
    }

    /**
     * Returns the history of transactions.
     * To use this method you need a privilege of the info key.
     * Optional parameters:
     * Parameter	description	assumes value	standard value
     * from	transaction ID, from which the display starts	numerical	0
     * count	number of transaction to be displayed	numerical	1000
     * from_id	transaction ID, from which the display starts	numerical	0
     * end_id	transaction ID on which the display ends	numerical	∞
     * order	sorting	ASC or DESC	DESC
     * since	the time to start the display	UNIX time	0
     * end	the time to end the display	UNIX time	∞
     * <p>
     * When using the parameters since or end, the order parameter automatically assumes the value ASC.
     * Sample response:
     * {
     * "success":1,
     * "return":{
     * "1081672":{
     * "type":1,
     * "amount":1.00000000,
     * "currency":"BTC",
     * "desc":"BTC Payment",
     * "status":2,
     * "timestamp":1342448420
     * }
     * }
     * }
     * <p>
     * Array keys: Transaction ID.
     * type: Transaction type. 1/2 - deposit/withdrawal, 4/5 - credit/debit.
     * amount: Transaction amount.
     * currency: Transaction currency.
     * desc: Transaction description.
     * status: Transaction status. 0 - canceled/failed, 1 - waiting for acceptance, 2 - successful, 3 – not confirmed
     * timestamp: Transaction time.
     *
     */
    public static synchronized JsonObject transHistory(long from, long count, long from_id, long end_id, String order,
            long since, long end) {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("method", "TransHistory"));
        params.add(new BasicNameValuePair("from", String.valueOf(from)));
        params.add(new BasicNameValuePair("count", String.valueOf(count)));
        params.add(new BasicNameValuePair("from_id", String.valueOf(from_id)));
        params.add(new BasicNameValuePair("end_id", String.valueOf(end_id)));
        params.add(new BasicNameValuePair("order", order));
        params.add(new BasicNameValuePair("since", String.valueOf(since)));
        params.add(new BasicNameValuePair("end", String.valueOf(end)));

        return sendAuthorizedRequest(params);
    }

    /* internal methods */

    private static synchronized JsonObject sendAuthorizedRequest(ArrayList<NameValuePair> params) {
        for (NameValuePair param : params) {
            if (param.getName().equals("nonce")) {
                params.remove(param);
                break;
            }
        }

        params.add(new BasicNameValuePair("nonce", String.valueOf(nonce++)));
        HashMap<String, String> headers = createHeaders(params);
        return sendRequest(params, headers);
    }

    private static synchronized long getActualNonce() {
        LOGGER.debug("Get actual nonce");
        ArrayList<NameValuePair> postData = new ArrayList<>();
        postData.add(new BasicNameValuePair("method", "getInfo"));
        try {
            HashMap<String, String> headers = createHeaders(postData);
            JsonObject result = sendRequest(postData, headers);
            String error = result.get("error").getAsString();
            String sNonce = error.split("you should send:")[1];
            long nonce = Long.parseLong(sNonce);
            LOGGER.debug("Actual nonce is [{}]", nonce);
            return nonce;
        } catch (Exception e) {
            LOGGER.error("Error has occurred while getting actual [nonce]", e);
            return 0;
        }
    }

    private static synchronized HashMap<String, String> createHeaders(ArrayList<NameValuePair> postData) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Key", AuthData.getKey());
        String postDataStr = getPostDataAsString(postData);
        String sign = null;
        try {
            sign = Encoder.encode(postDataStr, AuthData.getSecret());
        } catch (Exception e) {
            e.printStackTrace();
        }
        headers.put("Sign", sign);

        return headers;
    }

    private static synchronized String getPostDataAsString(ArrayList<NameValuePair> postData) {
        StringBuilder sb = new StringBuilder();
        for (NameValuePair pair : postData) {
            sb.append(pair.getName());
            sb.append("=");
            sb.append(pair.getValue());
            sb.append("&");
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    private static synchronized JsonObject sendRequest(ArrayList<NameValuePair> postData,
            HashMap<String, String> headers) {
        LOGGER.debug("Send POST request: {}", postData);
        String message = null;
        JsonObject result = null;

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(WEX_TRADE_API_URL);

        try {
            post.setEntity(new UrlEncodedFormEntity(postData, "UTF-8"));
            headers.forEach(post::addHeader);

            HttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();
            message = EntityUtils.toString(entity);
            result = new JsonParser().parse(message).getAsJsonObject();
        } catch (Exception e) {
            LOGGER.error("Failed to perform POST request", e);
            Toolkit.getDefaultToolkit().beep();
        }

        LOGGER.debug("Got response: {}", message);
        return result;
    }

}
