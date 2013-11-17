package models;

import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*; 

@Entity
public class JobOutput extends Model {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO, generator="jobs_output_seq_gen")
    @SequenceGenerator(name="job_output_seq_gen", sequenceName="JOB_OUTPUT_SEQ")
    public Long Id;
    @Constraints.Required
    public String filename;
    @ManyToMany(mappedBy="outputs")
    public List<JobQuery> queries;

    public JobOutput(String filename, List<JobQuery> queries) {
      this.filename = filename;
      this.queries = queries;
    }

    public boolean usesQuery(Long queryId) {
      return false;
      // TODO
    }

    public static Finder<Long,JobOutput> find = 
        new Finder<>(Long.class, JobOutput.class); 
}
