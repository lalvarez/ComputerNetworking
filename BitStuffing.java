/**
 * Java Network Workbench 2 (JNW2)
 * Copyright 2013/2017 Networking and Simulation Laboratory/George Mason University
 * 
 * Description: Stuff and Unstuff DLC Frames
 * 
 * @version 2.2.3
 */

package JNW2.message;

public class BitStuffing 
{
  /**
   * Adds a zero after any sequence of 5 ones in bitFrame
   * Puts the result back into bitFrame
   * @param bitFrame - the unstuffed frame
   * @return bitFrame - the stuffed version
   */
  public static BitSequence stuff(BitSequence bitFrame)
  {
    // this version does nothing, only returns the input
    // student adds code here to do stuffing
    // and be sure to add your name below
    // student name: Laura Alvarez
    
    //Auxiliar variables
    boolean value;
    int leng = bitFrame.size();
    //Set the 1's counter to 0
    int counter_1 = 0;
    //Variable to iterate the new BitSequence
    int aux = 0;
    //New BitFrame created with the stuffed bit sequence
    BitSequence bitFrame2 = new BitSequence();
   
    
    for(int i =0; aux <(leng); i++){
      //i iterates the given BitSequence
      //We get the value
      value = bitFrame.getValue(i);
      
      if(value == true){
        //If the value is a one
        counter_1++;
        if(counter_1 == 5){
          //Set the value
            bitFrame2.setValue(aux, true);
            aux += 1;
          //Add a 0 after that value
            bitFrame2.setValue(aux, false);
          //We have one extra bit now
            leng += 1;
          //Set the 1's counter to 0 again
            counter_1 =0;
        }
        else{
          //If we do not have 5 ones we just set the value
            bitFrame2.setValue(aux, true);
        }
      
      }
        else{
        //Value is a 0
        bitFrame2.setValue(aux, false);
        counter_1 =0;
        }
      //Going to the next position in the new BitSequence
      aux++;
    }
    
    
    //Return new bit frame stuffed
    return bitFrame2;
      
  }// end stuff()
  
  /**
   * Removes zero after any sequence of 5 ones in bitFrame
   * Puts the result back into bitFrame
   * @param bitFrame - a stuffed frame
   * @return bitFrame - the unstuffed version
   */
  public static BitSequence unstuff(BitSequence bitFrame)
  {
    // this version does nothing; only returns the input
    // student adds code here to do unstuffing
    // student name: Laura Alvarez
    
    //Auxiliar variables
    boolean value;
    int leng = bitFrame.size();
    //Set the 1's counter to 0
    int counter_1 = 0;
    //Variable to iterate the new BitSequence
    int aux = 0;
    //New BitFrame created with the stuffed bit sequence
    BitSequence bitFrame2 = new BitSequence();
   
    
    for(int i =0; i<(leng); i++){
      //We get the bit value
      value = bitFrame.getValue(i);
      if(value == true){
        //If the value is a one
        counter_1++;
        if(counter_1 == 5){
          //If we detect the 5th 1 we set that value and ignore the next
          //because the next value will be the extra 0
            bitFrame2.setValue(aux, true);
            i++;
            counter_1 =0;
        }
        else{
          //Set value 1
            bitFrame2.setValue(aux, true);
        }
      
      }
        else{
        //Value is a 0
        bitFrame2.setValue(aux, false);
        counter_1 =0;
        }
      aux++;
    
    }
    
    //Return new bit frame unestuffed
    return bitFrame2;
      
  }// end unstuff()
  
