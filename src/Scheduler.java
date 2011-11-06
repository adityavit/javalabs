import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;



/**
 * The Java Program to show different Scheduling Implementation for the operation Systems processes.
 * The input is given in a file which is given as an argument to the program.
 * Every line in input file shows a process with various attributes described as <ProcessID> <CpuTime> <IOTime> <ArrivalTime>
 * 
 * The program is using JDK 1.6
 * The compilation is done as ->javac Scheduler.java
 * For running the byte code use ->java Scheduler <Scheduling Algorithm Number> <Input-File> <Output-file>
 * The Scheduling Algorithm Number is described as 
 * 0.First Come First Serve.
 * 1.Round Robin Algorithm.
 * 2.Shortest Remaining Job First.
 */

/**
 * @author Aditya
 *
 */
public class Scheduler {
	
	private static List<Process> processList = new ArrayList<Process>();
	
	private static List<Process> blockedProcessList = new ArrayList<Process>();
	
	private static List<Process> readyProcessList = new ArrayList<Process>();
	
	private static Process runningProcess = null;
	
	private static Integer finishTime =-1;
	
	private static Integer numberOfProcesses;
	
	private static Integer numberOfProcessesTerminated = 0;
	
	private static Integer PROCESSOR_RR_QUANTUM = 2;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String inputFileName = null;
		String outputFileName = null;
		Integer schedulingAlgoNumber = null;
		Scanner sc = null;
		if (args.length == 3) {
			try{
			schedulingAlgoNumber = Integer.parseInt(args[0]);
			if(schedulingAlgoNumber >3 || schedulingAlgoNumber <0) 
				throw new Exception("Number not between 0-2");
			}catch(Exception e){
				System.out.println("The first argument should be between 0-2 for \n 0.First Come and First Serve \n 1.Round Robin Quantum 2 \n 2.Shortest remaining Job First \n" + e);
				System.exit(-1);
			}
			inputFileName = args[1];
			outputFileName = args[2];
		} else {
			System.out
					.println("Error: Only Three arguments are required.Scheduling algorithm \n 0.First Come and First Serve \n 1.Round Robin Quantum 2 \n 2.Shortest remaining Job First \n and Input File name followed by output file name");
			System.exit(-1);
		}
		try {
			sc = new Scanner(new FileReader(inputFileName));
		} catch (FileNotFoundException fe) {
			System.out.println("Error: File Not Found Exception: "
					+ inputFileName);
			System.exit(-1);
		}
		Integer processCount = 0;
		while (sc.hasNextLine()) {
			processList.add(new Process((Integer)sc.nextInt(), (Integer)sc.nextInt(), (Integer)sc.nextInt(), (Integer)sc.nextInt()));
			processCount++;
			if(sc.hasNextLine())
			sc.nextLine();
		}
		numberOfProcesses = processCount;
		
		switch(schedulingAlgoNumber){
		case 2:
			System.out.println("First Come First Serve");
			fcfs(processList);
			break;
		case 1:
			System.out.println("Round Robin Quantum 2");
			roundRobin(processList);
			break;
		case 0:
			System.out.println("Shortest remaining Job First");
			srjf(processList);
			break;
		default:
			System.out.println("The first argument should be between 0-2 for \n 0.First Come and First Serve \n 1.Round Robin Quantum 2 \n 2.Shortest remaining Job First \n");
		}
		
