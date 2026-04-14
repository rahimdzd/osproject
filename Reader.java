// This file defines class "Reader".
// Reader threads attempt to access a shared resource (the Critical Section).
// In this simulation, Writers have priority. If a Writer is waiting, a new Reader
// must wait, even if other Readers are currently reading.

public class Reader extends Thread {
  int myName;  // The variable myName stores the numerical identity of this thread (e.g., Reader 1, Reader 2).
  RandomSleep rSleep;  // Helper object to provide randomized sleep delays

  public Reader(int name) {
    myName = name;  
    rSleep = new RandomSleep();  
  }  

  public void run () {
    // Initial graphical spawn state: Set the process to sit in the "Thinking" field globally.
    GUI.setState(true, myName, "THINKING");
    
    // An initial long random sleep simulating the process "doing something else" before it ever tries to read.
    rSleep.doSleep(1000, 2500);

    // Each thread loops 5 times in its lifetime trying to enter the Critical Section.
    for (int I = 0;  I < 5; I++) {
      
      // Step 1: The Reader now desires access to read the database.
      // Update the GUI to physically move this thread into the "Waiting Queue".
      GUI.setState(true, myName, "WAITING");
      System.out.println("Reader " + myName + " wants to read.");
      
      // We enforce a distinct waiting time visually so you can observe the queue building up.
      rSleep.doSleep(1000, 2500); 

      // ----------------- ENTRY PROTOCOL -----------------
      // Before inspecting or modifying the shared counters, we explicitly acquire the global mutex
      // to guarantee mutual exclusion (preventing race conditions).
      try {
          Synch.mutex.acquire();
      } catch (Exception e) { return; }

      // Writer-Priority Logic: A Reader must wait if there are ANY Writers currently active
      // OR if there are ANY Writers waiting to be active.
      if (Synch.activeWriters > 0 || Synch.waitingWriters > 0) {
          // Since it cannot enter, it registers itself as a purely waiting reader.
          Synch.waitingReaders++;
          // It MUST release the mutex before going to sleep, otherwise standard deadlock occurs.
          Synch.mutex.release();
          
          try {
              // The Reader goes to sleep here. It waits patiently in the semReader queue
              // until a leaving Writer explicitly passes the baton and wakes it up.
              Synch.semReader.acquire();
          } catch (Exception e) { return; }
          // Because of the 'Passing the Baton' pattern, when it wakes up here, it has ALREADY
          // bypassed the mutex and its state variables have already been updated for it!
      } else {
          // If no writers are active or waiting, the Reader is free to dive into the Critical Section!
          Synch.activeReaders++;
          // It releases the mutex to allow other innocent processes to queue up or join.
          Synch.mutex.release();
      }
      
      // ----------------- CRITICAL SECTION -----------------
      // Step 2: The Reader is successfully inside!
      // Update GUI to move this thread physically into the "Critical Section" box.
      GUI.setState(true, myName, "ACTIVE");
      System.out.println("Reader " + myName + " is now reading.");

      // Simulate the time physically taken for reading the shared database.
      // Multiple readers might be sitting in this delay simultaneously!
      rSleep.doSleep(1500, 3000);

      // We're finished reading. If the thread was interrupted (e.g. by Restart button), terminate nicely.
      if (Thread.currentThread().isInterrupted()) return;

      // ----------------- EXIT PROTOCOL -----------------
      // We need to carefully update shared counters to declare we are leaving. Acquire mutex!
      try {
          Synch.mutex.acquire();
      } catch (Exception e) { return; }

      // This reader exits the critical section.
      Synch.activeReaders--;
      
      // If this was the VERY LAST Reader to leave, and a Writer is patiently waiting outside:
      if (Synch.activeReaders == 0 && Synch.waitingWriters > 0) {
          // Transition the waiting writer into the active state on its behalf.
          Synch.waitingWriters--;
          Synch.activeWriters++;
          
          // "Pass the baton" by unblocking EXACTLY one waiting writer.
          Synch.semWriter.release();
      }
      
      System.out.println("Reader " + myName + " is finished reading.");
      // ALWAYS release the mutex when exiting the block so the system doesn't freeze permanently!
      Synch.mutex.release();

      // Step 3: The Reader is now finished with the database and returns to idle activities.
      // Update the GUI to physically jump this process back into the "Thinking" box.
      GUI.setState(true, myName, "THINKING");

      // Simulate "doing something else" before starting the loop all over again.
      if (Thread.currentThread().isInterrupted()) return;
      rSleep.doSleep(1000, 2500);
      if (Thread.currentThread().isInterrupted()) return;
    } 
  }  
}
