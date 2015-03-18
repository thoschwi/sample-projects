/**
 * Superklasse for ruter i sudokubrettet.
 * Holder sin verdi og pekere til de andre 
 * delene brettet den tilhorer. <p>
 * Rutene er lenket sammen og har ansvar 
 * for å finne losninger paa brettet.
 */
abstract class Square{
	
    /** Verdien til ruten*/
    protected char value;
    /** Peker til brettet.*/
    protected Board mainBoard;
    /** Pekere til raden, kolonnen og boksen ruten tilhorer.*/
    protected Field[] fields;
    /** Den neste ruten i brettet. Den siste ruten.neste er null. */
    protected Square next;
    /** Variabel brukt til å telle opp losninger under testing. */
    static int solutionCount = 0;
		
    /**
     * Rekursiv "bruteforce" av sudokubrett.
     * Denne finner alle losninger paa brett av alle storrelser. 
	 *
     * @param values char[] med brettets mulige verdier (1-9, 1-G osv).
     * @param s et Solution-objekt som skal skrive ned en losning. 
     * Tilordnes et nytt objekt naar en losning er funnet.
     * @return Solution-objektet sendes bakover i rekursjonen.
     */
    public abstract Solution fillInRemainderOfBoard(char[] values, Solution s);

    /** 
     * Sjekker alle rutens Fields for en verdis gyldighet.
     * @return True dersom en verdi IKKE finnes i rutens rad,
     * kolonne eller boks.
     */
    protected boolean isValid(char c){
	for (Field f : fields){
	    if (f.hasValue(c)) return false;
	}
	return true;
    }
    
    /** @return Rutens verdi. */
    public char getValue(){
	return value;
    }
}
