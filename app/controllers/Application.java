package controllers;

import java.io.*;
import java.util.*;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import play.*;
import play.libs.Json;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.ArrayNode;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Http.RequestBody;
import play.data.*;
import static play.data.Form.*;

import views.html.*;
import models.*;
import tweet.Config;
import tweet.Main;

import twitter4j.conf.ConfigurationBuilder;

public class Application extends Controller {

  public static twitter4j.conf.Configuration getTwitterConfiguration() {
         Configuration appConf = Play.application().configuration();
          ConfigurationBuilder cb = new ConfigurationBuilder();
          cb.setDebugEnabled(true)
            .setOAuthConsumerKey(appConf.getString("twitter4j.oauth.consumerKey"))
            .setOAuthConsumerSecret(appConf.getString("twitter4j.oauth.consumerSecret"))
            .setOAuthAccessToken(appConf.getString("twitter4j.oauth.accessToken"))
            .setOAuthAccessTokenSecret(appConf.getString("twitter4j.oauth.accessTokenSecret"));
          return cb.build();
  }

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static Result selectConfig() {
        return ok(selectconfig.render());
    }

    // FIXME #UGLY keep entire log in memory
    static List<String> messages = new ArrayList<String>();


    public static Result uploadConfig() {
        RequestBody mainbody = request().body();
        MultipartFormData body = mainbody.asMultipartFormData();
        if(body==null) {
            flash("error", "Missing file");
            return redirect(routes.Application.index());
        }
        FilePart bestand = body.getFile("bestand");
        if (bestand != null) {
          String fileName = bestand.getFilename();
          File file = bestand.getFile();
          try {
            Job job = new Job();
            job.save();
            job.addConfig(file);
            Config config = new Config(file);
            Main main = new Main();
            main.addLogger(new tweet.LogCallback() {
              public void log(String line) {
                Logger.info(line); 
                synchronized(messages) {
                    messages.add(line);
                }
              }
            });
            File[] files = main.runConfig(getTwitterConfiguration(), config, job.jobPath().toString());
            for(File f: files) {
              job.addExcelResult(f);
            }
            job.update();
            synchronized(messages) {
                messages = new ArrayList<String>();
            }
            /*
            Path zip = job.getZip();
            response().setContentType("application/x-download");  
            response().setHeader("Content-disposition","attachment; filename=results.zip"); 
            return ok(zip.toFile());
            */
            return redirect(routes.Application.showJobs());
          } catch (Exception e) {
            e.printStackTrace();
            flash("error", "File not found "+e.getMessage());
            return redirect(routes.Application.index());    
          }
        } else {
          flash("error", "Missing file");
          return redirect(routes.Application.index());    
        }
      }

    public static Result logLines(Integer lastLine) {
      ObjectNode result = Json.newObject();
      synchronized(messages) {
          result.put("lastLine", messages.size());
          ArrayNode jsonArray = result.putArray("messages");
          for(int i=lastLine; i<messages.size(); i++) {
              jsonArray.add("(" + i+") " + messages.get(i));
          }
      }
      return ok(result);
    }

    public static Result downloadExcel(Long jobId, String xlfile) {
        Job job = Job.find.byId(jobId);
        try {
            Path xl = job.jobPath().resolve(xlfile);
            if (!Files.exists(xl)) {
              return ok("File does not exist");
            }
            response().setContentType("application/x-download");  
            response().setHeader("Content-disposition","attachment; filename="+xlfile); 
            return ok(xl.toFile());
        } catch (IOException e) {
            Logger.info("Exception sending sxcel file", e);
            return ok("Exception opening file");
        }
    }

    public static Result downloadZip(Long jobId) {
        Job job = Job.find.byId(jobId);
        try {
            Path zip = job.getZip();
            if (!Files.exists(zip)) {
              return ok("File does not exist");
            }
            response().setContentType("application/x-download");  
            response().setHeader("Content-disposition","attachment; filename=job"+jobId+"-results.zip"); 
            return ok(zip.toFile());
        } catch (IOException e) {
            Logger.info("Exception sending zipfile", e);
            return ok("Exception opening file");
        }
    }

