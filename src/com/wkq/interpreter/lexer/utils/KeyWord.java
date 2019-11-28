package com.wkq.interpreter.lexer.utils;

/**
 * 关键字
 *
 * @author: wkq
 * @date: 2019/10/19 19:31
 */
public class KeyWord
{
    //数据类型关键字（12个） 控制语句关键字（12个）存储类型关键字（4个） 其它关键字（4个）
    public static final String[] KEYWORDS =
        {"auto", "break", "case", "char", "const", "continue", "default", "do", "double", "else", "enum", "extern",
            "float", "for", "goto", "if", "int", "long", "register", "return", "read", "short", "signed", "sizeof", "static",
            "struct", "switch", "typedef", "union", "unsigned", "void", "volatile", "while", "write", "print", "scan", "real"};

    //数据类型关键字（12个）
    public static final String[] DATATYPES =
        {"char", "double", "enum", "float", "int", "long", "short", "signed", "struct", "union", "unsigned", "void", "real"};
}
