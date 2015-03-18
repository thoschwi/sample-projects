/***
 * Representerer en losning av et brett.
 * Holder verdier i et char[][] som tilsvarer
 * brettet sine ruter.
 */
class Solution{

    /** Holder losningsverdiene. */
    private char[][] solution;
    private int dim;
	
    /**
     * @param dim Dimensjonen paa solution. Skal vaere
     * identisk med Board.squares.
     */
    Solution(int d){
	dim = d;
	solution = new char[d][d]; 
    }
	
    /**
     * Kalles ved utskrift til fil/skjerm.
     * 
     * @return Losning av brettet.	
     */
    public char[][] print(){
	return solution;
    }
		
    /**
     * Tar inn en peker til brettets rutearray
     * og tilordner verdiene i rutene til solution 
     * paa tilsvarende posisjoner. Kalles kun ved funnet losning
     * av fillInRemainderOfBoard().
     *
     * @param s Peker til brettets rutearray (squares).
     */
    public void drawSolution(Square[][] s){
	for (int x = 0; x < dim; x++){
	    for (int y = 0; y < dim; y++) {
		solution[x][y] = s[x][y].getValue();
	    }
	}
    }
}