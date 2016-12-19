package com.rokid.mydownloader.net;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by wn_dev on 2016/12/17.
 */

public class HttpUtils  {
    private static HttpUtils instance;
    private final OkHttpClient mOkHttpClient;

    public HttpUtils() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(10, TimeUnit.SECONDS);
        mOkHttpClient = builder.build();
    }

    public static HttpUtils getInstance() {
        if (instance == null) {
            synchronized (HttpUtils.class) {
                if (instance == null) {
                    instance = new HttpUtils();
                }
            }
        }
        return instance;
    }

    //the sync interface for request range bytes
    public Response sendHeaderRequest(long start, long end, String url){
        Response response=null;
        Headers.Builder headersBuilder = new Headers.Builder();
        headersBuilder.add("Range","bytes=" + start+ "-" + end);
        Headers headers = headersBuilder.build();
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.headers(headers).url(url);
        Request request = requestBuilder.build();
        try {
             response= mOkHttpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public Response sendGetRequest(String url){
        Response response=null;
        Request.Builder requestBuilder = new Request.Builder();
        Request request = requestBuilder.url(url).build();
        try {
            response = mOkHttpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }


}
