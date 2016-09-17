package com.hackathon.cybage;

import android.app.ProgressDialog;
import android.content.Context;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

public class ActionHandler {

    AsyncTaskComplete callback;
    ProgressDialog progressDialog, imageDialog;
    private String action_url = "http://ec2-54-173-188-212.compute-1.amazonaws.com/katta_api/action.php";
    private String order_url = "http://ec2-54-173-188-212.compute-1.amazonaws.com/katta_api/order.php";
    private String firebase_url = "http://ec2-54-173-188-212.compute-1.amazonaws.com/katta_api/firebase.php";
    private Context context;


    public ActionHandler(Context context, AsyncTaskComplete callback) {
        this.callback = callback;
        this.context = context;
        progressDialog = new ProgressDialog(context);
        imageDialog = new ProgressDialog(context);

    }

    public void availabilityitem(String name, boolean checkbox_flag, boolean special) {
        PostJsonObject(createavailabilityitem(name, checkbox_flag ? 1 : 0, special), action_url, "setAvailability", "Updating Server\nPlease Wait");

    }

    private JsonObject createavailabilityitem(String name, int availability_flag, boolean special) {
        JsonObject jsonObject = new JsonObject();

        try {
            jsonObject.addProperty("action", "setAvailability");
            jsonObject.addProperty("name", name);
            jsonObject.addProperty("availability", availability_flag);
            jsonObject.addProperty("special", special ? 1 : 0);

            return jsonObject;
        } catch (JsonIOException e) {
            e.printStackTrace();
        }
        return null;

    }

    private void PostJsonObject(final JsonObject jsonObject, String url, final String action, String progress_status) {
        HttpJsonPost httpJsonPost = new HttpJsonPost(url, action, progress_status, context, callback);
        httpJsonPost.execute(jsonObject);
        /*progressDialog.setMessage(progress_status);
        progressDialog.show();
        progressDialog.setCancelable(false);
        Ion.with(context)
                .load(url)
                .progressDialog(progressDialog)
                .setTimeout(10000)
                .setJsonObjectBody(jsonObject)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        if (e != null) {
                            Toast.makeText(context, "Connection Error.\nCheck your connection!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        } else {
                            try {
                                callback.handleResult(jsonObject, result, action);
                                progressDialog.dismiss();
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }

                });*/
    }

}
