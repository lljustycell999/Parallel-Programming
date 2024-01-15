package csc.pkg375.assignment.pkg1;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

// This class is designed to mathematically determine the affinity of a
// factory layout and ensure exchangement only occurs with factories that
// have the same number of each StationType
final class FactoryAffinity {
    
    // Size is the number of rows (or the number of columns)
    static final int SIZE = 8;
    
    // Array that will hold the StationTypes for each panel
    private final StationType[][] stations;
    
    // This is used for checking if the factories have the same number of each 
    // StationType (not necessarily in the exact layout)
    public final int exchangeSignature;

    /**
     * This constructor will create a random factory layout specifically
     * for the initial layout.
     */
    public FactoryAffinity(){
        
        // Keep track of the signature as we go
        int runningSignature = 0;
        
        // Keep track of the number of stations
        int numStations = 0;
        
        // Create a temporary array to hold the StationType of each panel
        StationType[][] temp = new StationType[SIZE][SIZE];
        
        // For every row
        for(int i = 0; i < SIZE; i++){
            // For every column in the current row
            for(int j = 0; j < SIZE; j++){
                
                // Keep track of where adjacency is valid
                boolean columnAdjacency = false;
                boolean rowAdjacency = false;
                Random random = new Random();
                
                // Initialize to a random StationType and check if we can still
                // insert more stations and that the current spot is not already taken.
                if((numStations < 48) && (temp[i][j] == null)){
                    temp[i][j] = getRandomStation();
                    
                    // Case 1: Yellow Station (No adjacency needed)
                    if(temp[i][j] == StationType.Yellow){
                        
                        // Nothing else to do since only one spot is used
                        runningSignature += getExchangeValue(temp[i][j]);
                        numStations++;
                    }
                    
                    // Case 2: Blue Station (One adjacent panel needed)
                    else if(temp[i][j] == StationType.Blue){
                        
                        // Check if potential spots are not out of bounds and if
                        // adjacent spots are available.
                        if((j+1 < SIZE) && (numStations + 1 < 48)){
                            
                            // An adjacent column is available
                            if(temp[i][j+1] == null)
                                columnAdjacency = true; 
                        }
                        if((i+1 < SIZE) && (numStations + 1 < 48)){
                            
                            // An adjacent row is available
                            if(temp[i+1][j] == null)
                                rowAdjacency = true;
                        }
                        
                        // If there is column and row adjacency, pick one at
                        // random to determine where the panels are going to be
                        if(columnAdjacency && rowAdjacency){
                            int randomAdjacency = random.nextInt(2);
                            if(randomAdjacency == 0){
                                temp[i][j+1] = StationType.Blue;
                                runningSignature += getExchangeValue(temp[i][j]);
                                runningSignature += getExchangeValue(temp[i][j+1]);
                                numStations += 2;
                            }
                            else{
                                temp[i+1][j] = StationType.Blue;
                                runningSignature += getExchangeValue(temp[i][j]);
                                runningSignature += getExchangeValue(temp[i+1][j]);
                                numStations += 2;
                            }
                                
                        }
                        
                        // Only column adjacency
                        else if(columnAdjacency && !rowAdjacency){
                            temp[i][j+1] = StationType.Blue;
                            runningSignature += getExchangeValue(temp[i][j]);
                            runningSignature += getExchangeValue(temp[i][j+1]);
                            numStations += 2;
                        }
                        
                        // Only row adjacency
                        else if(!columnAdjacency && rowAdjacency){
                            temp[i+1][j] = StationType.Blue;
                            runningSignature += getExchangeValue(temp[i][j]);
                            runningSignature += getExchangeValue(temp[i+1][j]);
                            numStations += 2;
                        }
                        
                        // If there is no adjacency, then try a different
                        // StationType
                        else{
                            temp[i][j] = null;
                            j--;
                        }
                        
                    }
                    
                    // Case 3: Red Station (Two adjacent panels needed)
                    else if(temp[i][j] == StationType.Red){
                        if((j+2 < SIZE) && (numStations + 2 < 48)){

                            // Two adjacent columns are available
                            if((temp[i][j+1] == null) && (temp[i][j+2] == null))
                                columnAdjacency = true;
                        }
                        if((i+2 < SIZE) && (numStations + 2 < 48)){
                            
                            // Two adjacent rows are available
                            if((temp[i+1][j] == null) && (temp[i+2][j] == null))
                                rowAdjacency = true;
                        }
                        if(columnAdjacency && rowAdjacency){
                            int randomAdjacency = random.nextInt(2);
                            if(randomAdjacency == 0){
                                temp[i][j+1] = StationType.Red;
                                temp[i][j+2] = StationType.Red;
                                runningSignature += getExchangeValue(temp[i][j]);
                                runningSignature += getExchangeValue(temp[i][j+1]);
                                runningSignature += getExchangeValue(temp[i][j+2]);
                                numStations += 3;  
                            }
                            else{
                                temp[i+1][j] = StationType.Red;
                                temp[i+2][j] = StationType.Red;
                                runningSignature += getExchangeValue(temp[i][j]);
                                runningSignature += getExchangeValue(temp[i+1][j]);
                                runningSignature += getExchangeValue(temp[i+2][j]);
                                numStations += 3;
                            }
                                
                        }
                        else if(columnAdjacency && !rowAdjacency){
                            temp[i][j+1] = StationType.Red;
                            temp[i][j+2] = StationType.Red;
                            runningSignature += getExchangeValue(temp[i][j]);
                            runningSignature += getExchangeValue(temp[i][j+1]);
                            runningSignature += getExchangeValue(temp[i][j+2]);
                            numStations += 3;
                        }
                        else if(!columnAdjacency && rowAdjacency){
                            temp[i+1][j] = StationType.Red;
                            temp[i+2][j] = StationType.Red;
                            runningSignature += getExchangeValue(temp[i][j]);
                            runningSignature += getExchangeValue(temp[i+1][j]);
                            runningSignature += getExchangeValue(temp[i+2][j]);
                            numStations += 3;
                        }                        
                        else{
                            temp[i][j] = null;
                            j--;
                        }
                    }
                    
                    // Case 4: Green Station (Three adjacent panels needed)
                    else if(temp[i][j] == StationType.Green){
                        if((j+3 < SIZE) && (numStations + 3 < 48)){

                            // Three adjacent columns are available
                            if((temp[i][j+1] == null) && (temp[i][j+2] == null) && (temp[i][j+3] == null))
                                columnAdjacency = true; 
                        }
                        if((i+3 < SIZE) && (numStations + 3 < 48)){
                            
                            // Three adjacent rows are available
                            if((temp[i+1][j] == null) && (temp[i+2][j] == null) && (temp[i+3][j] == null))
                                rowAdjacency = true;
                        }
                        if(columnAdjacency && rowAdjacency){
                            int randomAdjacency = random.nextInt(2);
                            if(randomAdjacency == 0){
                                temp[i][j+1] = StationType.Green;
                                temp[i][j+2] = StationType.Green;
                                temp[i][j+3] = StationType.Green;
                                runningSignature += getExchangeValue(temp[i][j]);
                                runningSignature += getExchangeValue(temp[i][j+1]);
                                runningSignature += getExchangeValue(temp[i][j+2]);
                                runningSignature += getExchangeValue(temp[i][j+3]);
                                numStations += 4;    
                            }
                            else{
                                temp[i+1][j] = StationType.Green;
                                temp[i+2][j] = StationType.Green;
                                temp[i+3][j] = StationType.Green;
                                runningSignature += getExchangeValue(temp[i][j]);
                                runningSignature += getExchangeValue(temp[i+1][j]);
                                runningSignature += getExchangeValue(temp[i+2][j]);
                                runningSignature += getExchangeValue(temp[i+3][j]);
                                numStations += 4;
                            }        
                        }
                        else if(columnAdjacency && !rowAdjacency){
                            temp[i][j+1] = StationType.Green;
                            temp[i][j+2] = StationType.Green;
                            temp[i][j+3] = StationType.Green;
                            runningSignature += getExchangeValue(temp[i][j]);
                            runningSignature += getExchangeValue(temp[i][j+1]);
                            runningSignature += getExchangeValue(temp[i][j+2]);
                            runningSignature += getExchangeValue(temp[i][j+3]);
                            numStations += 4;
                        }
                        else if(!columnAdjacency && rowAdjacency){
                            temp[i+1][j] = StationType.Green;
                            temp[i+2][j] = StationType.Green;
                            temp[i+3][j] = StationType.Green;
                            runningSignature += getExchangeValue(temp[i][j]);
                            runningSignature += getExchangeValue(temp[i+1][j]);
                            runningSignature += getExchangeValue(temp[i+2][j]);
                            runningSignature += getExchangeValue(temp[i+3][j]);
                            numStations += 4;
                        }                            
                        else{
                            temp[i][j] = null;
                            j--;
                        }
                    }
                    
                }
                // If no more stations are available, make the remaining spots
                // holes
                else if(numStations >= 48 && temp[i][j] == null){
                    temp[i][j] = StationType.Black;
                }
            }
        }
        // When done, set the stations and exchangeSignature attributes
        stations = temp;
        exchangeSignature = runningSignature;
    }
    
