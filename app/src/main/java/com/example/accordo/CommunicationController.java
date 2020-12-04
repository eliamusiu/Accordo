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
    private final String sid = Model.getInstance().getSid();
    private RequestQueue requestQueue = null;

    public CommunicationController(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    // TODO: mettere assert nelle richieste

    public void register(Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        final String serviceUrl = "register.php";
        final String url = BASE_URL + serviceUrl;
        final JSONObject jsonBody = new JSONObject();

        JsonObjectRequest request = new JsonObjectRequest(url, jsonBody, responseListener, errorListener);
        requestQueue.add(request);
    }

    public void getWall(Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) throws JSONException {
        final String serviceUrl = "getWall.php";
        final String url = BASE_URL + serviceUrl;
        final JSONObject jsonBody = new JSONObject();
        jsonBody.put("sid", sid);

        JsonObjectRequest request = new JsonObjectRequest(url, jsonBody, responseListener, errorListener);
        requestQueue.add(request);
    }

    public void getPosts(String ctitle, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) throws JSONException {
        final String serviceUrl = "getChannel.php";
        final String url = BASE_URL + serviceUrl;
        final JSONObject jsonBody = new JSONObject();
        jsonBody.put("sid", sid);
        jsonBody.put("ctitle", ctitle);

        JsonObjectRequest request = new JsonObjectRequest(url, jsonBody, responseListener, errorListener);
        requestQueue.add(request);
    }

    public void addChannel(String ctitle, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) throws JSONException {
        final String serviceUrl = "addChannel.php";
        final String url = BASE_URL + serviceUrl;
        final JSONObject jsonBody = new JSONObject();
        jsonBody.put("sid", sid);
        jsonBody.put("ctitle", ctitle);

        JsonObjectRequest request = new JsonObjectRequest(url, jsonBody, responseListener, errorListener);
        requestQueue.add(request);
    }

    public void setProfile(String name, String picture, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) throws JSONException {
        final String serviceUrl = "setProfile.php";
        final String url = BASE_URL + serviceUrl;
        final JSONObject jsonBody = new JSONObject();
        jsonBody.put("sid", sid);
        jsonBody.put("name", name);

        JsonObjectRequest request = new JsonObjectRequest(url, jsonBody, responseListener, errorListener);
        requestQueue.add(request);
    }

    public void getProfile(Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) throws JSONException {
        final String serviceUrl = "getProfile.php";
        final String url = BASE_URL + serviceUrl;
        final JSONObject jsonBody = new JSONObject();
        jsonBody.put("sid", sid);

        JsonObjectRequest request = new JsonObjectRequest(url, jsonBody, responseListener, errorListener);
        requestQueue.add(request);
    }

    public void addPost(String ctitle, String content, String type, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) throws JSONException {
        assert type.equals("t") || type.equals("i");
        final String serviceUrl = "addPost.php";
        final String url = BASE_URL + serviceUrl;
        final JSONObject jsonBody = new JSONObject();
        jsonBody.put("sid", sid);
        jsonBody.put("ctitle", ctitle);
        jsonBody.put("type", type);
        jsonBody.put("content", content);


        JsonObjectRequest request = new JsonObjectRequest(url, jsonBody, responseListener, errorListener);
        requestQueue.add(request);
    }

    public void addPost(String ctitle, String lat, String lon, String type, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) throws JSONException {
        assert type.equals("l");
        final String serviceUrl = "addPost.php";
        final String url = BASE_URL + serviceUrl;
        final JSONObject jsonBody = new JSONObject();
        jsonBody.put("sid", sid);
        jsonBody.put("ctitle", ctitle);
        jsonBody.put("lat", lat);
        jsonBody.put("lon", lon);
        jsonBody.put("type", type);

        JsonObjectRequest request = new JsonObjectRequest(url, jsonBody, responseListener, errorListener);
        requestQueue.add(request);
    }

    public void getPostImage(String pid, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) throws JSONException {
        final String serviceUrl = "getPostImage.php";
        final String url = BASE_URL + serviceUrl;
        final JSONObject jsonBody = new JSONObject();
        jsonBody.put("sid", sid);
        jsonBody.put("pid", pid);

        JsonObjectRequest request = new JsonObjectRequest(url, jsonBody, responseListener, errorListener);
        requestQueue.add(request);
    }

    public void getUserPicture(String uid, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) throws JSONException {
        final String serviceUrl = "getUserPicture.php";
        final String url = BASE_URL + serviceUrl;
        final JSONObject jsonBody = new JSONObject();
        jsonBody.put("sid", sid);
        jsonBody.put("uid", uid);

        JsonObjectRequest request = new JsonObjectRequest(url, jsonBody, responseListener, errorListener);
        requestQueue.add(request);
    }
}
