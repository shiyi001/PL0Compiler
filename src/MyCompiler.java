import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class MyCompiler extends JFrame{

	private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu projectMenu;
    private JMenu helpMenu;
    private JTextArea jTextArea;
    private JScrollPane jScrollPane;
    private JMenuItem openItem, closeItem, saveItem,aboutItem;
    private JMenuItem compileItem, runItem;
    
    private FileDialog open,save;
    private File file;  

    private JPanel tablePanel; //放置所有表格
    //token表格
    private JScrollPane tokenJScrollPane;
    private JTable tokenTable;
    String[] tokenColumnNames = {"符号类型","所在行", "符号值"};
    private TableModel tokenTableModel = new DefaultTableModel(tokenColumnNames, 0);

    //symbol表格
    private JScrollPane symbolJScrollPane;
    private JTable symbolTable;
    String[] symbolColumnNames = {"变量名", "变量类型", "变量值", "变量层次", "变量地址"};
    private TableModel symbolTableModel = new DefaultTableModel(symbolColumnNames, 0);

    //pcode表格
    private JScrollPane pcodeJScrollPane;
    private JTable pcodeTable;
    String[] pcodeColumnNames = {"F", "L", "A"};
    private TableModel pcodeTableModel = new DefaultTableModel(pcodeColumnNames, 0);

    private JTextArea errorMessage;
    private JScrollPane errorPane;

    private GSAnalysis gsa;
    private List<Token> allToken;
    private List<PerSymbol> allSymbol;
    private List<PerPcode> allPcode;

    public MyCompiler() {
    	init();
    }

    private void init() {
	    JFrame frame = new JFrame("PL0Compiler");
	    frame.setBounds(300, 300, 700, 450);
	    frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

	    menuBar = new JMenuBar();//菜单栏
	    fileMenu = new JMenu("文件");
	    projectMenu = new JMenu("项目");
	    helpMenu = new JMenu("帮助");
	    
	    jTextArea = new JTextArea(10, 40);
	    jTextArea.setFont(new Font("Monospaced",1,20));
	    jTextArea.setLineWrap(true);//到达指定宽度则换行
	    //应当首先利用构造函数指定JScrollPane的控制对象，此处为JTextArea，然后再添加JScrollPane
	    //添加进面板
	    jScrollPane = new JScrollPane(jTextArea);
	    //设置滚动条自动出现
	    jScrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
	    jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
	    jScrollPane.setViewportView(jTextArea);
	    openItem = new JMenuItem("打开");
	    saveItem = new JMenuItem("保存");
	    closeItem = new JMenuItem("关闭");
	    aboutItem = new JMenuItem("关于");
	    compileItem = new JMenuItem("编译");
	    runItem = new JMenuItem("运行");

	    //添加两个选项卡到JMenu
	    //添加字菜单项到菜单项
	    menuBar.add(fileMenu);
	    menuBar.add(projectMenu);
	    menuBar.add(helpMenu);
	    fileMenu.add(openItem);
	    fileMenu.add(saveItem);
	    fileMenu.add(closeItem);  
	    projectMenu.add(compileItem);
	    projectMenu.add(runItem);      
	    helpMenu.add(aboutItem);
	    
	    //设置token表格
	    tokenTable = new JTable(tokenTableModel);
	    tokenTable.setPreferredScrollableViewportSize(new Dimension(300, 100));
        tokenTable.setFillsViewportHeight(true);
        tokenJScrollPane = new JScrollPane(tokenTable);

       	//设置symbbol表格
        symbolTable = new JTable(symbolTableModel);
	    symbolTable.setPreferredScrollableViewportSize(new Dimension(300, 100));
        symbolTable.setFillsViewportHeight(true);
        symbolJScrollPane = new JScrollPane(symbolTable);

        //设置pcode表格
        pcodeTable = new JTable(pcodeTableModel);
	    pcodeTable.setPreferredScrollableViewportSize(new Dimension(300, 100));
        pcodeTable.setFillsViewportHeight(true);
        pcodeJScrollPane = new JScrollPane(pcodeTable);

        tablePanel = new JPanel();
        tablePanel.setLayout( new GridLayout (0, 1));
        tablePanel.add(tokenJScrollPane);
        tablePanel.add(symbolJScrollPane);
        tablePanel.add(pcodeJScrollPane);
        
		//出错信息
		errorMessage = new JTextArea();
		errorPane = new JScrollPane(errorMessage);
		errorPane.setPreferredSize(new Dimension(700, 100));

	    //放置菜单项及输入框
	    frame.add(menuBar, BorderLayout.NORTH);
	    frame.add(jScrollPane, BorderLayout.CENTER);
	    frame.add(tablePanel, BorderLayout.EAST);
	    frame.add(errorPane, BorderLayout.SOUTH);
            
	    open = new FileDialog(frame,"打开文档",FileDialog.LOAD);
	    save = new FileDialog(frame,"保存文档",FileDialog.SAVE); 

	    Event();
	    frame.setVisible(true);
    }

    private void Event() {
	    closeItem.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            System.exit(0);
	        }
	    });

	    aboutItem.addActionListener(new ActionListener() {      
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            JOptionPane.showMessageDialog(null, "PL0Compiler\n"
	                    + "made by shiyi001\ni_am_shiyi@163.com");
	        }
	    });

		openItem.addActionListener(new ActionListener() {//菜单条目监听：打开   
		    public void actionPerformed(ActionEvent e) {  
		        open.setVisible(true);  

		        String dirPath = open.getDirectory();  
	            String fileName = open.getFile();  
	            if (dirPath == null || fileName == null) {
	            	return;
	            }   
	            file = new File(dirPath, fileName);   

		     	jTextArea.setText("");//打开文件之前清空文本区域  

	            try { 
	                BufferedReader br = new BufferedReader(new FileReader(file));  
	                String line = null;  
	                while ((line = br.readLine()) != null) {  
	                    //将给定文本追加到文档结尾。如果模型为 null 或者字符串为 null 或空，则不执行任何操作。 
	                    //虽然大多数 Swing 方法不是线程安全的，但此方法是线程安全的。
	                    jTextArea.append(line + "\r\n");  
	                }  
	            }  
	            catch (IOException ex) {  
	                throw new RuntimeException("读取失败！");  
	            }  
	        }  
	    });  

	    saveItem.addActionListener(new ActionListener() {//菜单条目监听：保存       
	        public void actionPerformed(ActionEvent e) {  
				if (file == null) {
					newFile();
				}
				saveFile();  
	        }  
	    });  

	    compileItem.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		compile();
	    	}
	    });
	}

	private void compile() {
		if (file == null) {
			JOptionPane.showMessageDialog(null, "请先保存文件");
			newFile();
		}
		saveFile();
		gsa = new GSAnalysis(file);
		if (gsa.compile()) {
			displayAllToken();
			displayAllSymbol();
			displayAllPcode();
			errorMessage.setText("compile succeed!");
		} else {
			errorMessage.setText("compile failed!");
		}
	}

	private void displayAllToken() {
		flushTable(tokenTable);
		DefaultTableModel model = (DefaultTableModel)tokenTable.getModel();
		allToken = gsa.getAllToken();
		for (int i = 0; i < allToken.size(); i++) {
			Token token = allToken.get(i);
			Object[] rowValues = {token.getSt(), token.getLine(), token.getValue()};
			model.addRow(rowValues);
		}
	}

	private void displayAllSymbol() {
		flushTable(symbolTable);
		DefaultTableModel model = (DefaultTableModel)symbolTable.getModel();
		allSymbol = gsa.getAllSymbol();
		for (int i = 0; i < allSymbol.size(); i++) {
			PerSymbol symbol = allSymbol.get(i);
			Object[] rowValues = {symbol.getName(), symbol.getType(), symbol.getValue(), symbol.getLevel(), symbol.getAddress()};
			model.addRow(rowValues);
		}
	}

	private void displayAllPcode() {
		flushTable(pcodeTable);
		DefaultTableModel model = (DefaultTableModel)pcodeTable.getModel();
		allPcode = gsa.getAllPcode();
		for (int i = 0; i < allPcode.size(); i++) {
			PerPcode pcode = allPcode.get(i);
			Object[] rowValues = {pcode.getF(), pcode.getL(), pcode.getA()};
			model.addRow(rowValues);
		}
	}

	private void flushTable(JTable table) {
		((DefaultTableModel) table.getModel()).getDataVector().clear();   //清除表格数据
		((DefaultTableModel) table.getModel()).fireTableDataChanged();//通知模型更新
		table.updateUI();//刷新表格 
	}

    private void newFile() {
    	if (file == null) {  
            save.setVisible(true);  
            String dirPath = save.getDirectory();  
            String fileName = save.getFile();  
            if(dirPath == null || fileName == null) {
                return;  
            }
            file = new File(dirPath, fileName);                
        }  
    }

    private void saveFile() {
        try {  
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));  
            String text = jTextArea.getText();  
            bw.write(text);  
            bw.close();  
        } catch (IOException ex) {  
            throw new RuntimeException();  
        }  
    }
	
	public static void main(String[] args) {
		MyCompiler one = new MyCompiler();
		/*****
		try{
			JFileChooser fc = new JFileChooser();
			fc.showOpenDialog(null);
			File file = fc.getSelectedFile();
			if (file.isFile()) {
				GSAnalysis gsa = new GSAnalysis(file);
				gsa.showAllToken();
				if (gsa.compile()) {
					System.out.println("compile succeed!");
					gsa.showAllSymbol();
					gsa.showAllPcode();
					System.out.println("Do you want to run it? Y/N");
					Scanner in = new Scanner(System.in); 
				    String name = in.nextLine(); 
				    if (name.equals("Y") || name.equals("y")) {
				    	gsa.interpreter();
				    }
				} else {
					System.out.println("error happened!");
				}
			}
		} catch (Exception ex) {
			
		}
		******/
	}

}
