/**
 * Klasse som representerer et sudokubrett.
 * Dimensjonene paa brettet, samt eventuelle
 * forhaandsutfylte verdier, defineres i filen
 * som leses inn ved konstruksjon. Det tas IKKE
 * hoyde for at formateringen i filen kan vaere gal.<p>
 * Brettet har ansvar for aa iverksette sok etter
 * mulige losninger. 
 */
class Board{

    /**	Brettets mulige losninger lagres i denne ved funn. */
    SudokuContainer buffer;

    /**	Dimensjonen (hoyde*bredde). */
    int dim;
    /**	Hoyden paa boksene. */
    int boxHeight;
    /**	Bredden paa boksene. */
    int boxWidth;
	
    /**	Tabell over pekere til alle brettets ruteobjekter. */
    private Square[][] squares;
    /**	Pekere til radobjektene. */
    private Row[] rows;
    /**	Kolonneobjektene. */
    private Column[] columns;
    /**	Boksene.*/
    private Box[] boxes;
	
    /**
     * Innleste verdier i radene. 1 String holder 1 rad.
     * Initialiseres ved innlesning fra fil. 
     */
    private String[] readRows;
	
    /**	
     * Konstruerer et brettobjekt.
     *
     * @param i Inputfil. Sendes videre til read().
     */
    Board(File i){
	buffer = new SudokuContainer();
	read(i);
	constructBoard(readRows);
    }
	
    /**
     * Innlesning av filen som tas inn i konstruktoren.
     * Det tas utgangspunkt i at filen formateres paa	
     * folgende vis:
     * "dimensjon"<br>
     * "bokshoyde"<br>
     * "boksbredde"<br>
     * "ruteruteruterute" <- 1 rad med ruter som enten er en
     * tallverdi fra 1 til 9 eller 1-A og oppover
     * hvis verdien gaar over 9. Tomme ruter skal
     * noteres med et punktum ('.').
     */
    private void read(File input){
	try{
	    Scanner in = new Scanner(input);
	    dim = in.nextInt();
	    boxHeight = in.nextInt();
	    boxWidth = in.nextInt();
	    readRows = new String[dim];
	    System.out.print("Dimension: \t"+ dim + "\n" + "Box height: \t" 
						+ boxHeight + "\n" + "Box width: \t" 
						+ boxWidth + "\n" + "*THE BOARD*\n");
	    		
	    for (int i = 0; i < dim; i++){
		readRows[i] = in.next();
		System.out.print(readRows[i] + "\n");
	    }
	    in.close();		
	} catch (Exception e){
	    e.printStackTrace();
	}
    }
	
    /**	
     * Den egentlige konstruksjonen skjer her. 
     * Alle objekter av typen Field (rad, kolonne, boks)
     * og Squares opprettes, og pekere til disse distribueres 
     * dit de skal vaere.
     *
     * @param s Array med alle radene som er blitt lest inn.
     * Disse deles opp i et char[] rad for rad. Enkeltverdiene
     * sendes videre til sine respektive ruter.
     */	
    private void constructBoard(String[] s){
	squares = new Square[dim][dim];
	rows = new Row[dim]; 
	columns = new Column[dim];
	boxes = new Box[boxHeight*boxWidth];	
	createFields();
	
	/*NB! x er egentlig y og omvendt her.
	  Den ytterste lokken traverserer altsaa den forste
	  kolonnen, og den innerste alle radene.*/ 
	for (int x = 0; x < dim; x++){
	    String row = s[x];
	    char[] rowSolutions = row.toCharArray();
		
	    for (int y = 0; y < dim; y++){
		Box b = boxes[(x/boxHeight)*boxHeight+(y/boxWidth)];
		squares[x][y] = (rowSolutions[y] != '.') //sjekk om ruten er prefilled
		    ? new PreFilledSquare(rows[x], columns[y], b, this, rowSolutions[y])
		    : new EmptySquare(rows[x], columns[y], b, this);
		if (y > 0) squares[x][y-1].next = squares[x][y];
		if (x > 0 && y == 0) squares[x-1][dim-1].next = squares[x][y];
		rows[x].addSquare(squares[x][y]);
		columns[y].addSquare(squares[x][y]);
		b.addSquare(squares[x][y]);
	    }
	}
    }
	
    /**	
     * Utskrift fra buffer til fil.
     * Kalles kun dersom 2 eller fler filnavn
     * er tatt inn fra kommandolinjeargumenter.
     *
     * @param output Filen som losningene skrives til.
     */
    public void write(File output){
	try{
	    PrintStream out = new PrintStream(output);
			
	    for (int i = 0; i < buffer.getSolutionCount(); i++){
		char[][] c = buffer.get(i).print();
		out.print((i+1) + ":");
			
		for (int x = 0; x < dim; x++){
		    out.print(" ");
		    for (int y = 0; y < dim; y++){
			out.print(c[x][y]);
		    }
		    out.print("//");
		}
		out.print("\n");
		out.flush();
	    }
	    out.close();			
	} catch (Exception e){
	    e.printStackTrace();
	}
    }
		
    /**	
     * Oppretter alle Field-objektene og tilordner
     * pekerne til de globale arrayene.
     */	
    private void createFields(){
	for (int i = 0; i < dim; i++){
	    rows[i] = new Row(dim);
	    columns[i] = new Column(dim);
	    boxes[i] = new Box(dim);
	}
    }
	
    /**
     * Iverksetter sok etter alle mulige losninger
     * ved kall på squares[0][0].fillInRemainderOfBoard().
     * Rekkevidden paa de mulige verdiene i brettet bestemmes
     * basert paa dets dimensjon, og sendes med metodekallet
     * til ruten.
     */
    public void bruteforceBoard(){
	char[] valuesInThisBoard = new char[dim];
	int charValue = 48;
	for (int i = 0; i < dim; i++){
	    charValue = (charValue == 57) ? 65 : charValue+1;
	    valuesInThisBoard[i] = (char) charValue;
	}
	System.out.println("Finding solutions...");
	squares[0][0].fillInRemainderOfBoard(valuesInThisBoard, new Solution(dim));
    }
	
    public Square[][] getSquares(){
	return squares;
    }
	
    public SudokuContainer getBuffer(){
	return buffer;
    }
}