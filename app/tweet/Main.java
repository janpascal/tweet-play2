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

      private List<LogCallback> loggers;

      public Main() {
          loggers = new ArrayList<LogCallback>();
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
		
		List<Exporter> exporters = new ArrayList<Exporter>();
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
			fetcher.fetchAll(queryName, config.getQueryForName(queryName));
		}

                File[] result = new File[exporters.size()];

                int i=0;
		for(Exporter e: exporters) {
			try {
				e.write();
			} catch (FileNotFoundException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
                        result[i] = new File(e.filename);
                        i++;
        }
        return result;
	}
}
