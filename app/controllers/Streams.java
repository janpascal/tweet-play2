package controllers;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import twitter4j.Status;
import twitter4j.TwitterStream;
import twitter4j.TwitterException;
import twitter4j.TwitterStreamFactory;
import twitter4j.StatusListener;
import twitter4j.StatusDeletionNotice;
import twitter4j.StallWarning;
import twitter4j.FilterQuery;
import twitter4j.URLEntity;
import twitter4j.MediaEntity;
import twitter4j.HashtagEntity;
import twitter4j.UserMentionEntity;

import tweet.Exporter;
import tweet.SimpleTweet;

import play.*;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.RequestBody;
import play.data.*;

import models.*;
import views.html.*;

public class Streams extends Controller
{
    public static class TweetListener implements StatusListener {
      private List<String> terms;
      private Pattern matchPattern;

      public TweetListener(List<String> terms) {
        this.terms = terms;
        StringBuilder query = new StringBuilder();
        for(int i=0; i<terms.size(); i++) {
          if (i>0) query.append("|");
          //query.append("(").append(Pattern.quote(terms.get(i))).append(")");
          //query.append("(").append(terms.get(i)).append(")");
          query.append(terms.get(i));
        }
        Logger.info("Query match pattern: "+query.toString());
        this.matchPattern = Pattern.compile(query.toString(), Pattern.CASE_INSENSITIVE);
        Logger.info("Query match pattern: "+matchPattern.toString());
      }
      public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        Logger.info("Deletion notice");
       }
      public void  onScrubGeo(long userId, long upToStatusId) {
        Logger.info("Scrub geo");
      }
      public void  onStallWarning(StallWarning warning) {
        Logger.info("Stall warning");
      }
      public void onStatus(twitter4j.Status status) {
          Logger.info(status.getUser().getName() + " : " + status.getText());
          Tweet tweet = new Tweet(status);
          tweet.conformsToTerms = checkMatch(status);
          tweet.save();
      }
      public void  onTrackLimitationNotice(int numberOfLimitedStatuses){
        Logger.info("Track limitation, missed " + numberOfLimitedStatuses);
      }
      public void  onException(java.lang.Exception ex) {
        ex.printStackTrace();
        Logger.warn("Exception in Twitter Stream", ex);
      }

