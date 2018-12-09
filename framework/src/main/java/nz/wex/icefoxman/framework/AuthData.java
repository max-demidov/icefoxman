package nz.wex.icefoxman.framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by max user on 05.11.2017.
 */
public class AuthData {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthData.class);
    private static final String AUTH_DATA_DIR = System.getProperty("authDataDir", "");
    private static final String KEY_FILENAME = "key.txt";
    private static final String SECRET_FILENAME = "secret.txt";

    private static String key;
    private static String secret;

    public static String getKey() {
        return null == key ? readKey() : key;
    }

    public static String getSecret() {
        return null == secret ? readSecret() : secret;
    }

    private static String readKey() {
        LOGGER.debug("Read key");
        return key = readFile(KEY_FILENAME);
    }

    private static String readSecret() {
        LOGGER.debug("Read secret");
        return secret = readFile(SECRET_FILENAME);
    }

    private static String readFile(String filename) {
        if (AUTH_DATA_DIR.isEmpty()) {
            throw new IllegalArgumentException("[authDataDir] parameter has not been specified");
        }
        File file = new File(AUTH_DATA_DIR + File.separator + filename);
        StringBuilder sb = new StringBuilder();
        try {
            Files.readAllLines(file.toPath()).forEach(sb::append);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString().trim();
    }

}
