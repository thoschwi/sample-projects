/**
 * Beholder for losninger til et brett. En instans
 * av denne er ment som en buffer for utskrift/visning av
 * losninger.
 */
class SudokuContainer{
	
    private ArrayList<Solution> solutionBuffer;
	private int nextIndex = 0;
	
    SudokuContainer(){
	solutionBuffer = new ArrayList<Solution>();
    }
	
    /**
     * Legger inn en Solution paa toppen
     * av listen. Det legges inn maks 500 losninger.
     */
    public void insert(Solution s){
	if(solutionBuffer.size() < 500){
	    solutionBuffer.add(s);
	}
    }
	
    /**
     * Returnerer en Solution paa gitt index
     *
     * @param index Indeksen.
     * @return Peker til Solution paa gitt indeks.
     */
    public Solution get(int index){
	return solutionBuffer.get(index);
    }
	
	public Solution getNext(){
		if (nextIndex == 0){
			nextIndex++;
			return solutionBuffer.get(0);
		}
		nextIndex++;
		if (nextIndex == solutionBuffer.size()-1) nextIndex = 0;
		return solutionBuffer.get(nextIndex);
	}

    /** @returns antallet losninger i beholderen.*/
    public int getSolutionCount(){
	return solutionBuffer.size();
    }
}
