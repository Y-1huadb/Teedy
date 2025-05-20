package com.sismics.docs.rest.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;

import javax.crypto.Mac;
import java.util.Arrays;

public class TranslateRequestUtil {
    // 翻译开放平台：https://fanyi-api.baidu.com/manage/developer
    /**
     * APP ID
     */
    public static final String APPID = System.getenv("APPID");
    /**
     * 密钥
     */
    public static final String SECRET = System.getenv("SECRET");

    private static final String TRANSLATE_BASE_ROOT = "https://fanyi-api.baidu.com";

    /**
     * 创建报价任务
     */
    public static final String CREATE_QUOTE_JOB = "/transapi/doctrans/createjob/quote";
    /**
     * 查询报价进度
     */
    public static final String QUERY_QUOTE_JOB = "/transapi/doctrans/query/quote";
    /**
     * 创建翻译任务
     */
    public static final String CREATE_TRANS_JOB = "/transapi/doctrans/createjob/trans";
    /**
     * 查询翻译进度
     */
    public static final String QUERY_TRANS_JOB = "/transapi/doctrans/query/trans";

    /**
     * post 请求
     *
     * @param url
     * @param body
     */
    public static String doPost(String url, JSONObject body) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        try {
            // 创建post请求
            HttpPost request = new HttpPost(TRANSLATE_BASE_ROOT + url);
            signAndAddHeaders(body, request);
            StringEntity requestEntity = new StringEntity(body.toString(), StandardCharsets.UTF_8);
            request.setEntity(requestEntity);

            //执行post请求
            HttpResponse httpResponse = httpClient.execute(request);
            //获取响应消息实体
            HttpEntity responseEntity = httpResponse.getEntity();
            //响应状态
            printMsg("status: " + httpResponse.getStatusLine());
            //判断响应实体是否为空
            if (responseEntity != null) {
                String responseData = EntityUtils.toString(responseEntity);
                printMsg("requestQuote data: " + responseData);
                return responseData;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 关闭流并释放资源
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 生成签名并添加到header
     */
    private static void signAndAddHeaders(JSONObject body, HttpPost request) {
        try {
            // Step1: 传入http请求中的body（原始json串，保留原始格式）字符串作为字符串1。
            String str1 = body.toString();
            printMsg("str1: " + str1);

            // Step2. 将 APPID(X-Appid)，时间戳(X-Timestamp)， 字符串1，按照APPID+时间戳+字符串1顺序拼接得到字符串2。
            // 10位时间戳 - 秒
            long timeSecond = System.currentTimeMillis() / 1000;
            String str2 = APPID + timeSecond + str1;
            // printMsg("str2: " + str2);

            // Step3. 对字符串2 做hmac_sha256加密，密钥使用平台分配的密钥(可在管理控制台查看) 得到字符串3。
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(SECRET.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] str3 = sha256_HMAC.doFinal(str2.getBytes());
            printMsg("str3: " + Arrays.toString(str3));

            // Step4. 对字符串3 做base64编码得到X-Sign。
            String sign = Base64.encodeBase64String(str3);
            printMsg("sign: " + sign);
            request.addHeader("Content-Type", "application/json");
            request.addHeader("X-Appid", APPID);
            request.addHeader("X-Sign", sign);
            request.addHeader("X-Timestamp", "" + timeSecond);
        } catch (Exception e) {
            printMsg("signAndAddHeaders Error");
        }
    }

    private static void printMsg(String template, Object... args) {
        System.out.println(String.format(template, args));
    }

    interface Callback {
        /**
         * 成功回调
         *
         * @param response 回调内容
         */
        void success(String response);

        void fail();
    }
}
