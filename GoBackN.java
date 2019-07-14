/**
 * Java Network Workbench 2 (JNW2)
 * Copyright 2013-2018 Networking and Simulation Laboratory/George Mason University
 * 
 * Reliable Data Link Control Layer module for Network Workbench,
 * modified for Java Network Workbench 2. Where C++ NW used
 * pointers to structures (in particular, link_interface_state),
 * in Java we have SerialInterface dlSendState associated.
 * 
 * @version 2.2.7
 */

package JNW2.interfaces;

import static JNW2.Constants.*;
import JNW2.*;
import JNW2.message.*;
import JNW2.nodes.*;
import JNW2.stack.*;

public class GoBackN extends Interface
{    
   // instance variable
   int interfaceToWhichAttached;
    
   /** 
    * Creates a new instance of GoBackN 
    * @param stack - protocol stack for this Interface
    * @param interfaceNumber - ID of this Interface
    */
   public GoBackN(Stack stack, int interfaceNumber) 
   {
      super(stack);
      
      // Save to number of the node to which this interface is attached.
      interfaceToWhichAttached = interfaceNumber ;
   }

//   NOTES:
//   The ARQ used here is go-back-n.
//   1. There are two state variables that are very important
//   to the Workbench DLC, and occur at each interface:
//     dlSendState indicates which of possible states the send
//       software for the interface occupies
//     interfaceActive indicates whether the interface is actively sending
//       data and/or supervisory frames (ACK frames in response to the
//       other end of the link do not count here); the interface will
//       remain active if frames have been sent for which no ACK has 
//       been received, or if there are more frames on the queue.
//   2. The physical layer will automatically schedule a call to 
//   sendReliableFrame() each time a frame transmission is completed.
//   It is important to have dlSendState correct so sendReliableFrame() picks
//   up in the right place when this happens.
//   3. The NW DLC send algorithm is conservative in that, if it has  
//   no new data to send and frames in the buffer have not been ACKed,
//   it simply retransmits them during idle line time.  This can result
//   in better performance in the presence of errors, in that a frame
//   that suffered an error may be retransmitted before a NACK is
//   received and thus arrive sooner than it otherwise would have (this 
//   is a reasonable practice for a point-to-point link, but not for a 
//   shared link layer such as Ethernet where other senders might use  
//   the capacity instead).  A consequence of this behavior is that when
//   there are no frames to send, a node will be sending the last frame 
//   sent, or a supervisory frame.
//
//   dlInit
//
//   The current JNW2 DLC provides only an unreliable
//   link initiation. It just sets linkActive true,
//   dlSendState to DL_SENDING, and initializes the sequence
//   number state for this end of the link.
//
//   This is how a reliable DL initialization would work:
//
//   Before DlcLayer.send() is called for a link, that link must be
//   initialized; connection is established using the normal
//   three-way handshake.  To do this:
//   a.  the sending end sends an INIT_LINK frame including its
//   initial sequence number SN=0 to the receiving end  and continues
//   to send at intervals of MAX_LINK_RTT until response is received;
//   b.  the receiving end replies with a receiver_ready frame
//   including its initial sequence number SN=0, saves
//   the received SN as RN, and sets linkActive to TRUE for its
//   end of this link;
//   c.  when the sending end receives this reply, it saves the
//   received SN as RN, sets SNmin=SNmax=0, and sets
//   linkActive to TRUE for its end of  this link.  The link is
//   now ready to start sending (DlcLayer.send() case sending).
//
//
// NW dl_send algorithm
// --------------------
//
//   This is how the DL send works after the link is initialized:
//
//   Each end can send up to DL_WINDOW_FRAMES frames while waiting
//   for an ACK.  The algorithm for the sending side is below (initial 
//   dl_send_state=dl_sending).  
//
//
// state WAITING (in this state the DLC has been sending but has been blocked 
// by the fact the window is full; it wakes up here after a timeout and takes 
// some action):
// {
//   if the range between SNmin and SNmax is not smaller than DL_WINDOW_FRAMES
//   (the window is full so continue with the proper actions for waiting),
//   {
//     send the frame in buffer position SNmin;
//     after transmission continue with case waiting;
//   }
//   else (the range between SNmin and SNmax is smaller than the window),
//    an ACK has been received so continue with case dl_sending;
// }
//
// state DL_SENDING (in this state the DLC expects to dequeue and send 
// the next frame, the first step is to dequeue and buffer the frame):
// {
//   if the range between SNmin and SNmax is smaller than the window,
//   {
//     attempt to take a frame from the input queue;
//     if there is a frame available,
//     {
//       set SN=SNmax;
//       buffer the frame in position SNmax;
//       increment SNmax|mod DL_WINDOW_MAX;
//     }
//     else if frames remain unacked you need to continue re-sending the 
//     SNmin frame,
//       set SN=SNmin;
//     otherwise nothing remains to be sent,
//     { 
//       set link_active to FALSE; 
//       set send_state to waiting; 
//       escape from this case;
//     }
//     this leaves the next frame to be sent in buffer position SN,
//     finish that frame,
//     {
//       insert RN;
//       insert CRC-FCS;
//       stuff frame;
//     }
//     if the window becomes full with this frame,
//       set dlSendState to waiting so no more frames are sent until ACKs     
//       are received;
//     finish by sending the frame that was just made to the physical 
//     layer;
//   }
// }
//
//
// state SEND_SUPV (in this state send a receiver_ready supervisory frame 
// because a send_supv packet has been received indicating dl_receive found 
// a need for an ACK and queued that packet):
// {
//   make the supervisory packet;
//   send it;
// }.
//
//
// NW DLC receive algorithm
// ------------------------
//
// When a frame passing the CRC check is received,
// {
//    if RN of that frame is within the current window
//    and above SNmin in the window,
//       set SNmin = RN of that frame;
//    if SN of that frame == RN sent to other end,
//    {
//       release the contents to the network layer;
//       increment RN using modular arithmetic;
//    }
// }.
//
   
