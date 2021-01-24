package com.example.accordo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PictureController {
    private static final String TAG = PictureController.class.toString();
    private Context context;
    private Model model;
    private Runnable updateRecyclerViewRunnable;
    private Runnable updateImagesRunnable;

    public PictureController(Context context) {
        this.context = context;
        model = Model.getInstance(context);
    }

    //region Gestione immagini di profilo
    /**
     * Prende l'istanza di {@link Model} e chiama {@link Model#setUsersFromDB()}
     * @param runnable Callback che verrà chiamata ad ogni immagine ricevuta
     */
    public void setProfilePictures(Runnable runnable) {
        final Handler handler = new Handler(Looper.getMainLooper());
        (new Thread(() -> {
            model.setUsersFromDB();                 // Setta nella lista users del Model gli utenti presenti nel DB
            updateRecyclerViewRunnable = runnable;
            handler.post(updateRecyclerViewRunnable);       // Setta la recyclerView con le immagini del DB
            try {
                checkMissingProfilePictures();      // Prende le immagini mancanti o non aggiornate
            } catch (JSONException e) {
                e.printStackTrace();
            }
        })).start();
    }


    /**
     * Per ogni utente distinto che ha pubblicato post nel canale controlla se è presente in
     * {@link Model} o se la {@link User#getPversion()} è aggiornata e aggiunge questi utenti ad
     * una lista e richiama infine {@link #getUserPicture(ArrayList)}
     * @throws JSONException
     */
    private void checkMissingProfilePictures() throws JSONException {
        List<Post> usersInChannel = model.getAllPosts().stream().filter(distinctByKey(Post::getUid)).collect(Collectors.toList());
        ArrayList<String> profilePicToRequest = new ArrayList<>();

        for (Post post : usersInChannel) {       // Scorre i post distinti per uid
            String uid = post.getUid();
            // Se l'utente di questo post non è nel model/DB o se non ha l'immagine di profilo aggiornata
            if (model.getUser(uid) == null || !model.getUser(uid).getPversion().equals(post.getPversion())) {
                profilePicToRequest.add(uid);
            }
        }
        getUserPicture(profilePicToRequest);
    }

    /**
     * Viene chiamato una volta ricevuta l'ultima immagine di profilo dalla rete, tra quelle
     * mancanti. A sua volta chiama il runnable che aggiorna la recyclerView in {@link ChannelActivity}
     */
    Runnable pictureReceivedCallback = new Runnable() {
        @Override
        public void run() {
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.post(updateRecyclerViewRunnable);
            Log.d(TAG, "Ho ricevuto la picture dalla rete");
        }
    };

    /**
     * Toglie le ripetizioni in una lista in base a un attributo
     * @param keyExtractor
     * @param <T>
     * @return
     */
    private static <T> Predicate<T> distinctByKey(Function<? super T,Object> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * Scorre tutte gli uid dei profili di cui richiedere l'immagine e per ognuno effettua la
     * chiamata di rete per ottenere l'immagine e come callback aggiorna o aggiunge l'immagine.
     * Per l'ultimo uid, richiama {@link #pictureReceivedCallback}
     * @param profilePicToRequest Lista di uid degli utenti con immagine assente o non aggiornata
     * @throws JSONException
     */
    private void getUserPicture(ArrayList<String> profilePicToRequest) throws JSONException {
        CommunicationController cc = new CommunicationController(context);
        final Handler handler = new Handler(Looper.getMainLooper());

        for (String uid : profilePicToRequest) {
            User postAuthor = model.getUser(uid);
            cc.getUserPicture(uid, response -> {                  // Chiamata di rete
                        if (postAuthor != null) {                       // Se l'utente è già presente nel DB => aggiorna immagine
                            updateUser(response);                           // Setta i dati appena ottenuti nel model che fa l'update nel database
                        } else {                                        // Se l'utente non è già presente nel DB/model => aggiungi immagine
                            addUser(response);                              // Setta i dati appena ottenuti nel model che fa la insert
                        }
                        if (profilePicToRequest.indexOf(uid) == profilePicToRequest.size() - 1) {   // Se è l'ultima immagine ricevuta
                            handler.post(pictureReceivedCallback);
                        }
                    },
                    error -> Log.e(TAG,"Errore scaricamento immagine dalla rete: " + error.networkResponse));
        }
    }

    /**
     * Chiama {@link Model#updateUser(String, String, String)}
     * @param userJson
     */
    private void updateUser(JSONObject userJson) {
        try {
            model.updateUser(userJson.get("uid").toString(),
                    userJson.get("pversion").toString(),
                    userJson.get("picture").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Chiama {@link Model#addUser(String, String, String)}
     * @param userJson
     */
    private void addUser(JSONObject userJson) {
        try {
            model.addUser(userJson.get("uid").toString(),
                    userJson.get("pversion").toString(),
                    userJson.get("picture").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //endregion

    //region Gestione immagini dei post
    /**
     * Prende l'istanza di {@link Model} e chiama {@link Model#setImagesFromDB()} ()}
     * @param runnable Callback che verrà chiamata ad ogni immagine ricevuta
     */
    public void setPostImages(Runnable runnable) {
        (new Thread(() -> {
            model.setImagesFromDB();  // Setta nella lista posts del Model le immagini presenti nel DB
            updateImagesRunnable = runnable;
            try {
                checkMissingImages();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        })).start();
    }

    /**
     * Aggiunge ad una lista tutte le immagini che non sono già presenti nel DB/model e richiama
     * {@link #getImages(ArrayList)} passandogli la lista
     * @throws JSONException
     */
    private void checkMissingImages() throws JSONException {
        ArrayList<TextImagePost> imagePosts = model.getAllImagePosts();
        ArrayList<String> imagesToRequest = new ArrayList<>();
        final Handler handler = new Handler(Looper.getMainLooper());

        handler.post(updateImagesRunnable); // Le immagini che sono già nel database vengono caricate immediatamente

        for (TextImagePost post : imagePosts) {
            if (post.getContent() == null) {
                imagesToRequest.add(post.getPid());
            }
        }
        getImages(imagesToRequest);
    }

    /**
     * Per ogni immagine da richiedere fa la richiesta di rete nella cui response aggiunge a
     * {@link Model#addImage(String, String)} l'immagine e, nel caso dell'ultima da richiedere,
     * chiama {@link #updateImagesRunnable}
     * @param imagesToRequest
     * @throws JSONException
     */
    private void getImages(ArrayList<String> imagesToRequest) throws JSONException {
        CommunicationController cc = new CommunicationController(context);
        final Handler handler = new Handler(Looper.getMainLooper());

        for (String pid : imagesToRequest) {
            cc.getPostImage(pid,
                    response -> {
                        try {
                            Log.d(TAG, "Scarico immagini dalla rete");
                            model.addImage(pid, response.getString("content"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (imagesToRequest.indexOf(pid) == imagesToRequest.size() - 1) {
                            handler.post(updateImagesRunnable);
                        }
                    },
                    error -> Log.d(TAG, "request error: " + error.toString())
            );

        }
    }
    //endregion
}
