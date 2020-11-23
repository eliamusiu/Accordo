package com.example.accordo;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

public class CommunicationController {
    private static final String BASE_URL = "https://ewserver.di.unimi.it/mobicomp/accordo/";
    private RequestQueue requestQueue = null;

    public CommunicationController(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public void register(Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        final String serviceUrl = "register.php";
        final String url = BASE_URL + serviceUrl;
        final JSONObject jsonBody = new JSONObject();

        JsonObjectRequest request = new JsonObjectRequest(url, jsonBody, responseListener, errorListener);
        requestQueue.add(request);
    }

    public void getWall(String sid, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) throws JSONException {
        final String serviceUrl = "getWall.php";
        final String url = BASE_URL + serviceUrl;
        final JSONObject jsonBody = new JSONObject();
        jsonBody.put("sid", sid);

        JsonObjectRequest request = new JsonObjectRequest(url, jsonBody, responseListener, errorListener);
        requestQueue.add(request);
    }

    public void getPosts(String sid, String ctitle, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) throws JSONException {
        final String serviceUrl = "getChannel.php";
        final String url = BASE_URL + serviceUrl;
        final JSONObject jsonBody = new JSONObject();
        jsonBody.put("sid", sid);
        jsonBody.put("ctitle", ctitle);

        JsonObjectRequest request = new JsonObjectRequest(url, jsonBody, responseListener, errorListener);
        requestQueue.add(request);
    }

    public void addChannel(String sid, String ctitle, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) throws JSONException {
        final String serviceUrl = "addChannel.php";
        final String url = BASE_URL + serviceUrl;
        final JSONObject jsonBody = new JSONObject();
        jsonBody.put("sid", sid);
        jsonBody.put("ctitle", ctitle);

        JsonObjectRequest request = new JsonObjectRequest(url, jsonBody, responseListener, errorListener);
        requestQueue.add(request);
    }
}
