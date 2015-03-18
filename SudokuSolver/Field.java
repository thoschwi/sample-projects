/**
 * Representerer feltene et sudokubrett bestaar av:
 * rader, kolonner og bokser. Vet om sine ruter og 
 * er ansvarlig for aa sjekke disse for gyldighet.
 */
abstract class Field{

    /** Rutene feltet bestaar av. */
    protected Square[] squares;
    protected int squareCount = 0;
	
    /** 
     * Legger til en ny rute paa den forste ledige plassen.
     * @param s En ny Square.
     */
    public void addSquare(Square s){
	squares[squareCount] = s;
	++squareCount;
    }
	
    /** 
     * Henter en rute.
     * @param i Indeksverdien ruten skal finnes paa.
     * @return Ruten paa gitt indeks. 
     */
    public Square getSquare(int i){
	return squares[i];
    }

    /**
     * Traverserer alle ruter i feltet og sammenligner
     * deres verdier med en gitt verdi.
     *
     * @param c Char-verdien som skal sjekkes.
     * @return True dersom feltet har den gitte verdien.
     */
    public boolean hasValue(char c){
	for(int i = 0; i < squares.length; i++){
	    if (squares[i].getValue() == c)
		return true;
	}
	return false;
    }
}
