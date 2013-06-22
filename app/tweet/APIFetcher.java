package tweet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.JSONValue;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;
//
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;


public class APIFetcher {
	
	private int pageSize=100;
	private int maxPages=49;
	
	interface Handler {
		void handle(String query, Status tweet);
	}
	
	private List<Handler> handlers;
	
	public APIFetcher(int pageSize, int maxPages) {
		this.pageSize = pageSize;
		this.maxPages = maxPages;
		init();
	}
	
	public APIFetcher() {
		init();
	}
	
	private void init() {
		this.handlers = new ArrayList<Handler>();
	}
	
	public void addHandler(Handler handler) {
		this.handlers.add(handler);
	}
	
	public void fetchAll(String queryName, String terms) throws TwitterException {
        Twitter twitter = TwitterFactory.getSingleton();

        long maxId=-1;
		long previousLastId=Long.MAX_VALUE;
		for( int k=0; k<maxPages; k++ ) {
	        //query.setUntil("2013-06-06");
            Query query = new Query(terms).count(pageSize).resultType("recent");
			if(maxId>0) {
			    query.setMaxId(maxId);
			}
            System.err.println("Query: "+query.toString());
	        QueryResult result = twitter.search(query);
	        System.out.println("Aantal resultaten: " +result.getTweets().size());

	        long lastId=Long.MAX_VALUE;
            for (Status status : result.getTweets()) {
                Long id = status.getId();
                if (id<lastId) lastId=id;
                handleTweet(queryName, status);
                //System.out.println(id+"@" + status.getUser().getScreenName() + ":" + status.getCreatedAt() +":"+ status.getText());
            }
  	      	System.err.println("last id: "+lastId);
            maxId = lastId;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				// ignore
			}
			//try {
			//	Thread.sleep(5000);
			//} catch (InterruptedException e) {
			//	// ignore
			//}
			if (maxId>=previousLastId) {
				System.err.println("Last id not descending, quitting");
				break;
			}
			previousLastId=maxId;
	        if (k>=maxPages-1) {
	            System.err.println("Warning, more than "+maxPages+" results, ignoring the rest!!!");
	        }
		}
	}
	
	private void handleTweet(String query, Status tweet) {
		for(Handler h: handlers) {
			h.handle(query, tweet);
		}
	}
}

