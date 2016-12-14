import java.io.File;
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
			}
		} catch (Exception ex) {
			
		}
	}

}
