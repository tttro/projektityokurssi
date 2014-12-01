package fi.lbd.mobile.backendhandler;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
        String resultContent = "";
        URLResponse urlResponse = null;
        BufferedReader bufferedReader = null;
        InputStream contentStream = null;
        try {
            DefaultHttpClient httpClient = getHttpClient();
            HttpGet getObj = new HttpGet(url);
            getObj.addHeader("LBD_LOGIN_HEADER", "SimoSahkari"); // TODO: Headerit + Login?

            HttpResponse response = httpClient.execute(getObj);
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
