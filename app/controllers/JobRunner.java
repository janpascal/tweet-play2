package controllers;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import play.*;

import models.*;
import tweet.Config;
import tweet.Main;

import twitter4j.conf.ConfigurationBuilder;

public class JobRunner {

    private static class JobTask implements Runnable {
        protected Job job;

        protected JobTask(Job job) {
            this.job = job;
        }

        public void run() {
            Main main = new Main();
            main.addLogger(new tweet.LogCallback() {
                public void log(String line) {
                    Logger.info(line); 
                    try {
                        job.addLogLine(line);
                    } catch (IOException e) {
                        Logger.info("Error writing log line ", e);
                    }
                }
            });
            main.addHandler(
                    new Main.Handler() {
                        public void handleNumber(int numTweets) {
                            job.numTweets += numTweets;
                            job.update();
                        }
                        public void handleStatus(boolean waiting, int secondsToWait) {
                            if(waiting) {
                                job.status = Job.STATUS_WAITING;
                                job.secondsToWait = secondsToWait;
                            } else {
                                job.status = Job.STATUS_RUNNING;
                                job.secondsToWait = 0;
                            }
                            job.update();
                        }
                    });
            try {
                File[] files = main.runConfig(Application.getTwitterConfiguration(), job.getConfig(), job.jobPath().toString());
                Logger.info("Adding result files to job");
                for(File f: files) {
                    job.addExcelResult(f);
                }
                job.status = Job.STATUS_DONE;
                job.update();
                job.closeLog();
                JobRunner.getInstance().jobFinished(job);
            } catch (Exception e) {
                job.status = Job.STATUS_FAILED;
                job.update();
                JobRunner.getInstance().jobFinished(job);
                Logger.info("Caught exception fetching tweets", e);
                try {
                    job.addLogLine("Caught exception fetching tweets");
                    job.addLogLine(e.toString());
                } catch (IOException e2) {
                    Logger.info("Caught IOException writing to job log", e);
                }
            }
        }
    }

    private static JobRunner instance = null;

    private ExecutorService pool = null;
    private Set<Long> running = null;

    private JobRunner() {
        pool = Executors.newCachedThreadPool();
        running = new HashSet<Long>();
    }

    public static JobRunner getInstance() {
        if (instance==null) instance = new JobRunner();
        return instance;
    }

    public void runJob(Job job) {
        running.add(job.id);
        pool.submit(new JobTask(job));
    }

    private void jobFinished(Job job) {
        running.remove(job.id);
    }

    public boolean isJobRunning(Job job) {
        return running.contains(job.id);
    }

    // Clean up all jobs in the database that are marked as running, 
    // but that are not in this jobrunner. Probably they failed
    // because of an application restart
    public static void cleanup() {
        for(Job job: Job.find.all()) {
            if (job.status==null) continue; // legacy
            if (job.status==Job.STATUS_RUNNING ||
                    job.status==Job.STATUS_WAITING) {
                if (! getInstance().isJobRunning(job)) {
                    job.status = Job.STATUS_FAILED;
                    job.update();
                }
            }
        }
    }

}

