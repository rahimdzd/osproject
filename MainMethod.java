// MainMethod is primarily the orchestrator script representing the baseline application entry point entirely.
// It seamlessly creates the graphical interface, fundamentally allocates semaphores dynamically, and spawns independent Java Threads!

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MainMethod {

    // Actively maintained list safely in order to confidently tear-down running threads globally upon GUI restarting visually.
    private static List<Thread> activeThreads = new ArrayList<>();

    // main is primarily invoked exclusively when the JVM directly initializes the run physically.
    public static void main (String argv[]) {
        // Initials and lock-in the live Swing graphical visual simulation map securely.
        GUI.init();
        
        // Execute the first explicit baseline simulation run identically.
        startSimulation();
    }

    // startSimulation comprehensively handles gracefully tearing down the old environment completely and actively forging a newly fresh one computationally.
    public static void startSimulation() {
        
        // Step 1: Deliberately interrupt previously executing simulation threads dynamically if entirely any remain aggressively running independently.
        for (Thread t : activeThreads) {
            t.interrupt();
        }
        activeThreads.clear();

        // Step 2: Safely obliterate and explicitly rebuild the baseline coordination state variables structurally!
        // The active Java variables must be perfectly explicitly pristine absolutely each fundamental time.
        Synch.mutex = new Semaphore(1, true);     // Initializes absolute central mutual exclusion block 
        Synch.semReader = new Semaphore(0, true); // Initializes independent reader waiting gate strictly
        Synch.semWriter = new Semaphore(0, true); // Initializes distinct writer waiting gate firmly
        Synch.activeReaders = 0;
        Synch.activeWriters = 0;
        Synch.waitingReaders = 0;
        Synch.waitingWriters = 0;

        // Push graphical initial total entity counts globally straight successfully to the UI.
        GUI.setCreatedProcesses(8, 8); 

        // Step 3: Continuously instantiate and asynchronously independently trigger exactly 8 concurrent Readers completely and 8 absolute parallel Writers!
        for (int i=1; i<=8; i++) {
            Writer W = new Writer(i);
            Reader R = new Reader(i);
            activeThreads.add(W);
            activeThreads.add(R);
            
            // .start() comprehensively instructs natively individual thread instances to run uniquely inside their inner run() loops asynchronously!
            W.start();
            R.start();
        }
        
        // Final structural terminal validation logging strictly identifying the successful system sequence bootup.
        System.out.println("Main: Simulation Restarted/Started");
    }
}
