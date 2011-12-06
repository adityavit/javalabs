import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


/**
 * The input is given in a file which is given as an argument to the program.
 * Every line in input file shows a page described as <page number>
 * 
 * The program is using JDK 1.6
 * The compilation is done as ->javac Replacement.java
 * For running the byte code use ->java Replacement <Replacement-policy> <memory-size>  <Input-File> 
 * The Replacemet Algorithm Number is described as 
 * 0.First In First Out.
 * 1.Second Chance.
 * 2.Last Recently Used.
 */

/**
 * The Replacement class to provide the Replacement functionality.
 * @author aditya
 *
 */
public class Replacement {

	private static final String FIFO_REPLACEMENT_NAME = "fifo";
	
	private static final String SECOND_CHANCE_REPLACEMENT_NAME = "secondChance";
	
	private static final String LRU_REPLACEMENT_NAME = "lru";
	
	//Stores the arguments map.
	private static Map<Integer,String> replacementMapArgs;
	
	//Stores the pageSequence which are accessed in the memory.
	private static List<Page> pageSequence = new ArrayList<Page>();
	
	//Shows the virtual memory and stores the page.
	private static Page[] fixedMemory = null;
	
	private static Integer totalPages = null;
	
	//Keep count of page faults.
	private static Integer pageFaults = 0;
	
	private static PrintWriter pw = null;
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String inputFileName = null;
		String outputFileName = null;
		Integer replacementPolicy = null;
		Integer memorySize = null;
		Scanner sc = null;
		replacementMapArgs = new HashMap<Integer, String>();
		replacementMapArgs.put(0, FIFO_REPLACEMENT_NAME);
		replacementMapArgs.put(1, SECOND_CHANCE_REPLACEMENT_NAME);
		replacementMapArgs.put(2, LRU_REPLACEMENT_NAME);
		if (args.length == 3) {
			replacementPolicy = Integer.parseInt(args[0]);
			memorySize = Integer.parseInt(args[1]);
			fixedMemory = new Page[memorySize];
			inputFileName = args[2];
			if(replacementMapArgs.containsKey(replacementPolicy)){
			outputFileName = inputFileName+"."+replacementMapArgs.get(replacementPolicy);
			}else{
				System.out.println("Replacement Policy can be from 0-2\n" +
						"0.FIFO\n" +
						"1.Second Chance\n" +
						"2.LRU");
			}
		} else {
			System.out
					.println("Error: Only Three arguments are required.\n" +
							"1.Page Replacement Algorithm.\n" +
							"2.Memory Size.\n" +
							"3.Input File Path.\n");
			System.exit(-1);
		}
		try {
			sc = new Scanner(new FileReader(inputFileName));
		} catch (FileNotFoundException fe) {
			System.out.println("Error: File Not Found Exception: "
					+ inputFileName);
			System.exit(-1);
		}
		
