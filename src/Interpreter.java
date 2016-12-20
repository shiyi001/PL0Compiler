import java.util.List;
import java.util.Scanner;

public class Interpreter {
	private int STACK_SIZE = 1000;
	private int[] dataStack = new int[STACK_SIZE];
	List<PerPcode> pcode;

	public void setAllPcode(AllPcode allPcode) {
		pcode = allPcode.getAllPcode();
	}

	public void interpreter() {
		int pc = 0; //程序计数器，指向下一条指令
		int base = 0; //当前基地址
		int top = 0; //栈顶
		do {
			PerPcode currentPcode = pcode.get(pc);
			pc++;
			/*
			for (int i = 0; i < top; i ++) {
				System.out.print(dataStack[i] + " ");
			}
			System.out.println();
			*/
			if (currentPcode.getF() == Operator.LIT) {
				dataStack[top] = currentPcode.getA();
				top++;
			} else if (currentPcode.getF() == Operator.OPR) {
				switch(currentPcode.getA()) {
				case 0:
					top = base;
                              pc = dataStack[base + 2];
                              base = dataStack[base];
                              break;
                   	case 1:
                   		dataStack[top - 1] = -dataStack[top - 1];
                              break;
                   	case 2:
                   		dataStack[top - 2] = dataStack[top - 1] + dataStack[top - 2];
                   		top--;
                   		break;
                   	case 3:
                   		dataStack[top - 2] = dataStack[top - 2] - dataStack[top - 1];
                   		top--;
                   		break;
                   	case 4:
                   		dataStack[top - 2] = dataStack[top - 1] * dataStack[top - 2];
                   		top--;
                   		break;
                   	case 5:
                   		dataStack[top - 2] = dataStack[top - 2] / dataStack[top - 1];
                   		top--;
                   		break;
                   	case 6:
                   		dataStack[top - 1] = dataStack[top - 1] % 2;
                   		break;
                   	case 7:
                   		break;
                   	case 8:
                   		if (dataStack[top - 2] == dataStack[top - 1]) {
                   			dataStack[top - 2] = 1;
                   		} else {
                   			dataStack[top - 2] = 0;
                   		}
                   		top--;
                   		break;
                   	case 9:
                   		if (dataStack[top - 2] != dataStack[top - 1]) {
                   			dataStack[top - 2] = 1;
                   		} else {
                   			dataStack[top - 2] = 0;
                   		}
                   		top--;
                   		break;
                   	case 10:
                   		if (dataStack[top - 2] < dataStack[top - 1]) {
                   			dataStack[top - 2] = 1;
                   		} else {
                   			dataStack[top - 2] = 0;
                   		}
                   		top--;
                   		break;
                   	case 11:
                   		if (dataStack[top - 2] >= dataStack[top - 1]) {
                   			dataStack[top - 2] = 1;
                   		} else {
                   			dataStack[top - 2] = 0;
                   		}
                   		top--;
                   		break;
                   	case 12:
                   		if (dataStack[top - 2] > dataStack[top - 1]) {
                   			dataStack[top - 2] = 1;
                   		} else {
                   			dataStack[top - 2] = 0;
                   		}
                   		top--;
                   		break;
                   	case 13:
                   		if (dataStack[top - 2] <= dataStack[top - 1]) {
                   			dataStack[top - 2] = 1;
                   		} else {
                   			dataStack[top - 2] = 0;
                   		}
                   		top--;
                   		break;
                   	case 14:
                   		System.out.print(dataStack[top - 1]);
                   		System.out.print(" ");
                   		break;
                   	case 15:
                   		System.out.println();
                   		break;
                   	case 16:
                   		Scanner s = new Scanner(System.in);
                   		dataStack[top] = s.nextInt();
                   		top++;
                   		break;
				}
			} else if (currentPcode.getF() == Operator.LOD) {
				dataStack[top] = dataStack[currentPcode.getA() + getBase(base, currentPcode.getL())];
				top++;
			} else if (currentPcode.getF() == Operator.STO) {
				dataStack[currentPcode.getA() + getBase(base, currentPcode.getL())] = dataStack[top - 1];
				top--;
			} else if (currentPcode.getF() == Operator.CAL) {
				dataStack[top] = base;
				dataStack[top + 1] = getBase(base, currentPcode.getL());
				dataStack[top + 2] = pc;
				base = top;
				pc = currentPcode.getA();
			} else if (currentPcode.getF() == Operator.INT) {
				top = top + currentPcode.getA();
			} else if (currentPcode.getF() == Operator.JMP) {
				pc = currentPcode.getA();
			} else if (currentPcode.getF() == Operator.JPC) {
				if (dataStack[top - 1] == 0) {
					pc = currentPcode.getA();
				}
			}
			//System.out.println(pc + " " + base + " " + top);
		} while (pc != 0);
	}

    private int getBase(int nowBp,int lev) { 
        int oldBp = nowBp;      
        while (lev > 0) {
            oldBp = dataStack[oldBp + 1];
            lev--;
        }
        return oldBp;
    }
}
