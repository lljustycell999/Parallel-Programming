package csc.pkg375.assignment.pkg1;

import java.util.concurrent.Exchanger;
import java.util.concurrent.ThreadLocalRandom;

// This class will keep track of the FactorySolutions and implement the genetic
// algorithm loop
public class FactoryManager extends Thread{
    
    // The current factory layout + The best factory layout found so far
    private FactoryAffinity factory;
    private FactoryAffinity bestFactory;
    
    // The current affinity metric value + The best affinity metric value 
    // found so far
    private int metric;
    private int bestMetric;
    
    // Update what each factory should look like
    private FactorySolution display = null;
    
    // Helps with drawing only during specific intervals
    private long lastDraw = 0;
    
    // An exchanger is used by ALL THREADS to communicate solutions
    private static final Exchanger<FactoryAffinity> FactorySwap = new Exchanger
        <FactoryAffinity>();
    
    /** Constructor that holds the data of each FactoryManager Thread
     * @param fs1 A FactoryAffinity that represents the design of a factory
    */
    public FactoryManager(FactoryAffinity curFactory){
        
        // Current and best variables will be the same at first.
        factory = curFactory;
        bestFactory = curFactory;
        
        metric = FactoryAffinity.calculateFullMetric(curFactory);
        bestMetric = metric;
        
        // Ensures the FactoryManager thread can be terminated without issue
        setDaemon(true);
    }
    
    /**
     * Executes the Genetic Algorithm Plan:
     * Step 1: Calculate the affinity metric value (Done in constructor)
     * Step 2: Keep picking two random panels until they are different colors
     * Step 3: Swap the panels (their StationType values)
     * Step 4: Calculate the affinity metric again
     * Step 5: Keep the better layout, but there is a small chance that the 
     * new answer will be accepted, even if it is worse.
     * Step 6: Update necessary variables that deal with handling the panels 
     * and the metric
     * Step 7: Implement a chance for the current thread to swap its solution
     * with another thread
     * Step 8: Draw solutions at specific intervals (not too fast or slow)
     * Step 9: Return to Step 2 until the threads are interrupted
     */
    @Override
    public void run(){
        
        // Variable for Random Number Generator (isolated to the current thread)
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        
        final int size = FactoryAffinity.SIZE;
        while(!interrupted()){
            
            // Step 2:
            // First Random Panel
            int row1 = rand.nextInt(size);
            int col1 = rand.nextInt(size);
            
            // Second Random Panel
            int row2 = rand.nextInt(size);
            int col2 = rand.nextInt(size);
            
            // If both random panels are the same stationType, try again
            while(factory.station(row1, col1) == factory.station(row2, col2)){
                
                // New First Random Panel
                row1 = rand.nextInt(size);
                col1 = rand.nextInt(size);

                // New Second Random Panel
                row2 = rand.nextInt(size);
                col2 = rand.nextInt(size);
            }
            
            // Step 3:
            // Find First & Second StationType
            final StationType factoryColor1 = factory.station(row1, col1);
            final StationType factoryColor2 = factory.station(row2, col2);
            
            // Insert first value in second slot and second value in first slot
            // Note: factoryAlter() returns a clone with the change, so it is not
            // set in stone yet.
            final FactoryAffinity newLayout = factory.factoryAlter(factoryColor1, 
                row2, col2).factoryAlter(factoryColor2, row1, col1);
            
            // Steps 4 & 5:
            // - Calculates new metrics inside the keepBetter() Function
            // - There is a 5% chance that worse answers will be accepted
            keepBetter(newLayout, (rand.nextInt(20)==1));
            
            // Step 7: There is a 10% chance that an answer from a different 
            // thread will be accepted
            if(rand.nextInt(10) == 1){
                try{
                    final FactoryAffinity factoryOffer = FactorySwap.exchange(factory);
                    
                    // Ensure both stations have the same stations, just 
                    // arranged differently
                    if(factory.exchangeSignature == factoryOffer.exchangeSignature){
                        
                        // Still have to implement the chance that the 
                        // potentially worse answer from before may be accepted
                        keepBetter(factoryOffer, (rand.nextInt(20)== 1));
                        
                    }
                }
                catch(InterruptedException e){
                    
                    // Terminate the loop in case thread is interrupted by another
                    this.interrupt(); 
                }
            }
            
            // Step 8:
            // Wait about half a second before displaying new results
            if(display != null && lastDraw + 500 < System.currentTimeMillis()){
                
                // Lambda expression needed
                javax.swing.SwingUtilities.invokeLater(() -> {
                    display.updateFactory(factory, metric);
                });
                
                // Update the time tracker
                lastDraw = System.currentTimeMillis();
            }
            
            try{
                
                // Wait a bit in between swaps to closely monitor progress
                sleep(3);
            }
            catch(InterruptedException e){
                
                // Terminate the loop in case thread is interrupted by another
                this.interrupt();
            }
            // getPhaser % 100 // display every one hundred phases
            // Step 9: 
            // Go back to step 2 (That's why we are on a while loop)
        }
    }

    /**
     * @return The best metric found so far
     */
    public int getBestMetric(){
        return bestMetric;
    }
    
    /**
     * @return The best factory layout found so far
     */
    public FactoryAffinity getBestLayout(){
        return bestFactory;
    }
    
    /**
     * @return The last metric found.
     * Note: If the thread is running, returning the last metric is not certain
     */
    public int getLastMetric(){
        return metric;
    }
    
    /**
     * @return The last factory layout found
     * Note: If the thread is running, returning the last layout is not certain
     */
    public FactoryAffinity getLastLayout(){
        return factory;
    }
    
    /**
     * Helps with improving the layout and metric values of a factory
     * @param newFactory The potential new factory layout.
     * @param succeedAnyway Used to determine if the new layout will be used 
     * whether it has a higher metric or not
     */
    private void keepBetter(FactoryAffinity newFactory, boolean succeedAnyway){
        
        // Calculate the new metric affinity score
        final int newMetric = FactoryAffinity.calculateFullMetric(newFactory);
        
        // Check for improvement
        if(newMetric > metric){
            factory = newFactory;
            metric = newMetric;
            
            // Keep track of the best factory and its metric affinity score
            if(newMetric > bestMetric){
                bestMetric = newMetric;
                bestFactory = newFactory;
            }
        }
        
        // Check to see if we will go through with the new layout, even if it 
        // has the same or a worse metric score than the current layout.
        else if(succeedAnyway){
            metric = newMetric;
            factory = newFactory;
        }
        
    }
    
    /**
     * Lets the FactoryManager communicate with the GUI to display solutions
     * @param theFactory The factory design that needs to be displayed
     */
    public void setPanel(FactorySolution theFactory){
        display = theFactory;
    }
    
}
