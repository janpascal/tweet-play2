package models;

import java.util.*;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import javax.persistence.*;
import org.apache.commons.configuration.ConfigurationException;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.FileSystems;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;
import play.Logger;
import play.Play;

import com.avaje.ebean.*; 

import tweet.Config;

@Entity
public class Job extends Model {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO, generator="job_seq_gen")
    @SequenceGenerator(name="job_seq_gen", sequenceName="JOB_SEQ")
    public Long id; 

    @Constraints.Required
    public String name;

    public Date datum;

    @Transient
    private Path inifile;
    @Transient
    private List<Path> xls;
    @Transient
    private Path zipfile;
    @Transient
    private boolean loaded = false;
    @Transient
    private Config config;

    public Job() {
      xls = null;
      loaded = false;
      zipfile = null;
      inifile = null;
      datum = new Date();
    }

    public void addConfig(File config) throws IOException, ConfigurationException  {
        Path configPath = config.toPath();
        this.inifile = jobPath().resolve("config.ini");
        Files.copy(configPath, this.inifile);
        this.config = new tweet.Config(config);
    }

    public List<String> getQueries() {
      List<String> result = new ArrayList<String>();
      try {
          Config config = getConfig();
          for( String key: config.getQueryNames() ) {
            result.add(config.getQueryForName(key));
          }
      } catch (Exception e) {
          Logger.info("IO Exception getting config", e);
      }
      return result;
    }

    public List<Path> getResults() {
      Logger.info("Getting results for job "+id);
      if(xls==null) {
          Logger.info("Fetching results");
          xls = new ArrayList<Path>();
          try {
              File dir = jobPath().toFile();
              Logger.info("Looking in dir "+dir.toString());
              FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                  Logger.info("Checking file "+name);
                  return name.endsWith(".xls");
                }
              };
              for( File f: dir.listFiles(filter)) {
                Logger.info("Adding file "+f.getPath());
                xls.add(f.toPath());
              }
          } catch (IOException e) {
              Logger.info("IO Exception getting config", e);
          }

      }
      return xls;
    }

    public void addExcelResult(File excel) {
      if (xls==null) xls = new ArrayList<Path>();
      xls.add(excel.toPath());
    }

    public Path getZip() throws FileNotFoundException, IOException {
       if (zipfile!=null) return zipfile;

       Path zipfile = jobPath().resolve("results.zip");

       ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zipfile));

       for(Path f: xls) { 
         addToZip(out, f);
      }
      addToZip(out, inifile);
      out.close();
      loaded = true;
      return zipfile;
    }

    public tweet.Config getConfig() throws IOException, ConfigurationException {
      if (config!=null) return config;
      Path ini = getInifile();
      this.config = new Config(ini.toFile());
      return this.config;
    }

    public Path getInifile() throws IOException {
      if (inifile!=null) return inifile;
      inifile = jobPath().resolve("config.ini");
      return inifile;
    }

    public void addToZip(ZipOutputStream out, Path f) throws IOException {
         out.putNextEntry(new ZipEntry(f.getFileName().toString())); 
         InputStream in = Files.newInputStream(f);
         byte[] b = new byte[1024];
         int count;

         while ((count = in.read(b)) > 0) {
            out.write(b, 0, count);
         }
         in.close();
         out.closeEntry();
    }


    public Path jobPath() throws IOException {
        String basepath = Play.application().configuration().getString("tweet.basepath");
        Path path = FileSystems.getDefault().getPath(basepath).resolve(""+id);
        Files.createDirectories(path);
        return path;
    }
 
    public static Model.Finder<Long,Job> find = 
        new Finder<Long,Job>(Long.class, Job.class);
}
