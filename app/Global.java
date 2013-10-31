import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import controllers.Streams;
import controllers.JobRunner;

import com.avaje.ebean.Ebean;

import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.Play;

public class Global extends GlobalSettings {
    
    @Override
    public void onStart(Application app) {
          Logger.info("Application startup...");
          Streams.startConnection();
          JobRunner.cleanup();
    }

    @Override
    public void onStop(Application app) {
          Logger.info("Application shutdown...");
          Streams.closeConnection();
    }      
        
}


