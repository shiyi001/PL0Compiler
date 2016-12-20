import java.io.File;
import java.util.Scanner;

import javax.swing.JFileChooser;

public class MyCompiler {

	public static void main(String[] args) {
		try{
			JFileChooser fc = new JFileChooser();
			fc.showOpenDialog(null);
			File file = fc.getSelectedFile();
			if (file.isFile()) {
				GSAnalysis gsa = new GSAnalysis(file);
				gsa.showAllToken();
				if (gsa.compile()) {
					System.out.println("compile succeed!");
					gsa.showAllSymbol();
					gsa.showAllPcode();
					System.out.println("Do you want to run it? Y/N");
					Scanner in = new Scanner(System.in); 
				    String name = in.nextLine(); 
				    if (name.equals("Y") || name.equals("y")) {
				    	gsa.interpreter();
				    }
				} else {
					System.out.println("error happened!");
				}
			}
		} catch (Exception ex) {
			
		}
	}

}
