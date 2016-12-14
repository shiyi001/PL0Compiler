import java.io.File;
import java.util.List;

public class GSAnalysis {
	LexAnalysis lex;
	List<Token> allToken
	AllPcode allPcode;

	GSAnalysis(File file) {
		lex = new LexAnalysis(file);
		allToken = lex.getAllToken();

		allPcode = new AllPcode();
	}

	public void showAllToken() {
		for (int i = 0; i < allToken.size(); i++) {
			System.out.println(allToken.get(i).getSt() + " " + allToken.get(i).getLine() + " " + allToken.get(i).getValue());
		}
	}
}
