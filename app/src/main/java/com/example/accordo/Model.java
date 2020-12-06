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

    /**
     * Costruttore che istanzia le liste e costruisce il database
     * @param context
     */
    private Model(Context context) {
        channels = new ArrayList<>();
        posts = new ArrayList<>();
        users = new ArrayList<>();
        db = Room.databaseBuilder(context,
                AppDatabase.class, "Users").build();
    }

    /**
     * Ritorna l'istanza di {@link Model} che è un Singleton
     * @param context
     * @return
     */
    public static synchronized Model getInstance(Context context) {
        if (instance == null) {
            instance = new Model(context);
        }
        return instance;
    }

    /**
     * Trasforma il JSON in un oggetto di tipo {@link Channel} e lo aggiunge a {@link #channels}
     * @param wallJson Json contenente i canali
     * @throws JSONException
     */
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

    /**
     * Trasforma il JSON in un oggetto di tipo {@link Post} e lo aggiunge a {@link #posts}
     * @param channelJson Json contenente i post
     * @throws JSONException
     */
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

    // TODO: Togliere se non lo useremo mai
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

    // TODO: togliere?
    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSid() {
        return sid;
    }

    // TODO: togliere?
    public  String getUid() {
        return uid;
    }

    // TODO: togliere?
    public ArrayList<Channel> getAllChannels() {
        return channels;
    }

    /**
     * Ritorna tutti i post del canale attuale
     * @return Tutti i post del canale attuale
     */
    public ArrayList<Post> getAllPosts() {
        return posts;
    }

    /**
     * Ritorna tutti i post di tipo immagine del canale attuale
     * @return Tutti i post di tipo immagine del canale attuale
     */
    public ArrayList<TextImagePost> getAllImagePosts() {
        ArrayList<TextImagePost> textImagePosts = new ArrayList<>();
        for (Post post : posts) {
            if (post.getType().equals("i")) {
                textImagePosts.add((TextImagePost) post);
            }
        }
        return textImagePosts;
    }

    /**
     * Prende gli utenti nel database e li salva in {@link #users}
     */
    public void setUsersFromDB() {
        users = db.userDao().getAllUsers();
    }

    /**
     * Ritorna l'utente specificato
     * @param uid Utente che si vuole ottenere
     * @return Utente con l'uid passato
     */
    public User getUser(String uid) {
        return users.stream()
                .filter(user -> uid.equals(user.getUid()))
                .findAny()
                .orElse(null);
    }

    /**
     * Aggiorna l'utente nel database con i parametri specificati
     * @param uid
     * @param pversion
     * @param picture Immagine codificata in base64
     */
    public void updateUser(String uid, String pversion, String picture) {
        (new Thread(() -> db.userDao().updateUser(uid, pversion, picture))).start();
        getUser(uid).setPversion(pversion);
        getUser(uid).setPicture(picture);
    }

    /**
     * Istanzia un {@link User} con i parametri specificati e lo aggiunge al database
     * @param uid
     * @param pversion
     * @param picture Immagine codificata in base64
     */
    public void addUser(String uid, String pversion, String picture) {
        User user = new User();
        user.setUid(uid);
        user.setPversion(pversion);
        user.setPicture(picture);
        (new Thread(() -> db.userDao().insertUser(user))).start();
        users.add(user);
    }

    /**
     * Ritorna il canale alla posizione specificata
     * @param index
     * @return
     */
    public Channel getChannel(int index) {
        return channels.get(index);
    }

    /**
     * Ritorna il post alla posizione specificata
     * @param index
     * @return
     */
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
