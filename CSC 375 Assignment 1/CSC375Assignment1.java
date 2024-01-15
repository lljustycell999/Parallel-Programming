package csc.pkg375.assignment.pkg1;

import javax.swing.JFrame;
import java.io.IOException;
import java.awt.GridLayout;

public class CSC375Assignment1 {
    
    // Creating the GUI window so main thread can kill it when ending the program
    private static JFrame myFrame;
    
    // Number of Threads (At least 32)
    private static int numThreads = 32;
    
    // Main will launch all FactoryManager threads and set up the GUI
    public static void main(String[] args) throws IOException{
        
        // Declare the FactoryManager array (The number of factories we will 
        // have at one time)
        
        // Note: Set as final because it won't need to be changed after 
        // initialization (This will happen a lot to avoid common parallel 
        // programming bugs)
        final FactoryManager[] fms = new FactoryManager[numThreads];
        
        // Prepare an initial factory layout to work with
        final FactoryAffinity initFactory = new FactoryAffinity();
        for(int i = 0; i < fms.length; i++)
            
            // Initialize each factory manager with our starting factory layout
            fms[i] = new FactoryManager(initFactory);
            
        // Set up the GUI
        initializeGUI(fms);
        
        // Start the FactoryManager threads
        for(FactoryManager fm : fms)
            fm.start();
        
        // Continue until the user inputs something in the console
        try{
            System.in.read();
        }
        finally{
            // Attempt to interrupt all the FactoryManager threads (daemons) 
            // to terminate them.
            for(FactoryManager fm : fms)
                fm.interrupt();
        }
        // Kill the GUI
        myFrame.dispose();
   
    }
    
    /**
     * Initialize the GUI to visually monitor progress
     * @param fms: The factory managers (Contains our updated factories)
     * 
     * Note: Only the main thread may call this function.
     */
    private static void initializeGUI(FactoryManager[] fms){
         
        // GUI Title
        myFrame = new JFrame("Factory Arrangement via a Parallel Genetic "
            + "Algorithm");
         
        // Ensure closing the GUI ends the whole program
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Prepare a layout for the best factory to display
        myFrame.setLayout(new GridLayout(1, 0, 25, 0));
        
        int bestMetric = -1000000000;
        // Find the first factory that has the best metric
        for(FactoryManager fm : fms){  
            int potentialBestMetric = fm.getBestMetric();
            if(potentialBestMetric >= bestMetric)
                bestMetric = potentialBestMetric;
        }
        for(FactoryManager fm : fms){
            if(fm.getBestMetric() == bestMetric){
                
                FactorySolution aFactory = new FactorySolution(fm.getBestLayout(), fm.getBestMetric());

                // Add the current factory to the GUI
                myFrame.getContentPane().add(aFactory);

                // Tell the current factory manager thread which panels to update
                fm.setPanel(aFactory);
                break;
            }

        }
        myFrame.pack();
         
        // Show the window
        myFrame.setVisible(true);
         
    }
    
}