		Integer pageCount = 0;
		while (sc.hasNextLine()) {
			pageSequence.add(new Page((Integer)sc.nextInt()));
			pageCount++;
			if(sc.hasNextLine())
			sc.nextLine();
		}
		totalPages = pageCount;
		pw = new PrintWriter(new FileWriter(outputFileName));
		switch(replacementPolicy){
		case 0:
			//System.out.println("First In First Out");
			fifo(pageSequence);
			break;
		case 1:
			//System.out.println("Second Chance.");
			secondChance(pageSequence);
			break;
		case 2:
			//System.out.println("Last Recently Used.");
			lastRecentlyUsed(pageSequence);
			break;
		default:
			System.out.println("The first argument should be between 0-2 for \n 0.First Come and First Serve \n 1.Round Robin Quantum 2 \n 2.Shortest remaining Job First \n");
		}
		sc.close();
		pw.close();
	}
	
	/**
	 *Provide the logic of fifo on the page replacement.
	 **/
	public static void fifo(List<Page> pages){
		Integer pageToReplaceMemoryIndex = 0;
		Integer memoryIndex = 0;
		Boolean pageToBeReplaced;
		for(Page page : pages){
			pageToBeReplaced = true;
			memoryIndex = 0;
			while(memoryIndex < fixedMemory.length){
				if(fixedMemory[memoryIndex] == null){
					fixedMemory[memoryIndex] = page;
					pageToBeReplaced = false;
					pageFaults++;
					break;
				}else{
					if(fixedMemory[memoryIndex].getPageNumber() == page.getPageNumber()){
						pageToBeReplaced = false;
						break;
					}
				}
				memoryIndex ++;
			}
			if(pageToBeReplaced){
				fixedMemory[pageToReplaceMemoryIndex] = page;
				pageFaults++;
				if(pageToReplaceMemoryIndex == fixedMemory.length-1){
					pageToReplaceMemoryIndex = 0;
				}else{
					pageToReplaceMemoryIndex++;
				}
			}
		 printMemoryMap();
		}
		printPageFault();
	}
	
	/**
	 * Provides the logic of Second Chance algorithm for the list of pages.	
	 **/
	public static void secondChance(List<Page> pages){
		Integer pageToReplaceMemoryIndex = 0;
		Integer memoryIndex = 0;
		Boolean pageToBeReplaced;
		for(Page page : pages){
			pageToBeReplaced = true;
			memoryIndex = 0;
			while(memoryIndex < fixedMemory.length){
				if(fixedMemory[memoryIndex] == null){
					fixedMemory[memoryIndex] = page;
					pageToBeReplaced = false;
					pageFaults++;
					break;
				}else{
					if(fixedMemory[memoryIndex].getPageNumber() == page.getPageNumber()){
						fixedMemory[memoryIndex].setReferenceBit();
						pageToBeReplaced = false;
						break;
					}
				}
				memoryIndex ++;
			}
			if(pageToBeReplaced){
				while(pageToBeReplaced){
					if(fixedMemory[pageToReplaceMemoryIndex].isReferenceBitSet()){
						fixedMemory[pageToReplaceMemoryIndex].desetReferenceBit();
					}else{
						fixedMemory[pageToReplaceMemoryIndex] = page;
						pageFaults++;
						pageToBeReplaced = false;
					}
					if(pageToReplaceMemoryIndex == fixedMemory.length-1){
						pageToReplaceMemoryIndex = 0;
					}else{
						pageToReplaceMemoryIndex++;
					}
				}
			}
		 printMemoryMap();
		}
		printPageFault();
	}

	/**
	 * Provides the logic of lasr recently used algorithm for the list of pages.	
	 **/
	public static void lastRecentlyUsed(List<Page> pages){
		ArrayList<Integer> lastRecentlyUsedQueue = new ArrayList<Integer>();
		Integer memoryIndex = 0;
		Boolean pageToBeReplaced;
		for(Page page : pages){
			pageToBeReplaced = true;
			memoryIndex = 0;
			while(memoryIndex < fixedMemory.length){
				if(fixedMemory[memoryIndex] == null){
					fixedMemory[memoryIndex] = page;
					pageToBeReplaced = false;
					pageFaults++;
					if(lastRecentlyUsedQueue.contains(memoryIndex)){
						lastRecentlyUsedQueue.remove(memoryIndex);
					}
					
					lastRecentlyUsedQueue.add(memoryIndex);
					break;
				}else{
					if(fixedMemory[memoryIndex].getPageNumber() == page.getPageNumber()){
						pageToBeReplaced = false;
						if(lastRecentlyUsedQueue.contains(memoryIndex)){
							lastRecentlyUsedQueue.remove(memoryIndex);
						}
						
						lastRecentlyUsedQueue.add(memoryIndex);
						break;
					}
				}
				memoryIndex ++;
			}
			if(pageToBeReplaced){
				Integer memoryInd = lastRecentlyUsedQueue.get(0);
				fixedMemory[memoryInd] = page;
				pageFaults++;
				if(lastRecentlyUsedQueue.contains(memoryInd)){
					lastRecentlyUsedQueue.remove(memoryInd);
				}
				
				lastRecentlyUsedQueue.add(memoryInd);
			}
		 printMemoryMap();
		}
		printPageFault();
	}
       
        /**
	 * Prints the memory map.	
	 **/

	private static void printMemoryMap(){
		for(int i=0;i<fixedMemory.length;i++){
			if(fixedMemory[i] != null){
				pw.print(fixedMemory[i]);
			}
		}
		pw.print("\n");
	}
	
	private static void printPageFault(){
		pw.printf("\n Precentage of page faults: %.2f \n",(float)pageFaults/totalPages); 
	}

	/*
	 * Inner Page class to store Page information.
 	 */	
	private static class Page{
		
		private final Integer pageNumber;
		
		private Boolean referenceBit=false;
		
		public Page(Integer pageNumber){
			this.pageNumber = pageNumber;
		}
		
		public Integer getPageNumber(){
			return this.pageNumber;
		}
		
		public void setReferenceBit(){
			this.referenceBit = true;
		}
		
		public void desetReferenceBit(){
			this.referenceBit = false;
		}
		
		public Boolean isReferenceBitSet(){
			return referenceBit;
		}
		
		public String toString(){
			return getPageNumber() + " ";
		}
	}

}
