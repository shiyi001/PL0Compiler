import java.util.ArrayList;
import java.util.List;

/**
 * created by shiyi on 2016/12/14
 * 符号表
 */
public class AllSymbol {
	List<PerSymbol> allSymbol;

    private int con=1;              //常量类型用1表示
    private int var=2;              //变量类型用2表示
    private int proc=3;             //过程类型用3表示

	public AllSymbol() {
		allSymbol = new ArrayList<PerSymbol>();
	}

	//向符号表中插入常量
	public void enterConst(String name, int level, int value, int address) {
        allSymbol.add(new PerSymbol(con, value, level, address, 4, name));
    }

    //向符号表中插入变量
    public void enterVar(String name, int level, int address) {
    	allSymbol.add(new PerSymbol(var, level, address, 4, name));
    }

    //向符号表中插入过程
    public void enterProc(String name, int level, int address) {
    	allSymbol.add(new PerSymbol(proc, level, address, 4, name));
    }

	public int getLength() {
		return allSymbol.size();
	}

	public int getCon() {
		return con;
	}

	public int getVar() {
		return var;
	}

	public int getProc() {
		return proc;
	}
}