   // methods implementing logic of go-back-n ARQ
   // these were in dllogic.cpp in C++ NW

   /** tests whether range between two numbers is smaller than
    *  the window (DL_WINDOW_FRAMES), given DLC counter range (DL_WINDOW_MAX)
    *  @param Nmin is logically lower end of range, the lowest number not ACKed
    *  @param Nmax is logically higher end of range, the next number to be used 
    * @return true if range is smaller
    */
   public boolean LTwindow(byte Nmin, byte Nmax) 
   {
      int testmax = Nmax;
      if(Nmin > Nmax) testmax = testmax + DL_WINDOW_MAX;
      return(testmax-Nmin < DL_WINDOW_FRAMES);
   }

   //************************************************************
   // student work starts here
   // and be sure to add your name below
   // student name: Laura Alvarez
  /**  
    * tests whether range between two numbers is within the
    * window (DL_WINDOW_FRAMES), given DLC counter range (DL_WINDOW_MAX)
    * @param Nmin is logically lower end of range, the lowest number not ACKed
    * @param Nmax is logically higher end of range, the next number to be used 
    * @return true if range is within
    */
   public boolean INwindow(byte Nmin, byte Nmax) 
   {
     //// student replaces false with appropriate code
     
     // similar to LTwindow, but in this case "within"
     // return true also if range between numbers is equal to DL_WINDOW_FRAMES
      int testmax = Nmax;
      if(Nmin > Nmax) testmax = testmax + DL_WINDOW_MAX;
      return(testmax-Nmin <= DL_WINDOW_FRAMES);
   }

   /** 
    * tests whether the window has become full 
    * @param SNmin is SN of the lowest frame sent and not ACKed
    * @param SNmax is one greater (mod DL_WINDOW_MAX) than SN of
    *         highest frame sent and not ACKed 
    * @return true if window is full
    */
   public boolean windowFull(byte SNmin, byte SNmax) 
   {
     //// student replaces false with appropriate code
     
     // As SNmax is the next number to use (out of the window),
     // then is full if the range is equal
     int testmax = SNmax;
     if(SNmin > SNmax) testmax = testmax + DL_WINDOW_MAX;
     return (testmax - SNmin >= DL_WINDOW_FRAMES);
   }

   /** 
    * tests whether frames have been sent for which ACKs
    * have not been received
    * @param SNmin is SN of the lowest frame sent and not ACKed
    * @param SNmax is one greater (mod DL_WINDOW_MAX) than SN of
    *         highest frame sent and not ACKed
    * @return true if such frames have been sent
    */
   public boolean framesRemainUnacked(byte SNmin, byte SNmax) 
   {
     //// student replaces false with appropriate code
     
     // is there are frames waiting for ACK, then SNmax or SNmin
     // must be greater than 0
     return (SNmin != 0 && SNmax !=0);
   }

   /** 
    * returns incremented SNmax for an interface within
    * the modular range, which is of size DL_WINDOW_MAX 
    * @param SNmax is logical top end of window
    * @return incremented SNmax
    */
   public byte incrementSNmax(byte SNmax) 
   {
     //// student replaces 0 with appropriate code
     return (byte) ( SNmax++ % DL_WINDOW_MAX );
   }

