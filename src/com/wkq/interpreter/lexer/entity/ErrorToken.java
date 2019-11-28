package com.wkq.interpreter.lexer.entity;

/**
 * @author: wkq
 * @date: 2019/10/19 21:38
 */
public class ErrorToken
{
    //类别
    private String error;

    //符号值
    private String value;

    //所在行
    private int row;

    //本行的位置
    private int col;

    public ErrorToken(String error, String value, int row, int col){
        this.error = error;
        this.value = value;
        this.row = row;
        this.col = col;
    }

    public String getError()
    {
        return error;
    }

    public void setError(String error)
    {
        this.error = error;
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
        return String.format("line %d, %d:\terror: %s,\tvalue: %s.", row, col, error, value);
    }
}
