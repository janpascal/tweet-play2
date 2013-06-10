package models;

import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*; 

@Entity
public class JobQuery extends Model {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO, generator="jobs_query_seq_gen")
    @SequenceGenerator(name="job_query_seq_gen", sequenceName="JOB_QUERY_SEQ")
    public Long id;
    @Constraints.Required
    public String name;
    public String terms;
    @ManyToMany
    public List<JobOutput> outputs;

    public JobQuery(String name, String terms) {
      this.name = name;
      this.terms = terms;
    }

}
