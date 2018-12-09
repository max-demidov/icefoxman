package nz.wex.icefoxman.framework;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

/**
 * Created by max user on 05.11.2017.
 */
public class Encoder {

    public static final String ENCODER_TYPE = "HmacSHA512";

    public static String encode(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance(ENCODER_TYPE);
        mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), ENCODER_TYPE));
        return DatatypeConverter.printHexBinary(mac.doFinal(data.getBytes("UTF-8"))).toLowerCase();
    }

}