   /** 
    * returns updated value of SNmin in GoBackNState 
    * @param newSNmin replaces logical bottom end of window
    * @return updated value
    */
   public byte updateSNmin(byte newSNmin) 
   {
       //// student replaces 0 with appropriate code
     
       // after checking if receivedRN (newSNmin when the function is called)
      // is in window, the receivedRN becomes the new SNmin (same receivedRN)
       return newSNmin;
   }

   /** 
    * updates the RN of interface, based on received SN 
    * @param receivedSN replacement for SN
    * @return updated RN
    */
   public byte updateRN(byte receivedSN) {
     
      //// student replaces 0 with appropriate code
      
      // After checking if the receivedSN frame is accepoted, this funtion
      // is called to update the interface RN (last frame received)
      //The receivedSN becomes the interfcae RN
      
      return receivedSN;
   }

   /** tests whether to accept frame, based on its SN
    *  and the RN associated with receiving interface
    * @param  receivedSN is SN found in the frame
    * @param  interfaceRN is RN state of the Interface
    * @return true to accept frame
    */
   public boolean acceptFrame(byte receivedSN, byte interfaceRN) {
     
      //// student replaces false with appropriate code
      
      // to accept the frame, the receivedSN must be >= to interfaceRN
      // and within current sliding window     
      // The last ACK moved the start of the window to RN (next frame requested)
      // Cosequently interfaceRN is the start of the current window

      int windowEnd = interfaceRN + DL_WINDOW_FRAMES;
      return (receivedSN >= interfaceRN) && (receivedSN < windowEnd);
   }
   
