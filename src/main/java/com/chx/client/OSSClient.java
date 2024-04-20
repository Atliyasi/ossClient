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

/**
 * HTTP 客户端，用于上传文件到 OSS 服务器。
 *
 * <p>
 * 这个客户端提供了一些方法，用于向 OSS 服务器上传文件。
 * </p>
 *
 * @author Atliyasi
 * @version 1.0
 */
public final class OSSClient {

    private String baseURL = "https://chdclouds.com:1236";
    public HttpClient httpClient;

    /**
     * 获取基本 URL。
     *
     * @return 基本 URL 字符串
     */
    public String getBaseURL() {
        return baseURL;
    }

    /**
     * 设置基本 URL。
     *
     * @param baseURL 基本 URL 字符串
     */
    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    /**
     * 构造方法，创建一个 OSSClient 实例。
     * 默认使用基本 URL "https://chdclouds.com:1236"。
     */
    public OSSClient() {
        // 创建一个不进行证书验证的 SSLContext
        SSLContext sslContext = createTrustAllSSLContext();

        // 创建一个忽略主机名验证的 SSLConnectionSocketFactory
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                NoopHostnameVerifier.INSTANCE);

        // 创建 HttpClient，并设置 SSLConnectionSocketFactory
        httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory)
                .build();
    }

    /**
     * 构造方法，创建一个 OSSClient 实例。
     *
     * @param baseURL 基本 URL 字符串
     */
    public OSSClient(String baseURL) {
        this.baseURL = baseURL;

        // 创建一个不进行证书验证的 SSLContext
        SSLContext sslContext = createTrustAllSSLContext();

        // 创建一个忽略主机名验证的 SSLConnectionSocketFactory
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                NoopHostnameVerifier.INSTANCE);

        // 创建 HttpClient，并设置 SSLConnectionSocketFactory
        httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory)
                .build();
    }

    /**
     * 创建一个不进行证书验证的 SSLContext。
     *
     * @return SSLContext 对象
     */
    private SSLContext createTrustAllSSLContext() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom()
                    .loadTrustMaterial((chain, authType) -> true)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }
        return sslContext;
    }

    /**
     * 获取一个指定基本 URL 的 OSSClient 实例。
     *
     * @param url 基本 URL 字符串
     * @return OSSClient 实例
     */
    public static OSSClient getClient(String url) {
        return new OSSClient(url);
    }

    /**
     * 获取一个使用默认基本 URL 的 OSSClient 实例。
     *
     * @return OSSClient 实例
     */
    public static OSSClient getClient() {
        return new OSSClient();
    }

    /**
     * 将文件以字节数组的形式上传到 OSS 服务器。
     *
     * @param bucketName OSS 存储桶名称
     * @param userId     用户 ID
     * @param bytes      文件的字节数组
     * @param fileName   文件名
     * @return 上传结果字符串
     * @throws Exception 可能抛出的异常
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

    /**
     * 将文件上传到 OSS 服务器。
     *
     * @param bucketName OSS 存储桶名称
     * @param userId     用户 ID
     * @param file       要上传的文件
     * @return 上传结果字符串
     * @throws Exception 可能抛出的异常
     */
    public String uploadFile(String bucketName, String userId, File file) throws Exception {
        if (!file.isFile()) {
            return "文件为空或不存在该路径";
        }
        return uploadBytes(bucketName, userId, convertFileToByteArray(file), file.getName());
    }

    /**
     * 将指定路径的文件上传到 OSS 服务器。
     *
     * @param bucketName OSS 存储桶名称
     * @param userId     用户 ID
     * @param url        文件路径
     * @return 上传结果字符串
     * @throws Exception 可能抛出的异常
     */
    public String uploadFile(String bucketName, String userId, String url) throws Exception {
        File file = new File(url);
        if (!file.isFile()) {
            return "文件为空或不存在该路径";
        }

        return uploadBytes(bucketName, userId, convertFileToByteArray(file), file.getName());
    }

    /**
     * 读取 HTTP 响应的内容。
     *
     * @param response HTTP 响应对象
     * @return 响应内容字符串
     * @throws Exception 可能抛出的异常
     */
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

    /**
     * 解析并处理服务器响应的 JSON 数据。
     *
     * @param responseData 服务器响应的 JSON 数据字符串
     * @return 包含解析结果的 HashMap 对象
     */
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

    /**
     * 将文件转换为字节数组。
     *
     * @param file 要转换的文件
     * @return 文件的字节数组
     * @throws IOException 可能抛出的异常
     */
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
