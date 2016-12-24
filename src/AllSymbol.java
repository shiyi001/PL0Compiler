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

    private int ptr = 0;

	public AllSymbol() {
		allSymbol = new ArrayList<PerSymbol>();
	}

	//向符号表中插入常量
	public void enterConst(String name, int level, int value, int address) {
        allSymbol.add(new PerSymbol(con, value, level, address, 0, name));
        ptr++;
    }

    //向符号表中插入变量
    public void enterVar(String name, int level, int address) {
    	allSymbol.add(new PerSymbol(var, level, address, 0, name));
        ptr++;
    }

    //向符号表中插入过程
    public void enterProc(String name, int level, int address) {
    	allSymbol.add(new PerSymbol(proc, level, address, 0, name));
        ptr++;
    }

    //在符号表当前层查找变量是否存在
    //存疑？
    //这样暴力查找好像存在一些问题
    public boolean isNowExists(String name, int level) {
    	for (int i = allSymbol.size(); i >= 0; i--) {
    		if (allSymbol.get(i).getName().equals(name) && allSymbol.get(i).getLevel() == level) {
    			return true;
    		}
            if (allSymbol.get(i).getLevel() < level) {
                break;
            }
    	}
    	return false;
    }

    //在符号表之前层查找符号是否存在
    //存疑？
    //暴力查找存在问题
    public boolean isPreExists(String name, int level) {
    	for (int i = allSymbol.size(); i >= 0; i--) {
    		if (allSymbol.get(i).getName().equals(name) && allSymbol.get(i).getLevel() <= level) {
    			return true;
    		}
            if (allSymbol.get(i).getType == proc) {
                level--;
            }
    	}
    	return false;
    }

    //按名称查找变量
    public PerSymbol getSymbol(String name) {
    	for (int i = allSymbol.size() - 1; i >= 0; i--) {
    		if (allSymbol.get(i).getName().equals(name)) {
    			return allSymbol.get(i);
    		}
    	}
    	return null;
    }

    //查找当前层所在的过程
    public int getLevelProc(int level) {
    	for (int i = allSymbol.size() - 1; i >= 0; i--) {
    		if (allSymbol.get(i).getType() == proc) {
    			return i;
    		}
    	}
    	return -1;
    }

    public List<PerSymbol> getAllSymbol() {
        return allSymbol;
    }

    public void setPtr(int _ptr) {
        ptr = _ptr;
    }

    public int getPtr() {
        return ptr;
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
