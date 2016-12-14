/**
 *created by shiyi on 2016/12/14
 *token类
 */
public class Token {
	private SymType st;
	private int line;
	private String value;

	public Token(SymType _st, int _line, String _value) {
		st = _st;
		line = _line;
		value = _value;
	}

	public void setSt(SymType _st) {
		st = _st;
	}

	public void setLine(int _line) {
		line = _line;
	}

	public void setValue(String _value) {
		value = _value;
	}

	public SymType getSt() {
		return st;
	}

	public int getLine() {
		return line;
	}

	public String getValue() {
		return value;
	}
}
