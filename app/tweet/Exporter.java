package tweet;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.GeocoderStatus;
import com.google.code.geocoder.model.LatLng;

import twitter4j.GeoLocation;
import twitter4j.Status;

public class Exporter {
    private HashSet<Long> ids;
    private Workbook wb;
    private Sheet sheet;
    private CreationHelper createHelper;
    private CellStyle dateCellStyle;
    private DateFormat df;
    private int currentRow;
    
    protected String filename;
    private Geocoder geocoder;
    private CellStyle idCellStyle;

    private static final String[] columns = { 
        "id",
        "created_at",
        "from_user",
        "in_reply_to",
        "text",
        "from_user_id",
        "from_user_name",
        "geo",
        "geo_text",
        "profile_image_url_https",
        "iso_language_code",
        "to_user_name",
        "to_user_id",
        "to_user_id_str",
        "source",
        "from_user_id_str",
        "id_str",
        "profile_image_url",
        "metadata"};


    public Exporter(String filename) {
        this.filename = filename;
        this.geocoder = new Geocoder();
        
        ids = new HashSet<Long>();
        wb = new HSSFWorkbook();
        sheet = wb.createSheet("tweets");
        createHelper = wb.getCreationHelper();

        Row row = sheet.createRow((short)0);
        int colnr = 0;
        for (String col: columns) {
            row.createCell(colnr).setCellValue(col);
            colnr++;
        }

        dateCellStyle = wb.createCellStyle();
        dateCellStyle.setDataFormat(
            createHelper.createDataFormat().getFormat("d/m/yy h:mm:ss"));

        df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");

        DataFormat format = wb.createDataFormat();
        idCellStyle = wb.createCellStyle();
        idCellStyle.setDataFormat(format.getFormat("0"));
 
        currentRow = 1;
    }
    
    public void addTweet(SimpleTweet tweet) {
            if (ids.contains(tweet.id)) {
              return;
            }
            Row row = sheet.createRow((short)currentRow);
            int colnr=0;
            for (String col: columns) {
              Cell cell = row.createCell(colnr);
              
              switch (col) {
              case "id": cell.setCellType(HSSFCell.CELL_TYPE_STRING); cell.setCellValue(Long.toString(tweet.id)); break;
              case "created_at":  cell.setCellValue(tweet.createdAt); cell.setCellStyle(dateCellStyle); break;
              case "from_user": cell.setCellValue(tweet.userName); break;
              case "from_user_id": cell.setCellValue(tweet.userId); cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC); break;
              case "in_reply_to": cell.setCellValue(tweet.inReplyToName); break;
              case "text": cell.setCellValue(tweet.text); break;
              case "geo": if (tweet.latitude != null && tweet.longitude != null) { 
                    cell.setCellValue(tweet.latitude+","+tweet.longitude); 
                  }
                  break;
              case "geo_text": 
                           if (tweet.latitude!= null && tweet.longitude != null) {
                                    GeocoderRequest request = new GeocoderRequest();
                                    request.setLocation(new
                                    LatLng(Double.toString(tweet.latitude),
                                    Double.toString(tweet.longitude))); 
                                    request.setLanguage("nl");
                                    GeocodeResponse response = geocoder.geocode(request);
                                    if(response.getStatus().equals(GeocoderStatus.OK) && !response.getResults().isEmpty()) {
                                    GeocoderResult result = response.getResults().iterator().next();
                                    cell.setCellValue(result.getFormattedAddress()); 
                                    }
                            } 
                            break;
              }
              
              colnr++;
            }
            ids.add(tweet.id);
            currentRow++;
        }

    public void addTweet(Status tweet) {
        SimpleTweet simple = new SimpleTweet();
        simple.id = tweet.getId();
        simple.createdAt = tweet.getCreatedAt();
        simple.userName = tweet.getUser().getName(); 
        simple.userId = tweet.getUser().getId(); 
        simple.inReplyToName = tweet.getInReplyToScreenName(); 
        simple.text = tweet.getText(); 
        GeoLocation geo = tweet.getGeoLocation(); 
        if (geo != null) {
            simple.latitude = geo.getLatitude();
            simple.latitude = geo.getLongitude(); 
        } 
        addTweet(simple);
    }

    public void write(String filename) throws FileNotFoundException {
        write(new FileOutputStream(filename));
    }
    
    public void write(OutputStream out) {
        try {
            wb.write(out);
            out.close();
          } catch (IOException e) {
                    e.printStackTrace();
          }
    }

    public void write() throws FileNotFoundException {
        write(filename);
    }
    
    public String getFilename() {
        return filename;
    }
}
