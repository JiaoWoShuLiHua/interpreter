package com.wkq.interpreter.semantic;

import java.util.Arrays;
import java.util.List;

//符号表的表项
public class Symbol {
    private String symbol;
    // 数据类型: int double ...
    private String type;
    // 数组长度
    private int length = -1;
    // 元素的整形数值
    private int intValue;
    // 元素的浮点型数值
    private double realValue;
    // 元素的字符值
    private char charvalue;
    // 整数数组
    private int[] intArray;
    // 小数数组
    private double[] realArray;
    // 字符串数组
    private char[] charArray;
    //作用域
    private int level;
    //是否为数组
    private boolean isArray = false;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Symbol(String type, String symbol, int level) {
        this.type = type;
        this.symbol = symbol;
        this.level = level;
    }

    public String getName() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValue(String value) {
        try {
            switch (type) {

                case Type.INT:
                    if (value.contains(".")) {
                        value = value.split("\\.")[0];
                    }
                    intValue = Integer.parseInt(value);
                    //intValue = Double.valueOf(value).intValue();
                    break;
                case Type.REAL:
                    realValue = Double.parseDouble(value);
                    break;
                case Type.CHAR:
                    charvalue = value.charAt(0);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            throw new RuntimeException("赋值时类型发生错误");
        }
    }

    public void selfAdd() {
        switch (type) {
            case Type.INT:
                intValue++;
                break;
            case Type.REAL:
                realValue++;
                break;
            case Type.CHAR:
                charvalue++;
                break;
            default:
                break;
        }
    }

    public void selfMin() {
        switch (type) {
            case Type.INT:
                intValue--;
                break;
            case Type.REAL:
                realValue--;
                break;
            case Type.CHAR:
                charvalue--;
                break;
            default:
                break;
        }
    }

    public void setArrayValue(int index, String value) {
        try {
            switch (type) {
                case Type.INT:
                    if (value.contains(".")) {
                        value = value.split("\\.")[0];
                    }
                    Double v = Double.valueOf(value);
                    if (v > Integer.MAX_VALUE || v < Integer.MIN_VALUE) {
                        Double r = Double.valueOf(Integer.MIN_VALUE + (v - Integer.MAX_VALUE) - 1);
                        intArray[index] = r.intValue();
                    }
                    intArray[index] = Integer.parseInt(value);
                    break;
                case Type.REAL:
                    realArray[index] = Float.parseFloat(value);
                    break;
                case Type.CHAR:
                    realArray[index] = value.charAt(0);
                    break;
                default:
                    break;
            }
        } catch (NumberFormatException ex) {
            throw new RuntimeException("数字太大，数组越界");
        }
    }

    public int getIntValue() {
        return intValue;
    }

    public double getRealValue() {
        return realValue;
    }

    public double getValue() {
        switch (type) {
            case Type.INT:
                return intValue;
            case Type.REAL:
                return realValue;
        }
        return 0;
    }

    public String getValueString() {
        switch (type) {
            case Type.INT:
                return String.valueOf(intValue);
            case Type.REAL:
                return String.valueOf(realValue);
        }
        return "";
    }

    public String getArray() {
        switch (type) {
            case Type.INT:
                return Arrays.toString(intArray);
            case Type.REAL:
                return Arrays.toString(realArray);
            case Type.CHAR:
                return Arrays.toString(charArray);
        }
        return null;
    }

    public String getArray(int index) {
        switch (type) {
            case Type.INT:
                return "" + intArray[index];
            case Type.REAL:
                return "" + realArray[index];
            case Type.CHAR:
                return "" + charArray[index];
        }
        return null;
    }

    public char getCharvalue() {
        return charvalue;
    }

    public int getLength() {
        return length;
    }

    public int[] getIntArray() {
        return intArray;
    }

    public void setIntArray(int[] intArray) {
        this.intArray = intArray;
    }

    public double[] getRealArray() {
        return realArray;
    }

    public void setRealArray(double[] realArray) {
        this.realArray = realArray;
    }

    public char[] getCharArray() {
        return charArray;
    }

    public void setCharArray(char[] charArray) {
        this.charArray = charArray;
    }

    public void setLength(int length) {
        this.isArray = true;
        this.length = length;
        switch (type) {
            case Type.INT:
                intArray = new int[length];
            case Type.REAL:
                realArray = new double[length];
            case Type.CHAR:
                charArray = new char[length];
        }
    }

    public boolean isArray() {
        return isArray;
    }
}