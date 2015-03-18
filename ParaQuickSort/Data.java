/**
* Klasse ansvarlig for arrayet som skal sorteres. Sorger for I/O
* og maaler kjoretid basert paa sin levetid.
*/
class Data{

    private String[] words;
	final int THREAD_CNT;
    private int wordCnt;
	final int MIN_LENGTH;
	private File input;
    private File output;
	private long startTime;

    Data(String[] args){
		int threads = Integer.parseInt(args[0]);
		THREAD_CNT = (threads % 2 == 0) ? threads+1 : threads;
		input = new File(args[1]);
		output = new File(args[2]);
		readFile(input);
		MIN_LENGTH = wordCnt/THREAD_CNT;
		startTime = System.nanoTime();
    }

	/**
	* Innlesning av fil. Filer maa formateres paa
	* folgende vis:<br>
	* antall ord i filen(heltall)<br>
	* "string"<br>
	* osv...
	* Feil i formateringen, f.eks. galt antall ord i forhold til
	* faktisk lengde gjor at programmet avsluttes med feilmelding.
	* Se dok for java.util.Scanner.
	*/
    public void readFile(File input){

		try{
			Scanner in = new Scanner(input);
			wordCnt = in.nextInt();
			words = new String[wordCnt];

			int i = 0;
			while(i < wordCnt){
				words[i] = in.next();
				i++;
			}

			System.out.println("Read " + i + " words out of " + wordCnt);
			in.close();
		} catch (FileNotFoundException fe){
			System.exit(0);
		} catch (InputMismatchException ie){
			System.out.println("Error in formatting of input file, or wrong sequence of arguments.");
			System.exit(0);
		} catch (NoSuchElementException ne){
			System.out.println("Word count was higher than actual length of document.");
			System.exit(0);
		}
    }

	/** Utskrift av ferdig sortert liste til fil.*/
    public void printArray(){

		try{
			PrintWriter out = new PrintWriter(output);
			System.out.print("\nPrinting " + wordCnt + " words to file... ");

			for (String s : words){
				out.print(s + "\n");
			}
			out.flush();
			System.out.print("Done!\n");
			out.close();
		} catch (Exception e){
			e.getStackTrace();
		}
    }

    public String[] getWords(){
		return words;
    }

	public void runTime(){
		long t = startTime;
		t = System.nanoTime()-t;
		System.out.println("Sorting runtime: " + ((double)(t)/1000000.0) + "ms.");
	}
}