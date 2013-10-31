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
import java.nio.file.SimpleFileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;

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

    public Long numTweets;

    public Integer status;

    public static final int STATUS_INIT = 0;
    public static final int STATUS_RUNNING = 1;
    public static final int STATUS_WAITING = 2;
    public static final int STATUS_FAILED = 3;
    public static final int STATUS_DONE = 4;

    public Integer secondsToWait;

    @Transient
    private Path inifile;
    @Transient
    private List<Path> xls;
    @Transient
    private Path zipfile;
    @Transient
    private Path logfile;
    @Transient
    private PrintWriter logwriter;
    @Transient
    private boolean loaded = false;
    @Transient
    private Config config;

    public Job() {
        xls = null;
        loaded = false;
        zipfile = null;
        inifile = null;
        logfile = null;
        status = STATUS_INIT;
        numTweets = 0L;
        secondsToWait = 0;
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
        if(xls==null) {
            xls = new ArrayList<Path>();
            try {
                File dir = jobPath().toFile();
                FilenameFilter filter = new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".xls");
                    }
                };
                for( File f: dir.listFiles(filter)) {
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

        for(Path f: getResults()) { 
            addToZip(out, f);
        }
        addToZip(out, getInifile());
        addToZip(out, getLogfile());
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

    public Path getLogfile() throws IOException {
        if (logfile!=null) return logfile;
        logfile = jobPath().resolve("log.txt");
        return logfile;
    }

    private PrintWriter getLogWriter() throws IOException {
        if (logwriter!=null) return logwriter;
        Path path = getLogfile();
        logwriter = new PrintWriter(path.toFile());
        return logwriter;
    }

    public void addLogLine(String line) throws IOException {
        getLogWriter().println(line);
        getLogWriter().flush();
    }

    public void closeLog() throws IOException {
        getLogWriter().flush();
        getLogWriter().close();
        logwriter = null;
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

    public void remove() throws IOException {
        Path start= jobPath();
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
        {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException
        {
            if (e == null) {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            } else {
                // directory iteration failed
                throw e;
            }
        }
        });
        super.delete();
    }

    public String statusString() {
        if(status==null) return "Unknown";
        switch (status) {
            case STATUS_INIT: return "Starting";
            case STATUS_RUNNING: return "Running";
            case STATUS_WAITING: return "Waiting ("+secondsToWait+"s)";
            case STATUS_FAILED: return "Failed";
            case STATUS_DONE: return "Done";
            default: return "Unknown";
        }
    }

    public static Model.Finder<Long,Job> find = 
        new Finder<Long,Job>(Long.class, Job.class);
}
