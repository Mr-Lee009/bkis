package vn.edu.bkis.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class SecurityUtil {
    public static String calculateHmac(String data, String secret)
        throws NoSuchAlgorithmException, InvalidKeyException {
        // 1. Specify the algorithm
        String algorithm = "HmacSHA256";

        // 2. Create key specification
        SecretKeySpec secretKeySpec = new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8), algorithm
        );

        // 3. Get Mac instance and initialize with key
        Mac mac = Mac.getInstance(algorithm);
        mac.init(secretKeySpec);

        // 4. Calculate HMAC
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // 5. Convert bytes to Hex
        return HexFormat.of().formatHex(hmacBytes);
    }
}
