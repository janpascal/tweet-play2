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
    private List<Main.Handler> mainHandlers;
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
        this.handlers = new ArrayList<>();
        this.mainHandlers = new ArrayList<>();
        this.loggers = new ArrayList<>();
    }

    public void addHandler(Handler handler) {
        this.handlers.add(handler);
    }

    public void addMainHandler(Main.Handler handler) {
        this.mainHandlers.add(handler);
    }

    public void addLogger(LogCallback logger) {
        loggers.add(logger);
    }

    public void log(String line) {
        for (LogCallback logger: loggers) {
            logger.log(line);
        }
    }

    public void handleNumber(int numTweets) {
        for (Main.Handler handler: mainHandlers) {
            handler.handleNumber(numTweets);
        }
    }

    public void handleJobStatus(boolean waiting, int secondsToWait) {
        for (Main.Handler handler: mainHandlers) {
            handler.handleStatus(waiting, secondsToWait);
        }
    }

    // FIXME this method is too long, split it up
    public void fetchAll(String queryName, String terms) throws TwitterException {
        TwitterFactory tf = new TwitterFactory(twitterConfiguration);
        Twitter twitter = tf.getInstance();

        RateLimitStatus rateStatus = null;
        while (rateStatus==null) {
            try {
                rateStatus = twitter.getRateLimitStatus("search").get("/search/tweets");
            } catch (TwitterException e) {
                log("twitter.getRateLimit exception, error code="+e.getErrorCode()+": "+e.getErrorMessage());
                if (e.getErrorCode()!=130) throw e;
                log("Got over capacity message, sleeping for five seconds, then trying again");
                for(int i=0; i<5; i++) {
                    handleJobStatus(true, 5-i);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                        log("Wait interrupted "+e2.toString());
                    }
                }
                handleJobStatus(false, 0);
            }
        }
        int remaining = rateStatus.getRemaining();

        long maxId=-1;
        long previousLastId=Long.MAX_VALUE;

fetch:
        for( int k=0; k<maxPages; k++ ) {
            //query.setUntil("2013-06-06");
            while (remaining<10) {
                // log("RateLimit limit: "+rateStatus.getLimit());
                // log("RateLimit remaining: "+rateStatus.getRemaining());
                // log("RateLimit seconds to reset: "+rateStatus.getSecondsUntilReset());
                int secondsToWait = rateStatus.getSecondsUntilReset();
                log("Running into Twitter rate limiting, need to wait "+secondsToWait+" seconds...");
                for(int i=0; i<60 && secondsToWait>=0; i++) {
                    try {
                        handleJobStatus(true, secondsToWait);
                        Thread.sleep(1000);
                        secondsToWait--;
                    } catch (InterruptedException e) {
                        log("Wait interrupted "+e.toString());
                        break;
                    }
                }
                rateStatus = null;
                while (rateStatus==null) {
                    try {
                        rateStatus = twitter.getRateLimitStatus("search").get("/search/tweets");
                    } catch (TwitterException e) {
                        log("twitter.getRateLimit exception, error code="+e.getErrorCode()+": "+e.getErrorMessage());
                        if (e.getErrorCode()!=130) throw e;
                        log("Got over capacity message, sleeping for five seconds, then trying again");
                        for(int i=0; i<5; i++) {
                            try {
                                handleJobStatus(true, secondsToWait+5-i);
                                Thread.sleep(1000);
                            } catch (InterruptedException e2) {
                                log("Wait interrupted "+e2.toString());
                            }
                        }
                    }
                }
                rateStatus = twitter.getRateLimitStatus("search").get("/search/tweets");
                remaining = rateStatus.getRemaining();
            }
            handleJobStatus(false, 0);
            remaining--;
            Query query = new Query(terms).count(pageSize).resultType("recent");
            if(maxId>0) {
                query.setMaxId(maxId);
            }
            QueryResult result = null;
            while (result == null) {
                log("Query: "+query.toString());
                try {
                    result = twitter.search(query);
                } catch (TwitterException e) {
                    log("twitter.search exception, error code="+e.getErrorCode()+": "+e.getErrorMessage());
                    if (e.getErrorCode()==130) {
                        log("Got over capacity message, sleeping for five seconds, then trying again");
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e2) {
                            log("Wait interrupted "+e2.toString());
                        }
                    } else if (e.getErrorCode()==88) {
                        log("Got rate limit exhausted message");
                        remaining = 0;
                        continue fetch;
                    } else {
                        throw e;
                    }
                }
            }
            log("Aantal resultaten: " +result.getTweets().size());
            handleNumber(result.getTweets().size());
            if (result.getRateLimitStatus()!=null) {
                rateStatus = result.getRateLimitStatus();
                //log("Search query gave rate limit status, remaining="+rateStatus.getRemaining());
                remaining = rateStatus.getRemaining();
            }

            long lastId=Long.MAX_VALUE;
            for (Status status : result.getTweets()) {
                Long id = status.getId();
                if (id<lastId) lastId=id;
                handleTweet(queryName, status);
                //System.out.println(id+"@" + status.getUser().getScreenName() + ":" + status.getCreatedAt() +":"+ status.getText());
            }
            log("last id: "+lastId);
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
                log("Last id not descending, quitting");
                break;
            }
            previousLastId=maxId;
            if (k>=maxPages-1) {
                log("Warning, more than "+maxPages+" results, ignoring the rest!!!");
            }
        }
    }

    private void handleTweet(String query, Status tweet) {
        for(Handler h: handlers) {
            h.handle(query, tweet);
        }
    }
}

