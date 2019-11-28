package com.wkq.interpreter.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author: wkq
 * @date: 2019/10/31 10:37
 */
public class AnalyseProduction
{
    //文法开始符号
    private String start;

    private List<Production> productionList = new ArrayList<>();

    private Set<String> nonTerminator = new HashSet<>();
    private Set<String> terminator = new HashSet<>();
    private Set<String> nullNonTer = new HashSet<>();

    //first、follow、select集
    private Map<String, Set<String>> first = new HashMap<>();
    private Map<String, Set<String>> follow = new HashMap<>();
    private Map<Production, Set<String>> select = new HashMap<>();

    public AnalyseProduction(String start)
    {
        this("production.txt", start);
    }

    public AnalyseProduction(String filePath, String start)
    {
        this.start = start;
        readProduction(filePath);
        calcTerminal();
        calcNullNonTer();
        calcFirst();
        calcFollow();
        calcSelect();
        testLL1();
    }

    //读入产生式，获取非终结符号
    private void readProduction(String filePath)
    {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));)
        {
            String line = br.readLine();

            while (line != null)
            {
                //注释和空，则继续
                if (line.length() == 0 || line.startsWith("//"))
                {
                    line = br.readLine();
                    continue;
                }

                //添加这行产生式
                productionList.add(new Production(line));

                //计算非终结符号，产生式左部分
                nonTerminator.add(line.split(" -> ")[0]);

                line = br.readLine();
            }

            System.out.println("nonTerminal = " + nonTerminator);
            System.out.println();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println("read production error = " + e);
            System.out.println();
        }
    }

    /**
     * 计算终结符号
     */
    private void calcTerminal()
    {
        //遍历产生式列表
        for (Production production : productionList)
        {
            //获取右部所有符号
            for (String r : production.getRights())
            {
                //非终结符中是否包含此符号
                if (!nonTerminator.contains(r))
                {
                    //不包含则为终结符
                    terminator.add(r);
                }
            }
        }

        System.out.println("terminal = " + terminator);
        System.out.println();
    }

    /**
     * 计算能推出空串的非终结符
     */
    private void calcNullNonTer()
    {
        while (true)
        {
            boolean flag = true;

            for (Production production : productionList)
            {
                String left = production.getLeft();
                String right = production.getRight();
                String[] rights = production.getRights();

                //存在则继续
                if (nullNonTer.contains(left))
                {
                    continue;
                }

                if (right.equals("$"))
                {
                    nullNonTer.add(left);
                    flag = false;
                }

                for (int i = 0; i < rights.length; i++)
                {
                    //含有终结符，该非终结符一定不能产生空串
                    if (terminator.contains(rights[i]))
                    {
                        break;
                    }

                    //是非终结符则继续
                    if (!nullNonTer.contains(rights[i]))
                    {
                        break;
                    }

                    //最后一个非终结符也能产生空串
                    if (i == rights.length - 1)
                    {
                        nullNonTer.add(left);
                        flag = false;
                    }
                }
            }

            if (flag)
            {
                break;
            }
        }

        //如果非终结符不在以上两个集合中，则为空

        System.out.println("nullNonter = " + nullNonTer);
        System.out.println();
    }

    /**
     * 计算first集
     */
    private void calcFirst()
    {
        //终结符号为自己的first集
        for (String t : terminator)
        {
            first.put(t, new HashSet<String>()
            {{
                add(t);
            }});
        }

        //计算非终结符的first集

        //把所有非终结符作为key加入
        for (String n : nonTerminator)
        {
            first.put(n, new HashSet<>());
        }

        //对于能产生空串的非终结符
        for (String n : nullNonTer)
        {
            first.get(n).add("$");
        }

        //根据产生式计算first集
        while (true)
        {
            //当first集合不再变化，则退出循环
            boolean flag = true;

            for (Production production : productionList)
            {
                String left = production.getLeft();
                String right = production.getRight();
                String[] rights = production.getRights();

                Set<String> leftFirst = first.get(left);

                //以终结符打头并且该产生式未被添加
                if (terminator.contains(rights[0]) && !leftFirst.contains(rights[0]))
                {
                    leftFirst.add(rights[0]);
                    flag = false;
                }

                //非终结符打头，遍历右部
                for (int i = 0; i < rights.length; i++)
                {
                    //不能产生空串的非终结符或者终结符
                    if (!nullNonTer.contains(rights[i]))
                    {
                        //未被添加
                        if (!leftFirst.containsAll(first.get(rights[i])))
                        {
                            leftFirst.addAll(first.get(rights[i]));
                            flag = false;
                        }
                        break;
                    }
                    else
                    {
                        //能产生空串的非终结符号
                        //去除非终结符的first中的空串
                        Set<String> temp = new HashSet<>(first.get(rights[i]));
                        temp.remove("$");

                        //未被添加
                        if (!leftFirst.containsAll(temp))
                        {
                            leftFirst.addAll(temp);
                            flag = false;
                        }

                        //继续，直到最后一个非终结符也存在空串，并且未被添加，加入空串
                        if (i == rights.length - 1 && !leftFirst.contains("$"))
                        {
                            leftFirst.add("$");
                            flag = false;
                        }
                    }
                }
            }

            if (flag)
            {
                break;
            }
        }

        //加入所有产生式右部
        for (Production production : productionList)
        {
            if (!first.containsKey(production.getRight()))
            {
                first.put(production.getRight(), new HashSet<>());
            }
        }

        //计算所有产生式右部的first集
        for (Production production : productionList)
        {
            String right = production.getRight();
            String[] rights = production.getRights();

            Set<String> rightFirst = first.get(right);

            //第一个符号不能产生空串
            if (!nullNonTer.contains(rights[0]))
            {
                rightFirst.addAll(first.get(rights[0]));
            }
            else
            {
                //第一个符号能产生空串
                for (int i = 0; i < rights.length; i++)
                {
                    //去除非终结符的first中的空串
                    Set<String> temp = new HashSet<String>(first.get(rights[i]));
                    temp.remove("$");

                    //能产生空串的非终结符
                    if (nullNonTer.contains(rights[i]))
                    {
                        rightFirst.addAll(temp);

                        if (i == rights.length - 1)
                        {
                            rightFirst.add("$");
                        }
                    }
                    else
                    {
                        //不能产生空串的非终结符或者终结符
                        rightFirst.addAll(temp);
                        break;
                    }
                }
            }
        }

        System.out.println("first = " + first);
        System.out.println();
    }

    /**
     * 计算follow集
     */
    private void calcFollow()
    {
        //将所有非终结符号加入follow集
        for (String n : nonTerminator)
        {
            follow.put(n, new HashSet<>());
        }

        //将结束符号加入开始符号之后
        follow.get(start).add("#");

        while (true)
        {
            boolean flag = true;

            for (Production production : productionList)
            {
                String left = production.getLeft();
                String[] rights = production.getRights();

                //获取当前产生式左部非终结符的follow集
                Set<String> leftFollow = follow.get(left);

                //遍历右部
                for (int i = 0; i < rights.length; i++)
                {
                    //获取当前非终结符符号的follow集
                    Set<String> iRightFollow = follow.get(rights[i]);

                    //是终结符则继续
                    if (terminator.contains(rights[i]))
                    {
                        continue;
                    }

                    //当前是最后一个符号
                    if (i == rights.length - 1 && !iRightFollow.containsAll(leftFollow))
                    {
                        iRightFollow.addAll(leftFollow);
                    }

                    for (int j = i + 1; j < rights.length; j++)
                    {
                        //获取下一个符号的first集，去掉空串，然后加入到当前符号的follow集
                        Set<String> temp = first.get(rights[j]);
                        temp.remove("$");
                        if (!iRightFollow.containsAll(temp))
                        {
                            iRightFollow.addAll(temp);
                            flag = false;
                        }

                        //如果不能产生空串，跳出此循环
                        if (!nullNonTer.contains(rights[j]))
                        {
                            break;
                        }
                        else if (j == rights.length - 1 && !iRightFollow.containsAll(leftFollow))
                        {
                            //最后一个符号且能产生空串，将左部非终结符的follow集加入到当前非终结符的follow集
                            iRightFollow.addAll(leftFollow);
                            flag = false;
                        }
                    }
                }
            }

            if (flag)
            {
                break;
            }
        }

        System.out.println("follow = " + follow);
        System.out.println();
    }

    /**
     * 计算select集
     */
    private void calcSelect()
    {
        for (Production production : productionList)
        {
            //产生式右部有空串
            if (first.get(production.getRight()).contains("$"))
            {
                Set<String> result = new HashSet<>(first.get(production.getRight()));
                result.remove("$");
                result.addAll(follow.get(production.getLeft()));
                select.put(production, result);
            }
            else
            {
                //没有空串
                select.put(production, first.get(production.getRights()[0]));
            }
        }

        System.out.println("SELECT集:");

        for (Map.Entry<Production, Set<String>> entry : select.entrySet())
        {
            System.out.print("SELECT( " + entry.getKey().getProduction() + " )" + " = ");
            System.out.println(entry.getValue());
        }
        System.out.println();
    }

    /**
     * 判断是否为LL1文法
     */
    private Boolean testLL1()
    {
        Map<String, List<Production>> map = new HashMap<>();

        //先将非终结符加入
        for (String n : nonTerminator)
        {
            map.put(n, new ArrayList<>());
        }

        //加入产生式
        for (Production production : productionList)
        {
            map.get(production.getLeft()).add(production);
        }

        //对每一个非终结符，计算其select的交集
        //左部符号相同的进行交集比较，如果全部交集为空，则符合LL（1）文法
        boolean flag = true;
        for (String n : nonTerminator)
        {
            List<Production> list = map.get(n);
            int size = list.size();

            //两两之间求交集
            for (int i = 0; i < size - 1; i++)
            {
                for (int j = i + 1; j < size; j++)
                {
                    Set<String> result = new HashSet<>(select.get(list.get(i)));
                    //取交集
                    result.retainAll(select.get(list.get(j)));

                    //交集大于0则有交集
                    if (result.size() > 0)
                    {
                        flag = false;
                        System.out.println(
                            list.get(i).getProduction() + " /\\ " + list.get(j).getProduction() + " = " + result);
                    }
                    if(!flag){
                        System.out.println("有交集，不是LL(1)文法");
                    }
                }
            }
        }
        if(flag){
            System.out.println("是LL(1)文法");
        }
        System.out.println("");
        return flag;
    }

    public Map<Production, Set<String>> getSelect()
    {
        return select;
    }

    public Map<String, Set<String>> getFirst()
    {
        return first;
    }

    public Map<String, Set<String>> getFollow()
    {
        return follow;
    }

    public Set<String> getNonTerminator()
    {
        return nonTerminator;
    }

    public Set<String> getTerminator()
    {
        return terminator;
    }

    public static void main(String[] args)
    {
        //AnalyseProduction analyseProduction = new AnalyseProduction("testProduction.txt", "program");
        AnalyseProduction analyseProduction = new AnalyseProduction("program");
        analyseProduction.testLL1();
    }
}
