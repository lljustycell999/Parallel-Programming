package csc375assignment3multiserver;

import static csc375assignment3multiserver.CSC375Assignment3Protocol.C1;
import static csc375assignment3multiserver.CSC375Assignment3Protocol.C2;
import static csc375assignment3multiserver.CSC375Assignment3Protocol.C3;
import static csc375assignment3multiserver.CSC375Assignment3Protocol.HEIGHT;
import static csc375assignment3multiserver.CSC375Assignment3Protocol.LENGTH;
import static csc375assignment3multiserver.CSC375Assignment3Protocol.REGIONS;
import static csc375assignment3multiserver.CSC375Assignment3Protocol.REGION_TEMPS;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.logging.*;
import javax.swing.*;

public class CSC375MultiServer {
    
    static double S;
    static double T;
    
    public static void main(String[] args) throws IOException {

        int portNumber = 7;
        int twoClientsRequired = 0;
        List<Thread> clientThreads = new ArrayList<>();
        processInput();
        
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while(clientThreads.size() < 2) {
                Socket clientSocket = serverSocket.accept();
                twoClientsRequired++;
                Thread clientThread = new Thread(new KKMultiServerThread(clientSocket, twoClientsRequired, S, T));
                clientThreads.add(clientThread);
            }
            for(Thread thread : clientThreads){
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }
    
    public static void processInput(){
        
        String preS;
        String preT;

        boolean allowInput;
        Scanner kbd = new Scanner(System.in);
        
        System.out.println("Enter the temperature to heat the top left corner "
            + "of the metal alloy: ");
        preS = kbd.nextLine();
        allowInput = isValidInput(preS);
        while(!allowInput){
            System.out.println("Enter a number, you fool");
            preS = kbd.nextLine();
            allowInput = isValidInput(preS);
        }
        S = Double.parseDouble(preS);
        System.out.println("Enter the temperature to heat the bottom right "
            + "corner of the metal alloy: ");
        preT = kbd.nextLine();
        allowInput = isValidInput(preT);
        while(!allowInput){
            System.out.println("Enter a number, you fool");
            preT = kbd.nextLine(); 
            allowInput = isValidInput(preT);
        }
        T = Double.parseDouble(preT);
        
    }
    
    public static boolean isValidInput(String strNum){
        if(strNum == null){
            return false;
        }
        try{
            Double.valueOf(strNum);
        } catch (NumberFormatException nfe){
            return false;
        }
        return true;
    }
    
}

class KKMultiServerThread extends Thread {
    private Socket socket = null;
    private final int assignedTask;
    private final Double S;
    private final Double T;
    
    public KKMultiServerThread(Socket socket, int assignedTask, Double S, Double T) {
        super("KKMultiServerThread");
        this.socket = socket;
        this.assignedTask = assignedTask;
        this.S = S;
        this.T = T;
    }
     
