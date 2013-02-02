package com.mycompany.tgni.utils;

import org.springframework.util.StopWatch;

/**
 * Holder for Spring StopWatch so classes can refer to it
 * statically.
 */
public class StopwatchHolder {

  private static StopwatchHolder holder = new StopwatchHolder();
  private static StopWatch instance;
  
  private StopwatchHolder() {
    instance = new StopWatch();
  }
  
  public static StopWatch instance() {
    return instance;
  }
  
  public static void reset() {
    if (instance.isRunning()) {
      instance.stop();
    }
    instance = new StopWatch();
  }
}
