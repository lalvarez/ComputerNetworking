/**
 * Java Network Workbench 2 (JNW2)
 * Copyright 2013-2017 Networking and Simulation Laboratory/George Mason University
 * 
 * Calculate routingMatrix using Dijkstra's algorithm
 * 
 * @version 2.2.7
 */

package JNW2.utility;

import JNW2.*;
import static JNW2.Constants.*;
import JNW2.parsers.*;

public class DijkstraRouting
{
  // simulation environment
  SimulationEngine simEngine = SimulationEngine.getInstance();
  ConnectivityMatrix cm = simEngine.getConnectivityMatrix();
  Topology topology = simEngine.getTopology();
  static SimLogger simLogger = SimLogger.getInstance(SimLogger.INFO);
  
  // instance variables
  int numberOfSubnets = topology.getNumberOfSubnets();
  static String configFileName;
      
  /**
   * Count times through inner loop to compare Dijkstra algorithm
   * with Bellman-Ford 
   */
   int innerLoopCount = 0;
    
  /**
   * Builds a routing table
   * @return routingMatrix
   * @throws Exception
   */
  public int[][] computeRoutingMatrix() throws Exception
  {
    // JNW2 network array indexes range from 1 to numberOfSubnets
    // thus arrays must have dimension numberOfSubnets+1
    int[][] routingMatrix = new int[numberOfSubnets+1][];
    
    // compute each row of the matrix
    for(int rowNumber = 1; rowNumber <= numberOfSubnets; ++rowNumber)
      routingMatrix[rowNumber] = computeRoutingRow(rowNumber);
        
    // print out inner loop count for comparison
    simLogger.logInfo("Dijkstra inner loop count:" + innerLoopCount);

    // return to composite
    return routingMatrix;
  }
  
