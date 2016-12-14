import java.io.File;
import java.util.List;

public class GSAnalysis {
	LexAnalysis lex;

	GSAnalysis(File file) {
		lex = new LexAnalysis(file);
	}

	public void showAllToken() {
		List<Token> allToken = lex.getAllToken();
		for (int i = 0; i < allToken.size(); i++) {
			System.out.println(allToken.get(i).getSt() + " " + allToken.get(i).getLine() + " " + allToken.get(i).getValue());
		}
	}
}
