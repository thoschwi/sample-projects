/**
 * Forhaandsutfylt rute. Denne vet sin verdi allerede fra konstruksjon.
 */
class PreFilledSquare extends Square{

    PreFilledSquare(Row r, Column c, Box b, Board mb, char s){
	fields = new Field[3];
	fields[0] = r;
	fields[1] = c;
	fields[2] = b;
	mainBoard = mb;
	value = s;
    }
	
    /**
     * Overstyrt til aa ikke sjekke gyldighet for value og 
     * ikke aa nullstille den.
     */
    public Solution fillInRemainderOfBoard(char[] values, Solution s){
	if (next != null){
	    s = next.fillInRemainderOfBoard(values, s);
	} else {
	    s.drawSolution(mainBoard.getSquares());
	    mainBoard.buffer.insert(s);
	    s = new Solution(values.length);
	}
	return s;
    }
}