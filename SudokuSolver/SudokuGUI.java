/** 
 * Tegner ut et Sudoku-brett.
 */
class SudokuGUI extends JFrame implements ActionListener{

    private final int RUTE_STRELSE = 50;
    private final int PLASS_TOPP = 50;	

    private JTextField[][] brett;  
    private int dimensjon;	
    private int vertikalAntall;
    private int horisontalAntall;
    private SudokuContainer buffer;

    public SudokuGUI(int dim, int hd, int br, SudokuContainer b) {
	dimensjon = dim;
	vertikalAntall = hd;
	horisontalAntall = br;
	buffer = b;

	brett = new JTextField[dimensjon][dimensjon];

	setPreferredSize(new Dimension(dimensjon * RUTE_STRELSE, 
				       dimensjon  * RUTE_STRELSE + PLASS_TOPP));
	setTitle("Sudoku " + dimensjon +" x "+ dimensjon );
	setDefaultCloseOperation(EXIT_ON_CLOSE);
	setLayout(new BorderLayout());

	JPanel knappePanel = lagKnapper();
	JPanel brettPanel = lagBrettet();

	getContentPane().add(knappePanel,BorderLayout.NORTH);
	getContentPane().add(brettPanel,BorderLayout.CENTER);
	pack();
	setVisible(true);
    }

    /** 
     * Lager et panel med alle rutene. 
     * @return en peker til panelet som er laget.
     */
    private JPanel lagBrettet() {
	int topp, venstre;
	JPanel brettPanel = new JPanel();
	brettPanel.setLayout(new GridLayout(dimensjon,dimensjon));
	brettPanel.setAlignmentX(CENTER_ALIGNMENT);
	brettPanel.setAlignmentY(CENTER_ALIGNMENT);
	setPreferredSize(new Dimension(new Dimension(dimensjon * RUTE_STRELSE, 
						     dimensjon * RUTE_STRELSE)));		
	for(int i = 0; i < dimensjon; i++) {
	    topp = (i % vertikalAntall == 0 && i != 0) ? 4 : 1;
	    for(int j = 0; j < dimensjon; j++) {
		venstre = (j % horisontalAntall == 0 && j != 0) ? 4 : 1;

		JTextField ruten = new JTextField();
		ruten.setBorder(BorderFactory.createMatteBorder
				(topp,venstre,1,1, Color.black));
		ruten.setHorizontalAlignment(SwingConstants.CENTER);
		ruten.setPreferredSize(new Dimension(RUTE_STRELSE, RUTE_STRELSE));
		ruten.setText("A");
		brett[i][j] = ruten;
		brettPanel.add(ruten);
	    }
	}
	return brettPanel;
    }

    /** 
     * Lager et panel med noen knapper. 
     * @return en peker til panelet som er laget.
     */
    private JPanel lagKnapper(){
	JPanel knappPanel = new JPanel();
	knappPanel.setLayout(new BoxLayout(knappPanel, BoxLayout.X_AXIS));
	/*JButton finnSvarKnapp = new JButton("Find solutions"); 
	Denne er overflodig. Alle losningene er funnet for GUIet starter.*/
	JButton nesteKnapp = new JButton("Find next solution");
	nesteKnapp.addActionListener(this);
	//knappPanel.add(finnSvarKnapp);
	knappPanel.add(nesteKnapp);
	
	return knappPanel;
    }

    public void actionPerformed(ActionEvent e){
		char[][] c = buffer.getNext().print();
		for(int x = 0; x < c.length; x++){
			for(int y = 0; y < c.length; y++){
				brett[x][y].setText(c[x][y] + "");
			}
		}
    }
}