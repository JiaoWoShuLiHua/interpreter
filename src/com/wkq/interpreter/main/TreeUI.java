package com.wkq.interpreter.main;

import javax.swing.*;

/**
 * @author: wkq
 * @date: 2019/11/9 21:24
 */
public class TreeUI
{
    private JTabbedPane jTabbedPane = new JTabbedPane();

    private JFrame mainUI = null;

    private JMenuBar menuBar = null;
    
    public TreeUI()
    {
        mainUI = new JFrame();
        mainUI.setTitle("解释器");
        mainUI.setSize(1080, 640);


        setJMenuBar();
        mainUI.validate();
        mainUI.setEnabled(true);
        mainUI.setVisible(true);
        mainUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void setJMenuBar()
    {

        //创建并添加菜单栏
        menuBar = new JMenuBar();
        mainUI.setJMenuBar(menuBar);

        //创建并添加各菜单，注意：菜单的快捷键是同时按下Alt键和字母键，方法setMnemonic('F')是设置快捷键为Alt +Ｆ
        JMenu menuFile = new JMenu("文件(F)"), menuEdit = new JMenu("编辑(E)"), menuView = new JMenu("查看(V)");
        menuFile.setMnemonic('F');
        menuEdit.setMnemonic('E');
        menuView.setMnemonic('V');
        menuBar.add(menuFile);
        menuBar.add(menuEdit);
        menuBar.add(menuView);
    }

    public static void main(String[] args)
    {
//        try
//        {
//            AnalyseTable at = null;
//            Lexer lexer = new Lexer();
//            ArrayList<Token> tokens = lexer.lexing("testAnalyse.c");
//
//            if (lexer.getErrorTokens().size() > 0)
//            {
//                StringBuilder errorMsg = new StringBuilder();
//                for (int i = 0; i < lexer.getErrorTokens().size(); i++)
//                {
//                    errorMsg.append(lexer.getErrorTokens().get(i).toString() + "\n");
//                }
//                throw new RuntimeException(errorMsg.toString());
//            }
//
//            at = new AnalyseTable("Production.txt", "program");
//            at.analyze(tokens);
//            if (at.isCorrect())
//            {
//                ShowTree showTree = new ShowTree();
//                if (showTree.create(at.getHeader()))
//                {
//                    System.out.println("成功");
//                }
//                else
//                {
//                    System.out.println("失败");
//                }
//                Semantic semantic = new Semantic();
//                semantic.run(at.getHeader());
//            }
//            else{
//                for(String error : at.getErrors()){
//                    System.out.println(error);
//                }
//            }
//        }
//        catch (RuntimeException e)
//        {
//            System.out.println(e.getMessage());
//            System.out.println("遇到错误,终止进程，退出");
//            System.exit(0);
//        }
//        catch (IOException ioe)
//        {
//            System.out.println(ioe.getMessage());
//            System.out.println("IO操作出错,终止进程，退出");
//            System.exit(0);
//        }
        //MainUI mainUI = new MainUI();
    }
}
