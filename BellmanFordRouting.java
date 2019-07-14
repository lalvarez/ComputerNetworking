/**
 * Java Network Workbench 2 (JNW2)
 * Copyright 2018 Networking and Simulation Laboratory/George Mason University
 * 
 * Calculate routingMatrix using Bellman-Ford algorithm
 * 
 * @version 2.2.6
 */

package JNW2.utility;

import JNW2.*;
import JNW2.parsers.*;
import static JNW2.Constants.*;

public class BellmanFordRouting
{
  // simulation environment
  SimulationEngine simEngine = SimulationEngine.getInstance();
  ConnectivityMatrix cm = simEngine.getConnectivityMatrix();
  // NOTE that cm.connectionCost(fromRouter, toRouter) gives cost
  // of connection between them
  Topology topology = simEngine.getTopology();
  static SimLogger simLogger = SimLogger.getInstance(SimLogger.INFO);
  
  // instance variables
  int numberOfSubnets = topology.getNumberOfSubnets();
  static String configFileName; 
      
   /**
    * Count time through inner loop to compare Dijkstra algorithm
    * with Bellman-Ford 
    */
   int innerLoopCount = 0;
      
  /**
   * The cost at this point in algorithm to reach the destination
   * subscript router from the source router 
   * (source router's entry starts at zero)
   */
   float[] C = new float[numberOfSubnets+1];
  
    /**
   * The path at this point in algorithm from a source router to each  
   * destination router; from 0 to h hops which can be at most 
   * numberOfSubnets+1 hops
   * first dimension is the destination router (unused for 0 and source router)
   * second dimension is the routers in path; entry 0 is the source; 
   * last entry is the destination; -1 in unused positions
   */
  int[][] routingPaths = new int[numberOfSubnets+1][numberOfSubnets+1];
  int[] pathLengths = new int[numberOfSubnets+1];
  
  /**
   * stopping condition flag
   */
  boolean noChangeFromLastHop;

  /**
   * Builds a routing table
   * @return routingMatrix
   * @throws Exception
   */
  public int[][] computeRoutingMatrix() throws Exception
  {
    /**
     * JNW2 network array indexes range from 1 to numberOfSubnets
     * thus arrays must have dimension numberOfSubnets+1
     */
    int[][] routingMatrix = new int[numberOfSubnets+1][numberOfSubnets+1];
    
    // compute each row of the matrix
    for(int rowNumber = 1; rowNumber <= numberOfSubnets; ++rowNumber)
    {
      int[] routingRow = computeRoutingRow(rowNumber);
      for(int colNumber = 1; colNumber <= numberOfSubnets; ++colNumber)
        routingMatrix[rowNumber][colNumber] = routingRow[colNumber];
    }   
    
    // print out inner loop count for comparison
    simLogger.logInfo("Bellman-Ford inner loop count:" + innerLoopCount);
        
    // return to composite
    return routingMatrix;
  }  
  
  /**
   * Convenience function to display a routing path with its cost
   * @param label - string to display before the path
   * @param destRouter - router number at end of the path
   * @throws Exception
   */
  void showRoutingPath(String label, int destRouter)
  {
    // build a string containing the path
    String path = "";
    if(pathLengths[destRouter] > 0)
      for(int hopNumber = 0; hopNumber <= pathLengths[destRouter]; ++hopNumber)
        path += routingPaths[destRouter][hopNumber] + " ";
    simLogger.logInfo(label + "dest router:" + destRouter + " cost:" +
       C[destRouter] + " path:" + path);
  }
  
  /**
   * Copies a row of the routingPaths matrix to another row
   * Adds a router at end (thus should not be called if length
   * will exceed numberOfRouters)
   */
  void copyRoutingPathAndAdd(int copyFromRouter, int addRouter)
  {
    int copyLength = pathLengths[copyFromRouter];
    if(copyFromRouter != addRouter)
      for (int copyElement = 0; copyElement < copyLength; ++ copyElement)
        routingPaths[addRouter][copyElement] = routingPaths[copyFromRouter][copyElement];
    pathLengths[addRouter] = copyLength + 1;
    routingPaths[addRouter][copyLength] = addRouter;
  }
        
