import java.util.*;
import java.util.concurrent.*;
import java.io.*;

/**
* Sorterer String[] fra fil med parallell/rekursiv QuickSort
* for arrayer > 32 ord, eller sekvensielt med innstikksortering for < 11. <p>
* Skal kjores med kommandolinjeargumentene
* "antall traader", "inputfil(.txt)", "utfil(.txt)".
* @version 1.0
* @author Thomas Schwitalla <thoschwi@student.matnat.uio.no>
*/
public class ParaQuickSort {

    public static void main(String[] args){
		if(args.length < 3){
			System.out.println("Error! Need 3 arguments in the following sequence to run:" +
								"\n1: <Number of threads to sort with (integer)>." +
								"\n2: <name of input file(.txt)>" +
								"\n3: <name of output file(.txt)>");
			return;
		}

		Data d = new Data(args);
		String[] toSort = d.getWords();
		if(toSort.length <= 10){
			SorterThread s = new SorterThread(d);
			s.insertionSort(0, toSort.length-1);
		} else {
			SorterThread s = new SorterThread(d);
			s.start();
		}
    }
}