    /**
     * This constructor calculates the exchangeSignature for a factory
     * @param factoryColors The 2D Array of StationTypes for a factory
     */
    private FactoryAffinity(StationType[][] factoryColors){
        
        int runningSignature = 0;
        stations = factoryColors;
        
        // For every row that is within the allowed size
        for(int i = 0; i < factoryColors.length && i < SIZE; i++){
            
            // For every column (slot in the current row) that is within the allowed size
            for(int j = 0; j < factoryColors[i].length && j < SIZE; j++){
                
                // Update the signature
                runningSignature += getExchangeValue(factoryColors[i][j]);
            }
        }
        exchangeSignature = runningSignature;
    }
    
    /**
     * This method randomly selects a StationType
     * @return A stationType (excluding holes)
     */
    public static StationType getRandomStation(){
        int rand = ThreadLocalRandom.current().nextInt(4);
        switch(rand){
            case 0:
                return StationType.Yellow;
            case 1:
                return StationType.Blue;
            case 2:
                return StationType.Red;
            default:
                return StationType.Green;
        } 
    }
    
    /**
     * Obtain the StationType of a specific station
     * @param row The row of the station to get
     * @param col The column of the station to get
     */
    public StationType station(int row, int col){
        return stations[row % SIZE][col % SIZE];
    }
    
