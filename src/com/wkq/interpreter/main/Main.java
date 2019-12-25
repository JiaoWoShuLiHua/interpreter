package com.wkq.interpreter.main;

import com.wkq.interpreter.lexer.Lexer;
import com.wkq.interpreter.lexer.entity.Token;
import com.wkq.interpreter.lexer.utils.Category;
import com.wkq.interpreter.parser.AnalyseTable;
import com.wkq.interpreter.semantic.Semantic_v1;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author: wkq
 * @date: 2019/11/18 10:05
 */
public class Main
{
    public static void main(String[] args)
    {

        try
        {
            AnalyseTable at = null;
            Lexer lexer = new Lexer();
            ArrayList<Token> tokens = lexer.lexing("testAnalyse.c");
            for(int i = 0; i < tokens.size(); i++){
                if(tokens.get(i).getCategory().equals(Category.NOTES)){
                    tokens.remove(i);
                }
            }
            if (lexer.getErrorTokens().size() > 0)
            {
                StringBuilder errorMsg = new StringBuilder();
                for (int i = 0; i < lexer.getErrorTokens().size(); i++)
                {
                    errorMsg.append(lexer.getErrorTokens().get(i).toString() + "\n");
                }
                throw new RuntimeException(errorMsg.toString());
            }

            at = new AnalyseTable("production_v1.txt", "program");
            at.analyze(tokens);
            if (at.isCorrect())
            {
                ShowTree showTree = new ShowTree();
                if (showTree.create(at.getHeader()))
                {
                    System.out.println("成功");
                }
                else
                {
                    System.out.println("失败");
                }
                Semantic_v1 semantic = new Semantic_v1();
                semantic.run(at.getHeader());
            }
            else{
                for(String error : at.getErrors()){
                    System.out.println(error);
                }
            }
        }
        catch (RuntimeException e)
        {
            System.out.println(e.toString());
            System.out.println("遇到错误,终止进程，退出");
            System.exit(0);
        }
        catch (IOException ioe)
        {
            System.out.println(ioe.getMessage());
            System.out.println("IO操作出错,终止进程，退出");
            System.exit(0);
        }
    }
}
