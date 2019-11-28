package com.wkq.interpreter.lexer.entity;

/**
 * @author: wkq
 * @date: 2019/10/19 18:55
 */
public class Token
{
    //类别
    private String category;

    //符号值
    private String value;

    //所在行
    private int row;

    //本行的位置
    private int col;

    public Token(String category, String value, int row, int col){
        this.category = category;
        this.value = value;
        this.row = row;
        this.col = col;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public int getRow()
    {
        return row;
    }

    public void setRow(int row)
    {
        this.row = row;
    }

    public int getCol()
    {
        return col;
    }

    public void setCol(int col)
    {
        this.col = col;
    }

    @Override
    public String toString()
    {
        return String.format("line %d, %d:\tcategory: %s,\tvalue: %s.", row, col, category, value);
    }
}
