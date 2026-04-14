// This file defines class "Synch". This class contains all the semaphores
// and variables needed to coordinate the instances of the Reader and Writer
// classes. It explicitly serves as the global blueprint for managing
// the Writer-Priority system using the classical "Passing the Baton" technique.

import java.util.concurrent.*;

public class Synch {

  // The 'mutex' semaphore (acting as a mutual exclusion lock) ensures that 
  // only one thread at a time can examine or modify the shared state variables below.
  // It is initialized to 1 (available) and uses 'true' for a fair FIFO queue policy.
  public static Semaphore mutex = new Semaphore(1, true);

  // The 'semReader' semaphore is used by Reader threads to wait patiently in the queue
  // when a Writer is currently reading/writing or if another Writer is already waiting.
  // Initialized to 0, meaning it blocks immediately upon 'acquire()' until someone signals it.
  public static Semaphore semReader = new Semaphore(0, true);

  // The 'semWriter' semaphore is used by Writer threads to wait patiently in the queue
  // if another thread (Reader or Writer) is currently inside the Critical Section.
  // Initialized to 0, blocking writers until the active thread exits and passes the baton.
  public static Semaphore semWriter = new Semaphore(0, true);
  
  // Tracks exactly how many Readers are currently inside the Critical Section together.
  // Because multiple readers can read simultaneously, this can be greater than 1.
  public static int activeReaders = 0;

  // Tracks exactly how many Writers are currently inside the Critical Section.
  // Because writers demand exclusive access, this value will never legitimately exceed 1.
  public static int activeWriters = 0;

  // Tracks the number of Readers that want to enter but are mathematically blocked
  // and are sleeping inside the 'semReader' semaphore queue.
  public static int waitingReaders = 0;

  // Tracks the number of Writers that want to enter but are mathematically blocked
  // and are sleeping inside the 'semWriter' semaphore queue.
  public static int waitingWriters = 0;

}  // end of class "Synch"