    @Override
    public void run(){
        try{
            if(assignedTask == 1){
                CSC375Assignment3Protocol protocol = new CSC375Assignment3Protocol(S, T, assignedTask);
                protocol.initializeRegions();
                protocol.initializeGUI();
                protocol.startSimulation();
            }
            else if(assignedTask == 2){
                CSC375Assignment3Protocol protocol = new CSC375Assignment3Protocol(S, T, assignedTask);
                protocol.initializeRegions();
                protocol.initializeGUI();
                protocol.startSimulation();
            }
            socket.close();
        } catch (IOException e) {
        } catch (InterruptedException ex) {
            Logger.getLogger(KKMultiServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

class CSC375Assignment3Protocol extends JPanel{

    private JFrame myFrame;
    private JFrame legendFrame;
    private MetalSheet sheet;
    long lastDraw;
    
    static final int HEIGHT = 8;
    static final int LENGTH = HEIGHT * 4;
    
    static final double[][][] REGIONS = new double[HEIGHT][LENGTH][3];
          
    int assignedTask;
    
    static final double C1 = 0.75;
    static final double C2 = 1.00;
    static final double C3 = 1.25;
    
    static double[][] REGION_TEMPS = new double[HEIGHT][LENGTH];
    
    static double[][] PASTTEMPS1 = REGION_TEMPS;
    static double[][] PASTTEMPS2 = REGION_TEMPS;
    
    static boolean swapGrid = false;
    
    public CSC375Assignment3Protocol(Double S, Double T, int assignedTask){
        this.assignedTask = assignedTask;
        REGION_TEMPS[0][0] = S;
        REGION_TEMPS[HEIGHT-1][LENGTH-1] = T;
    }
    
    public void startSimulation() throws InterruptedException{
        
        CalculateRegionTemperatures crt = new CalculateRegionTemperatures(sheet, assignedTask);
        ForkJoinPool pool = new ForkJoinPool();
        
        Thread.sleep(1000);
        
        while(true){
            if(lastDraw + 30 < System.currentTimeMillis()){
                
                // Swap between the two metal alloys after each iteration
                swapGrid = !swapGrid;
                
                // Lambda expression needed
                javax.swing.SwingUtilities.invokeLater(() -> {
                    crt.reinitialize();
                    pool.invoke(crt);
                });

                // Update the time tracker
                lastDraw = System.currentTimeMillis();
            }
        }
    }
    
    public void initializeRegions(){
            
        // Prepare initial proportions for each metal in each region
        int[] probOfEachMetal = {33, 33, 33};
        
        if(assignedTask == 1){
            for(int row = 0; row < HEIGHT/2; row++){
                for(int col = 0; col < LENGTH; col++){

                    // Calculate a proportion for each metal while accounting for 
                    // up to 25% variation (Proportion of each metal will be 
                    // somewhere between 20.5% to 45.5%).
                    int variationOne = ThreadLocalRandom.current().nextInt(25) + 1;
                    int variationTwo = ThreadLocalRandom.current().nextInt(25) + 1;
                    int negativeOne = ThreadLocalRandom.current().nextInt(2);
                    int negativeTwo = ThreadLocalRandom.current().nextInt(2);

                    if(negativeOne == 1)
                        variationOne =  -variationOne;
                    if(negativeTwo == 1)
                        variationTwo = -variationTwo;

                    int metalOneProportion = probOfEachMetal[0] + variationOne;

                    // For the second metal, ensure the proportion is not too small
                    // or too large
                    int metalTwoProportion = probOfEachMetal[1] + variationTwo;

                    while((100 - metalOneProportion - metalTwoProportion > 
                        58) || (100 - metalOneProportion
                        - metalTwoProportion < 8)){
                        variationTwo = ThreadLocalRandom.current().nextInt(25) + 1;
                        metalTwoProportion = probOfEachMetal[1] + variationTwo;
                    }

                    // The third metal will just take the remaining proportion.
                    int metalThreeProportion = 100 - metalOneProportion - 
                        metalTwoProportion;

                    double dMetalOneProportion = metalOneProportion / (100.0 + Math.abs(variationOne));
                    double dMetalTwoProportion = metalTwoProportion / (100.0 + Math.abs(variationTwo));
                    double dMetalThreeProportion = metalThreeProportion / (100.0 + Math.abs(variationOne + variationTwo));

                    // Insert the proportions into the current region
                    REGIONS[row][col][0] = dMetalOneProportion;
                    REGIONS[row][col][1] = dMetalTwoProportion;
                    REGIONS[row][col][2] = dMetalThreeProportion;

                }
            }
        }
        else{
            for(int row = HEIGHT/2; row < HEIGHT; row++){
                for(int col = 0; col < LENGTH; col++){

                    // Calculate a proportion for each metal while accounting for 
                    // up to 25% variation (Proportion of each metal will be 
                    // somewhere between 20.5% to 45.5%).
                    int variationOne = ThreadLocalRandom.current().nextInt(25) + 1;
                    int variationTwo = ThreadLocalRandom.current().nextInt(25) + 1;
                    int negativeOne = ThreadLocalRandom.current().nextInt(2);
                    int negativeTwo = ThreadLocalRandom.current().nextInt(2);

                    if(negativeOne == 1)
                        variationOne =  -variationOne;
                    if(negativeTwo == 1)
                        variationTwo = -variationTwo;

                    int metalOneProportion = probOfEachMetal[0] + variationOne;

                    // For the second metal, ensure the proportion is not too small
                    // or too large
                    int metalTwoProportion = probOfEachMetal[1] + variationTwo;

                    while((100 - metalOneProportion - metalTwoProportion > 
                        58) || (100 - metalOneProportion
                        - metalTwoProportion < 8)){
                        variationTwo = ThreadLocalRandom.current().nextInt(25) + 1;
                        metalTwoProportion = probOfEachMetal[1] + variationTwo;
                    }

                    // The third metal will just take the remaining proportion.
                    int metalThreeProportion = 100 - metalOneProportion - 
                        metalTwoProportion;

                    double dMetalOneProportion = metalOneProportion / (100.0 + Math.abs(variationOne));
                    double dMetalTwoProportion = metalTwoProportion / (100.0 + Math.abs(variationTwo));
                    double dMetalThreeProportion = metalThreeProportion / (100.0 + Math.abs(variationOne + variationTwo));

                    // Insert the proportions into the current region
                    REGIONS[row][col][0] = dMetalOneProportion;
                    REGIONS[row][col][1] = dMetalTwoProportion;
                    REGIONS[row][col][2] = dMetalThreeProportion;

                }
            }
        }

    }
    
    public void initializeGUI(){
        
        if(assignedTask == 1){

            double[][] regionOneTemps = new double[HEIGHT/2][LENGTH];
            for(int i = 0; i < HEIGHT / 2; i++){
                regionOneTemps[i] = REGION_TEMPS[i];
            }
            
            myFrame = new JFrame("Heat Propogation Simulation (Region 1)");

            // Ensure closing the GUI ends the whole program
            myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            myFrame.setLayout(new GridLayout(1, 0, 25, 0));

            sheet = new MetalSheet(LENGTH, HEIGHT/2, regionOneTemps);
            myFrame.getContentPane().add(sheet);

            myFrame.pack();
            myFrame.setVisible(true);

            legendFrame = new JFrame("Temperature Legend");
            
            JLabel pinkLabel = new JLabel("Pink: 0°C or less");
            JLabel blueLabel = new JLabel("Blue: 0-10°C");
            JLabel cyanLabel = new JLabel("Cyan: 10-20°C");
            JLabel greenLabel = new JLabel("Green: 20-25°C");
            JLabel yellowLabel = new JLabel("Yellow: 25-30°C");
            JLabel orangeLabel = new JLabel("Orange: 30-35°C");
            JLabel redLabel = new JLabel("Red: 35-40°C");
            JLabel magentaLabel = new JLabel("Magenta: Above 40°C");

            pinkLabel.setForeground(Color.PINK);
            blueLabel.setForeground(Color.BLUE);
            cyanLabel.setForeground(Color.CYAN);
            greenLabel.setForeground(Color.GREEN);
            yellowLabel.setForeground(Color.YELLOW);
            orangeLabel.setForeground(Color.ORANGE);
            redLabel.setForeground(Color.RED);
            magentaLabel.setForeground(Color.MAGENTA);

            JPanel labels = new JPanel();

            labels.add(pinkLabel);
            labels.add(blueLabel);
            labels.add(cyanLabel);
            labels.add(greenLabel);
            labels.add(yellowLabel);
            labels.add(orangeLabel);
            labels.add(redLabel);
            labels.add(magentaLabel);
            labels.setBackground(Color.BLACK);

            legendFrame.add(labels);

            legendFrame.pack();
            legendFrame.setVisible(true);
        
        }
        else{
            double[][] regionTwoTemps = new double[HEIGHT/2][LENGTH];
            for(int i = 0; i < HEIGHT/2; i++){
                regionTwoTemps[i] = REGION_TEMPS[i + (HEIGHT/2)];
            }
            
            myFrame = new JFrame("Heat Propogation Simulation (Region 2)");

            // Ensure closing the GUI ends the whole program
            myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            myFrame.setLayout(new GridLayout(1, 0, 25, 0));

            sheet = new MetalSheet(LENGTH, HEIGHT/2, regionTwoTemps);
            myFrame.getContentPane().add(sheet);

            myFrame.pack();
            myFrame.setVisible(true);
        }
        
    }
    
}

class CalculateRegionTemperatures extends RecursiveAction{
        
    double[][] neighboringRegions = new double[4][4];
    double[] totalNeighborProducts = new double[3];
    MetalSheet sheet;
    int assignedTask;

    public CalculateRegionTemperatures(MetalSheet sheet, int assignedTask){
        this.sheet = sheet;
        this.assignedTask = assignedTask;
    }

    @Override
    protected void compute(){
        
        if(CSC375Assignment3Protocol.swapGrid)
            REGION_TEMPS = CSC375Assignment3Protocol.PASTTEMPS1;
        else
            REGION_TEMPS = CSC375Assignment3Protocol.PASTTEMPS2;
        
        // Random chance the two heated-up corners may have a different 
        // temperature
        for(int i = 0; i < 2; i++){
            int changeTemp = ThreadLocalRandom.current().nextInt(100);
            if(changeTemp == 0){
                double minTemp = -40.0;
                double maxTemp = 100.0;

                double newTemp = minTemp + (maxTemp - minTemp) * ThreadLocalRandom.current().nextDouble();

                if(i == 0){
                    REGION_TEMPS[0][0] = newTemp;
                }
                else{
                    REGION_TEMPS[HEIGHT-1][LENGTH-1] = newTemp;
                }
            }
        }

        
        // Subtask 1: Upper Half
        if(assignedTask == 1){
            for(int i = 0; i < HEIGHT/2; i++){
                for(int j = 0; j < LENGTH; j++){

                    // Skip the two heated regions
                    if(!((i == 0 && j == 0) || (i == HEIGHT-1 && j == LENGTH-1))){

                        int numNeighboringRegions = 0;
                        // Step 2: Get the neighboring regions (Doing this step here 
                        // for faster runtime)

                        // Right Neighbor
                        if(j+1 < LENGTH){
                            neighboringRegions[0][0] = REGIONS[i][j+1][0];
                            neighboringRegions[0][1] = REGIONS[i][j+1][1];
                            neighboringRegions[0][2] = REGIONS[i][j+1][2];
                            neighboringRegions[0][3] = REGION_TEMPS[i][j+1];
                            numNeighboringRegions++;
                        }

                        // Left Neighbor
                        if(j-1 >= 0){
                            neighboringRegions[1][0] = REGIONS[i][j-1][0];
                            neighboringRegions[1][1] = REGIONS[i][j-1][1];
                            neighboringRegions[1][2] = REGIONS[i][j-1][2];
                            neighboringRegions[1][3] = REGION_TEMPS[i][j-1];
                            numNeighboringRegions++;
                        }

                        // Lower Neighbor
                        if(i+1 < HEIGHT){
                            neighboringRegions[2][0] = REGIONS[i+1][j][0];
                            neighboringRegions[2][1] = REGIONS[i+1][j][1];
                            neighboringRegions[2][2] = REGIONS[i+1][j][2];
                            neighboringRegions[2][3] = REGION_TEMPS[i+1][j];
                            numNeighboringRegions++;
                        }

                        // Upper Neighbor
                        if(i-1 >= 0){
                            neighboringRegions[3][0] = REGIONS[i-1][j][0];
                            neighboringRegions[3][1] = REGIONS[i-1][j][1];
                            neighboringRegions[3][2] = REGIONS[i-1][j][2];
                            neighboringRegions[3][3] = REGION_TEMPS[i-1][j];
                            numNeighboringRegions++;
                        }

                        double[] metalTempValues = new double[3];

                        // For each of the three metals
                        for(int metal = 0; metal < 3; metal++){

                            // Multiply the metal's thermal constant with the summation of the 
                            // neighboring region's temperatures multiplied by the proportion 
                            // of that metal in the neigboring region. Finally, divide the 
                            // result by the number of neighboring regions
                            double Cm; 

                            // Step 1: Get the right thermal constant
                            Cm = switch (metal) {
                                case 0 -> C1;
                                case 1 -> C2;
                                default -> C3;
                            };

                            // Step 3: Multiply the temperature of the current neighboring region
                            // with the percentage of the current metal in that region. Keep track of the sum   
                            double totalNeighborProduct = 0.0;
                            for(int neighbor = 0; neighbor < 4; neighbor++){
                                if(neighboringRegions[neighbor][metal] != 0.0){
                                    double neighborMetalProportion = neighboringRegions[neighbor][metal];
                                    double neighborTemp = neighboringRegions[neighbor][3];
                                    double neighborProduct = neighborMetalProportion * neighborTemp;
                                    totalNeighborProduct = totalNeighborProduct + neighborProduct;
                                }

                            }
                            // Record the result of each metal.
                            totalNeighborProducts[metal] = totalNeighborProduct;

                            // Step 4: Perform the temperature calculation for the 
                            // current metal
                            metalTempValues[metal] = Cm * totalNeighborProducts[metal] / numNeighboringRegions;

                        }

                        // Step 5: Calculate the temperature of the region and store 
                        // it in the global variable
                        REGION_TEMPS[i][j] = 
                              (metalTempValues[0] * (1.0 - Math.abs(REGIONS[i][j][0] - (1.0 / 3.0))))
                            + (metalTempValues[1] * (1.0 - Math.abs(REGIONS[i][j][1] - (1.0 / 3.0))))
                            + (metalTempValues[2] * (1.0 - Math.abs(REGIONS[i][j][2] - (1.0 / 3.0))));

                        // Step 6: Rinse and repeat for the other regions!
                        System.out.println(REGION_TEMPS[i][j]);
                    }
                }
            }

            sheet.updateMetalSheet();
        }
        else{
            // Subtask 2: Lower Half
            for(int i = HEIGHT/2; i < HEIGHT; i++){
                for(int j = 0; j < LENGTH; j++){

                    // Skip the two heated regions
                    if(!((i == 0 && j == 0) || (i == HEIGHT-1 && j == LENGTH-1))){

                        int numNeighboringRegions = 0;
                        // Step 2: Get the neighboring regions (Doing this step here 
                        // for faster runtime)

                        // Right Neighbor
                        if(j+1 < LENGTH){
                            neighboringRegions[0][0] = REGIONS[i][j+1][0];
                            neighboringRegions[0][1] = REGIONS[i][j+1][1];
                            neighboringRegions[0][2] = REGIONS[i][j+1][2];
                            neighboringRegions[0][3] = REGION_TEMPS[i][j+1];
                            numNeighboringRegions++;
                        }

                        // Left Neighbor
                        if(j-1 >= 0){
                            neighboringRegions[1][0] = REGIONS[i][j-1][0];
                            neighboringRegions[1][1] = REGIONS[i][j-1][1];
                            neighboringRegions[1][2] = REGIONS[i][j-1][2];
                            neighboringRegions[1][3] = REGION_TEMPS[i][j-1];
                            numNeighboringRegions++;
                        }

                        // Lower Neighbor
                        if(i+1 < HEIGHT){
                            neighboringRegions[2][0] = REGIONS[i+1][j][0];
                            neighboringRegions[2][1] = REGIONS[i+1][j][1];
                            neighboringRegions[2][2] = REGIONS[i+1][j][2];
                            neighboringRegions[2][3] = REGION_TEMPS[i+1][j];
                            numNeighboringRegions++;
                        }

                        // Upper Neighbor
                        if(i-1 >= 0){
                            neighboringRegions[3][0] = REGIONS[i-1][j][0];
                            neighboringRegions[3][1] = REGIONS[i-1][j][1];
                            neighboringRegions[3][2] = REGIONS[i-1][j][2];
                            neighboringRegions[3][3] = REGION_TEMPS[i-1][j];
                            numNeighboringRegions++;
                        }

                        double[] metalTempValues = new double[3];

                        // For each of the three metals
                        for(int metal = 0; metal < 3; metal++){

                            // Multiply the metal's thermal constant with the summation of the 
                            // neighboring region's temperatures multiplied by the proportion 
                            // of that metal in the neigboring region. Finally, divide the 
                            // result by the number of neighboring regions
                            double Cm; 

                            // Step 1: Get the right thermal constant
                            Cm = switch (metal) {
                                case 0 -> C1;
                                case 1 -> C2;
                                default -> C3;
                            };

                            // Step 3: Multiply the temperature of the current neighboring region
                            // with the percentage of the current metal in that region. Keep track of the sum   
                            double totalNeighborProduct = 0.0;
                            for(int neighbor = 0; neighbor < 4; neighbor++){
                                if(neighboringRegions[neighbor][metal] != 0.0){
                                    double neighborMetalProportion = neighboringRegions[neighbor][metal];
                                    double neighborTemp = neighboringRegions[neighbor][3];
                                    double neighborProduct = neighborMetalProportion * neighborTemp;
                                    totalNeighborProduct = totalNeighborProduct + neighborProduct;
                                }

                            }
                            // Record the result of each metal.
                            totalNeighborProducts[metal] = totalNeighborProduct;

                            // Step 4: Perform the temperature calculation for the 
                            // current metal
                            metalTempValues[metal] = Cm * totalNeighborProducts[metal] / numNeighboringRegions;

                        }

                        // Step 5: Calculate the temperature of the region and store 
                        // it in the global variable
                        REGION_TEMPS[i][j] = 
                              (metalTempValues[0] * (1.0 - Math.abs(REGIONS[i][j][0] - (1.0 / 3.0))))
                            + (metalTempValues[1] * (1.0 - Math.abs(REGIONS[i][j][1] - (1.0 / 3.0))))
                            + (metalTempValues[2] * (1.0 - Math.abs(REGIONS[i][j][2] - (1.0 / 3.0))));

                        // Step 6: Rinse and repeat for the other regions!
                        System.out.println(REGION_TEMPS[i][j]);
                    }
                }
            }
            sheet.updateMetalSheet();
        }
        if(CSC375Assignment3Protocol.swapGrid)
            CSC375Assignment3Protocol.PASTTEMPS2 = REGION_TEMPS;
        else
            CSC375Assignment3Protocol.PASTTEMPS1 = REGION_TEMPS;
    }

}

class MetalSheet extends JPanel{
    private final int length;
    private final int height;
    private final double [][] regionTemps;
    
    private final JPanel gridPanel;
    
    JLabel[][] panels;
    
    public MetalSheet(int length, int height, double[][] regionTemps){
        
        this.length = length;
        this.height = height;
        this.regionTemps = regionTemps;
        
        panels = new JLabel[height][length];
        
        // Allows the user to zoom into the GUI
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        gridPanel = new JPanel(new GridLayout(height, length));
        this.add(gridPanel);
        
        for(int row = 0; row < height; row++){
            for(int col = 0; col < length; col++){
                panels[row][col] = new JLabel(" O ");
                panels[row][col].setOpaque(true);
                gridPanel.add(panels[row][col]);
            }
        }
        
        // Get the first result
        updateMetalSheet();
    }
    
    public void updateMetalSheet(){
        
        // Update the layout display
        for(int row = 0; row < height; row++){
            for(int col = 0; col < length; col++){
                if(regionTemps[row][col] <= 0.0)
                    panels[row][col].setBackground(Color.PINK);
                else if(regionTemps[row][col] > 0.0 && regionTemps[row][col] <= 10.0)
                    panels[row][col].setBackground(Color.BLUE);
                else if(regionTemps[row][col] > 10.0 && regionTemps[row][col] <= 20.0)
                    panels[row][col].setBackground(Color.CYAN);
                else if(regionTemps[row][col] > 20.0 && regionTemps[row][col] <= 25.0)
                    panels[row][col].setBackground(Color.GREEN);
                else if(regionTemps[row][col] > 25.0 && regionTemps[row][col] <= 30.0)
                    panels[row][col].setBackground(Color.YELLOW);
                else if(regionTemps[row][col] > 30.0 && regionTemps[row][col] <= 35.0)
                    panels[row][col].setBackground(Color.ORANGE);
                else if(regionTemps[row][col] > 35.0 && regionTemps[row][col] <= 40.0)
                    panels[row][col].setBackground(Color.RED);
                else
                    panels[row][col].setBackground(Color.MAGENTA);
            }
        }
        
    }
}

/*
                        References

https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html#later

https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html


*/
