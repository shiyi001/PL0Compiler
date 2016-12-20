import java.io.File;
import java.util.List;

public class GSAnalysis {
	LexAnalysis lex;
	List<Token> allToken; //保存词法分析结果
	AllPcode allPcode; //保存生成的Pcode
	AllSymbol allSymbol; //符号表管理

	private boolean errorHappen = false; //记录编译过程中是否发生错误
	private int tokenPtr = 0; //指向当前token的指针

	private int level = 0;
	private int address = 0;
	private int addIncrement = 1;
	
	GSAnalysis(File file) {
		lex = new LexAnalysis(file);
		allToken = lex.getAllToken();

		allPcode = new AllPcode();

		allSymbol = new AllSymbol();
	}

	public boolean compile() {
		program();
		return (!errorHappen);
	}

	private void program() {
		//<主程序>::=<分程序>.
		block();
		if (allToken.get(tokenPtr).getSt() == SymType.POI) {
			tokenPtr++;
			if (allToken.get(tokenPtr).getSt() != SymType.EOF) {
				errorHandle(18, "");
			}
		} else {
			errorHandle(17, "");
		}
	}

	private void block() {
		//<分程序>::=[<常量说明部分>][<变量说明部分>][<过程说明部分>]<语句>
		int address_cp = address; //保存之前的address值
		
		//初始化本层的相关变量
		int start = allSymbol.getPtr(); //本层变量声明的初始位置
		int pos = 0; //本层过程声明在符号表中的位置
		address = 3; //默认已3开始，前几位存放一些跳转关键变量，如原来的base（基地址），pc（程序计数器）等
		if (start > 0) {
			pos = allSymbol.getLevelProc(level);
		}

		//设置跳转指令，跳过声明部分，后面回填
		int tmpPcodePtr = allPcode.getPcodePtr(); 
		allPcode.gen(Operator.JMP, 0, 0);

		if (allToken.get(tokenPtr).getSt() == SymType.CON) {
			conDeclare();
		}
		if (allToken.get(tokenPtr).getSt() == SymType.VAR) {
			varDeclare();
		}
		if (allToken.get(tokenPtr).getSt() == SymType.PROC) {
			proc();
		}

		allPcode.getAllPcode().get(tmpPcodePtr).setA(allPcode.getPcodePtr()); //回填跳转地址
		allPcode.gen(Operator.INT, 0, address); //申请空间
		if (start != 0) {
			//如果不是主函数，则需要在符号表中的value填入该过程在Pcode代码中的起始位置
			allSymbol.getAllSymbol().get(pos).setValue(allPcode.getPcodePtr() - 1 - allSymbol.getAllSymbol().get(pos).getSize());
		}

		statement();
		allPcode.gen(Operator.OPR, 0, 0); //过程结束

		address = address_cp;
	}

	private void conDeclare() {
		//<常量说明部分>::=const <常量定义>{,<常量定义>}
		if (allToken.get(tokenPtr).getSt() == SymType.CON) {
			tokenPtr++;
			conHandle();
			while (allToken.get(tokenPtr).getSt() == SymType.COMMA) {
				tokenPtr++;
				conHandle();
			}
			if (allToken.get(tokenPtr).getSt() == SymType.SEMIC) {
				tokenPtr++;
			} else { //缺少；
				errorHandle(0, "");
			}
		} else { //缺少const
			errorHandle(-1, "");
		}
	}

	private void conHandle() {
		//<常量定义>::=<标识符>=<无符号整数>
		String name;
		int value;
		if (allToken.get(tokenPtr).getSt() == SymType.SYM) {
			name = allToken.get(tokenPtr).getValue();
			tokenPtr++;
			if (allToken.get(tokenPtr).getSt() == SymType.EQU) {
				tokenPtr++;
				if (allToken.get(tokenPtr).getSt() == SymType.CONST) {
					value = Integer.parseInt(allToken.get(tokenPtr).getValue());
					if (allSymbol.isNowExists(name, level)) {
						errorHandle(15, name);
					}
					allSymbol.enterConst(name, level, value, address);
					//address += addIncrement;
					tokenPtr++;
				}
			} else { //赋值没用=
				errorHandle(3, "");
			}
		} else { //标识符不合法
			errorHandle(1, "");
		}
	}

