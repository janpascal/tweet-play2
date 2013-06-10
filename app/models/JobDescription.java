package models;

import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*; 

@Entity
public class JobDescription extends Model {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO, generator="jobs_desc_seq_gen")
    @SequenceGenerator(name="job_desc_seq_gen", sequenceName="JOB_DESC_SEQ")
    public Long id; 

    @Constraints.Required
    public String name;

    @OneToMany(cascade=CascadeType.ALL)
    private List<JobQuery> queries;
    @OneToMany(cascade=CascadeType.ALL)
    private List<JobOutput> outputs;
    
    public JobDescription() {
    }
    
    public void addQuery(String name, String query) {
        queries.add(new JobQuery(name, query));
    }
    
    public void addOutput(String name, Set<String> queryIds) {
        //outputs.put(name,  queryIds);
    }
    
    public void removeQuery(String name) {
        //queries.remove(name);
        //for(Map.Entry<String, Set<String>> e: outputs.entrySet()) {
        //    e.getValue().remove(name);
        //}
    }
    
    Set<String> getOutputs() {
        //return outputs.keySet();
        return new HashSet<String>();
    }

    public Set<String> getQueryNamesForOutput(String filename) {
        //return outputs.get(filename);
        return new HashSet<String>();
    }

    public Set<String> getQueryNames() {
        //return queries.keySet();
        return new HashSet<String>();
    }
    
    public String getQueryForName(String queryName) {
        //return queries.get(queryName);
        return "TODO";
    }
 
    public static Model.Finder<Long,JobDescription> find = 
        new Finder<Long,JobDescription>(Long.class, JobDescription.class);
}
