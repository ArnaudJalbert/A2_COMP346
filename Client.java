
import java.util.Scanner;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.InputMismatchException;
import java.util.concurrent.Semaphore;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/** Client class
 *
 * @author Kerly Titus
 */

public class Client extends Thread { 
    
    private static int numberOfTransactions;   		/* Number of transactions to process */
    private static int maxNbTransactions;      		/* Maximum number of transactions */
    private static Transactions [] transaction; 	        /* Transactions to be processed */
    private String clientOperation;    			/* sending or receiving */
    public Semaphore lock;
       
	/** Constructor method of Client class
 	 * 
     * @return 
     * @param
     */
     Client(String operation, Semaphore lock)
     { 
       if (operation.equals("sending"))
       { 
           System.out.println("\n Initializing client sending application ...");
           this.lock = lock;
           numberOfTransactions = 0;
           maxNbTransactions = 100;
           transaction = new Transactions[maxNbTransactions];  
           clientOperation = operation; 
           System.out.println("\n Initializing the transactions ... ");
           readTransactions();
           System.out.println("\n Connecting client to network ...");
           String cip = Network.getClientIP();
           if (!(Network.connect(cip)))
           {   System.out.println("\n Terminating client application, network unavailable");
               System.exit(0);
           }
       	}
       else
    	   if (operation.equals("receiving"))
           { 
    		   System.out.println("\n Initializing client receiving application ...");
               this.lock = lock;
    		   clientOperation = operation; 
           }
     }
           
    /** 
     * Accessor method of Client class
     * 
     * @return numberOfTransactions
     * @param
     */
     public int getNumberOfTransactions()
     {
         return numberOfTransactions;
     }
            
    /** 
     * Mutator method of Client class
     * 
     * @return 
     * @param nbOfTrans
     */
     public void setNumberOfTransactions(int nbOfTrans)
     { 
         numberOfTransactions = nbOfTrans;
     }
         
    /** 
     * Accessor method of Client class
     * 
     * @return clientOperation
     * @param
     */
     public String getClientOperation()
     {
         return clientOperation;
     }
         
    /** 
     * Mutator method of Client class
	 * 
	 * @return 
	 * @param operation
	 */
	 public void setClientOperation(String operation)
	 { 
	     clientOperation = operation;
	 }
         
    /** 
     * Reading of the transactions from an input file
     * 
     * @return 
     * @param
     */
     public void readTransactions()
     {
        Scanner inputStream = null;     	/* Transactions input file stream */
        int i = 0;                      		/* Index of transactions array */
        
        try
        {
        	inputStream = new Scanner(new FileInputStream("transaction2.txt"));
        }
        catch(FileNotFoundException e)
        {
            System.out.println("File transaction.txt was not found");
            System.out.println("or could not be opened.");
            System.exit(0);
        }
        while (inputStream.hasNextLine( ))
        {
            try
            {   transaction[i] = new Transactions();
                transaction[i].setAccountNumber(inputStream.next());            /* Read account number */
                transaction[i].setOperationType(inputStream.next());            /* Read transaction type */
                transaction[i].setTransactionAmount(inputStream.nextDouble());  /* Read transaction amount */
                transaction[i].setTransactionStatus("pending");                 /* Set current transaction status */
                i++;
            }
             catch(InputMismatchException e)
            {
                System.out.println("Line " + i + "file transactions.txt invalid input");
                System.exit(0);
            }
            
        }
        setNumberOfTransactions(i);		/* Record the number of transactions processed */
        
        System.out.println("\n DEBUG : Client.readTransactions() - " + getNumberOfTransactions() + " transactions processed");
        
        inputStream.close( );

     }
     
    /** 
     * Sending the transactions to the server 
     * 
     * @return 
     * @param
     */
     public void sendTransactions()
     {
         int i = 0;     /* index of transaction array */
         
         while (i < getNumberOfTransactions())
         {


             // YIELD: Object yields when in buffer is full.
             while(Network.getInBufferStatus().equals("full")) {
                 this.yield();
             }
                                              	
            transaction[i].setTransactionStatus("sent");   /* Set current transaction status */
           
             System.out.println("\n DEBUG : Client.sendTransactions() - sending transaction on account " + transaction[i].getAccountNumber());
            
            Network.send(transaction[i]);                            /* Transmit current transaction */
            i++;          
         }
         
    }
         
 	/** 
  	 * Receiving the completed transactions from the server
     * 
     * @return 
     * @param transact
     */
     public void receiveTransactions(Transactions transact)
     {
         int i = 0;     /* Index of transaction array */
         
         while (i < getNumberOfTransactions())
         {
             while (Network.getOutBufferStatus().equals("empty") && !Network.getServerConnectionStatus().equals("disconnected"))
                 this.yield();
                                                                            	
            Network.receive(transact);                               	/* Receive updated transaction from the network buffer */
            
            System.out.println("\n DEBUG : Client.receiveTransactions() - receiving updated transaction on account " + transact.getAccountNumber());
            
            System.out.println(transact);                               /* Display updated transaction */    
            i++;
         } 
    }
     
    /** 
     * Create a String representation based on the Client Object
     * 
     * @return String representation
     * @param 
     */
     public String toString() 
     {
    	 return ("\n client IP " + Network.getClientIP() + " Connection status" + Network.getClientConnectionStatus() + "Number of transactions " + getNumberOfTransactions());
     }
    
       
    /** Code for the run method
     * 
     * @return 
     * @param
     */
    public void run()
    {
        Transactions transact = new Transactions();
        long sendClientStartTime, sendClientEndTime, receiveClientStartTime, receiveClientEndTime;

        /* Implement here the code for the run method ... */

        // "The client class reads all transactions and save them in an array"

        if (clientOperation.equals("sending")) {

            this.readTransactions(); // check method definition to see if it fits with the comment above

            // "using Network.send, transfers the transactions to the network input buffer
            // and it yields the cpu in case the network input buffer is full"

            sendClientStartTime = System.currentTimeMillis();

            this.sendTransactions();

            sendClientEndTime = System.currentTimeMillis();

            System.out.println("\n Terminating client sending thread - Running time " + (sendClientEndTime - sendClientStartTime) + " milliseconds");

        } else {

            // "using Netwok.receive() method, client class retrieves the updated trasactions
            // from the netweork output buffer and it yields the cpu in case the buffer is empty.
            // Each updateed trasaction received is displayed immediately on the screen."

            receiveClientStartTime = System.currentTimeMillis();

            this.receiveTransactions(transact); // check method definition to see if it fits with the comment above

            receiveClientEndTime = System.currentTimeMillis();

            Network.disconnect(Network.getClientIP());

            System.out.println("\n Terminating client receiving thread - Running time " + (receiveClientEndTime - receiveClientStartTime) + " milliseconds");
        }
    }
}
