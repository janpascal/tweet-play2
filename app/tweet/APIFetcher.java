package tweet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Thread;
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
import twitter4j.RateLimitStatus;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.conf.Configuration;

public class APIFetcher {
	
        private twitter4j.conf.Configuration twitterConfiguration;
	private int pageSize=100;
	private int maxPages=49;
	
	interface Handler {
		void handle(String query, Status tweet);
	}
	
	private List<Handler> handlers;
        private List<LogCallback> loggers;
	
	public APIFetcher(Configuration twitterConfiguration, int pageSize, int maxPages) {
                this.twitterConfiguration = twitterConfiguration;
		this.pageSize = pageSize;
		this.maxPages = maxPages;
		init();
	}
	
	public APIFetcher(Configuration twitterConfiguration) {
                this.twitterConfiguration = twitterConfiguration;
		init();
	}
	
	private void init() {
		this.handlers = new ArrayList<Handler>();
                this.loggers = new ArrayList<LogCallback>();
	}
	
	public void addHandler(Handler handler) {
		this.handlers.add(handler);
	}

        public void addLogger(LogCallback logger) {
                loggers.add(logger);
        }

        public void log(String line) {
            for (LogCallback logger: loggers) {
                logger.log(line);
            }
        }
	
	public void fetchAll(String queryName, String terms) throws TwitterException {

          TwitterFactory tf = new TwitterFactory(twitterConfiguration);
          Twitter twitter = tf.getInstance();

          RateLimitStatus rateStatus = twitter.getRateLimitStatus("search").get("/search/tweets");
          int remaining = rateStatus.getRemaining();

          long maxId=-1;
   	  long previousLastId=Long.MAX_VALUE;
	  for( int k=0; k<maxPages; k++ ) {
             //query.setUntil("2013-06-06");
             while (remaining<10) {
                 rateStatus = twitter.getRateLimitStatus("search").get("/search/tweets");
                 log("RateLimit limit: "+rateStatus.getLimit());
                 log("RateLimit remaining: "+rateStatus.getRemaining());
                 log("RateLimit seconds to reset: "+rateStatus.getSecondsUntilReset());
                 long secondsToWait = rateStatus.getSecondsUntilReset();
                 log("Running into Twitter rate limiting, need to wait "+secondsToWait+" seconds...");
                 if(secondsToWait>60) secondsToWait=59;
                 try {
                     Thread.sleep((1+secondsToWait)*1000);
                 } catch (InterruptedException e) {
                     log("Wait interrupted "+e.toString());
                     break;
                 }
                 rateStatus = twitter.getRateLimitStatus("search").get("/search/tweets");
                 remaining = rateStatus.getRemaining();
             }
             remaining--;
             Query query = new Query(terms).count(pageSize).resultType("recent");
	     if(maxId>0) {
			    query.setMaxId(maxId);
			}
             log("Query: "+query.toString());
	     QueryResult result = twitter.search(query);
	     log("Aantal resultaten: " +result.getTweets().size());

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

