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
	    Planner p = new Planner(args[0], Integer.parseInt(args[1]));
	    p.runPlanner();
	}else{
	    System.out.println("Missing argument <input file> and/or <manpower value>.");	
        }
    }
}


