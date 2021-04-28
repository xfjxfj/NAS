package com.viegre.nas.pad.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/8/16.
 */
public class OkHttpHelper {
    private static OkHttpHelper mOkHttpHelperInstance;
    private static OkHttpClient okHttpClient;

    private OkHttpHelper() {
        okHttpClient = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS)
                                                 .writeTimeout(5, TimeUnit.SECONDS)
                                                 .readTimeout(5, TimeUnit.SECONDS)
                                                 .proxy(Proxy.NO_PROXY)
                                                 .connectionPool(new ConnectionPool(8, 3, TimeUnit.MINUTES))
                                                 .build();
    }

    public static OkHttpHelper getInstance() {
        synchronized (OkHttpHelper.class) {
            if (mOkHttpHelperInstance == null) {
                mOkHttpHelperInstance = new OkHttpHelper();
            }
        }
        return mOkHttpHelperInstance;
    }

    public void doRequest(final Request request, final HttpCallback callback) {
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(request, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String resultStr = response.body().string();
                    callback.onSuccess(response, resultStr);
                } else {
                    callback.onError(response, response.code(), null);
                }
            }
        });
    }

    public void get(String url, Map<String, Object> params, HttpCallback callback) {
        Request request = buildRequest(url, params, HttpMethodType.GET);
        doRequest(request, callback);
    }

    public void post(String url, Map<String, Object> params, HttpCallback callback) {
        Request request = buildRequest(url, params, HttpMethodType.POST);
        doRequest(request, callback);
    }

    public Response doRequest(final Request request) throws IOException {
        return okHttpClient.newCall(request).execute();
    }

    public Response get(String url, Map<String, Object> params) throws IOException {
        Request request = buildRequest(url, params, HttpMethodType.GET);
        return doRequest(request);
    }

    public Response post(String url, Map<String, Object> params) throws IOException {
        Request request = buildRequest(url, params, HttpMethodType.POST);
        return doRequest(request);
    }

    public Response post(String url, Map<String, String> paramms, String charset) throws IOException {
        StringBuffer sb = new StringBuffer();
        //设置表单参数
        for (String key : paramms.keySet()) {
            sb.append(key + "=" + paramms.get(key) + "&");
        }

        Request.Builder builder = new Request.Builder();
        builder.url(url);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=" + charset), sb.toString());
        builder.post(requestBody);
        return doRequest(builder.build());
    }

    public Response post(String url, String json) throws IOException {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.addHeader("Content-Type", "application/json");
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        builder.post(requestBody);
        return doRequest(builder.build());
    }

    private Request buildRequest(String url, Map<String, Object> params, HttpMethodType methodType) {
        Request.Builder builder = new Request.Builder();
        if (methodType == HttpMethodType.GET) {
            url = buildQueryString(url, params);
            builder.url(url);
            builder.get();
        } else if (methodType == HttpMethodType.POST) {
            builder.url(url);
            RequestBody body = buildFormatData(params);
            builder.post(body);
        }
        return builder.build();
    }

    private String buildQueryString(String url, Map<String, Object> paramsMap) {
        if (paramsMap == null || paramsMap.size() == 0) { return url; }
        StringBuilder tempParams = new StringBuilder();

        if (!url.contains("?")) {
            url += "?";
        }

        if ((!url.endsWith("?")) && (!url.endsWith("&"))) {
            url += "&";
        }

        int pos = 0;
        for (String key : paramsMap.keySet()) {
            if (pos > 0) {
                tempParams.append("&");
            }
            //对参数进行URLEncoder
            try {
                tempParams.append(String.format("%s=%s", key, URLEncoder.encode((String) paramsMap.get(key), "utf-8")));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            pos++;
        }

        url += tempParams.toString();
        return url;
    }

    private RequestBody buildFormatData(Map<String, Object> params) {
        FormBody.Builder builder = new FormBody.Builder();
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue().toString());
            }
        }
        return builder.build();
    }

    enum HttpMethodType {
        GET,
        POST
    }
}
