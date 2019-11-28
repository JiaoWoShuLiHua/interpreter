package com.wkq.interpreter.lexer.utils;

/**
 * Token类别
 *
 * @author: wkq
 * @date: 2019/10/19 18:55
 */
public class Category
{
    //标识符（由数字、字母和下划线组成的串，但必须以字母开头、且不能以下划线结尾的串）
    public static final String IDENTIFIER = "IDENTIFIER";

    //自增自减运算符 ++ --
    public static final String SELF_OP = "SELF_OP";

    //一元逻辑运算符 !
    public static final String UN_LOGIC_OP = "UN_LOGIC_OP";

    //第一优先级二元算术运算符 * / %
    public static final String BIN_AR_OP_1 = "BIN_AR_OP_1";

    //第二优先级二元算术运算符 + -
    public static final String BIN_AR_OP_2 = "BIN_AR_OP_2";

    //逻辑运算符 && || !
    public static final String LOGIC_OP = "LOGIC_OP";

    //关系运算符
    public static final String RELATION_OP = "RELATION_OP";

    //位运算
    public static final String BIT_OP = "BIT_OP";

    //赋值运算
    public static final String ASSIGNMENT_OP = "ASSIGNMENT_OP";

    //赋初值
    public static final String ASS_INIT_OP = "ASS_INIT_OP";

    //整数
    public static final String INT = "INT";

    //实数
    public static final String REAL = "REAL";

    //字符
    public static final String CHAR = "CHAR";

    //字符串
    public static final String STRING = "STRING";

    //布尔变量 true false
    public static final String BOOL = "BOOL";

    //数据类型 int float double long short char
    public static final String TYPE = "TYPE";

    //注释
    public static final String NOTES = "NOTES";

}
