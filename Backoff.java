/**
 * Java Network Workbench 2 (JNW2)
 * Copyright 2013-2018 Networking and Simulation Laboratory/George Mason University
 * 
 * Implements Binary Exponential Backoff for ContentionLan
 * 
 * @version 2.2.7
 */

package JNW2.interfaces;

import JNW2.*;
import JNW2.links.*;
import JNW2.nodes.*;
import JNW2.stack.*;
import JNW2.utility.*;

/**
 * Implements Binary Exponential Backoff for ContentionLan
 * @author JMP
 */
public class Backoff 
{
  private final static int MAX_BACKOFF = 16; // maxmimum times to backoff
  private final static int MAX_DOUBLE = 10;  // maxmimum times to double slots
  private final static int FAIL = -1;        // return this after MAX_BACKOFF tries
  
  // instance variable
  int backoffCount;
  long seed;
  private final RandomNumber random;
  private final byte interfaceNumber;
  private int maxBackoffSlots; // current max range of backoff values
  
  public Backoff(byte lanInterfaceNumber)
  {
    interfaceNumber = lanInterfaceNumber;
    seed = 100*lanInterfaceNumber + 1;
    random = new RandomNumber(seed);
    backoffCount = 0;
    maxBackoffSlots = 0; 
  }
  /**
   * Binary exponential backoff algorithm:
   *   try up to sixteen times
   *   first 10 tries, return 2^n-1 for nth try
   *   next 6 tries, return 2^10-1
   *   after 16 tries, return -1
   * @return number of backoff slots
   */
  public int binaryExponentialBackoff()
  {
    // student solution goes below
    // and be sure to include your name
    // student name: Laura Alvarez
   
    //First we update backpff count
    backoffCount++;
    if(backoffCount <= 10){
        maxBackoffSlots = (int)(Math.pow(2, backoffCount))-1;} 
    else if(backoffCount > 10 && backoffCount < 16){
        maxBackoffSlots = (int)(Math.pow(2, 10))-1;}
    else if(backoffCount > 16){
      return -1;} //ERROR
    
    // ********** end student solution *********************************
    
    double rand;
    rand = random.getRandomFloat();
    
    // return random int number of slots
    return (int)Math.round(rand * maxBackoffSlots);
  }
    
  /**
   * resets backoff count parameter to zero
   */
  public void zeroBackoffCount()
  {
    backoffCount = 0;
    maxBackoffSlots = 0; 
  }
  
  /**
   * returns current backoff count
   * @return backoffCount
   */
  public int getBackoffCount()
  {
    return backoffCount;
  }
  
  /**
   * returns current max backoff slots
   * @return maxBackoffSlots
   */
  public int getMaxBackoffSlots()
  {
    return maxBackoffSlots;
  }
  
  /**
   * tests the basic functions of Backoff 
   */
  static class BackoffTest
  {
    public BackoffTest()
    {
      // setup simulation environment
        SimLogger simLogger = SimLogger.getInstance(SimLogger.INFO);
        Topology topology = new Topology();
        SimulationEngine simEngine = new SimulationEngine(topology);
        simEngine.setInstance(simEngine);
      
      // configure network and run test
      try
      {
        // setup LAN and ContentionInterface where Backoff will exist
        int[] lanDataRate = {0,10};
        topology.setSubnetDataRate(lanDataRate);
        ContentionLan lan = new ContentionLan(1);
        Node node = new Node(1, 1, (byte)51, 1);
        Stack stack = new Stack(node);
        ContentionInterface lanInterface = new ContentionInterface(stack, 1, 1, (byte)1);
        Backoff backoff = lanInterface.getBackoff();
        
        // test backoff against spec
        System.out.println("TRY   RANDOM SLOTS    MAX SLOTS");
        for(int index=1; index<20; ++index)
          System.out.println(index + "        " +
            backoff.binaryExponentialBackoff() + "             " +
            backoff.getMaxBackoffSlots());
        backoff.zeroBackoffCount();
        System.out.println("\nRESET");
        for(int index=1; index<20; ++index)
          System.out.println(index + "        " +
           backoff.binaryExponentialBackoff() + "             " +
            backoff.getMaxBackoffSlots());
      }
      catch(Exception e)
      {
        System.err.println("Exception in BackoffTest:" + e.getMessage());
        simLogger.logException(e);
      }
    }//end BackoffTest() constructor  
  }// end class BackoffTest
  
  // Test Backoff
  public static void main(String[] args)
  {
    BackoffTest backoffTest;
    backoffTest = new BackoffTest();
  }// end main()  
  
}// end class Backoff

