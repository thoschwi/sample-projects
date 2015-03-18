/**
 * User interface class. Initializes the control object,
 * a SpellChecker.
 */
class View{

    SpellChecker c;
	
    View(String input){
		
	c = new SpellChecker(input);	
    }

    /**
     * Endless loop that prompts user to enter a word for
     * spellchecking, s for statistics or q to quit.
     */
    public void cmdLoop(){
	
	Scanner in = new Scanner(System.in);
	String selection = "a";
		
	while (! selection.equals("q")){
		
	    System.out.print("\nq: quit program\n" 
			     + "r: print test report."
			     + "s: statistics\n"
			     + "...or type a word for spell checking: ");
			
	    try{
		selection = in.next().toLowerCase();
		if(selection.equals("s")) 
		    System.out.print(c.showStats());
		else if(! selection.equals("q"))
		    c.spellCheck(selection);
		if(selection.equals("r"))
		    c.printToFile();
					
	    } catch(Exception e){
		e.printStackTrace();
		System.exit(0);
	    }
	}
	in.close();
    }
}
