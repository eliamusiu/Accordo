package com.example.accordo;

import android.content.Context;
import android.os.Handler;

import androidx.room.Room;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Model {
    private static Model instance = null;
    private static AppDatabase db = null;
    private ArrayList<Channel> channels;
    private ArrayList<Post> posts;
    private List<User> users;
    private String sid = null;
    private User actualUser = null;
    private Context context;

    /**
     * Costruttore che istanzia le liste e costruisce il database
     * @param context
     */
    private Model(Context context) {
        channels = new ArrayList<>();
        posts = new ArrayList<>();
        users = new ArrayList<>();
        this.context = context;
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
            Channel channel = gson.fromJson(channelJson.toString(), Channel.class);
            channels.add(channel);
        }
        getData();
    }

    /**
     * Aggiunge gli indici (lettera dell'alfabeto per raggruppare i canali con la stessa iniziale).
     * Oltre alle lettere sono un indice i canali "miei" (dell'utente loggato) e i caratteri
     * speciali, inclusi i numeri ("#")
     */
    private void getData() {
        ArrayList<Channel> newList = new ArrayList<>();
        String chFirstLetter = null, nextChFirstLetter = null;
        boolean isMine = false;

        for (int i = 0; i < channels.size(); i++) {
            if (i == 0) { // Se il primo canale e mio, setto l'indice che poi avrà l'icona della stella
                if (channels.get(i).getMine().equals("t")) {
                    Channel ch = new Channel();
                    ch.setIndex(Channel.MY_CHANNEL_INDEX);
                    newList.add(ch);
                    isMine = true;
                }
            } // Se il canale non è "mio"
            if (channels.get(i).getMine().equals("f")) {
                if (isMine) {                       // Aggiunge indice per i canali con primo char non lettera
                    isMine = false;
                    Channel ch = new Channel();
                    ch.setIndex(Channel.NO_LETTER_INDEX);
                    newList.add(ch);
                }
                if (!channels.get(i).getCtitle().equals("")) {      // Per evitare nullPointer del canale vuoto
                    chFirstLetter = Character.toString(channels.get(i).getCtitle().charAt(0)).toUpperCase();
                    try {
                        nextChFirstLetter = Character.toString(channels.get(i + 1).getCtitle().charAt(0)).toUpperCase();
                    } catch (IndexOutOfBoundsException e) {
                        nextChFirstLetter = Channel.NO_LETTER_INDEX;
                    }
                }
            }
            newList.add(channels.get(i));       // Aggiunge il canale alla lista
            if (chFirstLetter != null && !chFirstLetter.equalsIgnoreCase(nextChFirstLetter)
                    && Character.isLetter(nextChFirstLetter.charAt(0)) && !channels.get(i).getCtitle().equals("")
                    && i != channels.size() - 1) {
                    Channel ch = new Channel();
                    ch.setIndex(nextChFirstLetter);
                    newList.add(ch);            // Aggiunge la lettera alla lista
            }
        }
        channels = newList;
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
            if (type.equals(Post.TEXT) || type.equals(Post.IMAGE)) {
                TextImagePost post = gson.fromJson(postJson.toString(), TextImagePost.class);
                posts.add(post);
            } else if (type.equals(Post.LOCATION)) {
                LocationPost locationPost = gson.fromJson(postJson.toString(), LocationPost.class);
                posts.add(locationPost);
            }
        }
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getSid() {
        return sid;
    }

    /**
     * Ritorna tutti i post del canale attuale
     * @return Tutti i post del canale attuale
     */
    public ArrayList<Post> getAllPosts() {
        return new ArrayList<>(posts);
    }

    /**
     * Ritorna tutti i post di tipo immagine del canale attuale
     * @return Tutti i post di tipo immagine del canale attuale
     */
    public ArrayList<TextImagePost> getAllImagePosts() {
        ArrayList<TextImagePost> textImagePosts = new ArrayList<>();
        for (Post post : posts) {
            if (post.getType().equals(Post.IMAGE)) {
                textImagePosts.add((TextImagePost) post);
            }
        }
        return textImagePosts;
    }

    /**
     * Ritorna l'utente specificato
     * @param uid Utente che si vuole ottenere
     * @return Utente con l'uid passato
     */
    public User getUser(String uid) {
        return new ArrayList<>(users).stream()
                .filter(user -> uid.equals(user.getUid()))
                .findAny()
                .orElse(null);
    }

    /**
     * Ritorna il canale alla posizione specificata
     * @param index
     * @return
     */
    public Channel getChannel(int index) {
        return new ArrayList<>(channels).get(index);
    }

    /**
     * Ritorna il post alla posizione specificata
     * @param index
     * @return
     */
    public Post getPost(int index) {
        return getAllPosts().get(index);
    }

    /**
     * Ritorna il post con il PID specificato
     * @param pid
     * @return il post con il PID specificato
     */
    public Post getPost(String pid) {
        return new ArrayList<>(posts).stream()
                .filter(post -> pid.equals(post.getPid()))
                .findAny()
                .orElse(null);
    }

    public int getPostsSize() {
        return posts.size();
    }

    public int getChannelsSize() {
        return channels.size();
    }

    public User getActualUser() {
        return actualUser;
    }

    public void setActualUser(User actualUser) {
        this.actualUser = actualUser;
    }


    //region Database immagini di profilo
    /**
     * Prende gli utenti nel database e li salva in {@link #users}
     */
    public void setUsersFromDB() {
        users = db.userDao().getAllUsers();
        convertProfilePicToBitmap();
    }

    /**
     * Converte le immagini profilo in {@link android.graphics.Bitmap} usando {@link Utils#getBitmapFromBase64(String, Context)}
     * in modo da non farlo fare ogni volta in {@link PostViewHolder}, altrimenti la RecyclerView laggerebbe
     */
    private void convertProfilePicToBitmap() {
        for (User user : users) {
            user.setBitmapPicture(Utils.getBitmapFromBase64(user.getPicture(), context));
        }
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
        getUser(uid).setBitmapPicture(Utils.getBitmapFromBase64(picture, context));
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
        user.setBitmapPicture(Utils.getBitmapFromBase64(picture, context));
        (new Thread(() -> db.userDao().insertUser(user))).start();
        users.add(user);
    }
    //endregion

    //region Database immagini dei post
     /**
     * Prende le immagini dei post dal database e le setta ai post immagine del canale
     */
    public void setImagesFromDB() {
        List<TextImagePost> images = db.imagePostDao().getAllImages();
        ArrayList<TextImagePost> imagePosts = getAllImagePosts();
        for (TextImagePost imagePost : images) {
            imagePosts.stream().filter(post -> post.getPid().equals(imagePost.getPid()))
            .forEach(post -> post.setContent(imagePost.getContent()));
        }
    }

    public void addImage(String pid, String content) {
        TextImagePost imagePost = new TextImagePost();
        imagePost.setPid(pid);
        imagePost.setContent(content);
        (new Thread(() -> db.imagePostDao().insertImage(imagePost))).start();
        ((TextImagePost) getPost(pid)).setContent(content);
    }
    //endregion
}
