package com.example.namiskuukkel.login_test;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by namiskuukkel on 17.11.2014.
 */
public class Rest {

    public static JSONObject doPut(String[] params) {
        JSONObject json = null;

        HttpClient httpclient = new DefaultHttpClient();
        HttpPut httpput = new HttpPut(params[2]);
        // Accept JSON
        httpput.addHeader("Content-Type", "application/json");
        httpput.addHeader("LBD_LOGIN_HEADER", params[1]);
        httpput.addHeader("LBD_OAUTH_ID", params[0]);
        // Execute the request
        HttpResponse response;

        try {
            response = httpclient.execute(httpput);
            // Get the response entity
            // Log.e("myApp", "Issue is here...!");
            HttpEntity entity = response.getEntity();
            StatusLine status = response.getStatusLine();
            int code = status.getStatusCode();
            Log.d("OnResult",String.valueOf(code));
            // If response entity is not null
            if (entity != null) {
                // get entity contents and convert it to string
                InputStream instream = entity.getContent();
                String result= convertStreamToString(instream);
                Log.d("OnResult",result);
                // construct a JSON object with result
                json=new JSONObject(result);
                // Closing the input stream will trigger connection release
                instream.close();
            }
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Return the json
        return json;
    }

    private static String convertStreamToString(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                //Log.d("Server", line);
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }
}