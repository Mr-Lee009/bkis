package vn.edu.bkis;

import vn.edu.bkis.util.SecurityUtil;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class MockSign {
    /**
     * Chay thu viec ky request MoMo va in ra chuoi JSON request body.
     *
     * @param args tham so dong lenh, hien tai khong su dung
     * @return khong tra ve gia tri vi day la ham chay thu
     * @throws NoSuchAlgorithmException neu thuat toan HMAC khong ton tai
     * @throws InvalidKeyException neu secret key khong hop le
     */
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException {

        // Step 1: khoi tao du lieu mau de ky request MoMo.
        String accessKey = "F8BBA842ECF85";
        String secretKey = "K951B6PE1waDMi640xX08PD3vg6EkVlz";
        String orderInfo = "pay with MoMo";
        String partnerCode = "MOMO";
        String redirectUrl = "https://webhook.site/b3088a6a-2d17-4f8d-a383-71389a6c600b";
        String ipnUrl = "https://webhook.site/b3088a6a-2d17-4f8d-a383-71389a6c600b";
        String requestType ="payWithMethod";
        String amount = "1000";
        String orderId = partnerCode + new Date().getTime();
        String requestId = orderId;
        String extraData ="";
        String orderGroupId ="";
        boolean autoCapture = true;
        String lang = "vi";

        // Step 2: tao raw signature theo dung thu tu field MoMo yeu cau.
        String rawSignature ="accessKey=" + accessKey +"&amount=" + amount +"&extraData=" + extraData +"&ipnUrl=" + ipnUrl +"&orderId=" + orderId +"&orderInfo=" + orderInfo +"&partnerCode=" + partnerCode +"&redirectUrl=" + redirectUrl +"&requestId=" + requestId +"&requestType=" + requestType;
        System.out.println("orderId ="+ orderId);
        System.out.println("--------------------RAW SIGNATURE----------------");
        System.out.println(rawSignature);

        // Step 3: ky request va build JSON body hoan chinh de gui sang endpoint.
        String signature = SecurityUtil.calculateHmac(rawSignature, secretKey);
        System.out.println("HMAC-SHA256:" + signature);
        String requestJson = buildRequestJson(partnerCode, requestId, amount, orderId, orderInfo, redirectUrl,
            ipnUrl, lang, requestType, autoCapture, extraData, orderGroupId, signature);

        System.out.println("--------------------REQUEST JSON----------------");
        System.out.println(requestJson);
    }

    /**
     * Tao chuoi JSON request body de gui sang endpoint create payment cua MoMo.
     *
     * @param partnerCode ma doi tac MoMo
     * @param requestId ma request duy nhat
     * @param amount so tien thanh toan
     * @param orderId ma don hang duy nhat
     * @param orderInfo noi dung don hang
     * @param redirectUrl url redirect sau thanh toan
     * @param ipnUrl url callback server to server
     * @param lang ngon ngu request
     * @param requestType loai request MoMo
     * @param autoCapture co tu dong capture hay khong
     * @param extraData du lieu mo rong
     * @param orderGroupId ma nhom don hang
     * @param signature chu ky HMAC cua request
     * @return chuoi JSON hoan chinh de POST sang MoMo
     */
    private static String buildRequestJson(String partnerCode, String requestId, String amount, String orderId,
                                           String orderInfo, String redirectUrl, String ipnUrl, String lang,
                                           String requestType, boolean autoCapture, String extraData,
                                           String orderGroupId, String signature) {
        // Step 1: escape cac gia tri string de tranh vo JSON khi co ky tu dac biet.
        // Step 2: ghep thanh request body dung format create payment cua MoMo.
        return """
            {
              "partnerCode": "%s",
              "partnerName": "Test",
              "storeId": "MomoTestStore",
              "requestId": "%s",
              "amount": "%s",
              "orderId": "%s",
              "orderInfo": "%s",
              "redirectUrl": "%s",
              "ipnUrl": "%s",
              "lang": "%s",
              "requestType": "%s",
              "autoCapture": %s,
              "extraData": "%s",
              "orderGroupId": "%s",
              "signature": "%s"
            }
            """.formatted(
            escapeJson(partnerCode),
            escapeJson(requestId),
            escapeJson(amount),
            escapeJson(orderId),
            escapeJson(orderInfo),
            escapeJson(redirectUrl),
            escapeJson(ipnUrl),
            escapeJson(lang),
            escapeJson(requestType),
            autoCapture,
            escapeJson(extraData),
            escapeJson(orderGroupId),
            escapeJson(signature)
        );
    }

    /**
     * Escape ky tu dac biet trong gia tri string truoc khi dua vao JSON.
     *
     * @param value gia tri can escape
     * @return chuoi an toan de noi vao JSON
     */
    private static String escapeJson(String value) {
        // Step 1: tra ve chuoi rong neu dau vao null de tranh loi khi format.
        // Step 2: thay the cac ky tu can escape theo quy tac JSON co ban.
        if (value == null) {
            return "";
        }
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "\\r")
            .replace("\n", "\\n")
            .replace("\t", "\\t");
    }
}
