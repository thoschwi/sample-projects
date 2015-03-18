/**
 * Control class. Is responsible for the reading and storing of the dictionary
 * (in a BinarySearchTree), and all spellchecking routines. 
 */
class SpellChecker{

    /**Binary search tree containing the words read from file.*/
    private BinarySearchTree dictionary;
    /**A counter for similar words found in lookups.*/
    private int similarFound;	
	
    /**
     * @param input Name of the input file that is passed along from the 
     * commandline arguments.
     */
    SpellChecker(String input){
	
	dictionary = readFile(new File(input));
    }
	
    public BinarySearchTree readFile(File f){
	
	BinarySearchTree b = new BinarySearchTree();
		
	try{
	    Scanner in = new Scanner(f);
		
	    while(in.hasNext()){		
		String readWord = in.next();
		b.insert(readWord);
	    }
		
	    in.close();
		
	    b.remove("familie");
	    b.insert("familie");
	    b.computeStats();
		
	}catch(FileNotFoundException e){
	    e.printStackTrace();
	    System.exit(0);
	}
	return b;
    }
	
    /**
     * Runs a spelling check on the word passed from user interaction.
     * If no word is found, a call to suggestions(s) is made.
     * @param s The String passed from user interaction.
     */
    public void spellCheck(String s){
	
	if(dictionary.contains(s))
	    System.out.println(s + " was found!");
	else
	    suggestions(s);	
    }
	
    /**
     * Routine for generating suggestions for similar words in 
     * case the user enters a word that could not be found in the dictionary.
     * It begins by collecting all words that are of a similar 
     * (equal to or +/- 1 letter) to narrow down the search. If no words
     * of appropriate length can be found, no further searches are made.
     * Otherwise, 4 subroutines generating similar words (according to the
     * definitions given in the assignment) are run. A runtime report
     * is printed after the searches have been completed.
     * @param s The word entered by the user.
     * @return The suggestions found, if any. Used when printing to file.
     */
    public String suggestions(String s){
		
	similarFound = 0;
	int l = s.length();	
	String sug = "";
	BinarySearchTree lengthMatches = dictionary.getMatches(l);
				
	if(lengthMatches.isEmpty())
	    System.out.println("No similar words found.");
	else{
	    long t = System.nanoTime();
	    sug = switchTest(s, sug, lengthMatches);
	    sug = replacedTest(s, sug, lengthMatches);
	    sug = minusOneTest(s, sug, lengthMatches);
	    sug = plusOneTest(s, sug, lengthMatches);	
			
	    if (sug.length() > 0)
		System.out.print("\n\tDid you mean...\n" + sug + "?\n" 
				 + similarFound 
				 + " possible match(es) found.\n");	
	    else
		System.out.println("No similar words found.");
			
	    System.out.println("Time taken to generate and" 
			       + " look up similar words: " + runTime(t)
			       + " ms.");
	}
	return sug;
    }
	
    /**
     * Tests the possibility that the word entered may contain 
     * pairs of incorrectly switched letters. It sequentially swaps 
     * two and two letters and keeps all possible words for lookup in an array.
     * @param s The word to check for errors.
     * @param sug Any possible matches found is appended to this String
     * @param b The search tree containing words with which to compare.
     * @return String of suggestions found by this routine.
     */
    private String switchTest(String s, String sug, BinarySearchTree b){
	char[] word = s.toCharArray();
	char[] tmp;
		
	String[] words = new String[word.length-1];
		
	for(int i = 0; i < word.length-1; i++){
	    tmp = word.clone();
	    words[i] = swap(i, i+1, tmp);
	}		
	return appendSuggestions(sug, words, b);	
    }
	
    /**
     * Generates suggestions based on the possibility that one
     * letter may have been incorrectly replaced by any letter in the
     * Norwegian alphabet.
     * @param s The word to check for errors.
     * @param sug Any possible matches found is appended to this String
     * @param b The search tree containing words with which to compare.
     * @return String of suggestions found by this routine.
     */
    private String replacedTest(String s, String sug, BinarySearchTree b){
	char[] word = s.toCharArray();
	char[] tmp;
	char[] norAlphabet = {'a','b','c','d','e','f','g','h','i','j',
			      'k','l','m','n','o','p','q','r','s',
			      't','u','v','w','x','y','z','\u00e6','\u00f8','\u00e5'};
	List<String> l = new ArrayList<String>(); 
	String[] words = new String[norAlphabet.length*word.length];
		
	//Sequentially replaces one letter of the word by one from the alphabet.
	for(int i = 0; i < norAlphabet.length; i++){ 
	    for(int j = 0; j < word.length; j++){
		tmp = word.clone();
		tmp[j] = norAlphabet[i];
		l.add(new String(tmp));
	    }
	}
	words = l.toArray(words);
	return appendSuggestions(sug, words, b);
    }
	