  /**
   * Builds a row in a routing table
   * parameter source router is the subnet number of router (at packet source)
   * that seeks to find an optimal sent of routes for its packets
   */
  private int[] computeRoutingRow(int sourceRouter) throws Exception
  {       
    /**
     * The next-hop router to reach each network number
     * (JNW2 network array indexes range from 1 to numberOfSubnets
     * (thus arrays must have dimension numberOfSubnets+1);
     * we will compute this array and return it.
     */
    int[] forwardRouters = new int[numberOfSubnets+1];
    
//
    //  function to compute an optimal path in routing table routes[][]
    //  using the Bellman-Ford algorithm
    //
    //  produces one row of the routing table
    //
    //  parameter source router is the subnet number of router (i.e. packet source)
    //  that seeks to find an optimal sent of routes for its packets
    //  {
    //    Bellman-Ford route optimization algorithm for JNW2
    //     
    //    Data structures:
    //      #1 matrix routingPaths [number of subnets + 1][number of subnets + 1]
    //         there is a path for each dest router; number of hops which can be 
    //         in the path grows as the hops variable is incremented
    //      #2 array pathLengths in #1 [number of subnets +1] 
    //      #3 array C costs found thus far for paths in #1 [number of subnets +1] 
    //      #4 boolean flag indicating stopping condition has been met
    //      #5 from router, to look up link cost
    //    
    //    for each dest router path (row) in routing paths
    //    {    
    //      initialize each element of #2 to zero;
    //      initialize C each element of #3 to HUGEFLOAT;
    //    }
    //    initialize #1 best paths[source router][0] to source router;
    //    initialize #2 path lengths [source router] to 1;
    //    initialize #3 costs for path to source router to 0;
    //
    //    for all hops from 0 to number of subnets minus one (longest possible path)
    //    {
    //      set stopping condition flag #4 true
    //      for all dest router from 1 to number of subnets
    //      {
    //        if dest router is the source router ignore it
    //          by continuing to the next dest router;
    //        test all routers from 1 to number of subnets except dest router
    //          to see if they have a next hop that provides a lower-cost route: 
    //        {
    //          increment inner loop count;
    //          to be useful the test router must have a cost less than HUGEFLOAT;
    //            for other values continue the loop;
    //          (next we need to consider the cost of link between a 'from router'
    //            and a 'to router')
    //          set #5from router to end of path: 
    //            value of #1[test router][#2[test router] -1]
    //          (except if #2[test router] is zero; then set
    //            from router to source router);
    //          get link connection cost with next hop router from cm.connectionCost;
    //          if link cost is zero or greater than or equal to HUGEFLOAT 
    //            it is not a usable link so continue to next test router;
    //          calculate possible cost as link cost + cost #3[from router];
    //          if possible cost is less than #3[dest router] it is a lower-cost path:
    //          {
    //            update best paths found this far using copyRoutingPathAndAdd 
    //              (from router, dest router);
    //            update costs #3[dest router] to possible cost;
    //            set stopping condition #4 false;
    //          }
    //        }
    //      }
    //      if hops is greater than zero and there was no change from last hop, 
    //      stopping condition is met: display value of hops using simLogger.logInfo()
    //      and break out of hops loop;
    //    }
    //
    //    reorder paths to a routing table that each router can use to determine
    //      which interface to use in forwarding packets:reorder data to a routing table:
    //    for each dest router 1 to number of subnets
    //      set forwardRouters[dest router]:
    //        for source router, set to source router;
    //        for all others, et to routing paths position [1];
    //    return forwardRouters;
    //  }.
    //
      
    //*********** student solution goes here ****************
    //    Data structures:
    //      #1 matrix routingPaths [number of subnets + 1][number of subnets + 1] there is a path for each dest router; number of hops which can be in the path grows as the hops variable is incremented
    //      #2 array pathLengths in #1 [number of subnets +1] 
    //      #3 array C costs found thus far for paths in #1 [number of subnets +1] 
    //      #4 boolean flag indicating stopping condition has been met
    //      #5 from router, to look up link cost 
    for(int i = 0; i<routingPaths.length; i++){
      pathLengths[i] =0;
      C[i]= HUGEFLOAT;
    }
      
    routingPaths[sourceRouter][0] = sourceRouter;
    pathLengths[sourceRouter] = 1;
    C[sourceRouter] = 0;
    noChangeFromLastHop = false;
    
    //    for all hops from 0 to number of subnets minus one (longest possible path)
    String hops_s;
    int fromRouter; float linkcost, possiblecost;
    for(int hops =0;hops <=numberOfSubnets -1; hops++){
        noChangeFromLastHop = true;
      for(int destRouter =1;destRouter < numberOfSubnets+1; destRouter++){
        //if dest router is the source router ignore it by continuing to the next dest router;
        if(destRouter == sourceRouter) continue;
        for(int router =1; router < numberOfSubnets+1;router++){
            if( router != destRouter){
            innerLoopCount ++;
            if( C[router] < HUGEFLOAT){
              if(pathLengths[router] != 0) fromRouter = routingPaths[router][pathLengths[router]-1];
              else fromRouter = sourceRouter;
              linkcost = cm.connectionCost(fromRouter,destRouter);
              if (linkcost <0.0001 || linkcost>= HUGEFLOAT) {
              continue;}
              else{
                possiblecost = linkcost+C[fromRouter];
                if(possiblecost < C[destRouter]){
                  copyRoutingPathAndAdd(fromRouter, destRouter);
                  C[destRouter] = possiblecost;
                  noChangeFromLastHop = false;
                }
              }
            }
           }
          }//router loop
        }//destRouter loop
          
        if (hops>0 && noChangeFromLastHop){
          hops_s = String.valueOf(hops);
          simLogger.logInfo(hops_s);
          break;
          }
        }//hops loop
            
    for(int destRouter = 1; destRouter<numberOfSubnets+1; destRouter++){
      if (destRouter == sourceRouter) forwardRouters[destRouter] = sourceRouter;
      else forwardRouters[destRouter] = routingPaths[destRouter][1];
    }
    
    

    //*********** student solution ends here ****************
    
    // print results: paths with their costs;
    String forwarding = "";
    for (int destRouter = 1; destRouter <= numberOfSubnets; ++destRouter)
      showRoutingPath("Bellman-Ford routing ", destRouter);
      
    // return forward routers
    return forwardRouters;
  
   }// end computeRoutingMatrix()
  
  /**
   * make a test run of BellmanFordRouting
   * 
   * includes printing routing table but not running simulation
   */
  static class TestBellmanFordRouting
  {   
    public TestBellmanFordRouting()
    {
      // setup simulation environment
      Topology testTopology = new Topology(configFileName);
      SimulationEngine testSimEngine = new SimulationEngine(testTopology);
      testSimEngine.setInstance(testSimEngine);
      simLogger.setPrintAtLayers(testTopology.getPrintAtLayers());
      System.out.println("Running Bellman-Ford routing test");
 
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

    }// end TestBellmanFordRouting() constructor 
    
  }// end class TestBellmanFordRouting
    
  // test BellmanFordRouting
  public static void main(String args[])
  {
    configFileName = args[0];
    TestBellmanFordRouting testBellmanFordRouting;
    testBellmanFordRouting = new TestBellmanFordRouting();
    
  }// end main()
  
}// end class BellmanFordRouting

