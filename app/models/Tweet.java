package models;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.*;
import play.db.ebean.*;

import twitter4j.Status;
import twitter4j.GeoLocation;

@Entity
public class Tweet extends Model {
  @Id
  public Long id;

   public Date date;
   public String fromUser;
   public Long fromUserId;
   public String inReplyTo;
   public String text;
   public Double latitude;
   public Double longitude;

  public Tweet(Status status) {
    this.id = status.getId();
    this.date = status.getCreatedAt();
    this.fromUser = status.getUser().getName();
    this.fromUserId = status.getUser().getId();
    this.inReplyTo = status.getInReplyToScreenName();
    this.text = status.getText();
    GeoLocation loc = status.getGeoLocation();
    if (loc != null) {
      this.latitude = loc.getLatitude();
      this.longitude = loc.getLongitude();
    }
  }

    public static Model.Finder<Long,Tweet> find = 
        new Finder<Long,Tweet>(Long.class, Tweet.class);

}
