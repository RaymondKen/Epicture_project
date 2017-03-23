package com.epicure.chronos.epicure;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;

import static android.R.attr.data;

/**
 * Created by Chronos on 22/02/2017.
 */

public class EpicureAdapter extends AppCompatActivity {

    private static final String TAG = "";
    private static final String client_id = "8f49f691314eeea";
    private static final String client_id2 = "1fe8e98c72d8fe2";
    private static final String client_secret = "3dc8074566c4734ec2777f6f4b387962a091f823";
    private static final String client_secret2 = "2855da00cf1fdac1da8f51599040a28fc736b115";

    private static final String NETWORK_NAME ="Imgur";
    private static final String PROTECTED_RESOURCE_URL = "https://api.imgur.com/3/account/me";
    private static final String REDIRECT_URI = "http://android";

    private String AuthUrl = "";
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    /**private static Retrofit.Builder builder =
            new Retrofit.Builder()
            */


    public void connect() throws IOException {
        AuthUrl = "https://api.imgur.com/oauth2/authorize?client_id=" + client_id2 + "&response_type=pin&state=oob";
    }

    public  String getJSON(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("client_id", client_id2);
        conn.setRequestProperty("client_secret", client_secret2);
        conn.setRequestProperty("grant_type", "pin");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }
    //GET METHODS !!!

    public String getAuthorizationUrl()
    {
        return AuthUrl;
    }

    public String getRedirectUri()
    {
        return REDIRECT_URI;
    }

    public String getClient_id()
    {
        return client_id2;
    }

    public String getClient_secret()
    {
        return client_secret2;
    }

}
