import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
 * SudokuSolver.java
 * @author Thomas Schwitalla <thoschwi@student.matnat.uio.no>
 * @version v1.0
 * Loser tomme eller delvis utfylte sudokubrett og skriver
 * losningene riktig til fil og GUI.<br>
 * KNOWN ISSUES:<br>
 * Programmet krasjer dersom det finnes feil i formateringen
 * til losningsfilen.<br>
 * Ved GUI-visning av brett med bare 1 losning, laaser programmet seg hvis
 * man trykker mer enn 1 gang paa "next solution".
 */
public class SudokuSolver{

    public static void main(String[] args){
	if (args.length > 0){
	    newBoardFromArgs(args);
	} else {
	    newBoardFromBrowser();
	}
    }
	
    /**
     * Skaper et sudokubrett med parameterne fra
     * kommandolinjen, og ber det iverksette losning.
     * Dersom 2 eller flere argumenter er gitt,
     * brukes det forste til losning og det andre til utskrift.
     * Argumenter utover 2 blir ikke brukt til noe.
     */
    private static void newBoardFromArgs(String[] args){
	File in = new File(args[0]);
	File out = new File("");
	Board b = new Board(in);
	b.bruteforceBoard();
	if (args.length > 1){
	    out = new File(args[1]);
	    b.write(out);
		System.out.println("Solutions were written to " + args[1]);
	} else {
		SudokuGUI s = new SudokuGUI(b.dim, b.boxHeight, b.boxWidth, b.getBuffer());
		}
    }
	
    /**
     * Skaper et sudokubrett fra en losningsfil
     * som velges i en filvelger av typen SudokuFileBrowser.
     * Filvalget restrikteres til .txt.
     * Etter at alle mulige losninger er funnet, kan de fremvises 
     * 1 etter 1 i et GUI (skrives ikke til fil).
     */
    private static void newBoardFromBrowser(){
	SudokuFileBrowser sfb = new SudokuFileBrowser("Please select a solution file");
	int returnVal = sfb.showOpenDialog(sfb);
	if (returnVal == JFileChooser.APPROVE_OPTION){
	    File in = sfb.getSelectedFile();
	    Board b = new Board(in);
		b.bruteforceBoard();
	    SudokuGUI s = new SudokuGUI(b.dim, b.boxHeight, b.boxWidth, b.getBuffer());
	} else {
	    System.out.println("Cancelled.");
	}
    }	
}