    /**
     * Allows for the alteration of a specific factory station
     * @param newStationType The new StationType to insert
     * @param row The row location of the factory to alter
     * @param col The column location of the factory to alter
     * @return The updated affinity metric value for the factory
     */
    public FactoryAffinity factoryAlter(StationType newStationType, int row, int col){
        
        StationType[][] temp = new StationType[SIZE][SIZE];
        
        // For every row that is within the allowed size and the original array
        for(int i = 0; i < stations.length && i < SIZE; i++){
            
            // For every column/slot that is within the allowed size and the 
            // original array
            for(int j = 0; j < stations[i].length && j < SIZE; j++)
                
                // Get the current StationType
                temp[i][j] = stations[i][j];
        }
        
        // Update the specified station with newStationType
        temp[row % SIZE][col % SIZE] = newStationType;
        
        // Use the constructor to update the exchangeSignature value for the factory
        return new FactoryAffinity(temp);
    }
    
    /**
     * Returns an exchange value for a station based on the StationType.
     * 
     * Note: Adding all of the station's exchange values will produce a total 
     * exchange value for a factory that corresponds to the number of each StationType inside.
     * This means two factories will have the same exchange value if they have the same number
     * of every stationType.
     * 
     * @param stationColor The stationType we are interested in
     * @return The exchange value for the given station.
     */
    private static int getExchangeValue(StationType stationColor){
        
        // Formula for calculating what each station is worth:
        // Fill the floor solely with the next highest-value station, calculate
        // the sum, and add one at the end.
        switch(stationColor){
            case Yellow:
                return 1;
            case Blue:
                return (SIZE * SIZE) + 1;
            case Red:
                return (((SIZE * SIZE) + 1) * SIZE * SIZE) + 1;
            case Green:
                return (((((SIZE * SIZE) + 1) * SIZE * SIZE) + 1) * SIZE * SIZE) + 1;
            default: //Black
                return SIZE;        
        }
    }
    
