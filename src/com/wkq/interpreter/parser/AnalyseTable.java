package com.wkq.interpreter.parser;

import com.wkq.interpreter.lexer.Lexer;
import com.wkq.interpreter.lexer.entity.Token;

import java.util.*;

/**
 * @author: wkq
 * @date: 2019/10/31 10:35
 */
public class AnalyseTable
{
    private Map<Production, Set<String>> select;

    private AnalyseProduction analyseProduction;

    //语法树头结点,和当前结点
    private TreeNode header, curr;

    private ArrayList<String> errors = new ArrayList<>();

    //预测分析表
    private Map<String, Map<String, Production>> predictTable = new HashMap<>();

    //分析栈
    private Stack<String> analyzeStack;

    //当前语法没有错误
    private boolean correct = true;

    public AnalyseTable(String start)
    {
        this("production_v1.txt", start);
    }

    public AnalyseTable(String filePath, String start)
    {
        analyzeStack = new Stack<>();
        //加入开始符号
        analyzeStack.push(start);
        //语法树根节点
        header = new TreeNode(null, "");
        header.next = new TreeNode[] {new TreeNode(header, start)};
        curr = header.next[0];

        initTable(filePath, start);
    }

    //创建预测分析表
    private void initTable(String filePath, String start)
    {
        analyseProduction = new AnalyseProduction(filePath, start);

        //创建非终结符表头
        for (String non : analyseProduction.getNonTerminator())
        {
            predictTable.put(non, new HashMap<>());
        }

        Map<Production, Set<String>> select = analyseProduction.getSelect();

        //填充表
        for (Map.Entry<Production, Set<String>> entry : select.entrySet())
        {
            for (String ter : entry.getValue())
            {
                predictTable.get(entry.getKey().getLeft()).put(ter, entry.getKey());
            }
        }
    }

    /**
     * 开始分析
     *
     * @param tokens
     */
    public void analyze(List<Token> tokens)
    {
        int index = 0;
        //下一个token类型
        String preTokenCategory = tokens.get(index).getCategory();

        try
        {
            while (!analyzeStack.isEmpty())
            {
                System.out.println(preTokenCategory + " " + analyzeStack);
                String stackTop = analyzeStack.pop();

                if (stackTop.equals(preTokenCategory))
                {
                    System.out.printf("%s %s 匹配\n", preTokenCategory, tokens.get(index).getValue());
                    //语法树叶子结点
                    //curr.next = new TreeNode[]{new TreeNode(curr, tokens.get(index))};
                    curr.token = tokens.get(index);
                    curr = curr.findNext();
                    index++;
                    preTokenCategory = index >= tokens.size() ? "#" : tokens.get(index).getCategory();
                    continue;
                }

                //如果栈顶是终结符，又不是要匹配的符号，那么直接报错
                if (analyseProduction.getTerminator().contains(stackTop))
                {
                    Token token = tokens.get(index);
                    String error = String.format("语法错误(%d,%d): 此处应该是 %s ， 但实际是 %s",
                        token.getRow(),
                        token.getCol(),
                        stackTop,
                        preTokenCategory);
                    errors.add(error);

                    correct = false;
                    //continue;
                    //报错
                    throw new RuntimeException(error);
                }

                //非终结符，查表
                Production production = predictTable.get(stackTop).get(preTokenCategory);

                //查表未查到，应报错
                //考虑做应急恢复，栈顶是非终结符号，开始尝试恢复
                if (production == null)
                {
                    //先报错
                    Token token = tokens.get(index);
                    String error = String.format("语法错误(%d,%d): 不符合语法规则", token.getRow(), token.getCol());
                    errors.add(error);
                    correct = false;
                    //continue;
                    throw new RuntimeException(error);
                }
                else
                {
                    //System.out.println(production.getProduction());
                    //将产生式右部反序入到分析栈和语法树中
                    String[] rights = production.getRights();
                    curr.next = new TreeNode[rights.length];

                    //空产生式不入栈
                    if (rights[0].equals("$"))
                    {
                        curr.next = new TreeNode[] {new TreeNode(curr, "$")};
                        curr = curr.findNext();
                    }
                    else
                    {
                        //反序入到分析栈
                        for (int i = rights.length - 1; i >= 0; i--)
                        {
                            analyzeStack.push(rights[i]);
                            //语法树
                            curr.next[i] = new TreeNode(curr, rights[i]);
                        }
                        //移动当前语法树结点
                        curr = curr.next[0];
                    }
                }
            }

            System.out.println("接受");
        }
        catch (Exception e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }

    //返回语法树根节点
    public TreeNode getHeader()
    {
        return header;
    }

    //当前语法分析是否正确
    public boolean isCorrect()
    {
        return correct;
    }

    public ArrayList<String> getErrors()
    {
        return errors;
    }
}