      protected boolean checkMatch(twitter4j.Status status) {
          boolean result = false;
          if(matchPattern.matcher(status.getText()).find()) result=true;
          if(result) {
            Logger.debug("Terms found in text");
            Logger.debug("    \""+status.getText()+"\"");
            return result;
          }
          for(URLEntity ue: status.getURLEntities()) {
            if(matchPattern.matcher(ue.getDisplayURL()).find()) result=true;
            if(matchPattern.matcher(ue.getExpandedURL()).find()) result=true;
          }
          if(result) {
            Logger.debug("Terms found in URL entities");
            for(URLEntity ue: status.getURLEntities()) {
              Logger.debug("    "+ue.getDisplayURL());
              Logger.debug("    "+ue.getExpandedURL());
            }
            return result;
          }
          for(URLEntity ue: status.getMediaEntities()) {
            if(matchPattern.matcher(ue.getDisplayURL()).find()) result=true;
            if(matchPattern.matcher(ue.getExpandedURL()).find()) result=true;
          }
          if(result) {
            Logger.debug("Terms found in Media entities");
            for(URLEntity ue: status.getMediaEntities()) {
              Logger.debug("    "+ue.getDisplayURL());
              Logger.debug("    "+ue.getExpandedURL());
            }
            return result;
          }
          for(HashtagEntity he: status.getHashtagEntities()) {
            if(matchPattern.matcher(he.getText()).find()) result=true;
          }
          if(result) {
            Logger.debug("Terms found in Hashtag entities");
            for(HashtagEntity he: status.getHashtagEntities()) {
              Logger.debug("    "+he.getText());
            }
            return result;
          }
          for(UserMentionEntity me: status.getUserMentionEntities()) {
            if(matchPattern.matcher(me.getScreenName()).find()) result=true;
          }
          if(result) {
            Logger.debug("Terms found in User mention entities");
            for(UserMentionEntity me: status.getUserMentionEntities()) {
              Logger.debug("    "+me.getScreenName());
            }
            return result;
          }
          Logger.debug("Terms NOT FOUND");
            Logger.debug("    Terms not found in URL entities");
            for(URLEntity ue: status.getURLEntities()) {
              Logger.debug("    "+ue.getDisplayURL());
              Logger.debug("    "+ue.getExpandedURL());
            }
            Logger.debug("    Terms not found in Media entities");
            for(URLEntity ue: status.getMediaEntities()) {
              Logger.debug("    "+ue.getDisplayURL());
              Logger.debug("    "+ue.getExpandedURL());
            }
            Logger.debug("    Terms not found in Hashtag entities");
            for(HashtagEntity he: status.getHashtagEntities()) {
              Logger.debug("    "+he.getText());
            }
            Logger.debug("    Terms not found in User mention entities");
            for(UserMentionEntity me: status.getUserMentionEntities()) {
              Logger.debug("    "+me.getScreenName());
            }
          return result;
      }
    }

   private static TwitterStream twitter = null;

   private static void startStream(List<String> terms) throws TwitterException {
     /*
     if (twitter == null) {
        twitter4j.conf.Configuration tconf = Application.getTwitterConfiguration();
        TwitterStreamFactory tf = new TwitterStreamFactory(tconf);
        twitter = tf.getInstance();
        StatusListener l = new TweetListener(terms);
        twitter.addListener(l);
      } else {
        twitter.cleanUp();
      }
      */
      if(twitter!=null) {
        twitter.cleanUp();
      }
      twitter4j.conf.Configuration tconf = Application.getTwitterConfiguration();
      TwitterStreamFactory tf = new TwitterStreamFactory(tconf);
      twitter = tf.getInstance();
      StatusListener l = new TweetListener(terms);
      twitter.addListener(l);

      String[] tracks = new String[terms.size()];
      StringBuffer termsString = new StringBuffer();
      for(int i=0; i<terms.size(); i++) {
        tracks[i] = terms.get(i);
        if(i!=0) termsString.append(",");
        termsString.append(terms.get(i));
      }
      FilterQuery q = new FilterQuery().track(tracks);
      twitter.filter(q);
      Logger.info("Starting listening for tweets using terms "+termsString.toString()+"...");
   }

   public static void closeConnection() {
     if (twitter != null) {
       Logger.info("Closing down Twitter stream...");
       twitter.cleanUp(); 
     } else {
       Logger.info("No twitter stream found");
     }
   }

   private static StreamConfig getConfig() {
     List<StreamConfig> configs = StreamConfig.find.findList();
     if(configs.size()>1) {
       Logger.error("Multiple stream configurations present!");
     }
     StreamConfig config;
     if(configs.isEmpty()) {
       String[] tracks = new String[3];
       tracks[0] = "nl-alert";
       tracks[1] = "nlalert";
       tracks[2] = "\"nl alert\"";
       config = new StreamConfig(tracks);
       config.save();
     } else {
       config = configs.get(0);
     }
     return config;
   }

   public static Result editTerms() {
     StreamConfig config = getConfig();
     List<String> terms = config.listTerms();
     return ok(edit_stream_form.render(terms));
   }

   public static Result list() {
     List<Tweet> tweets = Tweet.find.where().eq("conformsToTerms",true).orderBy("date desc").findList();
     StreamConfig config = getConfig();
     String terms = config.listTermsAsString();
     return ok(stream_result_list.render(tweets, terms));
   }

   public static Result deleteTweet(Long id) {
     Tweet.find.ref(id).delete();
     flash("success", "Tweet deleted");
     return redirect(routes.Streams.list());
   }

   public static void startConnection() {
       StreamConfig config = getConfig();
        try {
            startStream(config.listTerms());
        } catch (TwitterException e) {
            Logger.info("Error starting twitter stream", e);
        }
   }

    public static Result start() {
       java.util.Map<String,String[]> map = request().body().asFormUrlEncoded();

       List<String> terms = new ArrayList<String>(map.size());
       for(int i=0; i<map.size(); i++) {
         String key = "terms["+i+"]";
         if(map.containsKey(key)) {
           String[] values = map.get(key);
           if ((values != null) && (values.length>=1)) {
             terms.add(values[0]);
           }
         }
       }

       StreamConfig config = getConfig();
       config.putTerms(terms);
       config.update();

       StringBuilder sb = new StringBuilder();
       for(String t: terms) {
         sb.append(t);
         sb.append(", ");
       }
       sb.delete(sb.length()-2, sb.length());

        try {
            startStream(terms);
            flash("success", "Twitter stream started ("+sb.toString()+")");
        } catch (TwitterException e) {
            Logger.info("Error starting twitter stream", e);
            flash("error", "Error starting Twitter stream" +e.getMessage());
        }
        return redirect(routes.Streams.list());
    }

    public static Result download() {
      Exporter exporter = new Exporter("/tmp/tweets.xls");

      List<Tweet> tweets = Tweet.find.all();

      try {
          for(Tweet t: tweets) {
              SimpleTweet simple = new SimpleTweet();
              simple.id = t.id;
              simple.createdAt = t.date;
              simple.userName = t.fromUser;
              simple.userId = t.fromUserId;
              simple.text = t.text;
              simple.inReplyToName = t.inReplyTo;
              simple.latitude = t.latitude;
              simple.longitude = t.longitude;

              exporter.addTweet(simple);
          }

          exporter.write();
      } catch (FileNotFoundException e) {
          flash("error", "Bestand niet gevonden");
          Logger.info("Bestand niet gevonden", e);
          return ok("Exception opening file");
      }

//      try {
          response().setContentType("application/x-download");  
          response().setHeader("Content-disposition","attachment; filename=tweets.xls"); 
          return ok(new File("/tmp/tweets.xls"));
/*
      } catch (IOException e) {
          Logger.info("Exception sending zipfile", e);
          return ok("Exception opening file");
*///      }
    }

}