    /**
     * Calculates the affinity metric for a factory
     * @param factory The factory to calculate the affinity metric for
     */
    public static int calculateFullMetric(FactoryAffinity factory){
        
        int metric = 0;
        
        // Used to avoid counting edges multiple times
        final int edge = FactoryAffinity.SIZE - 1;
        
        // For every row, excluding the bottom one
        for(int i = 0; i < edge; i++){
            
            // For each station throughout the row, except for the rightmost one
            for(int j = 0; j < edge; j++){
                
                // Compare the current station to the one right below it
                metric += calculateRightAndLowerAffinity(factory.stations[i][j], factory.stations[i+1][j]);
                
                // Compare the current station to the one to the right of it
                metric += calculateRightAndLowerAffinity(factory.stations[i][j], factory.stations[i][j+1]);
                
                // If a left side is available
                if(j-1 >= 0){
                    
                    // Compare the current station to the one to the left of it
                    metric += calculateLeftAndUpperAffinity(factory.stations[i][j], factory.stations[i][j-1]);
                    
                }
                // If an upper side is available
                if(i-1 >= 0){
                    
                    // Compare the current station to the one above it
                    metric += calculateLeftAndUpperAffinity(factory.stations[i][j], factory.stations[i-1][j]);
                    
                }
                
            }
            // Last column affinity metric
            metric += calculateRightAndLowerAffinity(factory.stations[i][edge], factory.stations[i+1][edge]);
            
            // Last row affinity metric
            metric += calculateRightAndLowerAffinity(factory.stations[edge][i], factory.stations[edge][i+1]);
        }
        // Return the resulting total metric for the factory
        return metric;
    }
    
    /**
    * Calculates the affinity metric for a station based on its right and lower neighbors
    * The goal is to organize the factory in a way that gives higher metric 
    * values to stations that are adjacent to other stations of the same StationType.
    * @param station The station whose affinity we want to calculate
    * @param neighbor The station we are comparing the station of interest to
    * @return The new affinity metric value.
    */
    private static int calculateRightAndLowerAffinity(StationType station, StationType neighbor){
        
        // The more matching adjacent StationTypes together, the better
        if(station == neighbor)
            return 50;
        
        // Yellow's Target: Top-Left Quadrant 
        // - Blue stations should be to the right (Positive metric)
        // - Red stations should be right below (Positive metric)
        // - Green stations should be diagonally below to the right (Negative metric). 
        // - Preferably want holes near the middle of the factory (Slightly positive metric)
        else if(station == StationType.Yellow){
            if(neighbor == StationType.Blue)
                return 15;
            if(neighbor == StationType.Red)
                return 15;
            else if(neighbor == StationType.Green)
                return -15;
            else if(neighbor == StationType.Black)
                return 5;
        }
        
        // Blue's Target: Top-Right Quadrant
        // - Yellow stations should be to the left (Negative metric)
        // - Red stations should be diagonally below to the left (Negative metric)
        // - Green stations should be right below (Positive metric). 
        // - Preferably want holes near the middle of the factory (Slightly positive metric)
        else if(station == StationType.Blue){
            if(neighbor == StationType.Yellow)
                return -15;
            else if(neighbor == StationType.Red)
                return -15;
            else if(neighbor == StationType.Green)
                return 15;
            else if(neighbor == StationType.Black)
                return 5;
        }
        
        // Red's Target: Bottom-Left Quadrant 
        // - Yellow stations should be right above (Negative metric)
        // - Green stations should be on the right (Positive metric)
        // - Blue stations should be diagonally up to the right (Negative metric). 
        // - Preferably want holes near the middle (Slightly positive metric)
        else if(station == StationType.Red){
            if(neighbor == StationType.Yellow)
                return -15;
            else if(neighbor == StationType.Green)
                return 15;
            else if(neighbor == StationType.Blue)
                return -15;
            else if(neighbor == StationType.Black)
                return 5;
        }
        
        // Green's Target: Bottom-Right Quadrant 
        // - Yellow stations should be diagnoally above to the left (Negative metric)
        // - Red stations should be on the left (Negative metric)
        // - Blue stations should be right above (Negative metric). 
        // - Preferably want holes near the middle (Slightly positive metric)
        else if(station == StationType.Green){
            if(neighbor == StationType.Yellow)
                return -15;
            else if(neighbor == StationType.Red)
                return -15;
            else if(neighbor == StationType.Blue)
                return -15;
            else if(neighbor == StationType.Black)
                return 5;
        }
        
        // Hole's Target: Center of Factory (or at least all close together)
        else if(station == StationType.Black){
            if(neighbor != StationType.Black)
                return 20;
            else
                return 50;
        }
        
        // Used in case we change the StationType class (shouldn't be reached)
        return 0;
        
    }
    
