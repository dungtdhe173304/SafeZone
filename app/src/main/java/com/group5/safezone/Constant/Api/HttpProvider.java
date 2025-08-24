package com.group5.safezone.Constant.Api;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.TlsVersion;

public class HttpProvider {
    public static JSONObject sendPost(String URL, RequestBody formBody) {
        JSONObject data = new JSONObject();
        try {
            ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .cipherSuites(
                            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                            CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                    .build();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectionSpecs(Collections.singletonList(spec))
                    .callTimeout(60000, TimeUnit.MILLISECONDS) // Tăng timeout lên 60 giây
                    .connectTimeout(30000, TimeUnit.MILLISECONDS) // Timeout kết nối 30 giây
                    .readTimeout(30000, TimeUnit.MILLISECONDS)   // Timeout đọc response 30 giây
                    .retryOnConnectionFailure(true) // Tự động retry khi mất kết nối
                    .build();

            Request request = new Request.Builder()
                    .url(URL)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .post(formBody)
                    .build();

            Log.d("HttpProvider", "Sending POST request to: " + URL);
            Log.d("HttpProvider", "Timeout settings - Call: 60s, Connect: 30s, Read: 30s");
            
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                Log.e("HttpProvider", "HTTP Error: " + response.code() + " - " + errorBody);
                data = null;
            } else {
                String responseBody = response.body().string();
                Log.d("HttpProvider", "Response body: " + responseBody);
                data = new JSONObject(responseBody);
            }

        }  catch (IOException e) {
            Log.e("HttpProvider", "IO Exception: " + e.getMessage(), e);
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e("HttpProvider", "JSON Exception: " + e.getMessage(), e);
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("HttpProvider", "Unexpected Exception: " + e.getMessage(), e);
            e.printStackTrace();
        }

        return data;
    }
}
