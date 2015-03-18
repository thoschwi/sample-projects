import java.util.*;
import java.io.*;

/**
 * @author Thomas Schwitalla <thoschwi@student.matnat.uio.no>
 * @version 1.0
 * Notice: The identation may be weird. I've been switching between
 * machines with different character encoding.
 * KNOWN ISSUES:
 * Slack appears to be miscalculated on the screen printouts, but 
 * everything works correctly despite of this.
 */
public class ProjectPlanner{

    public static void main(String[] args){
		
	if(args.length > 1){
	    ProjectPlanner p = new ProjectPlanner(args[0], Integer.parseInt(args[1]));
	    p.runPlanner();
	}else{
	    System.out.println("Missing argument <input file> and/or <manpower value>.");	
        }
    }
}

/**
 * Project planning utility implemented as a graph.
 */
class ProjectPlanner{

    /** The total of tasks in the project. */
    private int taskCount;
    private int manpower;
    private int timeSpent;
	
    /** 
     * All tasks (vertices) in the project. The id of each task is used as key.
     */
    private Map<Integer, Task> tasks;
	/**
	* The optimum sequence of tasks as found by optimumTime().
	*/
    private List<Task> optimumSeq;
	/**
	* The critical tasks.
	*/
    private List<Task> criticals;
	/**
	* Any cycle found by hasCycle() is stored here.
	*/
	private List<Task> cycle;
    
    ProjectPlanner(String input, int m){
		
	tasks = new HashMap<Integer, Task>();
	readFile(input);
	manpower = m;
    }
	
    private class Task implements Comparable<Task>{
			
	int id;
	int time;
	int staff;
	int inDegree;
	int outDegree;
	int earliestStart;
	int latestStart;
	int slack;
	String name;
	/**
	 * Condition variable used by hasCycle() to determine whether
	 * a task is unchecked (-1), still checking (0) or checked (1).
	 */
	int cond = -1;
	/** Adjacency list. */
	List<Task> dependants;
	/** Predecessor list. */
	List<Task> predecessors;
		
	/** 
	 * Used when task is instantiated
	 * from a predecessor list. 
	 * @param i ID no. of the task.
	 */
	Task(int i){
		
	    id = i;
	    dependants = new ArrayList<Task>();
	    predecessors = new ArrayList<Task>();
	}
	
	/**
	 * Full initialization of fields. Used when the current
	 * task being read does not already exist in the map.
	 * @param n Name of task.
	 * @param i ID no. of task.
	 * @param t Time estimate.
	 * @param s Manpower.		
	 */
	Task(String n, int i, int t, int s){
	
	    id = i;
	    name = n;
	    time = t;
	    staff = s;
	    dependants = new ArrayList<Task>();
	    predecessors = new ArrayList<Task>();
	}
	
	public int compareTo(Task t){
		Integer i = new Integer(slack);
		return i.compareTo(t.slack);
	}
		
	String printData(){
			
	    return "\nTask " + id + ": " + name 
		+ "\nTime estimate:\t" + time 
		+ "\nEarliest start:\t" + earliestStart
		+ "\nLatest start:\t" + latestStart
		+ "\nSlack:\t\t" + slack
		+ "\nAvail. staff:\t" + staff
		+ "\nDependants:\t" + printDeps();
	}
		
	String printDeps(){
	    String deps = "";
	    for(Task u : dependants){
		deps += u.id + " ";
	    }
	    return deps + "\n";
	}
		
	/**
	 * Initializes all data fields but the id 
	 * (which is always set on construction). Called when 
	 * the Task was found to be already placed in the map
	 * from a predecessor list.
	 * @param n Name of task.
	 * @param t Time estimate.
	 * @param s Manpower.
	 */
	void setValues(String n, int t, int s){

	    name = n;
	    time = t;
	    staff = s;
	}		
    } //end class Task
		
    /**
     * Finds any circular dependencies to determine 
     * whether or not the project can be realized.
     * @param t The node with which to start the search.
     */
    private void hasCycle(Task t){
	if(t.cond == 0){
		printCycle();
		System.exit(0);
		
	} else if (t.cond == -1){
	    t.cond = 0;
		cycle.add(t);
	    //System.out.println("Currently checking: " + t.id);
	    for(Task u : t.dependants){
			hasCycle(u);
	
	    }
	    t.cond = 1;
	}
    }

