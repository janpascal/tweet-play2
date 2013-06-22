package controllers;

import java.io.File;

import play.*;
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

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public static Result selectConfig() {
        return ok(selectconfig.render());
    }

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
            Config config = new Config(file);
            File[] files = Main.runConfig(config);
            File f = files[0];
            response().setContentType("application/x-download");  
            response().setHeader("Content-disposition","attachment; filename="+f.getName()); 
            return ok(f);
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
        System.out.println(jobDescriptionForm.toString());
        System.out.println(jobDescriptionForm.data());
        System.out.println(jobDescriptionForm.value().get());
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

}

