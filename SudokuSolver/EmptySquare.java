/** Tom rute.*/
class EmptySquare extends Square{

    EmptySquare(Row r, Column c, Box b, Board mb){
	fields = new Field[3];
	fields[0] = r;
	fields[1] = c;
	fields[2] = b;
	mainBoard = mb;
    }

    public Solution fillInRemainderOfBoard(char[] values, Solution s){

	for (int i = 0; i < values.length; i++){
	    char c = values[i];
	    if(isValid(c)){
		value = c;
		if (next != null){
		    s = next.fillInRemainderOfBoard(values, s);	
		} else {
		    s.drawSolution(mainBoard.getSquares());
		    mainBoard.buffer.insert(s);
		    s = new Solution(values.length);
		}
		value = '.';
	    }
	}
	return s;
    }


}