  /**
   * Builds a row in a routing table
   */
  private int[] computeRoutingRow(int sourceRouter) throws Exception
  {
    
    /**
     * The next-hop router to reach each network number
     * (JNW2 network array indexes range from 1 to numberOfSubnets
     ( thus arrays must have dimension numberOfSubnets+1)
     */
    //
    //  function to compute an optimal routing table in routes[][]
    //  using Dijkstra's algorithm
    //
    // source is the subnet number of router (at packet source) that seeks
    // to find an optimal sent of routes for its packets
    //
    // Dijkstra's route optimization algorithm for JNW2
    // required storage:
    //  array R that will represent Dijkstra's set R;
    //  array C of costs associated with path to a host;
    // initialize R[0] to source;
    
    // for all n from 1 to number subnets,
    // {
    //  initialize C[n] to cost, as seen from source, of the link from n to source;
    //  initialize forward routers[n] to n;
    // }
    // for all h from 1 to number of subnets,
    // {
    //  look for least-cost way to get to subnet h hops away:
    //  set cost of h to a large number;
    //  for all n from 1 to number of subnets,
    //  {
    //    increment inner loop count;
    //    for all j from 0 to h-1,
    //    {
    //      if any R[j] contains n, skip this n;
    //    }
    //    update R if the new R[h] offers a lower-cost path:
    //    if C[n] is less than cost_of_h
    //      set cost of h to that C[n] and R[h] to n;
    //  }
    
    //
    //  update R if the new R[h] offers a lower-cost path:
    //  for all n from 1 to number of subnets,
    //  {
    //    find cost of h to n as seen from source of the link from R[h] to n;
    //    if C[R[h]] plus cost of h to n is less than C[n],
    //    {
    //      set C[n] to C[R[h]] plus cost of h to n;
    //
    //      update R if the new R[h] offers a lower-cost path:
    //      if C[n] is less than cost_of_h
    //      set forward routers[n] to forward routers[R[h]];
    //    }
    //  }
    //
    //  check for stopping rule (all routers are contained in R):
    //  do for all n from 1 to number of subnets, 
    //  {
    //    do for all j from 0 to h,
    //    {
    //      look for n in all R[j];
    //    }
    //    if every n is found, stop
    //  }
    //}.
      
    // student solution goes below
    // and be sure to include your name
    // student name: Laura Alvarez
    //*********** student solution goes here ****************
    
    int[] forwardRouters = new int[numberOfSubnets+1];
    
    //Storage
    float[] C = new float[numberOfSubnets+1];
    int [] R = new int [numberOfSubnets+1];
    
    //Inicialization
    R[0] = sourceRouter;
    
    for(int n =1; n <numberOfSubnets+1; n++){
      C[n] = cm.pathCost(sourceRouter,sourceRouter, n);
      forwardRouters[n] = n; //NOT SUREEEEE
    }
    float [] cost_of_h = new float[numberOfSubnets+1];
    //EEEEEEEEEEE 
    for(int h =1; h <numberOfSubnets; h++){
      //add to R the router with the least-cost path to source:
      cost_of_h[h] = 10000;
      boolean skip = false;
      etiqueta:
      for(int n =1; n <numberOfSubnets+1; n++){
        innerLoopCount++;
          for(int j =0; j <h; j++){
            if ( R[j] == n){
              continue etiqueta;
            }
          }
          
            if(C[n] < cost_of_h[h]){
              cost_of_h[h] = C[n];
              R[h]=n;
            }
          
      }
      //update the least-cost paths:
      for(int n =1; n <numberOfSubnets+1; n++){
        float cost_of_h_to_n = cm.pathCost(sourceRouter,R[h], n);//EEEEEEEEU\
        if(C[R[h]]+ cost_of_h_to_n < C[n]){
          C[n]= C[R[h]]+ cost_of_h_to_n ;
          if(C[n] < cost_of_h[h]){ //EEEEEEEEY
          forwardRouters[n] = forwardRouters[R[h]];
          }
        }
      }
      int counter =0;
      boolean found = false;
      for(int n =1; n <numberOfSubnets+1; n++){
        for(int j =0; j <h+1; j++){
          if(R[j] == n){
            counter++;
          } 
        }
        
      }
      //if(counter == numberOfSubnets) break;
      
    }
    
    //*************** end student solution *****************
    for(int g =R.length -1; g>0;g--){
      R[g]=R[g-1];
    }
    R[0] = sourceRouter;
    return R;
    
   
    
  }// end computeRoutingMatrix()
  
  /**
   * make a test run of DijkstraRouting
   * 
   * includes printing routing table but not running simulation
   */
  static class TestDijkstraRouting
  {   
    public TestDijkstraRouting()
    {
      // setup simulation environment
      Topology testTopology = new Topology(configFileName);
      SimulationEngine testSimEngine = new SimulationEngine(testTopology);
      testSimEngine.setInstance(testSimEngine);
      simLogger.setPrintAtLayers(testTopology.getPrintAtLayers());
        
      // load network topology from config file
      ConfigParser configParser = new ConfigParser(testTopology);
    
      // parse the configuration
      try
      {
        if(!configParser.parseConfig())return;
      }
      catch(Exception e)
      {
        System.out.println("parse of file:" + configFileName + " bad input:" +
          e.getMessage());
        return;
      }
      
      // load the configuration into ConnectivityMatrix
      try
      {
        testSimEngine.loadConfiguration();
      }
      catch(Exception e)
      {
        simLogger.logError("Exception loading configuration:" + e);
        simLogger.logException(e);
      }

    }// end TestDijkstraRouting() constructor 
    
  }// end class TestDijkstraRouting
    
  // test DijkstraRouting
  public static void main(String args[])
  {
    configFileName = args[0];
    TestDijkstraRouting testDijkstraRouting;
    testDijkstraRouting = new TestDijkstraRouting();
    DijkstraRouting jeje = new DijkstraRouting();
  
    
    
    
  }// end main()
  
}// end class DijkstraRouting

