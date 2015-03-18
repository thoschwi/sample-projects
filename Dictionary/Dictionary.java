import java.util.*;
import java.io.*;

/**
 * @version 1.0.
 * @author Thomas Schwitalla <thoschwi@student.matnat.uio.no>
 * A program for reading and storing dictionaries. Provides a search
 * function with spellchecking.
 */
public class Dictionary{

    public static void main(String[] args){
		
	if (args.length < 1)
	    System.out.println("Missing input file.");
	else{
	    View v = new View(args[0]);
	    v.cmdLoop();
	}
    }
}
