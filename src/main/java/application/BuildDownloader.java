package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import application.model.BuildInfo;

/**
 * Responsible for running several threads that download data about the builds
 * we're processing.
 * 
 * @author Sid Botvin
 */
public class BuildDownloader {

    // ----------------------------------------------
    //
    // Fields
    //
    // ----------------------------------------------

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private BlockingQueue<BuildInfo> workQueue;
    private BlockingQueue<Map.Entry<BuildInfo, Document>> resultQueue;

    private final int workLoad;
    private volatile int workDone = 0;

    // ----------------------------------------------
    //
    // Constructor
    //
    // ----------------------------------------------

    /**
     * Creates a new {@link BuildDownloader}.
     * 
     * @param numOfWorkers
     *            The amount of threads to use for this worker.
     * @param workLoad
     *            The amount of {@link BuildInfo} instances we're expected to
     *            process.
     */
    public BuildDownloader(int numOfWorkers, int workLoad) {
        this.workLoad = workLoad;

        workQueue = new ArrayBlockingQueue<>(workLoad);
        resultQueue = new ArrayBlockingQueue<>(workLoad);

        for (int i = 0; i < numOfWorkers; i++) {
            executorService.execute(new Worker(workQueue, resultQueue));
        }
    }

    // ----------------------------------------------
    //
    // Public API
    //
    // ----------------------------------------------

    /**
     * Queues a new {@link BuildInfo} to be processed.
     */
    public void queueWork(BuildInfo buildInfo) {
        try {
            workQueue.put(buildInfo);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Returns an entry from the internal queue of finished work. Blocks if the
     * internal queue is empty.
     */
    public Map.Entry<BuildInfo, Document> getResult() {
        try {
            return resultQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if there's still work to process or results to be fetched.
     */
    public boolean hasWork() {
        if (workDone != workLoad || !resultQueue.isEmpty()) {
            return true;
        } else {
            executorService.shutdownNow();
            return false;
        }
    }

    /**
     * Stops and aborts all current work.
     */
    public void cancelWork() {
        executorService.shutdownNow();
    }

    // ----------------------------------------------
    //
    // Inner class
    //
    // ----------------------------------------------

    /**
     * Worker class for performing the downloading.
     */
    private final class Worker implements Runnable {

        // ----------------------------------------------
        //
        // Fields
        //
        // ----------------------------------------------

        private final BlockingQueue<BuildInfo> workQueue;
        private BlockingQueue<Map.Entry<BuildInfo, Document>> resultQueue;

        // ----------------------------------------------
        //
        // Constructor
        //
        // ----------------------------------------------

        public Worker(BlockingQueue<BuildInfo> workQueue,
                BlockingQueue<Map.Entry<BuildInfo, Document>> resultQueue) {
            this.workQueue = workQueue;
            this.resultQueue = resultQueue;
        }

        // ----------------------------------------------
        //
        // run
        //
        // ----------------------------------------------

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {

                try {

                    BuildInfo buildInfo = workQueue.take();
                    String html = downloadHtml(buildInfo.getBuildUrl());

                    @SuppressWarnings("serial")
                    AbstractMap.SimpleEntry<BuildInfo, Document> mapEntry = new AbstractMap.SimpleEntry<BuildInfo, Document>(
                            buildInfo, Jsoup.parse(html.toString())) {
                    };

                    resultQueue.put(mapEntry);
                    workDone++;

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

            }
        }

        // ----------------------------------------------
        //
        // Private API
        //
        // ----------------------------------------------

        /**
         * Downloads the HTML form the given {@link URL}.
         */
        private String downloadHtml(URL url) {
            try {

                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(url.openStream(), "UTF-8"));

                StringBuilder html = new StringBuilder();
                bufferedReader.lines().forEach(html::append);
                bufferedReader.close();

                return html.toString();

            } catch (IOException e) {
                throw new IllegalStateException("Could not fetch the document", e);
            }
        }
    }

}