	private void varDeclare() {
		//<变量说明部分>::=var<标识符>{,<标识符>}
		String name;
		int value;
		if (allToken.get(tokenPtr).getSt() == SymType.VAR) {
			tokenPtr++;
			if (allToken.get(tokenPtr).getSt() == SymType.SYM) {
				name = allToken.get(tokenPtr).getValue();
				if (allSymbol.isNowExists(name, address)) {
					errorHandle(15, name);
				}
				allSymbol.enterVar(name, level, address);
				address += addIncrement;
				tokenPtr++;
				while (allToken.get(tokenPtr).getSt() == SymType.COMMA) {
					tokenPtr++;
					if (allToken.get(tokenPtr).getSt() == SymType.SYM) {
						name = allToken.get(tokenPtr).getValue();
						if (allSymbol.isNowExists(name, address)) {
							errorHandle(15, name);
						}
						allSymbol.enterVar(name, level, address);
						address += addIncrement;
						tokenPtr++;
					} else { //非法标识符
						errorHandle(1, "");
						return;
					}
				}
				if (allToken.get(tokenPtr).getSt() != SymType.SEMIC) {
					//缺少；
					errorHandle(0, "");
					return;
				} else {
					tokenPtr++;
				}
			} else { //非法标识符
				errorHandle(1, "");
				return;
			}
		} else { //缺少var
			errorHandle(-1, "");
			return;
		}
	}

	private void proc() {
		//<过程说明部分>::=<过程首部><分程序>{;<过程说明部分>};
		//<过程首部>::=procedure<标识符>;
		if (allToken.get(tokenPtr).getSt() == SymType.PROC) {
			tokenPtr++;
			//System.out.println(allToken.get(tokenPtr).getSt() + " " + errorHappen);
			int count = 0; //记录参数个数
			int pos; //记录该过程在符号表中的位置
			if (allToken.get(tokenPtr).getSt() == SymType.SYM) {
				String name = allToken.get(tokenPtr).getValue();
				if (allSymbol.isNowExists(name, level)) {
					errorHandle(15, name);
				}
				pos = allSymbol.getPtr();
				allSymbol.enterProc(name, level, address);
				address += addIncrement;
				level++;
				tokenPtr++;
				/*********不需要形式参数
				if (allToken.get(tokenPtr).getSt() == SymType.LBR) {
					tokenPtr++;
					if (allToken.get(tokenPtr).getSt() == SymType.SYM) {
						allSymbol.enterVar(allToken.get(tokenPtr).getValue(), level, 3 + address);
						address += addIncrement;
						count++;
						allSymbol.getAllSymbol().get(pos).setSize(count);
						tokenPtr++;
						while (allToken.get(tokenPtr).getSt() == SymType.COMMA) {
							tokenPtr++;
							if (allToken.get(tokenPtr).getSt() == SymType.SYM) {
								allSymbol.enterVar(allToken.get(tokenPtr).getValue(), level, 3 + address);
								address += addIncrement;
								count++;
								allSymbol.getAllSymbol().get(pos).setSize(count);
								tokenPtr++;
							} else {
								errorHandle(1, "");
								return;
							}
						}
					} 
					if (allToken.get(tokenPtr).getSt() == SymType.RBR) {
							tokenPtr++;
							if (allToken.get(tokenPtr).getSt() != SymType.SEMIC) {
								errorHandle(0, "");
								return;
							} else {
								tokenPtr++;
								block();
								while (allToken.get(tokenPtr).getSt() == SymType.SEMIC) {
									tokenPtr++;
									proc();
								}
							}
						} else { //缺少）
							errorHandle(5, "");
							return;
						}
				} else { //缺少（
					errorHandle(4, "");
					return;
				}
				*************/
				if (allToken.get(tokenPtr).getSt() != SymType.SEMIC) {
					errorHandle(0, "");
					return;
				} else {
					tokenPtr++;
					block();
					while (allToken.get(tokenPtr).getSt() == SymType.SEMIC) {
						tokenPtr++;
						level--;
						proc();
					}
				}
			} else {
				errorHandle(-1, "");
				return;
			}
			//System.out.println(allToken.get(tokenPtr).getSt() + " " + errorHappen);
		}
	}

