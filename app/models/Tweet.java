package models;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.*;
import play.db.ebean.*;

import twitter4j.Status;
import twitter4j.GeoLocation;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.GeocoderStatus;
import com.google.code.geocoder.model.LatLng;

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

   public Boolean conformsToTerms;

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

  public String location() {
     if (latitude!= null && longitude != null) {
        Geocoder geocoder = new Geocoder();
          GeocoderRequest request = new GeocoderRequest();
          request.setLocation(new LatLng(Double.toString(latitude), Double.toString(longitude))); 
          request.setLanguage("nl");
          GeocodeResponse response = geocoder.geocode(request);
          if(response.getStatus().equals(GeocoderStatus.OK) && !response.getResults().isEmpty()) {
              GeocoderResult result = response.getResults().iterator().next();
              return result.getFormattedAddress(); 
          }
    } 
    return null;
  }

    public static Model.Finder<Long,Tweet> find = 
        new Finder<Long,Tweet>(Long.class, Tweet.class);

}
