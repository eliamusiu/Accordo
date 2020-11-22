package com.example.accordo;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Model {
    private static Model instance = null;
    private ArrayList<Channel> channels = null;
    private ArrayList<Post> posts = null;   // TODO: istanziare nel costruttore

    private Model() { channels = new ArrayList<Channel>(); }

    public static synchronized Model getInstance() {
        if (instance == null) {
            instance = new Model();
        }
        return instance;
    }

    public void addChannels(JSONObject wallJson) throws JSONException {
        Gson gson = new Gson();
        JSONArray jsonArray = wallJson.getJSONArray("channels");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject channelJson = jsonArray.getJSONObject(i);
            Channel channel = gson.fromJson(String.valueOf(channelJson), Channel.class);
            channels.add(channel);
        }
    }

    public ArrayList<Channel> getAllChannels() {
        return channels;
    }

    public Channel get(int index) {
        return channels.get(index);
    }

    public int getSize() {
        return channels.size();
    }
}
