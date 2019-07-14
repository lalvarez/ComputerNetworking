/**
 * Java Network Workbench 2 (JNW2)
 * Copyright 2013-2017 Networking and Simulation Laboratory/George Mason University
 * 
 * Controls send & receive of Transport Layer segments
 * 
 * Because JNW2 reliable transport supports only one connection at a time
 * this is considerably simpler than TCP; but it still deals with reliable
 * transmission and ordering of a stream of segments.
 * 
 * This subclass is used for both projects TRN1 and TRN2
 * 
 * @version 2.2.5
 */

package JNW2.message;

import static JNW2.Constants.*;
import JNW2.stack.*;

/**
 *
 * @author JMP
 */
public class SendSegments extends Segments
{
  public SendSegments(
    Stack stackRef,
    int sourceNetworkNumberRef, 
    int sourceHostNumberRef)
  {
    super(stackRef, sourceNetworkNumberRef, sourceHostNumberRef);
  }

  // this portion of reliable transport send is left for
  // student completion; see comments below for functions
  //
  // note that most variables used in this class are defined in
  // its superclass Segment
  public void sendRtlSegments(Buffer replySegment) throws Exception
  {
// the transport Segments of the email to be transmitted are available
// when sendRtlSegments is invoked as TL segments in savedSendSegments
// CAUTION: when savedSendSegments elements are ACKed, they are set to null
// 
// algorithm for sending TL segments (method sendRtlSegments)
//
// Step 1:
// if replySegment is not null and contains an ackNumber (ack==1, syn==0, fin==0);
// indicating that a previously sent segment has not been received
// (seqNumberAcked < lastCharSent):
//   do for all non-null segments in the buffer (number of these is 
//     numberOfEmailSegments):
//     if that segment has had time to reach its destination 
//     (segment send time is greater than zero and
//      des.getSimulationTimeInTicks() minus last send time
//       is greater than 2 times RTT),
//       send that segment again from the savedSendSegments and update its
//       segmentSentTime;
//     during this process update the RTT for this TCPConnection as the
//       difference between the send time of the segment with matching segmentNumber
//       and the global "clock" des.getSimulationTimeInTicks()
//       if there is any with matching segmentNumber
//
// Step 2:
// calculate congestionWindowEnd as the smaller of 
//   (sendWindowStart+congestionWindowSize-1)
//   and (sendEmailLength-1);
//   NOTE: for TRN1 getCongestionWindowSize(() is always DEFAULT_TL_SEND_WINDOW_SIZE;
// while lastCharSent is less than windowEnd,
// {
//   send another segment from the message;
// }
//
// Step 3:
// for each non-null segment in savedSendSegments,
//   if segment has timed out waiting for ACK (des.getSimulationTimeInTicks()
//     minus segment send time is greater than RTT), retransmit the segment in
//     savedSendSegments;
//   if segment did not timeout and replySegment is an ACK ???
//
// for every segment tranmitted, save segmentSendTime from
//   des.getSimulationTimeInTicks()
//
// students should modify the code immediately following this to
// implement the above algorithm
//
// to send a segment after connection is open:
//   sendNewSegment(
//     destNetworkNumber, 
//     destHostNumber, 
//     tlSendState.NEXT STATE  (that is, TL_SENDING or CLOSE_WAIT)
//     segmentBuffer, 
//     isUrgent);
    // studnet TRN1 solution goes below
        // and be sure to include your name
        // student name: 
        // ************* TRN1  Step 1 student work goes here *************

//
    // Step 1: Receive a NACK, resend the failed segment, and update RTT
        if (replySegment != null) {
            if (replySegment.size() > 0) {
                if (this.getSyn(replySegment) == 0 && this.getFin(replySegment) == 0 && this.getAck(replySegment)==1) {
                    for (int i = 0; i < savedSendSegments.length; i++) {
                        if ((savedSendSegments[i] != null)) {
                            if (segmentSendTicks[i] > 0 && (des.getSimulationTimeInTicks() - segmentSendTicks[i - 1])
                                    > 2 * (des.getSimulationTimeInTicks() - segmentSendTicks[i])) {
                                sendNewSegment(destNetworkNumber, destHostNumber,tlSendState.TL_SENDING, savedSendSegments[i], false);
                                segmentSendTicks[i] = des.getSimulationTimeInTicks();
                            }
                            this.rtt = (int) (des.getSimulationTimeInTicks() - segmentSendTicks[i]);
                        }
                    }
                }

            }// end if(replySegment.size() > 0) 
        }// end if(replySeg != null)
    
    // Step 2: Send a segment within window size
    // Update window end
    long sendWindowEnd = sendWindowStart + getCongestionWindowSize() - 1;
    if(sendWindowEnd > sendEmailLength)
       sendWindowEnd = sendEmailLength;
    
    // send if window has room
    while(lastCharSent < sendWindowEnd) 
    {
      int sendSegmentIndex = sequenceNumberToBufferIndex(lastCharSent + 1);
      if(sendSegmentIndex < 0)break;
      Buffer segmentToSend = savedSendSegments[sendSegmentIndex];
      if(getPayloadSize(segmentToSend) <= 0)break;
      sendNewSegment(
        destNetworkNumber,
        destHostNumber,
        tlSendState.TL_SENDING,
        segmentToSend,
        false);
        lastCharSent += getPayloadSize(segmentToSend);
      
      // save send time for segment
      segmentSendTicks[sendSegmentIndex] = des.getSimulationTimeInTicks();
   }//end while(lastCharSent < sendWindowEnd)
   
   // Step 3: send timeout segments

   // **************** TRN1 Step 3 student work goes here *****************
        for (int i = 0; i < savedSendSegments.length; i++) {
            if ((savedSendSegments[i] != null)) {
                if ((des.getSimulationTimeInTicks() - segmentSendTicks[i]) > this.rtt) {
                    sendNewSegment(destNetworkNumber, destHostNumber,tlSendState.TL_SENDING, savedSendSegments[i], false);
                    segmentSendTicks[i] = des.getSimulationTimeInTicks();
                }
            }
        }
//
   // ***************** end TRN1 Step 3student work ***********************
// Step 4:
// if sendWindowStart is greater than or equal to size of message being sent
// (all data has been sent and ACKed),
// {
//   send FIN;
//   set sendState to CLOSE_WAIT;
// }.
//
   // Step 4: All segments sent, send FIN
   if(sendWindowStart >= sendEmailLength) 
   {
     sendNewSegment(
       destNetworkNumber, 
       destHostNumber, 
       tlSendState.CLOSE_WAIT,
       finSegment, true);
     finSentTicks = des.getSimulationTimeInTicks();
   }

  }// end sendRtlSegments()  
  
  
  /**
   * this method is intended to be programmed by students
   * in order to implement TCP-like slow start in TRN2
   * 
   * It is called each time an acknowledgment is received,
   * after RTT is updated.
   * 
   * @param receiverMaxWindow - max window sent in header from receiver
   * @param timeoutOccurred - true if timeout occurred listening for segment
   */
  public void updateCongestionWindowSize(
    int receiverMaxWindow, 
    boolean timeoutOccurred)
  {
    // JNW2 version of slow start:
    // start with minimum window of TL_SEGMENT_DATA_SIZE, 
    //   threshold of TL_SLOW_START_THRESHOLD
    // subject to receiverMaxWindow:
    // double window size each round-trip
    // if no congestion seen (sensed by timeout), 
    //   continue doubling until threshold is reached
    //   then add TL_SEGMENT_DATA_SIZE per RTT up to receiver max
    //   (NOTE: we are using TL_SEGMENT_DATA_SIZE in place of MTU)
    // if congestion is experienced (timeoutOccurred), 
    //   cut threshold in half and begin “slow start” again
    
    // solution requires working with these variables from Segments.java:
    // sendWindowStart,congestionWindowSize, 
    // receiverWindowSize,slowStartThreshold
    
    // to increase the congestionWindowSize, use method
    // changeCongestionWindowSizeTo() and to reset it use
    // resetCongestionWindowSize()
        
    // to obtain the slowStartThreshold use method
    // getSlowStartThreshold() and to change it use
    // setSlowStartThreshold()

    // student TRN2 work goes below
    // and be sure to include your name
    // student name:
    // ****************** TRN2 student work goes here ******************** 

    
    // **************** end TRN2 student work  ***************************
    
  }// end updateCongestionWindowSize()
   
}// end class SendSegments
