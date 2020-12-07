package com.example.accordo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
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
        (new Thread(() -> {
            model.setUsersFromDB();                 // Setta nella lista users del Model gli utenti presenti nel DB
            try {
                getMissingProfilePictures(runnable);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        })).start();
    }


    /**
     * Per ogni utente distinto che ha pubblicato post nel canale controlla se è presente in
     * {@link Model} o se la {@link User#getPversion()} è aggiornata. In caso contrario chiama
     * {@link #getUserPicture(String, Runnable)}
     * @param runnable Callback che verrà chiamata quando l'immagine è stata ricevuta e salvata in
     *                 {@link Model}
     * @throws JSONException
     */
    private void getMissingProfilePictures(Runnable runnable) throws JSONException {
        List<Post> usersInChannel = model.getAllPosts().stream().filter(distinctByKey(Post::getUid)).collect(Collectors.toList());
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(runnable);

        for (Post post : usersInChannel) {       // Scorre i post distinti per uid
            String uid = post.getUid();

            // Se l'utente di questo post non è nel model/DB o se non ha l'immagine di profilo aggiornata
            if (model.getUser(uid) == null || !model.getUser(uid).getPversion().equals(post.getPversion())) {
                getUserPicture(uid,
                        () -> {     // Callback se l'immagine è stata presa dalla rete per aggiornarla/aggiungerla
                            handler.post(runnable);
                            Log.d(TAG, "Ho ricevuto la picture dalla rete");
                        });
            }
        }
    }

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
     * Definisce i metodi di callback da passare a {@link #getUserInfo(String, Response.Listener)}
     * in base a se si tratta di aggiornare l'immagine di un utente già presente in {@link Model}
     * oppure aggiungerne uno nuovo
     * @param uid
     * @param updateUserCallback Callback che viene chiamata una volta che la richiesta di rete è
     *                           stata ricevuta e {@link Model} è stato settato con i valori appena
     *                           ricevuti
     * @throws JSONException
     */
    private void getUserPicture(String uid, Runnable updateUserCallback) throws JSONException {
        final Handler handler = new Handler(Looper.getMainLooper());
        User postAuthor = model.getUser(uid);

        if (postAuthor != null) {                       // Se l'utente è già presente nel DB => aggiorna immagine
            getUserInfo(uid, response -> {                  // Chiamata di rete
                updateUser(response);                           // Setta i dati appena ottenuti nel model che fa l'update nel database
                handler.post(updateUserCallback);               // Chiama la callback per informare che i dati sono stati aggiornati
            });
        } else {                                        // Se l'utente non è già presente nel DB/model => aggiungi immagine
            getUserInfo(uid, response -> {                  // Chiamata di rete
                addUser(response);                              // Setta i dati appena ottenuti nel model che fa la insert
                handler.post(updateUserCallback);               // Chiama la callback per informare che i dati sono stati aggiunti
            });
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

    /**
     * Chiama {@link CommunicationController#getUserPicture(String, Response.Listener, Response.ErrorListener)}
     * passandogli la callback definita in {@link #getUserPicture(String, Runnable)}
     * @param uid Uid del post
     * @param responseCallback Callback chiamata quando la richiesta di rete è andata a buon fine
     * @throws JSONException
     */
    private void getUserInfo(String uid, Response.Listener<JSONObject> responseCallback) throws JSONException {
        CommunicationController cc = new CommunicationController(context);
        cc.getUserPicture(uid, responseCallback,
                error -> Log.e(TAG,"Errore scaricamento immagine dalla rete: " + error.networkResponse)
        );
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
            try {
                getImages(runnable);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        })).start();
    }

    private void getImages(Runnable runnable) throws JSONException {
        CommunicationController cc = new CommunicationController(context);
        ArrayList<TextImagePost> imagePosts = model.getAllImagePosts();
        final Handler handler = new Handler(Looper.getMainLooper());

        handler.post(runnable); //le immagini che sono già nel database vengono caricate immediatamente

        for (TextImagePost post : imagePosts) {
            if (post.getContent() == null) {
                cc.getPostImage(post.getPid(),
                        response -> {
                            try {
                                Log.d(TAG, "scarico immagini dalla rete");
                                model.addImage(post.getPid(), response.getString("content"));
                                handler.post(runnable);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        },
                        error -> Log.d(TAG, "request error: " + error.toString())
                );
            }
        }
    }
    //endregion
}
