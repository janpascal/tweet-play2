package models;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.*;
import play.*;
import play.db.ebean.*;
import play.libs.Json;

import twitter4j.Status;
import twitter4j.GeoLocation;

import org.codehaus.jackson.JsonNode;

@Entity
public class StreamConfig extends Model {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO, generator="streamconfig_seq_gen")
    @SequenceGenerator(name="streamconfig_seq_gen", sequenceName="STREAMCONFIG_SEQ")
    public Long id;

    // JSON-encoded list
    public String terms;

    public StreamConfig(String[] terms) {
        JsonNode result = Json.toJson(terms);
        this.terms = Json.stringify(result);
        Logger.info("StreamConfig set json string: " + this.terms);
    }

    public List<String> listTerms() {
        JsonNode node = Json.parse(terms);
        List<String> result = new ArrayList<String>(node.size());
        for(int i=0; i<node.size(); i++) {
            result.add(node.get(i).getTextValue());
        }
        return result;
    }

    public String listTermsAsString() {
        JsonNode node = Json.parse(terms);
        StringBuilder result = new StringBuilder();
        for(int i=0; i<node.size(); i++) {
            if(i>0) result.append(", ");
            result.append(node.get(i).getTextValue());
        }
        return result.toString();
    }

    public void putTerms(List<String> terms) {
        JsonNode result = Json.toJson(terms);
        this.terms = Json.stringify(result);
        Logger.info("StreamConfig set json string: " + this.terms);
    }

    public static Model.Finder<Long,StreamConfig> find = 
        new Finder<Long,StreamConfig>(Long.class, StreamConfig.class);

}
