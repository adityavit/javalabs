/**
 * 
 */

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

/**
 * @author Aditya Bhatia
 * 
 */
public class Linker {

	/**
	 * s_symboltable is a hashmap which is required to store the symbol table
	 * for the applicaiton. The symbol table stores information as
	 * <symbol,position>
	 */
	private static HashMap<String, Integer> s_symbolTable = null;

	/**
	 * Stores the base address of the modules available in the memory.
	 */
	private static ArrayList<Integer> s_moduleAddress = null;

	private static ArrayList<String[]> s_memory = null;

	private static HashMap<Integer, String[]> s_usedModuleVariables = null;
	
	private static ArrayList<Integer> s_moduleSize = null;
	
	private static ArrayList<String> s_errorList = null;
	
	private static ArrayList<String> s_warningList = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		String inputFileName = null;
		String outputFileName = null;
		Scanner sc = null;
		if (args.length == 2) {
			inputFileName = args[0];
			outputFileName = args[1];
		} else {
			System.out
					.println("Error: Only Two arguments are required.Input File name followed by output file name");
			System.exit(-1);
		}
		try {
			sc = new Scanner(new FileReader(inputFileName));
		} catch (FileNotFoundException fe) {
			System.out.println("Error: File Not Found Exception: "
					+ inputFileName);
			System.exit(-1);
		}

