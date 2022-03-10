

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.concurrent.Semaphore;

/**
 *
 * @author Kerly Titus
 */
public class Driver {

    /** 
     * main class
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Semaphore lock = new Semaphore(1);
        Semaphore networkLock = new Semaphore(1);

    	Network objNetwork = new Network(networkLock);            /* Activate the network */
        objNetwork.start();

        Client objClient1 = new Client("sending");          /* Start the sending client thread */
        objClient1.start();
        Client objClient2 = new Client("receiving");        /* Start the receiving client thread */
        objClient2.start();
        
      /*..............................................................................................................................................................*/

        Server server1 = new Server("Thread1", lock);
        Server server2 = new Server("Thread2", lock);

        server1.start();
        server2.start();

    }
    
 }
