package csc375assignment3multiserver;

import java.io.*;
import java.net.*;

public class CSC375Client {
    
    public static void main(String[] args) throws IOException {
        
        /*
        if(args.length != 2) {
            System.err.println(
                "Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }
        */
 
        String hostName = "MacBook-Pro.local"; // Change this to the hostname 
        //of your own computer
        int portNumber = 7; // Ensure the portNumber is the same for the client
        // AND the server (must be an unused port)
 
        try(
            Socket kkSocket = new Socket(hostName, portNumber);
        ) {

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        }
    }
}
