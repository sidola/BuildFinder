package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import application.model.BuildInfo;
import javafx.util.Pair;

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
    private BlockingQueue<ResultItem<Pair<BuildInfo, Document>>> resultQueue;

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
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns an entry from the internal queue of finished work. Blocks if the
     * internal queue is empty.
     */
    public ResultItem<Pair<BuildInfo, Document>> getResult() {
        try {
            return resultQueue.take();
        } catch (InterruptedException e) {
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
        private BlockingQueue<ResultItem<Pair<BuildInfo, Document>>> resultQueue;

        // ----------------------------------------------
        //
        // Constructor
        //
        // ----------------------------------------------

        public Worker(BlockingQueue<BuildInfo> workQueue,
                BlockingQueue<ResultItem<Pair<BuildInfo, Document>>> resultQueue) {
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

                BuildInfo buildInfo = null;

                try {
                    buildInfo = workQueue.take();
                } catch (InterruptedException e) {
                    resultQueue.add(new ResultItem<>(e));

                    workDone++;
                    continue;
                }

                try {

                    String html = downloadHtml(buildInfo.getBuildUrl());

                    Pair<BuildInfo, Document> result = new Pair<BuildInfo, Document>(
                            buildInfo, Jsoup.parse(html.toString()));

                    resultQueue.add(new ResultItem<>(result));

                } catch (IOException e) {

                    Pair<BuildInfo, Document> result = new Pair<BuildInfo, Document>(
                            buildInfo, null);

                    resultQueue.add(new ResultItem<Pair<BuildInfo, Document>>(result, e));

                }

                workDone++;

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
        private String downloadHtml(URL url) throws IOException {
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(url.openStream(), "UTF-8"));

            StringBuilder html = new StringBuilder();
            bufferedReader.lines().forEach(html::append);
            bufferedReader.close();

            return html.toString();
        }
    }

}
