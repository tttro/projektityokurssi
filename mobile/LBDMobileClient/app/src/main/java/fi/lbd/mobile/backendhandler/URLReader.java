package fi.lbd.mobile.backendhandler;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Static methods for reading an url.
 *
 * Created by Tommi.
 */
public final class URLReader {
    private static final int CONNECTIONS_PER_ROUTE = 4;
    private static DefaultHttpClient httpClient;

    /**
     * Creates a concurrent http manager and recycles the http client.
     *
     * @return  Http client.
     */
    private synchronized static DefaultHttpClient getHttpClient() {
        if (httpClient != null) {
            return httpClient;
        }
        httpClient = new DefaultHttpClient();

        ClientConnectionManager manager = httpClient.getConnectionManager();
        HttpParams httpParams = httpClient.getParams();

//        httpParams.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true);
//        httpParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, new Integer(60000));
//        httpParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, new Integer(60000));

        ConnPerRoute connPerRoute = new ConnPerRouteBean(CONNECTIONS_PER_ROUTE);
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, connPerRoute);
        ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager(httpParams, manager.getSchemeRegistry());

        httpClient = new DefaultHttpClient(connManager , httpParams);
        return httpClient;
    }

    private URLReader() {}

    /**
     * Opens the given url and returns an URLResponse object if the connection succeeds and the
     * contents in the given url can be read.
     *
     * @param url   URL to read
     * @return  URLResponse-object with results from the URL.
     */
	public static URLResponse get(String url) {
        HttpGet getObj = new HttpGet(url);
        getObj.addHeader("LBD_LOGIN_HEADER", "SimoSahkari"); // TODO: Headerit + Login?
	    return URLReader.process(getObj); // If the url reading fails, null is returned.
	}

    public static URLResponse postJson(String url, String jsonContents) {
        StringEntity entity = null;
        try {
            entity = new StringEntity(jsonContents, HTTP.UTF_8);
            entity.setContentType("application/json");
        } catch (UnsupportedEncodingException e) {
            Log.e(URLReader.class.getSimpleName(), "Url post contents were invalid. Resulted in UnsupportedEncodingException. {}", e);
            return null;
        }
        HttpPost postObj = new HttpPost(url);
        postObj.addHeader("LBD_LOGIN_HEADER", "SimoSahkari"); // TODO: Headerit + Login?
        postObj.setEntity(entity);
        return URLReader.process(postObj); // If the url reading fails, null is returned.
    }



    private static URLResponse process(HttpUriRequest request) {
        String resultContent = "";
        URLResponse urlResponse = null;
        BufferedReader bufferedReader = null;
        InputStream contentStream = null;
        try {
            DefaultHttpClient httpClient = getHttpClient();
            HttpResponse response = httpClient.execute(request);
            contentStream = response.getEntity().getContent();

            if (contentStream != null) {
                StringBuilder sb = new StringBuilder();
                bufferedReader = new BufferedReader(new InputStreamReader(contentStream));

                String currLine = bufferedReader.readLine();
                while (currLine != null) {
                    sb.append(currLine);
                    currLine = bufferedReader.readLine();
                }
                resultContent = sb.toString();
            }

            urlResponse = new URLResponse(
                    resultContent,
                    URLResponse.ResponseStatus.get(response.getStatusLine().getStatusCode()),
                    response.getStatusLine().getReasonPhrase());

        } catch (ClientProtocolException e) {
            Log.e(URLReader.class.getSimpleName(), "Reading url encountered HTTP protocol error: "+ e.getMessage());
        } catch (IOException e) {
            Log.e(URLReader.class.getSimpleName(), "Reading url encountered I/O error: "+ e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.e(URLReader.class.getSimpleName(), "Given url is not in valid format: "+ e.getMessage());
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.e(URLReader.class.getSimpleName(), "Failed to close buffered reader: "+ e.getMessage());
                }
            }
            if (contentStream != null) {
                try {
                    contentStream.close();
                } catch (IOException e) {
                    Log.e(URLReader.class.getSimpleName(), "Failed to close content stream: "+ e.getMessage());
                }
            }
        }

        return urlResponse; // If the url reading fails, null is returned.
    }
}
