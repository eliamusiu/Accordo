package com.example.accordo;

import android.content.Context;
import android.os.Handler;

import androidx.room.Room;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Model {
    private static Model instance = null;
    private static AppDatabase db = null;
    private ArrayList<Channel> channels = null;
    private ArrayList<Post> posts = null;
    private List<User> users = null;
    private String sid = null;
    private String uid = null;

    private Model(Context context) {
        channels = new ArrayList<>();
        posts = new ArrayList<>();
        users = new ArrayList<>();
        db = Room.databaseBuilder(context,
                AppDatabase.class, "Users").build();
    }

    public static synchronized Model getInstance(Context context) {
        if (instance == null) {
            instance = new Model(context);
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

    public void addPost(String uid, String name, String pversion, String pid, String type, String content) {
        TextImagePost post = new TextImagePost();
        post.setUid(uid);
        post.setName(name);
        post.setPversion(pversion);
        post.setType(type);
        post.setPid(pid);
        post.setContent(content);
        posts.add(post);
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSid() {
        return sid;
    }

    public  String getUid() {
        return uid;
    }

    public ArrayList<Channel> getAllChannels() {
        return channels;
    }

    public ArrayList<Post> getAllPosts() {
        return posts;
    }

    public ArrayList<TextImagePost> getAllImagePosts() {
        ArrayList<TextImagePost> textImagePosts = new ArrayList<>();
        for (Post post : posts) {
            if (post.getType().equals("i")) {
                textImagePosts.add((TextImagePost) post);
            }
        }
        return textImagePosts;
    }

    public void setUsersFromDB() {
        users = db.userDao().getAllUsers();
    }

    public User getUser(String uid) {
        return users.stream()
                .filter(user -> uid.equals(user.getUid()))
                .findAny()
                .orElse(null);
    }

    public void updateUser(String uid, String pversion, String picture) {
        (new Thread(new Runnable(){
            public void run() {
                db.userDao().updateUser(uid, pversion, picture);
                //setUsersFromDB();
            }
        })).start();
        getUser(uid).setPversion(pversion);
        getUser(uid).setPicture(picture);
    }

    public void addUser(String uid, String pversion, String picture) {
        final Handler handler = new Handler();
        User user = new User();
        user.setUid(uid);
        user.setPversion(pversion);
        user.setPicture(picture);
        (new Thread(new Runnable(){
            public void run() {
                db.userDao().insertUser(user);
                //setUsersFromDB();
            }
        })).start();
        users.add(user);
    }

    public Channel getChannel(int index) {
        return channels.get(index);
    }

    public Post getPost(int index) {
        return posts.get(index);
    }

    public int getPostsSize() {
        return posts.size();
    }

    public int getChannelsSize() {
        return channels.size();
    }
}
