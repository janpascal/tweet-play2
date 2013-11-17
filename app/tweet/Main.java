package tweet;

import java.io.FileNotFoundException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.configuration.ConfigurationException;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.conf.Configuration;

public class Main {

    public interface Handler {
        void handleNumber(int numTweets);
        void handleStatus(boolean waiting, int secondsToWait);
    }

    private List<LogCallback> loggers;
    private List<Handler> handlers;

    public Main() {
        loggers = new ArrayList<>();
        handlers = new ArrayList<>();
    }

    public void addHandler(Handler handler) {
        handlers.add(handler);
    }

    public void addLogger(LogCallback logger) {
        loggers.add(logger);
    }

    public void log(String line) {
        for (LogCallback logger: loggers) {
            logger.log(line);
        }
    }

    public File[] runConfig(Configuration twitterConfiguration, Config config, String prefix) throws TwitterException, ConfigurationException {
        Locale.setDefault(Locale.US); // Needed because day and month names are in English
        APIFetcher fetcher = new APIFetcher(twitterConfiguration, config.getPageSize(),config.getMaxPages());
        fetcher.addLogger(new LogCallback(){
            public void log(String line) {
                Main.this.log(line);
            }
        });

        for(Handler handler: handlers) {
            fetcher.addMainHandler(handler);
        }

        List<Exporter> exporters = new ArrayList<>();
        for(String filename: config.getExcelSet()) {
            final Exporter e = new Exporter(prefix+"/"+filename);
            final List<String> terms = config.getTermsForExcel(filename);
            exporters.add(e);
            fetcher.addHandler( new APIFetcher.Handler() {
                public void handle(String queryName, Status tweet) {
                    if(terms.contains(queryName)) {
                        //System.out.println("    tweet for term " + queryName + " added to " + e.getFilename() + "    "+tweet.getId()+": "+tweet.getText());
                        e.addTweet(tweet);
                    }
                }
            });
        }

        for(String queryName: config.getQueryNames()) {
            log("Running query "+queryName);
            fetcher.fetchAll(queryName, config.getQueryForName(queryName));
        }
        log("Done running queries, starting export");

        File[] result = new File[exporters.size()];

        int i=0;
        for(Exporter e: exporters) {
            try {
                e.write();
            } catch (FileNotFoundException ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
                log("Caught exception writing output file");
                log(ex.toString());
            }
            result[i] = new File(e.filename);
            i++;
        }
        log("Done writing data to excel files");
        return result;
    }
}
