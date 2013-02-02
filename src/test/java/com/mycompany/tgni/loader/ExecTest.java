package com.mycompany.tgni.loader;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

/**
 * TODO: class level javadocs
 */
public class ExecTest {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  
  private static final int NUM_WORKERS = Runtime.getRuntime().availableProcessors();
  private static final long TASK_TIMEOUT_MILLIS = 1000L;
  private static final int NUM_TASKS = 1000;
  
  @Test
  public void testExec() throws Exception {
    init();
    // seed input queue
    final BlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();
    for (int i = 0; i < NUM_TASKS; i++) {
      queue.put(i);
    }
    // add poison pills
    for (int i = 0; i < NUM_WORKERS; i++) {
      queue.put(-1);
    }
    // set up worker threads
    ExecutorService workerPool = Executors.newFixedThreadPool(NUM_WORKERS);
    CountDownLatch latch = new CountDownLatch(NUM_WORKERS);
    for (int i = 0; i < NUM_WORKERS; i++) {
      Worker worker = new Worker(i, latch, queue);
      workerPool.execute(worker);
    }
    try {
      latch.await();
    } catch (InterruptedException e) { /* NOOP */ }
    workerPool.shutdown();
    destroy();
  }
  
  private void init() {
    logger.info("LC: Manager init");
  }

  private void destroy() {
    logger.info("LC: Manager destroy");
  }

  private class Worker implements Runnable {

    private int workerId;
    private CountDownLatch latch;
    private BlockingQueue<Integer> queue;
    private AtomicInteger count;
    private int totalTasks;
    
    public Worker(int workerId, CountDownLatch latch, 
        BlockingQueue<Integer> queue) {
      this.workerId = workerId;
      this.latch = latch;
      this.queue = queue;
      this.count = new AtomicInteger(0);
      this.totalTasks = queue.size();
    }
    
    @Override
    public void run() {
      StopWatch watch = new StopWatch();
      try {
        initWorker();
        ExecutorService taskExec = Executors.newSingleThreadExecutor();
        for (;;) {
          watch.start("point 1");
          Integer oid = queue.take();
          if (oid < 0) {
            break;
          }
          int curr = count.incrementAndGet();
          if (curr % 100 == 0) {
            logger.info("Worker " + workerId + " processed (" + curr + 
              "/" + totalTasks + ") OIDs");
          }
          watch.stop();
          watch.start("point 2");
          logger.info("oid=" + oid);
          Task task = new Task(oid);
          Future<Integer> result = taskExec.submit(task);
          try {
            result.get(TASK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
          } catch (ExecutionException e) {
            logger.warn("Task (OID:" + oid + ") skipped", e);
            reinitWorker();
            watch.stop();
            continue;
          } catch (TimeoutException e) {
            result.cancel(true);
            logger.warn("Task (OID:" + oid + ") timed out", e);
            reinitWorker();
            watch.stop();
            continue;
          }
          watch.stop();
        }
      } catch (InterruptedException e) {
        logger.error("Worker:" + workerId + " Interrupted", e);
      } finally {
        destroyWorker();
        latch.countDown();
      }
      System.out.println(watch.prettyPrint());
    }
    
    private void reinitWorker() {
      logger.info("LC: Worker:" + workerId + " reinit resources");
    }
    
    private void initWorker() {
      logger.info("LC: Worker:" + workerId + " init");
    }
    
    private void destroyWorker() {
      logger.info("LC: Worker:" + workerId + " destroy");
    }
  }
    
  private class Task implements Callable<Integer> {

    private int oid;
    
    public Task(Integer oid) {
      this.oid = oid;
    }
    
    @Override
    public Integer call() throws Exception {
      if (oid % 50 == 0) {
        Thread.sleep(2000L);
      } else {
        Thread.sleep(10L);
      }
      logger.info("Executed task (OID:" + oid + ")");
      return oid;
    }
  }
}