  // Test code
   @SuppressWarnings("empty-statement")
  void testGoBackN()
  {   
    try
    {
      // specify Topology matrices for test nodes to send and receive
      // WAN: three subnets interconnected by network #1 router
      topology.setNumberOfSubnets(3);
      topology.setDiameter(2);
      topology.setReliableLinks(true);
      int links[][] = {{0},{0,0,1544,384},{0,1544,0,0},{0,384,0,0}};
      topology.setLinksMatrix(links);
      byte exitInterfaces[][] = {{0},{0,0,1,2},{0,3,0,0},{0,4,0,0}};
      topology.setExitInterfacesMatrix(1,exitInterfaces);
      int[][] routingMatrix = {{0},{0,1,2,3},{0,1,2,1},{0,1,1,3}};
      topology.setStaticRoutingMatrix(routingMatrix);
      
      // setup LAN: 
      // subnet 1 has two hosts; subnets 2 and 3 have one host
      // all subnets are 100 MB/s contention LANs
      int[] lanTypes = {0,1,1,1};
      topology.setLanTypes(lanTypes);
      int[] subnetHostCount = {0,2,1,1};
      topology.setSubnetHostCount(subnetHostCount);
      int[] subnetDataRate = {0,100,100,100};
      topology.setSubnetDataRate(subnetDataRate);
      byte[][] lanInterfaces = {{0},{0,51,52,53},{0,54,55},{0,56,57}};
      topology.setLanInterfacesMatrix(lanInterfaces);
      topology.findTotalNumberOfHosts();
      
      // setup print layers
      topology.setPrintAtLayer(2);
      topology.setPrintAtLayer(3);
      topology.setPrintAtLayer(5);
      topology.setPrintAtLayer(7);
      
      // print out the new topology and load it into simulator
      topology.printTopology();   
          
      // load the configuration into simulator
      if (!simEngine.loadConfiguration())
      {
        simLogger.logWarn("Configuration Error - check config.txt");
        return;
      }
      ConnectivityMatrix cm = simEngine.getConnectivityMatrix();
      simLogger.setPrintAtLayers(topology.getPrintAtLayers());
      
      // set hostnames for printout
      Router router1 = (Router)cm.getHost(1, 1);
      router1.setName("router-1.1");
      Host host1_2 = (Host)cm.getHost(1, 2);
      host1_2.setName("host-1.2");
      Host host1_3 = (Host)cm.getHost(1, 3);
      host1_3.setName("host-1.3");
      Router router2 = (Router)cm.getHost(2, 1);
      router2.setName("router-2.1");
      Host host2_2 = (Host)cm.getHost(2, 2);
      host2_2.setName("host-2.2");
      Router router3 = (Router)cm.getHost(3, 1);
      router3.setName("router-3.1");
      Host host3_2 = (Host)cm.getHost(3,2);
      host3_2.setName("host-3.2");

      // send 10 messages over unreliable transport from host 1.1 to host 2.1, via WAN (1 hop)
      for (int i = 1; i <= 10; i++)
      {
        router1.sendMessage(i + " 1.1 Watson are you there?", 2, 1, Message.UNRELIABLE);
      }
   
      // send 10 messages over unreliable transport from host 1.2 to host 2.2, via WAN (1 hop)
      for (int i = 1; i <= 10; i++)
      {
        host1_2.sendMessage(i + " 1.1 Watson are you there?", 2, 2, Message.UNRELIABLE);
      }
      
      // send 10 messages over unreliable transport from host 2.2 to host 1.2, via WAN (1 hop)
      for (int i = 1; i <= 10; i++)
      {
        host2_2.sendMessage(i + " 2.2 Yep I'm here.", 1, 2, Message.UNRELIABLE);
      }
         
      // send 10 messages over unreliable transport from host 2.2 to host 3.2, via WAN (2 hops)
      for (int i = 1; i <= 10; i++)
      {
        host2_2.sendMessage(i + " 2.2 Watson are you there?", 3, 2, Message.UNRELIABLE);
      }
     
      // send 10 messages over unreliable transport from host 3.2 to host 2.2, via WAN (2 hops)
      for (int i = 1; i <= 10; i++)
      {
        host3_2.sendMessage(i + " 3.2 Yep I'm here.", 2, 2, Message.UNRELIABLE);
      }
   
      // send 10 messages over unreliable transport from host 1.1 to host 2.1, via WAN (1 hop)
      for (int i = 1; i <= 10; i++)
      {
        router1.sendMessage(i + " 1.1 Watson are you there?", 2, 1, Message.UNRELIABLE);
      }
  
      // execute the simulation
      des = simEngine.getDes();
      while (des.nextEvent());
      
      // print out connectivity matrix
      System.out.println("\nConfiguration:\n" + cm.toString());
    
      // print out host statistics
      System.out.println("\nStatistics for Simulation:");
      System.out.println("Router node 1.1 LAN:");
      System.out.println(router1.getLanInterface().getStatistics().toString());
      System.out.println("Router node 1.1 link to 2.1:");
      System.out.println(router1.getLinkInterfaceById(1).getStatistics().toString());
      System.out.println("Router node 1.1 link to 3.1:");
      System.out.println(router1.getLinkInterfaceById(2).getStatistics().toString());
      System.out.println("Host node 1.2:");
      System.out.println(host1_2.getLanInterface().getStatistics().toString());
      System.out.println("Host node 1.3:");
      System.out.println(host1_3.getLanInterface().getStatistics().toString());
      System.out.println("Router node 2.1 LAN:");
      System.out.println(router2.getLanInterface().getStatistics().toString());
      System.out.println("Router node 2.1 link to 1.1:");
      System.out.println(router2.getLinkInterfaceById(3).getStatistics().toString());
      System.out.println("Host node 2.2:");
      System.out.println(host2_2.getLanInterface().getStatistics().toString());
      System.out.println("Router node 3.1 LAN:");
      System.out.println(router3.getLinkInterfaceById(4).getStatistics().toString());
      System.out.println("Router node 3.1 link to 1.1):");
      System.out.println(router3.getLanInterface().getStatistics().toString());
      System.out.println("Host node 3.2:");
      System.out.println(host3_2.getLanInterface().getStatistics().toString());

      simLogger.logStats("Configuration:\n" + cm.toString());
      simLogger.logInfo("Simulation Finished");
      simLogger.close();
    }
    catch(Exception e)
    {
      simLogger.logError("Exception in testGoBackN:" + e.getMessage());
      simLogger.logException(e);
    }      
      
  }// end testGoBackN()
  
  /**
   * Invokes testGoBackN() to exercise the GoBackN class
   * @param args - no arguments passed 
   */
  public static void main(String[] args)
  {
    // simulation environment
    Topology topology = new Topology();
    SimulationEngine simEngine = new SimulationEngine(topology);
    simEngine.setInstance(simEngine);
    
    // test environment
    Host testHost = null;
    try
    {
      testHost = new Host(1,1,(byte)1,1);
    }
    catch (Exception e)
    {
      System.out.println("Exception in GoBackN test");
      e.printStackTrace(System.err);
    }
    Stack testStack = null;
    try
    {
      testStack = new Stack(testHost);
    }
      catch(Exception e)
    {
      System.out.println("Exception in testGoBackN:" + e.getMessage());
      e.printStackTrace(System.err);
    } 
    
    // run test
    GoBackN goBackN = new GoBackN(testStack, 1);
    goBackN.testGoBackN();
    
  }// end main()

}// end class GoBackN