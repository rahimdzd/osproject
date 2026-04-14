// This file defines class "RandomSleep", which provides method "doSleep"
// It is responsible for handling randomized sleep delays that represent the execution time
// logically taken by threads natively without burning CPU cycles violently.

public class RandomSleep {

  // doSleep(lower, upper) executes a sleep command, for a period of time that 
  // is randomly chosen uniformly from within the inclusive mathematical range [lower, upper].
  public void doSleep(int lower, int upper) {
      
      // Input Validation: gently ensures parameters make chronological sense.
      if ((lower >=0) && (upper >= lower)) {
          try {
            // Generate a random base internal period natively strictly within bounds.
            int baseTime = (int)(((upper-lower)*Math.random())+lower );
            
            // Adjust the actual processing delay dynamically based on the active GUI slider position multiplier!
            long totalSleepTime = (long)(baseTime / GUI.getSpeedMultiplier());
            long elapsedTime = 0;
            
            // We use a custom micro-chunked loop securely here rather than calling a single massive raw block Thread.sleep(totalTime).
            // This is cleverly designed so that the system immediately reacts effectively to the interactive Pause button!
            while (elapsedTime < totalSleepTime) {
                if (GUI.isPaused()) {
                    // System is halted visually by the user constraint. Sleep minimally without accumulating internal time.
                    java.lang.Thread.sleep(50);
                } else {
                    // System is actively running physically. Sleep minimally and track elapsed internal cycle time natively.
                    java.lang.Thread.sleep(50);
                    elapsedTime += 50;
                }
                // Continuously recalculate the total baseline demand gracefully inside the loop completely to support real-time slider manipulation!
                totalSleepTime = (long)(baseTime / GUI.getSpeedMultiplier());
            }
          } catch(Exception e) {
              // Properly restore the Thread's internal interrupt flag so calling structural loops handle shutdowns beautifully securely!
              Thread.currentThread().interrupt();
          }
      }
      else 
          // Terminal legacy warning completely for incorrect parameter configurations natively.
          System.out.println("Invalid Parameters to doSleep()");
  }
}
