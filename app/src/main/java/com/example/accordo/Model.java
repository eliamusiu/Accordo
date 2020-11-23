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

    private Model() {
        channels = new ArrayList<Channel>();
        posts = new ArrayList<Post>();
    }

    public static synchronized Model getInstance() {
        if (instance == null) {
            instance = new Model();
        }
        return instance;
    }

    public void addChannels(JSONObject wallJson) throws JSONException {
        channels.clear();
        Gson gson = new Gson();
        JSONArray jsonArray = wallJson.getJSONArray("channels");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject channelJson = jsonArray.getJSONObject(i);
            Channel channel = gson.fromJson(String.valueOf(channelJson), Channel.class);
            channels.add(channel);
        }
    }

    public void addPosts(JSONObject channelJson) throws JSONException {
        posts.clear();
        Gson gson = new Gson();
        JSONArray jsonArray = channelJson.getJSONArray("posts");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject postJson = jsonArray.getJSONObject(i);
            String type = (String) postJson.get("type");
            if (type.equals("t") || type.equals("i")) {
                TextImagePost post = gson.fromJson(String.valueOf(postJson), TextImagePost.class);
                posts.add(post);
            } else if (type.equals("l")) {
                LocationPost locationPost = gson.fromJson(String.valueOf(postJson), LocationPost.class);
                posts.add(locationPost);
            }
        }
    }

    public ArrayList<Channel> getAllChannels() {
        return channels;
    }

    public ArrayList<Post> getAllPosts() {
        return posts;
    }

    public Channel getChannel(int index) {
        return channels.get(index);
    }

    public Post getPost(int index) {
        return posts.get(index);
    }
}
