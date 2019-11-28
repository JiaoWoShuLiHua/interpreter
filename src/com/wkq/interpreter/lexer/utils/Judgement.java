package com.wkq.interpreter.lexer.utils;

import java.util.regex.Pattern;

/**
 * 判断方法
 *
 * @author: wkq
 * @date: 2019/10/19 19:34
 */
public class Judgement
{
    private static char[] operators = {'+', '-', '*', '/', '=', '>', '<', '!', '%'};

    //判断字母
    public static boolean isLETTER(char ch)
    {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }

    //判断数字
    public static boolean isNum(char ch)
    {
        return ch >= '0' && ch <= '9';
    }

    //判断行结束
    public static boolean isEnd(char ch)
    {
        return isEOF(ch) || isEnter(ch);
    }

    //判断是否换行
    public static boolean isEnter(char ch)
    {
        return ch == '\r' || ch == '\n';
    }

    //判断是否结束
    public static boolean isEOF(char ch)
    {
        return ch == (char)-1;
    }

    //判断是否为运算符
    public static boolean isOperator(char ch)
    {
        for (char c : operators)
        {
            if (ch == c)
            {
                return true;
            }
        }
        return false;
    }

    //判断一个数是否符合浮点数的形式
    public static boolean isReal(String num)
    {
        //小数点在中间
        if (Pattern.matches("[0-9]+\\.[0-9]+", num))
        {
            return true;
        }
        //小数点在前面
        if (Pattern.matches("\\.[0-9]+", num))
        {
            return true;
        }
        //小数点在后面
        if (Pattern.matches("[0-9]+\\.", num))
        {
            return true;
        }
        return false;
    }

    //是否为整数
    public static boolean isInt(String num)
    {
        if (Pattern.matches("[0-9]+", num))
        {
            return true;
        }
        return false;
    }

    //判断关键字
    public static boolean isKeyword(String word)
    {
        for (String key : KeyWord.KEYWORDS)
        {
            if (key.equals(word))
            {
                return true;
            }
        }
        return false;
    }

    //判断是不是一个数据类型的关键字
    public static boolean isType(String type)
    {
        for (String t : KeyWord.DATATYPES)
        {
            if (type.equals(t))
            {
                return true;
            }
        }
        return false;
    }
}
