package com.wkq.interpreter.semantic;

import com.wkq.interpreter.lexer.utils.Category;

/**
 * @author: wkq
 * @date: 2019/11/6 19:10
 */
public class Utils
{
    public static boolean classCheck(Object c, String type){
        Object typeClass = null;
        switch (type){
            case "int":
                typeClass = Integer.class;
                break;
            case "real":
                typeClass = Double.class;
                break;
            case "char":
                typeClass = Character.class;
                break;
            default:
                break;
        }
        if(typeClass.equals(Double.class) && c.equals(Integer.class)){
            return true;
        }
        if(typeClass.equals(c)){
            return true;
        }
        else
        {
            return false;
        }
    }


    //仅支持REAL和INT运算
    public static String calcCate(String cate1, String cate2)
    {
        //其中一个为空，输出另一个,即表达式类型的开始，决定整个表达式的类型
        if (cate1.isEmpty() || cate2.isEmpty())
        {
            return cate1.isEmpty() ? cate2 : cate1;
        }
        //等于REAL和INT任意一个即可，不等则说明表达式和变量类型不一致
        if ((cate1.equals(Category.REAL) || cate1.equals(Category.INT) || cate1.equals(Category.CHAR)) !=
            (cate2.equals(Category.REAL) || cate2.equals(Category.INT) || cate2.equals(Category.CHAR)))
        {
            return "";
        }

        //取REAL类型
        if (cate1.equals(Category.REAL) || cate2.equals(Category.REAL))
        {
            return Category.REAL;
        }
        return Category.INT;
    }

    public static boolean cateConvert(String dis, String src)
    {
        switch (dis)
        {
            case Type.INT:
                dis = Category.INT;
                break;
            case Type.REAL:
                dis = Category.REAL;
                break;
            case Type.CHAR:
                dis = Category.CHAR;
            default:
                break;
        }
        if (dis.equals(Category.REAL) && (src.equals(Category.INT) || src.equals(Category.REAL)))
        {
            return true;
        }
        if (dis.equals(src))
        {
            return true;
        }
        return false;
    }

    public static String type2Cate(String type)
    {
        switch (type)
        {
            case Type.INT:
                return Category.INT;
            case Type.REAL:
                return Category.REAL;
            case Type.CHAR:
                return Category.CHAR;
            default:
                break;
        }
        return null;
    }

    public static void main(String[] args)
    {
        System.out.print(Utils.calcCate("REAL", "STRING"));
    }
}
