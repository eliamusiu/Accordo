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
    private final String sid;
    private RequestQueue requestQueue = null;
    public static final int MAX_IMAGE_LENGTH = 137000, MAX_TEXT_LENGTH = 100;

    public CommunicationController(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        sid = Model.getInstance(context).getSid();
    }

    // TODO: mettere assert nelle richieste

    /**
     * Richiesta di rete che ritorna il numero di sessione. Viene creato un nuovo utente senza nome
     * e senza immagine di profilo
     * @param responseListener Callback chiamata quando la richiesta di rete va a buon fine
     * @param errorListener Callback chiamata quando la richiesta di rete ha riscontrato un errore
     */
    public void register(Response.Listener<JSONObject> responseListener,
                         Response.ErrorListener errorListener) {
        final String serviceUrl = "register.php";
        final String url = BASE_URL + serviceUrl;
        final JSONObject jsonBody = new JSONObject();

        JsonObjectRequest request = new JsonObjectRequest(url, jsonBody, responseListener,errorListener);
        requestQueue.add(request);
    }

    /**
     * Richiesta di rete che ritorna un array con un oggetto per ogni canale e indica se quel canale
     * è stato creato dall’utente che invia la richiesta. Vengono restituiti prima i canali creati
     * dall’utente. In ogni caso i canali sono ordinati in ordine alfabetico
     * @param responseListener Callback chiamata quando la richiesta di rete va a buon fine.
     *                         Contiene la bacheca
     * @param errorListener Callback chiamata quando la richiesta di rete ha riscontrato un errore
     */
    public void getWall(Response.Listener<JSONObject> responseListener,
                        Response.ErrorListener errorListener) throws JSONException {
        final String serviceUrl = "getWall.php";
        final String url = BASE_URL + serviceUrl;
        final JSONObject jsonBody = new JSONObject();
        jsonBody.put("sid", sid);

        JsonObjectRequest request = new JsonObjectRequest(url, jsonBody, responseListener, errorListener);
        requestQueue.add(request);
    }

    /**
     * Richiesta di rete che ritorna un array con l’elenco di post, ordinati dal più recente al più
     * vecchio. Per ogni post vengono anche passati i dati relativi all’utente che lo ha pubblicato
     * (uid, nome e versione dell’immagine). Per i post di tipo immagine, l’immagine
     * stessa non viene ritornata, ma deve essere richiesta altra chiamata
     * @param ctitle Nome del quale si voglio ottenere i post
     * @param responseListener Callback chiamata quando la richiesta di rete va a buon fine.
     *                         Contiene i post
     * @param errorListener Callback chiamata quando la richiesta di rete ha riscontrato un errore
     * @throws JSONException
     */
    public void getChannel(String ctitle, Response.Listener<JSONObject> responseListener,
                           Response.ErrorListener errorListener) throws JSONException {
        final String serviceUrl = "getChannel.php";
        final String url = BASE_URL + serviceUrl;
        final JSONObject jsonBody = new JSONObject();
        jsonBody.put("sid", sid);
        jsonBody.put("ctitle", ctitle);

        JsonObjectRequest request = new JsonObjectRequest(url, jsonBody, responseListener, errorListener);
        requestQueue.add(request);
    }

    /**
     * Richiesta di rete che inserisce un nuovo canale con il titolo indicato (“ctitle”) creato
     * dall’utente di cuiè fornito il sid. La lunghezza massima del titolo è di 20 caratteri. Non
     * ritorna nulla. Nota: i titoli dei canali devo essere tutti differenti, dunque se si fornisce
     * un titolo già esistente viene tornato errore 400.
     * @param ctitle Nome del canale che si vuole aggiungere
     * @param responseListener Callback chiamata quando la richiesta di rete va a buon fine
     * @param errorListener Callback chiamata quando la richiesta di rete ha riscontrato un errore
     * @throws JSONException
     */
    public void addChannel(String ctitle, Response.Listener<JSONObject> responseListener,
                           Response.ErrorListener errorListener) throws JSONException {
        final String serviceUrl = "addChannel.php";
        final String url = BASE_URL + serviceUrl;
        final JSONObject jsonBody = new JSONObject();
        jsonBody.put("sid", sid);
        jsonBody.put("ctitle", ctitle);

        JsonObjectRequest request = new JsonObjectRequest(url, jsonBody, responseListener, errorListener);
        requestQueue.add(request);
    }

    /**
     * Richiesta di rete che non ritorna nulla. Aggiorna i dati di profilo. Sia “name” che
     * “picture” sono  opzionali ma almeno uno dei due deve essere specificato. Se viene cambiata
     * l’immagine, il numero di versione viene incrementato. Se viene indicato un
     * nome già usato da un altro utente, viene ritornato un errore 400.
     * @param name Nuovo nome dell'utente
     * @param picture Nuova immagine del profilo (null se non la si vuole aggiornare)
     * @param responseListener Callback chiamata quando la richiesta di rete va a buon fine
     * @param errorListener Callback chiamata quando la richiesta di rete ha riscontrato un errore
     * @throws JSONException
     */
    public void setProfile(String name, String picture,
                           Response.Listener<JSONObject> responseListener,
                           Response.ErrorListener errorListener) throws JSONException {
        final String serviceUrl = "setProfile.php";
        final String url = BASE_URL + serviceUrl;
        final JSONObject jsonBody = new JSONObject();
        jsonBody.put("sid", sid);
        jsonBody.put("name", name);
        if (picture != null) {
            jsonBody.put("picture", picture);
        }

        JsonObjectRequest request = new JsonObjectRequest(url, jsonBody, responseListener, errorListener);
        requestQueue.add(request);
    }

    /**
     * Richiesta di rete che ritorna i dati dell’utente (uid, name, picture, pversion). L’immagine
     * di profilo è codificata in base64.
     * @param responseListener Callback chiamata quando la richiesta di rete va a buon fine.
     *                         Contiene il profilo (uid, nome, picture)
     * @param errorListener Callback chiamata quando la richiesta di rete ha riscontrato un errore
     * @throws JSONException
     */
    public void getProfile(Response.Listener<JSONObject> responseListener,
                           Response.ErrorListener errorListener) throws JSONException {
        final String serviceUrl = "getProfile.php";
        final String url = BASE_URL + serviceUrl;
        final JSONObject jsonBody = new JSONObject();
        jsonBody.put("sid", sid);

        JsonObjectRequest request = new JsonObjectRequest(url, jsonBody, responseListener, errorListener);
        requestQueue.add(request);
    }

    /**
     * Richiesta di rete che inserisce un nuovo post di tipo testo o immagine, che devono avere il
     * valore content, nel canale indicato.  Non ritorna nulla
     * @param ctitle
     * @param content
     * @param type
     * @param responseListener Callback chiamata quando la richiesta di rete va a buon fine
     * @param errorListener Callback chiamata quando la richiesta di rete ha riscontrato un errore
     * @throws JSONException
     */
    public void addPost(String ctitle, String content, String type,
                        Response.Listener<JSONObject> responseListener,
                        Response.ErrorListener errorListener) throws JSONException {
        assert type.equals(Post.TEXT) || type.equals(Post.IMAGE);
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

    /**
     * Richiesta di rete che inserisce un nuovo post di tipo posizione, contenente il
     * valore di lat e lon, nel canale indicato. Non ritorna nulla.
     * @param ctitle
     * @param lat
     * @param lon
     * @param type
     * @param responseListener Callback chiamata quando la richiesta di rete va a buon fine
     * @param errorListener Callback chiamata quando la richiesta di rete ha riscontrato un errore
     * @throws JSONException
     */
    public void addPost(String ctitle, String lat, String lon, String type,
                        Response.Listener<JSONObject> responseListener,
                        Response.ErrorListener errorListener) throws JSONException {
        assert type.equals(Post.LOCATION);
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

    /**
     * Richiesta di rete che ritorna l’immagine (in base64) per il post (di tipo immagine) di cui è
     * passato il pid. Ritorna errore se il PID non corrisponde ad un post di tipo immagine
     * @param pid
     * @param responseListener Callback chiamata quando la richiesta di rete va a buon fine.
     *                         Contiene l'immagine base64 (content)
     * @param errorListener Callback chiamata quando la richiesta di rete ha riscontrato un errore
     * @throws JSONException
     */
    public void getPostImage(String pid, Response.Listener<JSONObject> responseListener,
                             Response.ErrorListener errorListener) throws JSONException {
        final String serviceUrl = "getPostImage.php";
        final String url = BASE_URL + serviceUrl;
        final JSONObject jsonBody = new JSONObject();
        jsonBody.put("sid", sid);
        jsonBody.put("pid", pid);

        JsonObjectRequest request = new JsonObjectRequest(url, jsonBody, responseListener, errorListener);
        requestQueue.add(request);
    }

    /**
     * Richiesta di rete che ritorna, per l’utente indicato con lo uid: l’immagine di profilo
     * codificata in  base64, lo uid dell’utente e la versione dell’immagine
     * @param uid
     * @param responseListener Callback chiamata quando la richiesta di rete va a buon fine.
     *                         Contiene l'immagine del profilo in base64, pversion e uid
     * @param errorListener Callback chiamata quando la richiesta di rete ha riscontrato un errore
     * @throws JSONException
     */
    public void getUserPicture(String uid, Response.Listener<JSONObject> responseListener,
                               Response.ErrorListener errorListener) throws JSONException {
        final String serviceUrl = "getUserPicture.php";
        final String url = BASE_URL + serviceUrl;
        final JSONObject jsonBody = new JSONObject();
        jsonBody.put("sid", sid);
        jsonBody.put("uid", uid);

        JsonObjectRequest request = new JsonObjectRequest(url, jsonBody, responseListener, errorListener);
        requestQueue.add(request);
    }
}
