import java.io.File;
import java.util.List;

public class GSAnalysis {
	LexAnalysis lex;
	List<Token> allToken;
	AllPcode allPcode;
	AllSymbol allSymbol;

	private boolean errorHappen = false;
	private int tokenPtr = 0;

	GSAnalysis(File file) {
		lex = new LexAnalysis(file);
		allToken = lex.getAllToken();

		allPcode = new AllPcode();

		allSymbol = new AllSymbol();
	}

	private void condition() {
		//<条件>::=<表达式><关系运算符><表达式> | odd<表达式>
		if (allToken.get(tokenPtr).getSt() == SymType.ODD) {
			allPcode.gen(Operator.OPR, 0, 6);
			tokenPtr++;
			expression();
		} else {
			expression();
			SymType tmp = allToken.get(tokenPtr).getSt();
			tokenPtr++;
			expression();
			if (tmp == SymType.EQU) { //两个结果是否相等
				allPcode.gen(Operator.OPR, 0, 8);
			} else if (tmp == SymType.NEQE) { //两个结果是否不等
				allPcode.gen(Operator.OPR, 0, 9);
			} else if (tmp == SymType.LES) { //小于
				allPcode.gen(Operator.OPR, 0, 10);
			} else if (tmp == SymType.LARE) { //大于等于
				allPcode.gen(Operator.OPR, 0, 11);
			} else if (tmp == SymType.LAR) { //大于
				allPcode.gen(Operator.OPR, 0, 12);
			} else if (tmp == SymType.LESE) { //小于等于
				allPcode.gen(Operator.OPR, 0, 13);
			} else { //不合法的比较运算符
				errorHandle(2, ""); 
			}
		}
	}

	private void expression() {
		//<表达式>::=[+|-]<项>{<加法运算符><项>}
		//<加法运算符>::=+|-
		SymType tmp = allToken.get(tokenPtr).getSt();
		tokenPtr++;
		term();
		if (tmp == SymType.SUB) {
			allPcode.gen(Operator.OPR, 0, 1);
		}
		while (allToken.get(tokenPtr).getSt() == SymType.ADD || allToken.get(tokenPtr).getSt() == SymType.SUB) {
			tmp = allToken.get(tokenPtr).getSt();
			tokenPtr++;
			term();
			if (tmp == SymType.ADD) {
				allPcode.gen(Operator.OPR, 0, 2);
			} else if (tmp == SymType.SUB) {
				allPcode.gen(Operator.OPR, 0, 3);
			}
		}
	}

	private void term() {
		//<项>::=<因子>{<乘法运算符><因子>}
		//<乘法运算符>::=*|/
		factor();
		while (allToken.get(tokenPtr).getSt() == SymType.MUL || allToken.get(tokenPtr).getSt() == SymType.DIV) {
			SymType tmp = allToken.get(tokenPtr).getSt();
			tokenPtr++;
			if (tmp == SymType.MUL) {
				allPcode.gen(Operator.OPR, 0, 4);
			} else if (tmp == SymType.DIV) {
				allPcode.gen(Operator.OPR, 0, 5);
			}
		}
	}

	private void factor() {
		//<因子>::=<标识符> | <无符号整数> | '('<表达式>')'
		if (allToken.get(tokenPtr).getSt() == SymType.CONST) {
			allPcode.gen(Operator.LIT, 0, Integer.parseInt(allToken.get(tokenPtr).getValue()));
			tokenPtr++;
		} else if (allToken.get(tokenPtr).getSt() == SymType.LBR) {
			tokenPtr++;
			expression();
			if (allToken.get(tokenPtr).getSt() == SymType.RBR) {
				tokenPtr++;
			} else { //缺少右括号
				errorHandle(5, "");
			}
		} else if (allToken.get(tokenPtr).getSt() == SymType.SYM) {
			//标识符，需要查表，先不写
		}
	}

	private void errorHandle(int k, String name) {
		errorHappen = true;
		switch(k) {
			case -1: //常量定义不是const开头，变量定义不是var开头
				System.out.print("ERROR" + k + "in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("wrong token"); 
				break;
			case 0: //缺少分号
				System.out.print("ERROR" + k + "in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Missing semicolon"); 
				break;
			case 1: //标识符不合法
				System.out.print("ERROR" + k + "in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Identifier illegal"); 
				break;
			case 2: //不合法的比较符
				System.out.print("ERROR" + k + "in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("illegal compare symbol"); 
				break;
			case 3: //赋值没用:=
				System.out.print("ERROR" + k + "in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Assign must be :="); 
				break;
			case 4: //缺少（
				System.out.print("ERROR" + k + "in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Missing ("); 
				break;
			case 5: //缺少）
				System.out.print("ERROR" + k + "in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Missing )"); 
				break;
			case 6: //缺少begin
				System.out.print("ERROR" + k + "in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Missing begin"); 
				break;
			case 7: //缺少end
				System.out.print("ERROR" + k + "in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Missing end"); 
				break;
			case 8: //缺少then
				System.out.print("ERROR" + k + "in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Missing then"); 
				break;
			case 9: //缺少do
				System.out.print("ERROR" + k + "in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Missing do"); 
				break;
			case 10: //call, write, read语句中，不存在标识符
				System.out.print("ERROR" + k + "in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Not exist" + allToken.get(tokenPtr).getValue()); 
				break;
			case 11: //该标识符不是proc类型
				System.out.print("ERROR" + k + "in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println(allToken.get(tokenPtr).getValue() + "is not a procedure"); 
				break;
			case 12: //read, write语句中，该标识符不是var类型
				System.out.print("ERROR" + k + "in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println(allToken.get(tokenPtr).getValue() + "is not a variable"); 
				break;
			case 13: //赋值语句中，该标识符不是var类型
				System.out.print("ERROR" + k + "in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println(name + "is not a varible"); 
				break;
			case 14: //赋值语句中，该标识符不存在
				System.out.print("ERROR" + k + "in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("not exist" + name); 
				break;
			case 15: //该标识符已存在
				System.out.print("ERROR" + k + "in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Aready exist" + name); 
				break;
			case 16: //调用函数参数错误
				System.out.print("ERROR" + k + "in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Number of parameters of procedure " + name + "is incorrect"); 
				break;
		}
	}

	public void showAllToken() {
		for (int i = 0; i < allToken.size(); i++) {
			System.out.println(allToken.get(i).getSt() + " " + allToken.get(i).getLine() + " " + allToken.get(i).getValue());
		}
	}	
}
