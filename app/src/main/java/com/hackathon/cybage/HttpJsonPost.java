package com.hackathon.cybage;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.MalformedJsonException;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by ameyaapte1 on 18/5/16.
 */
public class HttpJsonPost extends AsyncTask<JsonObject, Integer, JsonObject> {

    private String url, progressinfo, action;
    private Context context;
    private ProgressDialog progressDialog;
    private AsyncTaskComplete callback;
    private JsonObject input;

    public HttpJsonPost(String url, String action, String progressinfo, Context context, AsyncTaskComplete callback) {
        this.context = context;
        this.url = url;
        this.callback = callback;
        this.action = action;
        this.progressinfo = progressinfo;
    }


    public void setProgressinfo(String progressinfo) {
        this.progressinfo = progressinfo;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(progressinfo);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected JsonObject doInBackground(JsonObject... params) {
        this.input = params[0];
        return postJsonObject(params[0]);
    }

    @Override
    protected void onPostExecute(JsonObject result) {
        progressDialog.dismiss();
        if (result == null)
            Toast.makeText(context, "Server not Reachable.Check Connection.", Toast.LENGTH_LONG).show();
        try {
            callback.handleResult(input, result, action);
        } catch (JSONException e) {
            e.printStackTrace();

        }
    }

    private JsonObject postJsonObject(JsonObject jsonObject) {
        try {
            String data = jsonObject.toString();
            URL object = new URL(url);

            HttpURLConnection httpURLConnection = (HttpURLConnection) object.openConnection();
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestMethod("POST");

            httpURLConnection.setConnectTimeout(6000);
            httpURLConnection.setReadTimeout(25000);

            OutputStreamWriter wr = new OutputStreamWriter(httpURLConnection.getOutputStream());
            wr.write(data);
            wr.flush();

            StringBuilder sb = new StringBuilder();
            int HttpResult = httpURLConnection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
            }
            return new JsonParser().parse(sb.toString()).getAsJsonObject();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedJsonException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