  /**
   * Test code main function for file BitStuffing.java
   * This file goes through 4 tests for each function stuff and unstuff.
   * File follows the format of setting up variables and then testing.
   * Prints out input, result, expected result, as well as other statements 
   * of progress.
   * @param args - no arguments passed
   * 
   * This main method is subject to the following statement:
   * This test code was programmed by Liam Dannaher, 
   * who has agreed to make it available under the open source copyright 
   * in code directory JNW2.
   */
  public static void main(String[] args)
  {
    System.out.println("Testing BitStuffing.java:");

    //Variables to keep track of tests passed
    boolean passedAllTests = true;
    int totalTests = 4;
    int passedTests = 0;
    
    /*
     * Setup BitSequences:
     * There are four tests currently for stuffing & unstuffing.
     * The setup for tests follows the pattern:
     * Create unstuffed BitSequence.
     * Initialize unstuffed BitSequence with setValue(int index, boolean value) function.
     * Create stuffed BitSequence.
     * Initialize stuffed BitSequence with setValue(int index, boolean value) function.
     * =============================================================================
     * |Test # |Test name| unstuffed sequence        | stuffed sequence            |
     * |___________________________________________________________________________|
     * |Test 1 | *_test1 |101101110111101111101111110|10110111011110111110011111010|
     * |Test 2 | *_test2 |1111100000                 |11111000000                  |
     * |Test 3 |   test3 |11110111101                |11110111101                  |
     * |Test 4 | *_test4 |111111111111               |11111011111011               |
     * =============================================================================
     * Note: the * is replaced by unstuffed or stuffed depending on which test it is.
     * Note: test3 does not have seperate unstuff and stuff sequences because there 
     *   should be no difference.
     */

    // Test 1
    BitSequence unstuffed_test1 = new BitSequence(27);//101101110111101111101111110
    for(int i = 0;i < unstuffed_test1.size();i++)
    {
      if(i==1||i==4||i==8||i==13||i==19||i==26)
      {
        unstuffed_test1.setValue(i,false);
      }
      else
      {
        unstuffed_test1.setValue(i,true);
      }
    }

    BitSequence stuffed_test1 = new BitSequence(29);//10110111011110111110011111010
    for(int i = 0;i < stuffed_test1.size();i++)
    {
      if(i==1||i==4||i==8||i==13||i==19||i==20||i==26||i==28)
      {
        stuffed_test1.setValue(i,false);
      }
      else
      {
        stuffed_test1.setValue(i,true);
      }
    }

    // Test 2
    BitSequence unstuffed_test2 = new BitSequence(10);//1111100000
    for(int i = 0;i < 5;i++)
    {
      unstuffed_test2.setValue(i,true);
    }
    for(int i = 5;i < unstuffed_test2.size();i++)
    {
      unstuffed_test2.setValue(i,false);
    }

    BitSequence stuffed_test2 = new BitSequence(11);//11111000000
    for(int i = 0;i < 5;i++)
    {
      stuffed_test2.setValue(i,true);
    }
    for(int i = 5;i < stuffed_test2.size();i++)
    {
      stuffed_test2.setValue(i,false);
    }

    // Test 3
    BitSequence test3 = new BitSequence(11);//11110111101
    for(int i = 0;i < test3.size();i++)
    {
      if(i==4||i==9)
      {
        test3.setValue(i,false);
      }
      else
      {
        test3.setValue(i,true);
      }
    }

    // Test 4
    BitSequence unstuffed_test4 = new BitSequence(12);//111111111111
    for(int i = 0;i < unstuffed_test4.size();i++)
    {
      unstuffed_test4.setValue(i,true);
    }

    BitSequence stuffed_test4 = new BitSequence(14);//11111011111011
    for(int i = 0;i < stuffed_test4.size();i++)
    {
      if(i==5||i==11)
      {
        stuffed_test4.setValue(i,false);
      }
      else
      {
        stuffed_test4.setValue(i,true);
      }
    }
    // end setup

    /*
     * Outline of testing part:
     * Start of testing stuff function
     * ------------------------------------
     * Print input
     * Print resulting output
     * Print expected output
     * Print pass/fail depending on resulting output
     * Repeat previous 4 steps for 3 more tests
     * Print out number of tests passed out of total tests
     * Repeat previous steps for unstuff function
     * Print out if all tests were passed or not
     */

    // Beginning of the testing of the stuff function
    System.out.println("\n\nTesting 'stuff' function");
    System.out.println("---------------------------------------------------------------------");

    System.out.println("For the test sequence:             "+unstuffed_test1);//Input
    System.out.println("The output of your stuff function: "+stuff(unstuffed_test1));//Resulting output
    System.out.println("Expected Result:                   "+stuffed_test1);//Expected output
    if(stuff(unstuffed_test1).equals(stuffed_test1))//Resulting & Expected outputs are the same (test passed)
    {
      System.out.println("Passed Test 1");
      passedTests++;
    }
    else// Resulting & Expected outputs are not the same (test failed)
    {
      System.out.println("Failed Test 1");
    }

    System.out.println("For the test sequence:             "+unstuffed_test2);//Input
    System.out.println("The output of your stuff function: "+stuff(unstuffed_test2));//Resulting output
    System.out.println("Expected Result:                   "+stuffed_test2);//Expected output
    if(stuff(unstuffed_test2).equals(stuffed_test2))//Resulting & Expected outputs are the same (test passed)
    {
      System.out.println("Passed Test 2");
      passedTests++;
    }
    else// Resulting & Expected outputs are not the same (test failed)
    {
      System.out.println("Failed Test 2");
    }

    System.out.println("For the test sequence:             "+test3);//Input
    System.out.println("The output of your stuff function: "+stuff(test3));//Resulting output
    System.out.println("Expected Result:                   "+test3);//Expected output
    if(stuff(test3).equals(test3))//Resulting & Expected outputs are the same (test passed)
    {
      System.out.println("Passed Test 3");
      passedTests++;
    }
    else// Resulting & Expected outputs are not the same (test failed)
    {
      System.out.println("Failed Test 3");
    }

    System.out.println("For the test sequence:             "+unstuffed_test4);//Input
    System.out.println("The output of your stuff function: "+stuff(unstuffed_test4));//Resulting output
    System.out.println("Expected Result:                   "+stuffed_test4);//Expected output
    if(stuff(unstuffed_test4).equals(stuffed_test4))//Resulting & Expected outputs are the same (test passed)
    {
      System.out.println("Passed Test 4");
      passedTests++;
    }
    else// Resulting & Expected outputs are not the same (test failed)
    {
      System.out.println("Failed Test 4");
    }

    // Print out the passed tests and total tests
    System.out.println("\nYou passed "+passedTests+" out of "+totalTests+" for the 'stuff' function");
    if(passedTests != totalTests){passedAllTests = false;}
    passedTests = 0;//reset the number of tests passed

    // Beginning of the testing of the unstuff function
    System.out.println("\n\nTesting 'unstuff' function");
    System.out.println("---------------------------------------------------------------------");

    System.out.println("For the test sequence:               "+stuffed_test1);//Input
    System.out.println("The output of your unstuff function: "+unstuff(stuffed_test1));//Resulting output
    System.out.println("Expected Result:                     "+unstuffed_test1);//Expected output
    if(unstuff(stuffed_test1).equals(unstuffed_test1))//Resulting & Expected outputs are the same (test passed)
    {
      System.out.println("Passed Test 1");
      passedTests++;
    }
    else// Resulting & Expected outputs are not the same (test failed)
    {
      System.out.println("Failed Test 1");
    }

    System.out.println("For the test sequence:               "+stuffed_test2);//Input
    System.out.println("The output of your unstuff function: "+unstuff(stuffed_test2));//Resulting output
    System.out.println("Expected Result:                     "+unstuffed_test2);//Expected output
    if(unstuff(stuffed_test2).equals(unstuffed_test2))//Resulting & Expected outputs are the same (test passed)
    {
      System.out.println("Passed Test 2");
      passedTests++;
    }
    else// Resulting & Expected outputs are not the same (test failed)
    {
      System.out.println("Failed Test 2");
    }

    System.out.println("For the test sequence:               "+test3);//Input
    System.out.println("The output of your unstuff function: "+unstuff(test3));//Resulting output
    System.out.println("Expected Result:                     "+test3);//Expected output
    if(unstuff(test3).equals(test3))// Resulting & Expected outputs are the same (test passed)
    {
      System.out.println("Passed Test 3");
      passedTests++;
    }
    else// Resulting & Expected outputs are not the same (test failed)
    {
      System.out.println("Failed Test 3");
    }

    System.out.println("For the test sequence:               "+stuffed_test4);//Input
    System.out.println("The output of your unstuff function: "+unstuff(stuffed_test4));//Resulting output
    System.out.println("Expected Result:                     "+unstuffed_test4);//Expected output
    if(unstuff(stuffed_test4).equals(unstuffed_test4))//Resulting & Expected outputs are the same (test passed)
    {
      System.out.println("Passed Test 4");
      passedTests++;
    }
    else// Resulting & Expected outputs are not the same (test failed)
    {
      System.out.println("Failed Test 4");
    }

    // Print out the passed tests and total tests
    System.out.println("\nYou passed "+passedTests+" out of "+totalTests+" for the 'unstuff' function");

    // Print out final results
    if(passedTests == totalTests && passedAllTests)//Passed all tests for functions stuff & unstuff
    {
      System.out.println("\nCongratulations you passed all the tests!");
    }
    else//Failed a test for function stuff & unstuff
    {
      System.out.println("\nLooks like you have some work to do...");
    }
    // end tests
  }// end main
    
}// end class BitStuffing