    /**
    * Calculates the affinity metric for a station based on its left and upper neighbors
    * The goal is to organize the factory in a way that gives higher metric values
    * to stations that are adjacent to other stations of the same StationType.
    * @param station The station whose affinity we want to calculate
    * @param neighbor The station we are comparing the station of interest to
    * @return The new affinity metric value.
    */
    private static int calculateLeftAndUpperAffinity(StationType station, StationType neighbor){
        
        // The closer two matching color stations are together, the better!
        if(station == neighbor)
            return 50;
        
        // Yellow's Target: Top-Left Quadrant 
        // - Blue stations should be to the right (Negative metric)
        // - Red stations should be right below (Negative metric)
        // - Green stations should be diagonally below to the right (Negative metric)
        // - Preferably want holes near the middle of the factory (Negative metric)
        else if(station == StationType.Yellow){
            if(neighbor == StationType.Blue)
                return -15;
            if(neighbor == StationType.Red)
                return -15;
            else if(neighbor == StationType.Green)
                return -15;
            else if(neighbor == StationType.Black)
                return -15;
        }
        
        // Blue's Target: Top-Right Quadrant
        // - Yellow stations should be to the left (Positive metric)
        // - Red stations should be diagonally below to the left (Negative)
        // - Green stations should be right below (Negative metric) 
        // - Preferably want holes near the middle of the factory (Positive metric)
        else if(station == StationType.Blue){
            if(neighbor == StationType.Yellow)
                return 15;
            else if(neighbor == StationType.Red)
                return -15;
            else if(neighbor == StationType.Green)
                return -15;
            else if(neighbor == StationType.Black)
                return 15;
        }
        
        // Red's Target: Bottom-Left Quadrant 
        // - Yellow stations should be right above (Positive metric)
        // - Green stations should be on the right (Negative metric)
        // - Blue stations should be diagonally up to the right (Negative metric)
        // - Preferably want holes near the middle (Slightly negative metric)
        else if(station == StationType.Red){
            if(neighbor == StationType.Yellow)
                return 15;
            else if(neighbor == StationType.Green)
                return -15;
            else if(neighbor == StationType.Blue)
                return -15;
            else if(neighbor == StationType.Black)
                return -5;
        }
        
        // Green's Target: Bottom-Right Quadrant 
        // - Yellow stations should be diagnoally above to the left (Negative metric)
        // - Red stations should be on the left (Positive metric)
        // - Blue stations should be right above (Positive metric)
        // - Preferably want holes near the middle (Positive metric)
        else if(station == StationType.Green){
            if(neighbor == StationType.Yellow)
                return -15;
            else if(neighbor == StationType.Red)
                return 15;
            else if(neighbor == StationType.Blue)
                return 15;
            else if(neighbor == StationType.Black)
                return 15;
        }
        
        // Hole's Target: Center of Factory (or at least all close together)
        else if(station == StationType.Black){
            if(neighbor != StationType.Black)
                return 20;
            else
                return 50;
        }
        
        // Used in case we change the StationType class (shouldn't be reached)
        return 0;
        
    }
    
}
