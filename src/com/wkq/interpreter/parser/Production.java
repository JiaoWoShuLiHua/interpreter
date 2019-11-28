package com.wkq.interpreter.parser;

/**
 * @author: wkq
 * @date: 2019/10/31 10:27
 */
public class Production
{
    //产生式
    private String production;

    //产生式左部
    private String left;

    //产生式右部
    private String right;

    //产生式右部数组
    private String[] rights;

    public Production(String production)
    {
        this.production = production;
        String[] list = production.split(" -> ");
        left = list[0];
        right = list[1];
        rights = right.split(" ");
    }

    public String getProduction()
    {
        return production;
    }

    public String getLeft()
    {
        return left;
    }

    public String getRight()
    {
        return right;
    }

    public String[] getRights()
    {
        return rights;
    }
}
