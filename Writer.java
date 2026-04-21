

public class Writer extends Thread {
  int myName; 
  RandomSleep rSleep;  

  public Writer(int name) {
    myName = name;  
    rSleep = new RandomSleep();  
  }  

  public void run () {
    GUI.setState(false, myName, "THINKING");
    
  
    rSleep.doSleep(1000, 2500);

    for (int I = 0;  I < 5; I++) {
  
      GUI.setState(false, myName, "WAITING");
      System.out.println("Writer " + myName + " wants to write");
      
 
      rSleep.doSleep(1000, 2500);

      try {
          Synch.mutex.acquire();
      } catch (Exception e) { return; }

      if (Synch.activeReaders > 0 || Synch.activeWriters > 0) {
         
          Synch.waitingWriters++;
      
          Synch.mutex.release();
          
          try {
             
              Synch.semWriter.acquire();
          } catch (Exception e) { return; }
      
      } else {
        
          Synch.activeWriters++;
        
          Synch.mutex.release();
      }

   
      GUI.setState(false, myName, "ACTIVE");
      System.out.println("Writer " + myName + " is now writing");

      rSleep.doSleep(1500, 3000);

   
      if (Thread.currentThread().isInterrupted()) return;

    
      try {
          Synch.mutex.acquire();
      } catch (Exception e) { return; }

      Synch.activeWriters--;

      if (Synch.waitingWriters > 0) {
        
          Synch.waitingWriters--;
          Synch.activeWriters++;
          
          
          Synch.semWriter.release();
          
    
      } else if (Synch.waitingReaders > 0) {
          
          int readersToWake = Synch.waitingReaders;
         
          Synch.activeReaders += Synch.waitingReaders;
          Synch.waitingReaders = 0;
         
          for (int i = 0; i < readersToWake; i++) {
              Synch.semReader.release();
          }
      }
      
      System.out.println("Writer " + myName + " is finished writing");
     
      Synch.mutex.release();

     
      GUI.setState(false, myName, "THINKING");

    
      if (Thread.currentThread().isInterrupted()) return;
      rSleep.doSleep(1000, 2500);
      if (Thread.currentThread().isInterrupted()) return;
    } 
  }  
}