	/**
	* Computes and stores the sequence of tasks (V) which
	* results in the shortest possible completion time
	* for the whole project. It does so using a
	* topological sort with a PriorityQueue for storing
	* the nodes with indegree = 0 (E), i.e the task(s)
	* currently running. Running time is O(|V| + |E|).
	*/
    private void optimumTime(){
		optimumSeq = new ArrayList<Task>();
		PriorityQueue<Task> parallels = new PriorityQueue<Task>();
	
		for(Task u : tasks.values())
			if(u.inDegree == 0)
				parallels.add(u);
	
		while(!parallels.isEmpty()){
			int longest = 0;
			Task t = parallels.poll();
			if(t.slack == 0)
					timeSpent += t.time;
			//Handling the special case of the last nodes in the graph:
			if(t.inDegree == 0 && t.outDegree == 0){
				for(Task u : parallels)
					if(u.time > longest)
						longest = u.time;
				timeSpent += longest;
			}

			optimumSeq.add(t);
	    	   
			for(Task v : t.dependants){
				v.inDegree--;
				if(v.inDegree == 0){
					parallels.add(v);
				}	
			}
		}
    }		

	/**
	* Computes the earliest possible start times for
	* each task. First step in determining their slack.
	*/
    private void setEarliestStart(){ 
	LinkedList<Task> startTasks = new LinkedList<Task>();
	
	for(Task u : tasks.values())
	    if(u.inDegree == 0)
		startTasks.add(u);
	
	while(!startTasks.isEmpty()){
	    Task t = startTasks.poll();
	    int dist = 0;

	    for(Task u : t.dependants){
		u.inDegree--;
		dist = t.earliestStart + t.time;
		if(dist > u.earliestStart)
		    u.earliestStart = dist;
	
		if(u.inDegree == 0){
		    startTasks.add(u);

		}
	    }
	}
	resetInDegrees();
    }
    
    /**
     * Computes the latest start for each task in the 
     * project, and sets their slack. 
	 * Also determines the criticals and adds them to a list.
     */
    private void setLatest(){
		for(Task t : tasks.values()){
			int i = 0;
			for(Task u : t.predecessors){
			i = (t.earliestStart - u.time);
			if(i < u.latestStart || u.latestStart == 0)
				u.latestStart = i;	
			}
		}
		criticals = new ArrayList<Task>();
		for(Task t : tasks.values()){
			t.slack = t.earliestStart - t.latestStart;
			if(t.slack == 0)
				criticals.add(t);			
		}
		System.out.println(printCriticals());
    }

    /**
     * Runs all computations necesssary for finding the optimum
     * time, starting with a check for cycles. The program 
     * terminates here if a cycle is found.
     */
    public void runPlanner(){
	cycle = new ArrayList<Task>();
	System.out.print("Checking if project can be realized...");
		for(Task t : tasks.values()){
			hasCycle(t);
		}	
	    System.out.print(" success! Project is valid.\n");
	    setEarliestStart();
	    setLatest();
	    optimumTime();
		System.out.print(printOpt());
		printToFile();
	    
    }
	
	private String printCriticals(){
		String crit = "\n\t***CRITICAL TASKS***\n";
		for(Task t : criticals){
			crit += t.id + ". " + t.name + "\n";
		}
		return crit;
	}
    
	private void printCycle(){
		System.out.print(" failure! Project is invalid.\n"
						+ "The following circular dependency was detected:\n");
		for(Task t : cycle)
			System.out.print(t.id + " -> ");
	}
	
	private String printOpt(){
		String opt = "\n\t***OPTIMUM SEQUENCE***\n";
		for(Task t : optimumSeq)
			opt += t.printData();
		return 
			opt + "\nFastest possible finishing time is " + timeSpent + "\n";
	}
	
    /**
     * Resets the indegrees of each task after setEarliest().
     */
    private void resetInDegrees(){
	for(Task t : tasks.values()){
	    t.inDegree = t.predecessors.size();
	}
    }
	
    private void printToFile(){
	
		try{
		PrintStream out = new PrintStream(new File("output.txt"));
		out.print(printOpt());
		
		out.flush();
		out.close();
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	
    }
	
    private void readFile(String s){
		
	try{
	    Scanner in = new Scanner(new File(s));
	    int taskCount = in.nextInt();		
			
	    while(in.hasNext()){
		int id = in.nextInt();
		String name = in.next();
		int timeEst = in.nextInt();
		int manpower = in.nextInt();
		int dep = in.nextInt();
				
		Task t = tasks.get(id);
		if(t == null){
		    t = new Task(name, id, timeEst, manpower);
		    tasks.put(id, t);
		}else 
		    t.setValues(name, timeEst, manpower);

		while(dep > 0){
		    Task predecessor = tasks.get(dep);
		    if(predecessor == null){
			predecessor = new Task(dep);
			tasks.put(dep, predecessor);
		    }
					
		    t.inDegree++;
		    t.predecessors.add(predecessor);
		    predecessor.outDegree++;
		    predecessor.dependants.add(t);
		    dep = in.nextInt();
		}
	    }
		
	}catch (InputMismatchException e){
	    e.printStackTrace();
	    System.out.println("Error in input file formatting!");
	}catch (FileNotFoundException e){
	    e.printStackTrace();
	}
    }
	
	
}

