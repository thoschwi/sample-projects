/**
 * Traadklasse for sortering av String[].
 * Deler arrayet opp i segmenter basert på data.THREAD_CNT,
 * og sorterer parallellt med quickSort.
 * Hvis wordCnt <= 10, benyttes innstikksortering, da dette er hurtigere
 * enn QS paa arrayer rundt 10 elementer.<p>
 * Invarianter:<br>
 * Traader lages parvis utover den forste, uansett THREAD_CNT.
 * Det brukes derfor kun odde antall traader til sortering.<br>
 * Barnetraad.range er mother.range/2 (+/-1).<br>
 * Rekursiv sortering tar over naar range/2 <= wordCnt/THREAD_CNT.
 */
class SorterThread extends Thread{

	/** Peker til monitorobjekt.*/
    private Data data;
	/**
	* Arrayet som sorteres. Hver traad sorterer et felles array, men vet
	* hvor i arrayet de skal starte og stoppe sorteringen.
	*/
    private String[] words;
	/** "Pivot" til bruk i quickSort()*/
	private String pivot;
	/** Startindeksen for sorteringen. */
    private int sInd;
	/** Sluttindeksen. */
    private int eInd;
	/** ID-nr brukt til debugging. Er mor.id*2 (+1 for hoyre barn). */
	int id;
    private SorterThread mother = this;
	/** Antallet elementer som traaden skal sortere (eInd - sInd).*/
	private int range;
    private int finishedChildren;

	/**
	* Konstruktor for sekvensiell sortering.
	* @param w Arrayet som skal sorteres.
	*/
	SorterThread(String[] w){
		words = w;
		System.out.println("Sorting " + w.length + " words sequentially...");
    }

    /**
     * Startkonstruktor. Skal kun kjores for den forste
     * traaden som opprettes av main.
     * @param d peker til dataobjekt som holder arrayet som skal sorteres.
     */
    SorterThread(Data d){
		data = d;
		words = d.getWords();
		sInd = 0;
		eInd = words.length-1;
		id = 1;
		range = eInd-sInd;
		System.out.print("Sorting words. Please wait... ");
    }

    /**
     * Konstruktor for barnetraader.
     * @param w Arrayet som sorteres.
     * @param m Peker til mortraaden.
     * @param s Startindeksen for sorteringen.
     * @param e Sluttindeksen.
     */
    SorterThread(String[] w, Data d, SorterThread m, int s, int e, int i){
		data = d;
		words = w;
		mother = m;
		sInd = s;
		eInd = e;
		range = eInd-sInd;
		id = i;
    }

	public void run(){

		quickSort(sInd, eInd);

		if (mother != this) mother.finished();
		else {
			System.out.print("Done!\n");
			data.runTime();
			data.printArray();
		}
	}

	/**
	* Instansierer 2 SorterThread og starter dem. De faar tildelt hver sin halvpart
	* av morens segment. Kalles av quickSort() for aa erstatte rekursive kall.
	*/
	void makeChildren(int lStart, int lEnd, int rStart, int rEnd ){
		new SorterThread(words, data, this, lStart, lEnd, id*2).start();;
		new SorterThread(words, data, this, rStart, rEnd, (id*2)+1).start();;
	}

	/**
	* Parallell/rekursiv QuickSort. Rekursjon benyttes forst naar
	* arraysegmentets lengde er under en gitt minimumsverdi.
	* @param start Startindeksen for sorteringen.
	* @param end Sluttindeksen.
	*/
	void quickSort(int start, int end){
		int pIndex = (start+end)/2;
		pivot = words[pIndex];
		int bigger = start+1;
		int smaller = start+1;

		swap(pIndex, start);

		while(bigger <= end){
			if(words[bigger].compareTo(pivot) < 0){
				swap(bigger, smaller);
				++smaller;
			}
			++bigger;
		}

		swap(start, smaller-1);

		if(range/2 > data.MIN_LENGTH){
			makeChildren(start, smaller-2, smaller, end);
			waitForChildren();
		} else {
			if(smaller-start > 2) quickSort(start, smaller-2);
			if(end-smaller > 0) quickSort(smaller, end);
		}
	}
	/**
	* Bytter words[small] og words[big]. Kalles av quickSort() for a flytte pivot.
	* @param small Den minste verdien.
	* @param big Den storste verdien.
	*/
	void swap(int small, int big){
		String tmp = words[small];
		words[small] = words[big];
		words[big] = tmp;
	}

	/**
	* Innstikksortering av words[start] til words[end].<p>
	* @param start Startindeksen
	* @param end Sluttindeksen.
	*/
	public void insertionSort(int start, int end){
		for(int i = start+1; i < end; i++){
			String insert = words[i];
			int hole = i;
			while(hole > 0 && insert.compareTo(words[hole-1]) < 0){
				words[hole] = words[hole-1];
				--hole;
			}
			words[hole] = insert;
		}
    }

	synchronized void waitForChildren(){
		while(finishedChildren != 2){
			try{
				wait();
			} catch (InterruptedException ie){
				System.exit(0);
			}
		}
    }

	synchronized void finished(){
		finishedChildren++;
		notify();
    }
}