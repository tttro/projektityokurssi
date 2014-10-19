package fi.lbd.mobile.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

// TODO: KESKEN!
public class URLReader {
	// http://api-lbdserver.rhcloud.com/locationdata/api/Streetlights/near/?latitude=23.795199257764725&longitude=61.503697166613755

	
	// TODO: Tartteeko aina uusia instansseja?
	public static String get(String url) {
		String resultContent = "";
	    try {
	        DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = httpClient.execute(new HttpGet(url));
			
			InputStream contentStream = response.getEntity().getContent();
			if (contentStream != null) {
				StringBuilder sb = new StringBuilder();
		        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(contentStream));
		        
		        String currLine = bufferedReader.readLine();
		        while (currLine != null) {
		        	sb.append(currLine);
		        	currLine = bufferedReader.readLine();
		        }
		        resultContent = sb.toString();
			} else {
				//Log.e(tag, msg) // TODO: Lokitus
			}
	        contentStream.close();
		} catch (ClientProtocolException e) {
			e.printStackTrace(); // TODO: Lokitus
		} catch (IOException e) {
			e.printStackTrace(); // TODO: Lokitus
		}
        // TODO: Streamit kiinni finally:ssä
	    return resultContent;
	}
}
