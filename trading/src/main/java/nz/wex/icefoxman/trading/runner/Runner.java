package nz.wex.icefoxman.trading.runner;

import nz.wex.icefoxman.trading.bot.Bot4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by max user on 26.12.2017.
 */
public class Runner {
    private static final Logger LOGGER = LoggerFactory.getLogger(Runner.class);
    private static final int funds = 15_000;

    @SuppressWarnings("unchecked") public static void main(String args[]) throws InterruptedException {
        List<Bot4> bots = new ArrayList() {
            {
//                add(new Bot4("bch_usd", 1.2));
//                add(new Bot4("btc_usd", .37));
//                add(new Bot4("dsh_usd", 2.6));
//                add(new Bot4("eth_usd", 2.9));
//                add(new Bot4("ltc_usd", 10.3));
//                add(new Bot4("nmc_usd", 380));
//                add(new Bot4("nvc_usd", 260));
//                add(new Bot4("ppc_usd", 340));
//                add(new Bot4("zec_usd", 4.7));

//                add(new Bot4("bch_usd", funds, .10));
//                add(new Bot4("btc_usd", funds, .25));
//                add(new Bot4("dsh_usd", funds, .10));
//                add(new Bot4("eth_usd", funds, .10));
//                add(new Bot4("ltc_usd", funds, .10));
//                add(new Bot4("nmc_usd", funds, .05));
//                add(new Bot4("nvc_usd", funds, .05));
//                add(new Bot4("ppc_usd", funds, .05));
//                add(new Bot4("zec_usd", funds, .10));

                add(new Bot4("bch_usd", funds, .15));
                add(new Bot4("btc_usd", funds, .20));
                add(new Bot4("dsh_usd", funds, .15));
                add(new Bot4("eth_usd", funds, .15));
                add(new Bot4("ltc_usd", funds, .15));
                add(new Bot4("zec_usd", funds, .15));
            }
        };
        for (Bot4 bot : bots) {
            bot.start();
            Thread.sleep(10_000);
        }
        while (true) {
            LOGGER.debug("====================");
            for (Bot4 bot : bots) {
                LOGGER.debug("{} {}", bot.getName(), bot.getState().name());
            }
            LOGGER.debug("====================");
            Thread.sleep(300_000);
        }
    }

}
