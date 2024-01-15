package csc.pkg375.assignment.pkg1;

import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

// This class represents a single factory arrangement solution graphically
class FactorySolution extends JPanel {
    
    // Will be used to call the size of the factory (Format is SIZExSIZE,
    // so a size of 8 will represent an 8x8 factory for example)
    public static final int SIZE = FactoryAffinity.SIZE;
    
    // Will be used to organize each panel of the factory to its desiginated 
    // station type
    private final JPanel gridPanel;
    
    // Prepare a SIZE * SIZE JLabel array to hold every panel of the factory
    JLabel[] panels = new JLabel[SIZE * SIZE];
    
    // Used to display the current metric for each factory
    JLabel metricTag;
    
    /**
     * Converts a StationType to a Color
     * @param stationColor The station to draw in the GUI
     * @return The color of the station
     */
    public static Color getColor(StationType stationColor){
        if(stationColor == StationType.Green)
            return Color.green;
        else if(stationColor == StationType.Red)
            return Color.red;
        else if(stationColor == StationType.Blue)
            return Color.blue;
        else if(stationColor == StationType.Yellow)
            return Color.yellow;
        else
            return Color.black;
    }
    
    /**
     * Constructor that prepares a new Factory for GUI deployment
     * @param factory Contains the layout of the factory
     * @param metric Provides the affinity metric score for the factory
     */
    public FactorySolution(FactoryAffinity factory, int metric){
        
        // Allows the user to zoom into the GUI
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // Prepare the Grid Panel for incoming FactorySolution Stations
        gridPanel = new JPanel(new GridLayout(SIZE, SIZE));
        this.add(gridPanel);
        
        // Helps with keeping track of the metric score
        metricTag = new JLabel();
        this.add(metricTag);
        
        // Initialize and add each factory panel
        for(int i = 0; i < panels.length; i++){
            panels[i] = new JLabel(" O ");
            panels[i].setOpaque(true);
            gridPanel.add(panels[i]);
        }
        
        // Get the first result
        updateFactory(factory, metric);
        
    }
    
    /**
     * Updates the factory information
     * @param factory Contains the layout of the factory
     * @param metric Provides the affinity metric score for the factory
     */
    void updateFactory(FactoryAffinity factory, int metric){
        
        int index = 0;
        
        // Update the layout display
        for(int row = 0; row < SIZE; row++){
            for(int col = 0; col < SIZE; col++){
                panels[index].setBackground(getColor(factory.station(row, col)));
                index++;
            }
        }
        // Display the updated metric
        metricTag.setText("Current Metric: " + metric);
        
    }
    
}