		createDataStructures();
		readFirstPass(sc);
		readSecondPass();
		generateFinalOutput(outputFileName);
		sc.close();
	}

	/**
	 * Function Creates the Data Structures Needed for the Linker.
	 * 
	 * @param sc
	 */
	private static void createDataStructures() {
		s_symbolTable = new HashMap<String, Integer>();
		s_moduleAddress = new ArrayList<Integer>();
		s_memory = new ArrayList<String[]>();
		s_usedModuleVariables = new HashMap<Integer, String[]>();
		s_moduleSize = new ArrayList<Integer>();
		s_errorList = new ArrayList<String>();
		s_warningList = new ArrayList<String>();
	}

	/**
	 * Reads the input file to go over the First Pass. The main purpose of the
	 * first pass is to create the symbol table and to compute the base address
	 * of the modules after computation it stores them in the corresponding data
	 * Structures.
	 * 
	 * @param sc
	 */
	private static void readFirstPass(Scanner sc) {
		int moduleCount = 0;
		while (sc.hasNextLine()) {
			// parsing first line of every module
			String moduleVariables[] = null;
			int moduleVariablesCount = sc.nextInt();
			if (moduleVariablesCount != 0) {
				moduleVariables = new String[moduleVariablesCount];
				for (int i = 0; i < moduleVariablesCount; i++) {
					String variable = sc.next();
					Integer variableRelAddr = sc.nextInt();
					moduleVariables[i] = variable;
					if (!s_symbolTable.containsKey(variable)) {
						s_symbolTable.put(variable, variableRelAddr);
					} else {
						s_errorList.add("Error: A symbol is multiply defined:" + variable);
					}
				}
			}
			sc.nextLine();
			// Parsing of First Line Ends Here.
			// Parsing of Second Line Begins here.
			Integer usedVariablesCount = sc.nextInt();
			String usedVariables[] = new String[usedVariablesCount];
			if (usedVariablesCount != 0) {
				for (int i = 0; i < usedVariablesCount; i++) {
					String usedVariable = sc.next();
					usedVariables[i] = usedVariable;
				}
			}
			s_usedModuleVariables.put(moduleCount, usedVariables);
			sc.nextLine();
			// parsing of Second Line Ends Here.
			// Parsing of Third Line Starts Here.
			Integer moduleMemoryCount = sc.nextInt();
			if (moduleMemoryCount != 0) {
				s_moduleSize.add(moduleMemoryCount);
				if (s_memory.size() == 0) {
					s_moduleAddress.add(0, 0);
				} else {
					s_moduleAddress.add(moduleCount, s_memory.size());
				}
				for (int i = 0; i < moduleMemoryCount; i++) {
					String memoryValue[] = new String[2];
					memoryValue[0] = sc.next();
					memoryValue[1] = sc.next();
					s_memory.add(memoryValue);
				}
			}
			// parsing of Third Line Ends here.
			// Resolving Base Address in Symbol table to Absolute address
			if (moduleVariables != null) {
				for (String moduleVariable : moduleVariables) {
					Integer variableRelAddr = s_symbolTable.get(moduleVariable);
					Integer varAbsAddr = variableRelAddr+ s_moduleAddress.get(moduleCount);
					if(varAbsAddr > s_moduleAddress.get(moduleCount)+ s_moduleSize.get(moduleCount)){
						s_errorList.add("Error: An address "+variableRelAddr+" appearing in a definition exceeds the module size of "+s_moduleSize.get(moduleCount)+":moduleVariable:"+moduleVariable);
					}
					s_symbolTable.put(moduleVariable, variableRelAddr
							+ s_moduleAddress.get(moduleCount));
				}
			}
			if (sc.hasNextLine()) {
				sc.nextLine();
			}

			moduleCount++;
		}
		for(String variableKey:s_symbolTable.keySet()){
			Boolean variableUsedFlag = false;
			for(int i= 0;i<moduleCount;i++){
				String[] usedVarOfModules = s_usedModuleVariables.get(i);
				if(usedVarOfModules.length>0){
				for(int j=0;j<usedVarOfModules.length;j++){
					if(usedVarOfModules[j].equalsIgnoreCase(variableKey)){
						variableUsedFlag = true;
					}
				}
				}
			}
			if(!variableUsedFlag){
				s_warningList.add("Warning:A symbol is defined but not used:"+variableKey);
			}
		}
	}
	/**
	 * The second Pass of the Linker deals with two things.
	 * 1. Relocating the Relative address.
	 * 2. Resolving the external address.
	 * 
	 */
	private static void readSecondPass() {
		Iterator<Integer> moduleSizeItr = s_moduleSize.iterator();
		Iterator<Integer> moduleBaseAddrItr = s_moduleAddress.iterator();
		Integer moduleCount = 0;
		while(moduleSizeItr.hasNext() && moduleBaseAddrItr.hasNext()){
			Integer moduleSize = moduleSizeItr.next();
			Integer moduleBaseAddr = moduleBaseAddrItr.next();
			for(int i=moduleBaseAddr;i<moduleSize+moduleBaseAddr;i=i+1){
				String[] memory = s_memory.get(i);
				if(memory[0].equalsIgnoreCase("R")){
					String memoryAddressValue = memory[1];
					Integer memoryAddress = Integer.parseInt(memoryAddressValue.substring(1));
					if(memoryAddress > moduleSize){
						s_errorList.add("Error: A relative address "+memoryAddress+" exceeds the size of the module "+moduleCount);
					}
					String memoryAddressStr = String.valueOf(memoryAddress+moduleBaseAddr);
					if(memoryAddressStr.length()==2){
						memoryAddressStr = "0"+memoryAddressStr;
					}else if(memoryAddressStr.length()==1){
						memoryAddressStr = "00"+memoryAddressStr;
					}
					memory[1] = memory[1].substring(0, 1) + memoryAddressStr;
				}
					
			}
				String[] usedModVariables = s_usedModuleVariables.get(moduleCount);
				if(usedModVariables.length!= 0){
					for(int i = 0; i<usedModVariables.length;i++){
						String usedModVariable = usedModVariables[i];
						if(s_symbolTable.containsKey(usedModVariable)){
							Integer varAbsPosition = s_symbolTable.get(usedModVariable);
							Boolean varAvailableAsUsed = false;
							for(int j=moduleBaseAddr;j<moduleSize+moduleBaseAddr;j=j+1){
								String[] memory = s_memory.get(j);
								if(memory[0].equalsIgnoreCase("E")){
								 String memoryAddressValue = memory[1];
								 Integer memoryAddress = Integer.parseInt(memoryAddressValue.substring(1));
								 if(memoryAddress >= usedModVariables.length){
									 s_errorList.add("Error:An External Address "+memoryAddressValue+" is too large to reference an entry in used list in module "+moduleCount);
								 }
								 if(memoryAddress == i){
									 String varAbsAddress = String.valueOf(varAbsPosition);
									 memory[0] = "K";
									 if(varAbsAddress.length()==2){
										 varAbsAddress = "0"+varAbsAddress;
										}else if(varAbsAddress.length()==1){
											varAbsAddress = "00"+varAbsAddress;
										}
									 memory[1] = memory[1].substring(0, 1) + varAbsAddress;
									 if(varAbsPosition>=s_memory.size() && i==0){
										 s_errorList.add("Error:An absolute address "+varAbsPosition+" exceeds the size of the machine "+s_memory.size()+" variable="+usedModVariable+" module="+moduleCount);
									 }
									 varAvailableAsUsed = true;
								 }
								}
							}
							if(!varAvailableAsUsed){
								s_warningList.add("Warning:A symbol "+usedModVariable+" appears in a use list but it not actually used in the module "+moduleCount);
							}
						}else{
							s_errorList.add("Error:A symbol "+usedModVariable+" is used but not defined");
						}
					}
						
				}
				moduleCount++;
		}
	}

	private static void generateFinalOutput(String outputFileName)throws Exception {
		HashMap outSymbolTable = new HashMap<String, Integer>();;
		List outMemoryMap = new ArrayList<String[]>();
		outSymbolTable = s_symbolTable;
		outMemoryMap = s_memory;
		PrintWriter pw = new PrintWriter(new FileWriter(outputFileName));
		if(s_warningList.size() !=0){
			for(String warning : s_warningList){
				pw.println(warning);
			}
		}
		if(s_errorList.size()!=0){
			for(String error : s_errorList){
				pw.println(error);
			}
		}
		if(s_errorList.size()==0){
			pw.println("Symbol Table");
		for(String symbol : s_symbolTable.keySet()){
			pw.println(symbol+"="+s_symbolTable.get(symbol));
		}
		pw.println("\nMemory Map");
		int i=0;
		for(String[] memory : s_memory){
			pw.println(i+":  "+memory[1]);
			i++;
		}
		}
		pw.flush();
		pw.close();
	}

}
