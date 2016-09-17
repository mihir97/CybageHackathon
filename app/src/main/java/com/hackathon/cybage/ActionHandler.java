package com.hackathon.cybage;

import android.app.ProgressDialog;
import android.content.Context;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

public class ActionHandler {

    AsyncTaskComplete callback;
    ProgressDialog progressDialog;
    private String server_url = "http://cybageandroid.16mb.com/index.php";
    private Context context;


    public ActionHandler(Context context, AsyncTaskComplete callback) {
        this.callback = callback;
        this.context = context;
        progressDialog = new ProgressDialog(context);
    }

    public void addLocation(Double latitude, Double longitude) {
        PostJsonObject(createaddLocationitem(latitude, longitude), server_url, "Add", "Updating Server\nPlease Wait");
    }

    private JsonObject createaddLocationitem(Double latitude, Double longitude) {
        JsonObject jsonObject = new JsonObject();

        try {
            jsonObject.addProperty("action", "Add");
            jsonObject.addProperty("latitude", String.valueOf(latitude));
            jsonObject.addProperty("longitude", String.valueOf(longitude));
            return jsonObject;

        } catch (JsonIOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public void fetchLocation(Double latitude, Double longitude) {
        PostJsonObject(createfetchLocationitem(latitude, longitude), server_url, "Fetch", "Querying from Server\nPlease Wait");
    }

    private JsonObject createfetchLocationitem(Double latitude, Double longitude) {
        JsonObject jsonObject = new JsonObject();

        try {
            jsonObject.addProperty("action", "Fetch");
            jsonObject.addProperty("latitude", String.valueOf(latitude));
            jsonObject.addProperty("longitude", String.valueOf(longitude));
            return jsonObject;

        } catch (JsonIOException e) {
            e.printStackTrace();
        }
        return null;

    }

    private void PostJsonObject(final JsonObject jsonObject, String url, final String action, String progress_status) {
        HttpJsonPost httpJsonPost = new HttpJsonPost(url, action, progress_status, context, callback);
        httpJsonPost.execute(jsonObject);
    }

}