	private void body() {
		//<复合语句>::=begin<语句>{;<语句>}end
		if (allToken.get(tokenPtr).getSt() == SymType.BEG) {
			tokenPtr++;
			statement();
			while (allToken.get(tokenPtr).getSt() == SymType.SEMIC) {
				tokenPtr++;
				statement();
			}
			if (allToken.get(tokenPtr).getSt() == SymType.END) {
				tokenPtr++;
			} else { //缺少end
				errorHandle(7, "");
				return;
			}
		} else { //缺少begin
			errorHandle(6, "");
			return;
		}
	}

	private void statement() {
		//<语句>::=<赋值语句> | <条件语句> | <当循环语句> | <过程调用语句> | <复合语句> | <读语句> | <写语句> | <空>
		if (allToken.get(tokenPtr).getSt() == SymType.IF) {
			//<条件语句>::=if<条件>then<语句>else<语句>
			tokenPtr++;
			condition();
			if (allToken.get(tokenPtr).getSt() == SymType.THEN) {
				int pos1 = allPcode.getPcodePtr();
				allPcode.gen(Operator.JPC, 0, 0);
				tokenPtr++;
				statement();
				int pos2 = allPcode.getPcodePtr();
				allPcode.gen(Operator.JMP, 0, 0);
				allPcode.getAllPcode().get(pos1).setA(allPcode.getPcodePtr());
				allPcode.getAllPcode().get(pos2).setA(allPcode.getPcodePtr());
				if (allToken.get(tokenPtr).getSt() == SymType.ELS) {
					tokenPtr++;
					statement();
					allPcode.getAllPcode().get(pos2).setA(allPcode.getPcodePtr());
				}
			} else {
				errorHandle(8, "");
				return;
			}
		} else if (allToken.get(tokenPtr).getSt() == SymType.WHI) {
			//<当循环语句>::=while<条件>do<语句>
			int pos1 = allPcode.getPcodePtr();
			tokenPtr++;
			condition();
			if (allToken.get(tokenPtr).getSt() == SymType.DO) {
				int pos2 = allPcode.getPcodePtr();
				allPcode.gen(Operator.JPC, 0, 0);
				tokenPtr++;
				statement();
				allPcode.gen(Operator.JMP, 0, pos1);
				allPcode.getAllPcode().get(pos2).setA(allPcode.getPcodePtr());
			} else {
				errorHandle(9, "");
				return;
			}
		} else if (allToken.get(tokenPtr).getSt() == SymType.CAL) {
			//<过程调用语句>::=call<标识符>
			tokenPtr++;
			int count = 0; //参数数目
			PerSymbol tmp;
			if (allToken.get(tokenPtr).getSt() == SymType.SYM) {
				String name = allToken.get(tokenPtr).getValue();
				if (allSymbol.isPreExists(name, level)) {
					tmp = allSymbol.getSymbol(name);
					if (tmp.getType() == allSymbol.getProc()) {
						allPcode.gen(Operator.CAL, level - tmp.getLevel(), tmp.getValue());;
					} else {
						errorHandle(11, "");
						return;
					}
				} else { //不存在该过程
					errorHandle(10, "");
					return;
				}
				tokenPtr++;
				/**************过程调用不需要参数
				if (allToken.get(tokenPtr).getSt() == SymType.LBR) {
					tokenPtr++;
					if (allToken.get(tokenPtr).getSt() == SymType.RBR) {
						tokenPtr++;
						allPcode.gen(Operator.CAL, level - tmp.getLevel(), tmp.getValue());
					} else {
						expression();
						count++;
						while (allToken.get(tokenPtr).getSt() == SymType.COMMA) {
							tokenPtr++;
							expression();
							count++;
						}
						if (count != tmp.getSize()) {
							errorHandle(16, "");
							return;
						}
						allPcode.gen(Operator.CAL, level - tmp.getLevel(), tmp.getValue());
						if (allToken.get(tokenPtr).getSt() == SymType.RBR) {
							tokenPtr++;
						} else {
							errorHandle(5, "");
							return;
						}
					} 
				} else {
					errorHandle(4, "");
					return;
				}
				**************/
			} else {
				errorHandle(1, "");
				return;
			}
		} else if (allToken.get(tokenPtr).getSt() == SymType.REA) {
			//<读语句>::=read'('<标识符>{,<标识符>}')'
			tokenPtr++;
			if (allToken.get(tokenPtr).getSt() == SymType.LBR) {
				tokenPtr++;
				if (allToken.get(tokenPtr).getSt() == SymType.SYM) {
					String name = allToken.get(tokenPtr).getValue();
					if (!allSymbol.isPreExists(name, level)) {
						errorHandle(10, "");
						return;
					} else {
						PerSymbol tmp = allSymbol.getSymbol(name);
						if (tmp.getType() == allSymbol.getVar()) {
							allPcode.gen(Operator.OPR, 0, 16);
							allPcode.gen(Operator.STO, level - tmp.getLevel(), tmp.getAddress());
						} else {
							errorHandle(12, "");
							return;
						}
					}
				}
				tokenPtr++;
				while (allToken.get(tokenPtr).getSt() == SymType.COMMA) {
					tokenPtr++;
					if (allToken.get(tokenPtr).getSt() == SymType.SYM) {
						String name = allToken.get(tokenPtr).getValue();
						if (!allSymbol.isPreExists(name, level)) {
							errorHandle(10, "");
							return;
						} else {
							PerSymbol tmp = allSymbol.getSymbol(name);
							if (tmp.getType() == allSymbol.getVar()) {
								allPcode.gen(Operator.OPR, 0, 16);
								allPcode.gen(Operator.STO, level - tmp.getLevel(), tmp.getAddress());
							} else {
								errorHandle(12, "");
								return;
							}
						}
						tokenPtr++;
					} else {
						errorHandle(1, "");
						return;
					}
				}
				if (allToken.get(tokenPtr).getSt() == SymType.RBR) {
					tokenPtr++;
				} else {
					errorHandle(5, "");
					return;
				}
			} else {
				errorHandle(4, "");
				return;
			}
		} else if (allToken.get(tokenPtr).getSt() == SymType.WRI) {
			//<写语句>::=write '('<表达式>{,<表达式>}')'
			tokenPtr++;
			if (allToken.get(tokenPtr).getSt() == SymType.LBR) {
				tokenPtr++;
				expression();
				allPcode.gen(Operator.OPR, 0, 14);
				while (allToken.get(tokenPtr).getSt() == SymType.COMMA) {
					tokenPtr++;
					expression();
					allPcode.gen(Operator.OPR, 0, 14);
				}
				allPcode.gen(Operator.OPR, 0, 15);
				if (allToken.get(tokenPtr).getSt() == SymType.RBR) {
					tokenPtr++;
				} else { //缺少)
					errorHandle(5, "");
					return;
				}
			} else { //缺少（
				errorHandle(4, "");
			}
		} else if (allToken.get(tokenPtr).getSt() == SymType.BEG) {
			//<复合语句>::=begin<语句>{;<语句>}end
			body();
		} else if (allToken.get(tokenPtr).getSt() == SymType.SYM) {
			//<赋值语句>::=<标识符>:=<表达式>
			String name = allToken.get(tokenPtr).getValue();
			tokenPtr++;
			if (allToken.get(tokenPtr).getSt() == SymType.CEQU) {
				tokenPtr++;
				expression();
				if (!allSymbol.isPreExists(name, level)) {
					errorHandle(14, name);
					return;
				} else {
					PerSymbol tmp = allSymbol.getSymbol(name);
					if (tmp.getType() == allSymbol.getVar()) {
						allPcode.gen(Operator.STO, level - tmp.getLevel(), tmp.getAddress());
					} else {
						errorHandle(13, name);
						return;
					}
				}
			} else {
				errorHandle(3, "");
				return;
			}
		} else if (allToken.get(tokenPtr).getSt() == SymType.REP) {
			//<重复语句> ::= repeat<语句>{;<语句>}until<条件>
			tokenPtr++;
			int pos = allPcode.getPcodePtr();
			statement();
			while (allToken.get(tokenPtr).getSt() == SymType.SEMIC) {
				tokenPtr++;
				statement();
			}
			if (allToken.get(tokenPtr).getSt() == SymType.UNT) {
				tokenPtr++;
				condition();
				allPcode.gen(Operator.JPC, 0, pos);
			} else {
				errorHandle(19, "");
				return;
			}
		} else {
			errorHandle(1, "");
			return;
		}
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
		if (tmp == SymType.ADD || tmp == SymType.SUB) {
			tokenPtr++;
		}
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
			factor();
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
			String name = allToken.get(tokenPtr).getValue();
			if (! allSymbol.isPreExists(name, level)) {
				errorHandle(10, "");
				return;
			} else {
				PerSymbol tmp = allSymbol.getSymbol(name);
				if (tmp.getType() == allSymbol.getVar()) {
					allPcode.gen(Operator.LOD, level - tmp.getLevel(), tmp.getAddress());
				} else if (tmp.getType() == allSymbol.getCon()) {
					allPcode.gen(Operator.LIT, 0, tmp.getValue());
				} else {
					errorHandle(12, "");
					return;
				}
			}
			tokenPtr++;
		} else {
			errorHandle(1, "");
			return;
		}
	}

	private void errorHandle(int k, String name) {
		errorHappen = true;
		switch(k) {
			case -1: //常量定义不是const开头，变量定义不是var开头
				System.out.print("Error happened in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("wrong token"); 
				break;
			case 0: //缺少分号
				System.out.print("Error happened in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Missing semicolon"); 
				break;
			case 1: //标识符不合法
				System.out.print("Error happened in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Identifier illegal"); 
				break;
			case 2: //不合法的比较符
				System.out.print("Error happened in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("illegal compare symbol"); 
				break;
			case 3: //赋值没用=
				System.out.print("Error happened in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Assign must be ="); 
				break;
			case 4: //缺少（
				System.out.print("Error happened in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Missing ("); 
				break;
			case 5: //缺少）
				System.out.print("Error happened in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Missing )"); 
				break;
			case 6: //缺少begin
				System.out.print("Error happened in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Missing begin"); 
				break;
			case 7: //缺少end
				System.out.print("Error happened in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Missing end"); 
				break;
			case 8: //缺少then
				System.out.print("Error happened in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Missing then"); 
				break;
			case 9: //缺少do
				System.out.print("Error happened in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Missing do"); 
				break;
			case 10: //call, write, read语句中，不存在标识符
				System.out.print("Error happened in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Not exist" + allToken.get(tokenPtr).getValue()); 
				break;
			case 11: //该标识符不是proc类型
				System.out.print("Error happened in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println(allToken.get(tokenPtr).getValue() + "is not a procedure"); 
				break;
			case 12: //read, write语句中，该标识符不是var类型
				System.out.print("Error happened in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println(allToken.get(tokenPtr).getValue() + "is not a variable"); 
				break;
			case 13: //赋值语句中，该标识符不是var类型
				System.out.print("Error happened in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println(name + "is not a varible"); 
				break;
			case 14: //赋值语句中，该标识符不存在
				System.out.print("Error happened in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("not exist" + name); 
				break;
			case 15: //该标识符已存在
				System.out.print("Error happened in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Aready exist" + name); 
				break;
			case 16: //调用函数参数错误
				System.out.print("Error happened in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Number of parameters of procedure " + name + "is incorrect"); 
				break;
			case 17: //缺少.
				System.out.print("Error happened in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Missing ."); 
				break;
			case 18: //多余代码
				System.out.print("Error happened in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("too much code after ."); 
				break;
			case 19: //多余代码
				System.out.print("Error happened in line" + allToken.get(tokenPtr).getLine() + ":");
				System.out.println("Missing until"); 
				break;
		}
	}

	public void showAllToken() {
		for (int i = 0; i < allToken.size(); i++) {
			System.out.println(allToken.get(i).getSt() + " " + allToken.get(i).getLine() + " " + allToken.get(i).getValue());
		}
	}

	public void showAllSymbol() {
		List<PerSymbol> display = allSymbol.getAllSymbol();
		for (int i = 0; i < display.size(); i++) {
			System.out.println(display.get(i).getType() + " " +
				display.get(i).getName() + " " +
				display.get(i).getValue() + " " +
				display.get(i).getLevel() + " " +
				display.get(i).getAddress());
		}
	}	

	public void showAllPcode() {
		List<PerPcode> display = allPcode.getAllPcode();
		for (int i = 0; i < display.size(); i++) {
			System.out.print(i + " " + display.get(i).getF() + "     ");
			System.out.println(" " + display.get(i).getL() + " " +display.get(i).getA());
		}
	}

	public void interpreter() {
		if (errorHappen) {
			return;
		}
		Interpreter one = new Interpreter();
		one.setAllPcode(allPcode);
		one.interpreter();
	}
}
