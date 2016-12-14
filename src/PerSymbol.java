/**
 * created by shiyi on 2016/12/14
 * PerSymbol类
 */
public class PerSymbol {
	private int type;           //表示常量、变量或过程
    private int value;          //表示常量或变量的值
    private int level;          //嵌套层次
    private int address;        //相对于所在嵌套过程基地址的地址
    private int size;           //表示常量，变量，过程所占的大小
    private String name;        //变量、常量或过程名

    public PerSymbol(int _type, int _value, int _level, int _address, int _size, String _name) {
    	type = _type;
    	value = _value;
    	level = _level;
    	address = _address;
    	size = _size;
    	name = _name;
    }

    public PerSymbol(int _type, int _level, int _address, int _size, String _name) {
    	//变量刚声明时没有初始值，过程声明没有值
    	type = _type;
    	level = _level;
    	address = _address;
    	size = _size;
    	name = _name;
    }

    public void setType(int _type) {
    	type = _type;
    }

    public void setValue(int _value) {
        value = _value;
    }

    public void setLevel(int _level) {
        level = _level;
    }

    public void setAddress(int _address) {
        address = _address;
    }

    public void setSize(int _size) {
        size = _size;
    }

    public void setName(String _name) {
        name = _name;
    }

    public int  getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public int getLevel() {
        return level;
    }

    public int getAddress() {
        return address;
    }

    public int getSize() {
        return size;
    }

    public String getName() {
        return name;
    }
}
