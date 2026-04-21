

public class Reader extends Thread {
  int myName;  
  RandomSleep rSleep;  

  public Reader(int name) {
    myName = name;  
    rSleep = new RandomSleep();  
  }  

  public void run () {
    
    GUI.setState(true, myName, "THINKING");
    

    rSleep.doSleep(1000, 2500);

 
    for (int I = 0;  I < 5; I++) {
      

      GUI.setState(true, myName, "WAITING");
      System.out.println("Reader " + myName + " wants to read.");
      
     
      rSleep.doSleep(1000, 2500); 

    
      try {
          Synch.mutex.acquire();
      } catch (Exception e) { return; }

   
      if (Synch.activeWriters > 0 || Synch.waitingWriters > 0) {
         
          Synch.waitingReaders++;
          Synch.mutex.release();
          
          try {
     
              Synch.semReader.acquire();
          } catch (Exception e) { return; }
         
      } else {
       
          Synch.activeReaders++;
    
          Synch.mutex.release();
      }
      
    
      GUI.setState(true, myName, "ACTIVE");
      System.out.println("Reader " + myName + " is now reading.");

    
      rSleep.doSleep(1500, 3000);

      if (Thread.currentThread().isInterrupted()) return;

      try {
          Synch.mutex.acquire();
      } catch (Exception e) { return; }

      Synch.activeReaders--;
      
   
      if (Synch.activeReaders == 0 && Synch.waitingWriters > 0) {
      
          Synch.waitingWriters--;
          Synch.activeWriters++;
          
     
          Synch.semWriter.release();
      }
      
      System.out.println("Reader " + myName + " is finished reading.");
    
      Synch.mutex.release();

     
      GUI.setState(true, myName, "THINKING");


      if (Thread.currentThread().isInterrupted()) return;
      rSleep.doSleep(1000, 2500);
      if (Thread.currentThread().isInterrupted()) return;
    } 
  }  
}
