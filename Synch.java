

import java.util.concurrent.*;

public class Synch {
  public static Semaphore mutex = new Semaphore(1, true);
  public static Semaphore semReader = new Semaphore(0, true);
  public static Semaphore semWriter = new Semaphore(0, true);
  public static int activeReaders = 0;
  public static int activeWriters = 0;
  public static int waitingReaders = 0;
  public static int waitingWriters = 0;

}  
