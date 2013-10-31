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
import java.sql.Timestamp;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;
import play.Logger;
import play.Play;

import com.avaje.ebean.*; 
import com.avaje.ebean.annotation.CreatedTimestamp;

@Entity
public class LogLine extends Model {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO, generator="log_line_seq_gen")
    @SequenceGenerator(name="log_line_seq_gen", sequenceName="LOG_LINE_SEQ")
    public Long id; 

    @Constraints.Required
    public Job job;

    @CreatedTimestamp
    public Timestamp timestamp;

    @Constraints.Required
    public String logline;

    public LogLine(Job job, String logline) {
        this.job = job;
        this.logline = logline;
    }

    public static Model.Finder<Long,LogLine> find = 
        new Finder<Long,LogLine>(Long.class, LogLine.class);
}
