package fi.lbd.mobile.backendhandler;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.Scopes;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import fi.lbd.mobile.ApplicationDetails;
import fi.lbd.mobile.myActivity;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

/**
 * Static methods for reading an url.
 *
 * Created by Tommi.
 */
public class BasicUrlReader implements UrlReader {
    static final int REQUEST_AUTHORIZATION = 1138;
    private static final int CONNECTIONS_PER_ROUTE = 4;
    private static DefaultHttpClient httpClient;
    private Context context;
    public BasicUrlReader() {}

    public void initialize(Context cont,
                           @NonNull Pair<String, Certificate> firstCertificate,
                           @NonNull Pair<String, Certificate>... certificates) throws UrlReaderException {
        try {
            context = cont;
            // Store given certificates to the key store
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);

            keyStore.setCertificateEntry(firstCertificate.first, firstCertificate.second);
            int i = 1;
            for (Pair<String, Certificate> certificate : certificates) {
                keyStore.setCertificateEntry(certificate.first, certificate.second);
            }

            SSLSocketFactory sf = new SSLSocketFactory(keyStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER); // TODO: Hostname is aki's name??

            httpClient = new DefaultHttpClient();

            ClientConnectionManager manager = httpClient.getConnectionManager();
            HttpParams httpParams = httpClient.getParams();

//        httpParams.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true);
//        httpParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, new Integer(60000));
//        httpParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, new Integer(60000));

            ConnPerRoute connPerRoute = new ConnPerRouteBean(CONNECTIONS_PER_ROUTE);
            ConnManagerParams.setMaxConnectionsPerRoute(httpParams, connPerRoute);

            SchemeRegistry registry = manager.getSchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager(httpParams, registry);
            httpClient = new DefaultHttpClient(connManager, httpParams);

        } catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException
                | KeyManagementException | UnrecoverableKeyException ex) {
            Log.e(BasicUrlReader.class.getSimpleName(), "ERROR!", ex);
            throw new UrlReaderException("Failed to initialize url reader.", ex);
        }
    }

    private void setHeaders(AbstractHttpMessage message) {
        if(message != null) {
            AccountManager accountManager = AccountManager.get(context);
            Account[] accounts = accountManager.getAccountsByType("com.google");
            Account account;
            if (accounts.length > 0) {
                account = accounts[0];
            } else {
                account = null;
            }
            Log.d("******", account.toString());
            String email = account.name;
            String token = "foo";
            String scope = "oauth2:" + Scopes.PROFILE;
            String gid = "";
            try {
                token = GoogleAuthUtil.getToken(context, email, scope);
                gid = GoogleAuthUtil.getAccountId(context, email);
            } catch (UserRecoverableAuthException userRecoverableException) {
                // GooglePlayServices.apk is either old, disabled, or not present, which is
                // recoverable, so we need to show the user some UI through the activity.

//                startActivityForResult(new myActivity(), userRecoverableException.getIntent(),
//                        REQUEST_AUTHORIZATION, new Bundle());
                Log.d("******", userRecoverableException.toString());
            } catch (GoogleAuthException fatalException) {
                Log.d("******", fatalException.toString());
                Log.d("******", fatalException.getMessage());
            } catch (IOException io) {
                Log.d("******", "Blah3");
            }
            Log.i(this.getClass().getSimpleName(), "USER ID: "+ ApplicationDetails.get().getUserId());
            Log.i(this.getClass().getSimpleName(), "USER ID: "+ gid);
            message.addHeader("LBD_LOGIN_HEADER", token); // TODO: Access token
            message.addHeader("LBD_OAUTH_ID", gid); // Google id
        }
    }

    /**
     * Creates a concurrent http manager and recycles the http client.
     *
     * @return  Http client.
     */
    private HttpClient getHttpClient() {
        if (httpClient != null) {
            return httpClient;
        }
        throw new RuntimeException("Call initialize method before performing requests!");
    }

    /**
     * Opens the given url and returns an URLResponse object if the connection succeeds and the
     * contents in the given url can be read.
     *
     * @param url   URL to read
     * @return  URLResponse-object with results from the URL.
     */
    @Override
	public UrlResponse get(String url) {
        try {
            HttpGet getObj = new HttpGet(url);
            setHeaders(getObj);
            return this.process(getObj); // If the url reading fails, null is returned.
        } catch (Exception exception){
            Log.d("URL get unsuccessful. Resulted in exception: ",
                    exception.getClass().toString() + ": " + exception.getMessage());
            return null;
        }
	}

    /**
     * Tries to remove the contents in the given url.
     *
     * @param url
     * @return
     */
    @Override
    public UrlResponse delete(String url) {
        HttpDelete deleteObj = new HttpDelete(url);
        setHeaders(deleteObj);
        return this.process(deleteObj); // If the url reading fails, null is returned.
    }

    @Override
    public UrlResponse postJson(String url, String jsonContents) {
        StringEntity entity;
        try {
            entity = new StringEntity(jsonContents, HTTP.UTF_8);
            entity.setContentType("application/json");
        } catch (UnsupportedEncodingException e) {
            Log.e(BasicUrlReader.class.getSimpleName(),
                    "Url post contents were invalid. Resulted in UnsupportedEncodingException. {}", e);
            return null;
        }
        try {
            HttpPost postObj = new HttpPost(url);
            setHeaders(postObj);
            postObj.setEntity(entity);
            return this.process(postObj); // If the url reading fails, null is returned.
        } catch (Exception exception){
            Log.d("URL post unsuccessful. Resulted in exception: ",
                    exception.getClass().toString() + ": " + exception.getMessage());
            return null;
        }
    }



    @Override
    public UrlResponse putJson(String url, String jsonContents) {
        StringEntity entity;
        try {
            entity = new StringEntity(jsonContents, HTTP.UTF_8);
            entity.setContentType("application/json");
        } catch (UnsupportedEncodingException e) {
            Log.e(BasicUrlReader.class.getSimpleName(), "Url post contents were invalid. Resulted in UnsupportedEncodingException. {}", e);
            return null;
        }
        HttpPut putObj = new HttpPut(url);
        setHeaders(putObj);
        putObj.setEntity(entity);
        return this.process(putObj); // If the url reading fails, null is returned.
    }

    private UrlResponse process(HttpUriRequest request) {
        String resultContent = "";
        UrlResponse urlResponse = null;
        BufferedReader bufferedReader = null;
        InputStream contentStream = null;
        try {
            HttpClient httpClient = getHttpClient();
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

            urlResponse = new UrlResponse(
                    resultContent,
                    UrlResponse.ResponseStatus.get(response.getStatusLine().getStatusCode()),
                    response.getStatusLine().getReasonPhrase());

        } catch (ClientProtocolException e) {
            Log.e(BasicUrlReader.class.getSimpleName(), "Reading url encountered HTTP protocol error!", e);
        } catch (IOException e) {
            Log.e(BasicUrlReader.class.getSimpleName(), "Reading url encountered I/O error!", e);
        } catch (IllegalArgumentException e) {
            Log.e(BasicUrlReader.class.getSimpleName(), "Given url is not in valid format!", e);
        } catch (IllegalStateException e){
            Log.e(BasicUrlReader.class.getSimpleName(), "Given url is not in valid format!", e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.e(BasicUrlReader.class.getSimpleName(), "Failed to close buffered reader!", e);
                }
            }
            if (contentStream != null) {
                try {
                    contentStream.close();
                } catch (IOException e) {
                    Log.e(BasicUrlReader.class.getSimpleName(), "Failed to close content stream!", e);
                }
            }
        }

        return urlResponse; // If the url reading fails, null is returned.
    }
}