    /**
     * Checks to see if one letter may have been added by accident anywhere in
     * the word (if the word was actually meant to be shorter).
     * @param s The word to check for errors.
     * @param sug Any possible matches found is appended to this String
     * @param b The search tree containing words with which to compare.
     * @return String of suggestions found by this routine.
     */
    private String minusOneTest(String s, String sug, BinarySearchTree b){
	int indexRange = s.length()-1;
	String[] words = new String[indexRange+1];
		
	/*i and j begins at opposite ends. When they meet, the letter in the
	  index is skipped.*/
	for(int i = indexRange; i >= 0; i--){
	    String oneShorter = "";
	    for(int j = 0; j < indexRange+1; j++){
		if (j != i) //else, skip this letter
		    oneShorter += s.charAt(j);
	    }
	    words[i] = oneShorter;
	}
	return appendSuggestions(sug, words, b);
    }
	
    /**
     * The opposite of minusOneTest.
     * @param sug Any possible matches found is appended to this String
     * @param b The search tree containing words with which to compare.
     * @return String of suggestions found by this routine.
     */
    private String plusOneTest(String s, String sug, BinarySearchTree b){

	char[] norAlphabet = {'a','b','c','d','e','f','g','h','i','j',
			      'k','l','m','n','o','p','q','r','s',
			      't','u','v','w','x','y','z','\u00e6','\u00f8','\u00e5'};
	String[] words = new String[norAlphabet.length*s.length()];
	List<String> l = new ArrayList<String>();
	
	/*Splits the word in two parts and puts the current letter 
	  in between them.*/
	for(int i = 0; i < s.length(); i++){
	    String oneLonger = "";
	    String partTwo = s.substring(i, s.length());
	    for(int j = 0; j < norAlphabet.length; j++){
		
		String partOne = s.substring(0,i);
		oneLonger = partOne + norAlphabet[j] + partTwo;  
		l.add(oneLonger);
	    }
	    for(int j = 0; j < norAlphabet.length; j++){
		oneLonger = s + norAlphabet[j];//last letter
		l.add(oneLonger);
	    }
	}
	words = l.toArray(words);
	return appendSuggestions(sug, words, b);
    }
	
    /**
     * Helper for switchTest. Swaps two letters of a word at the given indices
     * and returns the new word.
     * @param a Index a
     * @param b Index b
     * @param c Char[] representing the word on which to swap the letters.
     * @return The new word.
     */
    private String swap(int a, int b, char[] c){
	char tmp = c[a];
	c[a] = c[b];
	c[b] = tmp;
		
	return new String(c);
    }
	
    /**
     * Helper for the 4 check routines. Searches the given tree for words 
     * matching the generated words and returns any suggestions as one
     * String with spaces between them.
     * @param s The suggestion String passed along the routines.
     * @param w Array containing possible matches that must be confirmed.
     * @param b Tree with the possible matches.
     * @return String of suggestions that have been found.
     */
    private String appendSuggestions(String s, String[] w, BinarySearchTree b){
	for(int i = 0; i < w.length; i++){
	    if(b.contains(w[i])){
		s += w[i] + " ";
		similarFound++;
	    }
	}
	return s;
    }

    /**
     * Prints a test report containing statistics and spellchecking of
     * the words "etterfølger", "eterfølger", "etterfolger",
     * "etterfølgern" and "tterfølger" to file utskrift.txt. 
     * Is run by pressing 'r' in the commandline interface.
     */
    public void printToFile(){
		 
	try{
	    PrintStream out = new PrintStream(new File("utskrift.txt"));
		
	    out.print(showStats());
	    out.print(suggestions("etterfølger") + "\n");
	    out.print(suggestions("eterfølger") + "\n");
	    out.print(suggestions("etterfolger") + "\n");
	    out.print(suggestions("etterfølgern") + "\n");
	    out.print(suggestions("tterfølger") + "\n");
	    out.flush();
	    out.close();
	}catch(Exception e){
	    e.printStackTrace();
	    System.exit(0);
	}
		
    }
	
    private double runTime(long startTime){
	long t = startTime;
	t = System.nanoTime()-t;
	return ((double)(t)/1000000.0);
    }
	
    public String showStats(){
	return dictionary.computeStats();
    }
} //end SpellChecker