		sc.close();
	}
	
	/**
	 * Compute the first come First Serve results.
	 * @param processList
	 */
	private static void fcfs(List <Process>processList){
		
		Integer schedulerTime = 0;
		Boolean processorBusy = false;
		
		//Sorting the process List on the ioTime from low to high.
/*		Collections.sort(processList,new Comparator<Process>(){
			public int compare(Process first,Process second ){
				return first.getArrivalTime()-second.getArrivalTime();
			}
		});*/
		
		while(numberOfProcessesTerminated != numberOfProcesses){
			
			// Check the processList if there is a process with arrivalTime as that of schedulerTime put that in ready state.
			//Remove that process from the processList.
			for (Process pro : processList) {
				if (schedulerTime == pro.arrivalTime && !pro.getIsStarted()) {
					readyProcessList.add(pro);
					pro.setReadyState();
					pro.setIsStarted();
				}
			}
			
			//Removing elements from the blocked list to if there IoTime is equal to ioTimeSpent and then sending to the ready queue else they remain in the blocked queue.
			if (blockedProcessList.size() > 0) {
				List <Process> blockedProcessRemoved = new ArrayList<Process>(); 
				for (Process blockedProcess : blockedProcessList) {
					if (blockedProcess.getIoTimeSpent() == blockedProcess
							.getIoTime()) {
						Process tempBlockedProcess = blockedProcess;
						if(processorBusy == false){
							blockedProcessRemoved.add(blockedProcess);
							runningProcess = blockedProcess;
							tempBlockedProcess.setRunningState();
						}else if(processorBusy == true && runningProcess.getArrivalTime() > tempBlockedProcess.getArrivalTime()){
							runningProcess.setReadyState();
							readyProcessList.add(runningProcess);
							tempBlockedProcess.setRunningState();
							runningProcess = tempBlockedProcess;
							blockedProcessRemoved.add(blockedProcess);
						}else{
						blockedProcessRemoved.add(blockedProcess);
						readyProcessList.add(tempBlockedProcess);
						tempBlockedProcess.setReadyState();
						}
					}
				}
				for(Process blockedProcessRemove : blockedProcessRemoved){
					blockedProcessList.remove(blockedProcessRemove);
				}
				blockedProcessRemoved = null;
			}
			
			//Running process is checked with the CPU time spent and CPu time and depending on that it terminates or goes to the blocked queue.
			if(processorBusy == true && runningProcess != null){
				if(runningProcess.getCpuTime()/2 == runningProcess.getCpuTimeSpent() && (runningProcess.getIoTime() - runningProcess.getIoTimeSpent() != 0) ){
					blockedProcessList.add(runningProcess);
					runningProcess.setBlockedState();
					runningProcess = null;
					processorBusy = false;
				}else if(runningProcess.getCpuTime() == runningProcess.getCpuTimeSpent()){
					numberOfProcessesTerminated++;
					runningProcess.setIsTerminated();
					runningProcess = null;
					processorBusy = false;
				}
			}
			//Sort the process in the ready queue on the ioTime.If the processor is empty start running the process.
			if (readyProcessList.size() > 0) {
				Collections.sort(readyProcessList, new Comparator<Process>() {
					public int compare(Process first, Process second) {
						return first.getArrivalTime() - second.getArrivalTime();
					}
				});
			//Process from the ready state to running state the process which has the lowest arrival time gets the time of the CPU.
			//If there is already a process running then CPU is preemted if the arrival time of a process in the ready state is lesser than the one running. 
				if(processorBusy == false && runningProcess == null){
					runningProcess = readyProcessList.get(0);
					processorBusy = true;
					readyProcessList.remove(0);
					runningProcess.setRunningState();
				}else if(processorBusy == true && runningProcess != null && runningProcess.getArrivalTime() > readyProcessList.get(0).getArrivalTime()){
					readyProcessList.add(runningProcess);
					runningProcess.setReadyState();
					runningProcess = readyProcessList.get(0);
					runningProcess.setRunningState();
					readyProcessList.remove(0);
				}
			}
			if(numberOfProcessesTerminated != numberOfProcesses){
			finishTime++;
			System.out.print(schedulerTime+":");
			for(Process pro : processList){
				pro.increaseQuantum();
				if(pro.getIsStarted() && !pro.getIsTerminated())
				System.out.print(pro);
			}
			System.out.println();
			}
			schedulerTime++;
		}
		
		System.out.println("Finishing time: "+finishTime);
		for(Process pro : processList){
			System.out.println(pro.printTurnAroundTime());
		}
	}
	
	/**
	 * Implements the round robin algorithm for the process list.
	 * @param processList
	 */
	private static void roundRobin(List <Process>processList){
		Integer schedulerTime = 0;
		Boolean processorBusy = false;
		
		while(numberOfProcessesTerminated != numberOfProcesses){
			for(Process pro : processList){
				if(pro.getArrivalTime() == schedulerTime && !pro.getIsStarted()){
					readyProcessList.add(0,pro);
					pro.setReadyState();
					pro.setIsStarted();
				}
			}
			
			if (blockedProcessList.size() > 0) {
				List <Process> blockedProcessRemoved = new ArrayList<Process>(); 
				for (Process blockedProcess : blockedProcessList) {
					if (blockedProcess.getIoTimeSpent() == blockedProcess
							.getIoTime()) {
						Process tempBlockedProcess = blockedProcess;
						blockedProcessRemoved.add(tempBlockedProcess);
						tempBlockedProcess.setReadyState();
						readyProcessList.add(tempBlockedProcess);
					}
				}
				for(Process blockedProcessRemove : blockedProcessRemoved){
					blockedProcessList.remove(blockedProcessRemove);
				}
				blockedProcessRemoved = null;
			}
			
			if(processorBusy && runningProcess!=null){
				if(runningProcess.getCpuTime()/2 == runningProcess.getCpuTimeSpent() && (runningProcess.getIoTime() - runningProcess.getIoTimeSpent() != 0)){
					runningProcess.resetCpuQuantumTime();
					blockedProcessList.add(runningProcess);
					runningProcess.setBlockedState();
					runningProcess = null;
					processorBusy = false;
				}else if(runningProcess.getCpuTime() == runningProcess.getCpuTimeSpent()){
					runningProcess.resetCpuQuantumTime();
					numberOfProcessesTerminated++;
					runningProcess.setIsTerminated();
					runningProcess = null;
					processorBusy = false;
				}else if(runningProcess.getCpuQuantumTime() == PROCESSOR_RR_QUANTUM){
					runningProcess.resetCpuQuantumTime();
					readyProcessList.add(runningProcess);
					runningProcess.setReadyState();
					runningProcess=null;
					processorBusy = false;
				}
			}
			
			if (readyProcessList.size() > 0) {
				if(processorBusy == false && runningProcess == null){
					runningProcess = readyProcessList.get(0);
					processorBusy = true;
					readyProcessList.remove(0);
					runningProcess.setRunningState();
				}
			}
			
			if(numberOfProcessesTerminated != numberOfProcesses){
				finishTime++;
				System.out.print(schedulerTime+":");
				for(Process pro : processList){
					pro.increaseQuantum();
					if(pro.getIsStarted() && !pro.getIsTerminated())
					System.out.print(pro);
				}
				System.out.println();
				}
				schedulerTime++;	
		}
		System.out.println("Finishing time: "+finishTime);
		for(Process pro : processList){
			System.out.println(pro.printTurnAroundTime());
		}
	}
	
	/**
	 * Algorithm to implement shortest job first for the given set of processes in the processList.
	 * @param processList
	 */
	private static void srjf(List <Process>processList){
		Integer schedulerTime = 0;
		Boolean processorBusy = false;
		
		while(numberOfProcessesTerminated != numberOfProcesses){
			for(Process pro : processList){
				if(pro.getArrivalTime() == schedulerTime && !pro.getIsStarted()){
					readyProcessList.add(pro);
					pro.setReadyState();
					pro.setIsStarted();
				}
			}
			
			if (blockedProcessList.size() > 0) {
				List <Process> blockedProcessRemoved = new ArrayList<Process>(); 
				for (Process blockedProcess : blockedProcessList) {
					if (blockedProcess.getIoTimeSpent() == blockedProcess
							.getIoTime()) {
						Process tempBlockedProcess = blockedProcess;
						blockedProcessRemoved.add(tempBlockedProcess);
						tempBlockedProcess.setReadyState();
						readyProcessList.add(tempBlockedProcess);
					}
				}
				for(Process blockedProcessRemove : blockedProcessRemoved){
					blockedProcessList.remove(blockedProcessRemove);
				}
				blockedProcessRemoved = null;
			}
			
			if(processorBusy && runningProcess!=null){
				if(runningProcess.getCpuTime()/2 == runningProcess.getCpuTimeSpent() && (runningProcess.getIoTime() - runningProcess.getIoTimeSpent() != 0)){
					blockedProcessList.add(runningProcess);
					runningProcess.setBlockedState();
					runningProcess = null;
					processorBusy = false;
				}else if(runningProcess.getCpuTime() == runningProcess.getCpuTimeSpent()){
					numberOfProcessesTerminated++;
					runningProcess.setIsTerminated();
					runningProcess = null;
					processorBusy = false;
				}
			}
			
			if (readyProcessList.size() > 0) {
				Collections.sort(readyProcessList, new Comparator<Process>() {
					public int compare(Process first, Process second) {
						return first.getRemainingCpuTime() - second.getRemainingCpuTime();
					}
				});
				
				if(processorBusy == false && runningProcess == null){
					runningProcess = readyProcessList.get(0);
					processorBusy = true;
					readyProcessList.remove(0);
					runningProcess.setRunningState();
				}else if(processorBusy == true && runningProcess != null && runningProcess.getRemainingCpuTime() > readyProcessList.get(0).getRemainingCpuTime()){
					readyProcessList.add(runningProcess);
					runningProcess.setReadyState();
					runningProcess = readyProcessList.get(0);
					runningProcess.setRunningState();
					readyProcessList.remove(0);
				}
				
			}
			if(numberOfProcessesTerminated != numberOfProcesses){
				finishTime++;
				System.out.print(schedulerTime+":");
				for(Process pro : processList){
					pro.increaseQuantum();
					if(pro.getIsStarted() && !pro.getIsTerminated())
					System.out.print(pro);
				}
				System.out.println();
				}
				schedulerTime++;	
			
		}
		
		System.out.println("Finishing time: "+finishTime);
		for(Process pro : processList){
			System.out.println(pro.printTurnAroundTime());
		}
	}
	/**
	 * Inner Static class to store the process information.
	 * @author Aditya 	
	 *
	 */
	private static class Process{
		
		public static final String RUNNING_STATE = "running";
		
		public static final String BLOCKED_STATE = "blocked";
		
		public static final String READY_STATE = "ready";
		
		private final Integer processId;
		
		private final Integer cpuTime;
		
		private Integer cpuTimeSpent =0;

		private final Integer ioTime;
		
		private Integer ioTimeSpent =0 ;
		
		private Integer readyStateTimeSpent = 0;
		
		private final Integer arrivalTime;
		
		private Integer timeLeftForCompletion;
		
		private String processState = null; 
		
		private Integer turnAroundTime = 0;
		
		private Boolean isStarted = false;
		
		private Boolean isTerminated = false;
		
		private Integer cpuQuantumTime =0;
		
		Process(Integer processId,Integer cpuTime,Integer ioTime,Integer arrivalTime){
			
			this.processId = processId;
			
			this.arrivalTime = arrivalTime;
			
			this.ioTime = ioTime;
			
			this.cpuTime = cpuTime;
			
			this.timeLeftForCompletion = cpuTime + ioTime;
		}
		
		public Integer getTimeLeftForCompletion() {
			return timeLeftForCompletion;
		}

		public void setTimeLeftForCompletion(Integer timeLeftForCompletion) {
			this.timeLeftForCompletion = timeLeftForCompletion;
		}

		public Integer getProcessId() {
			return processId;
		}

		public Integer getCpuTime() {
			return cpuTime;
		}

		public Integer getIoTime() {
			return ioTime;
		}

		public Integer getArrivalTime() {
			return arrivalTime;
		}

		public String getProcessState(){
			return processState;
		}
		
		public Integer getTurnAroundTime(){
			return turnAroundTime;
		}

		public Integer getIoTimeSpent() {
			return ioTimeSpent;
		}
		
		public Integer getCpuTimeSpent() {
			return cpuTimeSpent;
		}
		
		public void setRunningState(){
			processState = Process.RUNNING_STATE;
		}
		
		public void setReadyState(){
			processState = Process.READY_STATE;
		}
		
		public void setBlockedState(){
			processState = Process.BLOCKED_STATE;
		}
		public void setIsStarted(){
			isStarted = true;
		}
		
		public Boolean getIsStarted(){
			return isStarted;
		}
		
		public void setIsTerminated(){
			isTerminated = true;
		}
		
		public Boolean getIsTerminated(){
			return isTerminated;
		}
		public void increaseTurnAroundTime(){
			turnAroundTime++;
		}
		
		public Integer getCpuQuantumTime(){
			return cpuQuantumTime;
		}
		public void increaseIoTimeSpent(){
			ioTimeSpent++;
		}
		
		public void increaseCpuTimeSpent(){
			cpuTimeSpent++;
		}
		
		public void increaseCpuQuantumTime(){
			cpuQuantumTime++;
		}
		
		public void increaseReadyTimeSpent(){
			readyStateTimeSpent++;
		}
		
		public void resetCpuQuantumTime(){
			cpuQuantumTime =0;
		}
		
		public Integer getRemainingCpuTime(){
			return cpuTime - cpuTimeSpent;
		}
		public void increaseQuantum(){
			if(isStarted && !isTerminated){
			if(processState.equals(READY_STATE)){
				increaseReadyTimeSpent();
			}else if(processState.equals(BLOCKED_STATE)){
				increaseIoTimeSpent();
			}else if(processState.equals(RUNNING_STATE)){
				increaseCpuTimeSpent();
				increaseCpuQuantumTime();
			}else{}}
			turnAroundTime = ioTimeSpent+cpuTimeSpent+readyStateTimeSpent;
		}
		public String toString(){
			return " " + processId + ":" + processState + " ";
		}
		
		public String printTurnAroundTime(){
			return "Turnaround process "+processId+": "+turnAroundTime;
		}
	}

}