    public static Result downloadConfig(Long jobId) {
        Job job = Job.find.byId(jobId);
        try {
            Path ini = job.jobPath().resolve("config.ini");
            if (!Files.exists(ini)) {
              return ok("File does not exist");
            }
            response().setContentType("application/x-download");  
            response().setHeader("Content-disposition","attachment; filename=job"+jobId+"-config.ini"); 
            return ok(ini.toFile());
        } catch (IOException e) {
            Logger.info("Exception sending zipfile", e);
            return ok("Exception opening file");
        }
    }

    public static Result deleteJob(Long id) {
      try {
          Job.find.byId(id).remove();
      flash("success", "Job removed");
      } catch (IOException e) {
        Logger.info(e.getMessage(), e);
        flash("error", "Error deleting job "+e.getMessage());
      }

      return redirect(routes.Application.showJobs());
    }

    public static Result job() {
      return TODO;
      /*  JobDescription job = new JobDescription();
        job.addQuery("0", "nlalert");
        job.addQuery("1", "nl-alert");
        job.addQuery("2", "\"nl alert\"");
        return ok(job_description.render(job));*/
    }

    public static Result jobDescriptionList() {
      return ok(jobDescriptionList.render(JobDescription.find.all()));
    }
    
    public static Result runJob(Long descriptionId) {
      return TODO;
    }
    
    public static Result exportJob(Long jobId) {
      return TODO;
    }

    /**
     * Display the 'new job description form'.
     */
    public static Result createJobDescription() {
        Form<JobDescription> jobDescriptionForm = form(JobDescription.class);
        return ok(
            createJobDescriptionForm.render(jobDescriptionForm)
        );
    }

    /**
     * Handle the 'new job description form step 1' submission 
     */
    public static Result createJobDescriptionStep2() {
        Form<JobDescription> jobDescriptionForm = form(JobDescription.class).bindFromRequest();
        if(jobDescriptionForm.hasErrors()) {
            return badRequest(createJobDescriptionForm.render(jobDescriptionForm));
        }
        Logger.info(jobDescriptionForm.toString());
        Logger.info(jobDescriptionForm.data().toString());
        Logger.info(jobDescriptionForm.value().get().toString());
        return ok(createJobDescriptionFormStep2.render(jobDescriptionForm));
    }  

    /**
     * Handle the 'new job description form' submission 
     */
    public static Result saveJobDescription() {
        Form<JobDescription> jobDescriptionForm = form(JobDescription.class).bindFromRequest();
        if(jobDescriptionForm.hasErrors()) {
            return badRequest(createJobDescriptionForm.render(jobDescriptionForm));
        }
        jobDescriptionForm.get().save();
        flash("success", "JobDescription " + jobDescriptionForm.get().name + " has been created");
        return jobDescriptionList();
    }  

    public static Result editJobDescription(Long id) {
        Form<JobDescription> jobDescriptionForm = form(JobDescription.class).fill(
            JobDescription.find.byId(id)
        );
        return ok(
            editJobDescriptionForm.render(id, jobDescriptionForm)
        );
    }

    /**
     * Handle the 'edit form' submission 
     *
     * @param id Id of the computer to edit
     */
    public static Result updateJobDescription(Long id) {
        Form<JobDescription> form = form(JobDescription.class).bindFromRequest();
        if(form.hasErrors()) {
            return badRequest(editJobDescriptionForm.render(id, form));
        }
        form.get().update(id);

        flash("success", "Job description " + form.get().name + " has been updated");
        return jobDescriptionList();
    }


    public static Result deleteJobDescription(Long id) {
      return TODO;
    }

  public static Result javascriptRoutes() {
    response().setContentType("text/javascript");
    return ok(
      Routes.javascriptRouter("jsRoutes",
        // Routes
        controllers.routes.javascript.Application.logLines()
      )
    );
  }

  public static Result showJobs() {
    List<Job> jobs = Job.find.all();
        return ok(resultlist.render(jobs));
  }
}

