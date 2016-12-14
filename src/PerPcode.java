/**
 * created by shiyi on 2016/12/14
 * 单句Pcode语句类
 */
public class PerPcode {
	private Operator F;
	private int L;
	private int A;

	PerPcode(Operator _F, int _L, int _A) {
		F = _F;
		L = _L;
		A = _A;
	} 

	public void setF(Operator _F) {
		F = _F;
	}

	public void setL(int _L) {
		L = _L;
	}

	public void setA(int _A) {
		A = _A;
	}

	public Operator getF() {
		return F;
	}

	public int getL() {
		return L;
	}

	public int getA() {
		return A;
	}
}
