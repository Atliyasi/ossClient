package com.chx.client;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public final class OSSClient {

    private String baseURL = "https://chdclouds.com:1236";
    public HttpClient httpClient;
    public String getBaseURL() {
        return baseURL;
    }
    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public OSSClient() {
        // 创建一个不进行证书验证的 SSLContext
        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom()
                    .loadTrustMaterial((chain, authType) -> true)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }
        // 创建一个忽略主机名验证的 SSLConnectionSocketFactory
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                NoopHostnameVerifier.INSTANCE);
        // 创建 HttpClient，并设置 SSLConnectionSocketFactory
        httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory)
                .build();
    }

    public OSSClient(String baseURL) {
        this.baseURL = baseURL;
        // 创建一个不进行证书验证的 SSLContext
        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom()
                    .loadTrustMaterial((chain, authType) -> true)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }

        // 创建一个忽略主机名验证的 SSLConnectionSocketFactory
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                NoopHostnameVerifier.INSTANCE);

        // 创建 HttpClient，并设置 SSLConnectionSocketFactory
        httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory)
                .build();
    }

    public static OSSClient getClient(String url) {
        return new OSSClient(url);
    }

    public static OSSClient getClient() {
        return new OSSClient();
    }

    /**
     *
     * @param bucketName : bucket name
     * @param userId : user id
     * @param bytes : byte[]
     * @param fileName : file name
     * @return : result (String)
     * @throws Exception :
     */
    public String uploadBytes(String bucketName, String userId, byte[] bytes, String fileName) throws Exception {
        HttpPost post = new HttpPost(this.baseURL + "/oss/upload");
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        builder.addTextBody("bucket", bucketName);
        builder.addTextBody("userId", userId);
        builder.addBinaryBody("file", bytes, ContentType.DEFAULT_BINARY, fileName);

        post.setEntity(builder.build());

        HttpResponse response = httpClient.execute(post);

        return readResponse(response);
    }


    public String uploadFile(String bucketName, String userId, File file) throws Exception {
        if (!file.isFile()) {
            return "文件为空或不存在该路径";
        }
        return uploadBytes(bucketName, userId, convertFileToByteArray(file), file.getName());
    }


    public String uploadFile(String bucketName, String userId, String url) throws Exception {
        File file = new File(url);
        if (!file.isFile()) {
            return "文件为空或不存在该路径";
        }

        return uploadBytes(bucketName, userId, convertFileToByteArray(file), file.getName());
    }
    private String readResponse(HttpResponse response) throws Exception {
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder result = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            return result.toString();
        } else {
            throw new Exception("File upload failed with status code: " + statusCode);
        }
    }


    public HashMap<String, String> parseAndHandleResponse(String responseData) {
        JSONObject jsonResponse = new JSONObject(responseData);
        int code = jsonResponse.getInt("code");
        String message = jsonResponse.getString("message");
        String data = jsonResponse.optString("data", ""); // 使用optString以避免在没有"data"字段时抛出异常
        HashMap<String, String> map = new HashMap<>();
        map.put("code", String.valueOf(code));
        map.put("message", message);
        map.put("data", data);
        return map;
    }

    public static byte[] convertFileToByteArray(File file) throws IOException, FileNotFoundException {
        try (FileInputStream fis = new FileInputStream(file); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toByteArray();
        }
    }
}
