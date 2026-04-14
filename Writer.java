// This file defines class "Writer".
// Writer threads attempt to exclusively access a shared resource (the Critical Section).
// In this simulation, Writers have absolute priority. They must be given the section
// immediately if there are any waiting, blocking any new readers from queuing up.

public class Writer extends Thread {
  int myName;  // Stores the numerical identity of this thread (e.g., Writer 1, Writer 2).
  RandomSleep rSleep;  

  public Writer(int name) {
    myName = name;  
    rSleep = new RandomSleep();  
  }  

  public void run () {
    // Initial graphical spawn state: Set the process to sit in the "Thinking" field globally.
    GUI.setState(false, myName, "THINKING");
    
    // An initial long random sleep simulating the process producing data independently before writing.
    rSleep.doSleep(1000, 2500);

    // Each thread loops 5 times in its lifetime trying to write sequentially.
    for (int I = 0;  I < 5; I++) {
      
      // Step 1: The Writer now desires exclusive access to write to the database.
      // Update GUI to transition physically into the "Waiting Queue" section.
      GUI.setState(false, myName, "WAITING");
      System.out.println("Writer " + myName + " wants to write");
      
      // Mandatory sleep to ensure the viewer clearly sees the writer entering the queue.
      rSleep.doSleep(1000, 2500);

      // ----------------- ENTRY PROTOCOL -----------------
      // Always acquire the global mutex lock first before inspecting safe shared states.
      try {
          Synch.mutex.acquire();
      } catch (Exception e) { return; }

      // A Writer demands completely exclusive access. Because of this, it MUST wait
      // if there are ANY readers currently active, OR if another writer is already active.
      if (Synch.activeReaders > 0 || Synch.activeWriters > 0) {
          // Increment the writer waiting tally. This automatically blocks new readers from joining!
          Synch.waitingWriters++;
          // Always release the mutex before sleeping!
          Synch.mutex.release();
          
          try {
              // Sleep on the semWriter queue. Another exiting process will manually wake us.
              Synch.semWriter.acquire();
          } catch (Exception e) { return; }
          // Because of 'Passing the Baton', when it wakes up, it theoretically bypasses the mutex
          // and assumes ownership of the Critical Section immediately!
      } else {
          // If the section is completely completely empty, the Writer takes ownership.
          Synch.activeWriters++;
          // Release the mutex.
          Synch.mutex.release();
      }

      // ----------------- CRITICAL SECTION -----------------
      // Step 2: The Writer has successfully gained exclusive ownership of the database!
      // Update the GUI to physically jump this thread into the "Critical Section" box.
      GUI.setState(false, myName, "ACTIVE");
      System.out.println("Writer " + myName + " is now writing");

      // Simulate the substantial time naturally taken to write/modify the database securely.
      // No other readers OR writers will be inside while this occurs!
      rSleep.doSleep(1500, 3000);

      // We're done writing.
      if (Thread.currentThread().isInterrupted()) return;

      // ----------------- EXIT PROTOCOL -----------------
      // We are leaving, so we need to coordinate updating states safely with the mutex.
      try {
          Synch.mutex.acquire();
      } catch (Exception e) { return; }

      // Relinquish ownership of the section physically.
      Synch.activeWriters--;

      // Priority Logic: Check if there's ANOTHER Writer waiting first.
      if (Synch.waitingWriters > 0) {
          // Prompts the waiting writer, transitioning it to the active field for it.
          Synch.waitingWriters--;
          Synch.activeWriters++;
          
          // "Pass the baton" over to the exact next waiting writer by releasing one semWriter lock.
          Synch.semWriter.release();
          
      // Next Priority check: If no writers exist, check if there are ANY abandoned Readers waiting!
      } else if (Synch.waitingReaders > 0) {
          // Since Readers can simultaneously read together, we want to unlock ALL of them!
          int readersToWake = Synch.waitingReaders;
          // Shovel all of them directly into the active field for them mathematically.
          Synch.activeReaders += Synch.waitingReaders;
          Synch.waitingReaders = 0;
          
          // Iteratively "Pass the baton" to every single currently waiting reader so they wake up!
          for (int i = 0; i < readersToWake; i++) {
              Synch.semReader.release();
          }
      }
      
      System.out.println("Writer " + myName + " is finished writing");
      // Always release the mutex for the wild when done parsing the exit protocol unconditionally.
      Synch.mutex.release();

      // Step 3: Writer has returned to independently building new data blocks.
      // Update GUI to leap back into the Idle/Thinking area natively.
      GUI.setState(false, myName, "THINKING");

      // Sleep safely offline without occupying any queues or critical sections.
      if (Thread.currentThread().isInterrupted()) return;
      rSleep.doSleep(1000, 2500);
      if (Thread.currentThread().isInterrupted()) return;
    } 
  }  
}
