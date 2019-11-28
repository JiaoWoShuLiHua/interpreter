package com.wkq.interpreter.semantic;

import com.wkq.interpreter.lexer.entity.Token;
import com.wkq.interpreter.lexer.utils.Category;
import com.wkq.interpreter.parser.TreeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 * 用来处理表达式的类，（）、+、-、*、/、&&、||、==、%、>=、>、<=、<、!=、
 *
 * @author: wkq
 * @date: 2019/11/6 19:10
 */
public class Expression {
    //运算符优先级
    private static HashMap<String, Integer> priority = new HashMap<String, Integer>() {{
        put("#", 0);
        //put("(", 17);
        put("*", 13);
        put("/", 13);
        put("%", 13);
        put("+", 11);
        put("-", 11);
        put(">=", 9);
        put(">", 9);
        put("<", 9);
        put("<=", 9);
        put("<>", 7);
        put("==", 7);
        put("&&", 5);
        put("||", 3);
        //put(")", 16);
    }};

    /**
     * 将树解析，转换为一个token列表
     *
     * @return
     */
    public static List<Token> getTokens(TreeNode curr) {
        List<Token> tokens = new ArrayList<>();

        if (curr.token != null) {
            tokens.add(curr.token);
            return tokens;
        }

        if (curr.cate.equals("$")) {
            return tokens;
        }

        for (int i = 0; i < curr.next.length; i++) {
            tokens.addAll(getTokens(curr.next[i]));
        }

        return tokens;
    }

    //中缀变后缀
    public static List<Token> infix2Postfix(List<Token> tokens) {
        List<Token> result = new ArrayList<>(tokens.size());
        Stack<Token> stack = new Stack<>();
        stack.push(new Token("END", "#", 0, 0));

        //中缀转后缀
        for (int i = 0; i <= tokens.size(); i++) {
            if (i == tokens.size()) {
                //将栈中运算符全部放到结果列表中
                while (stack.size() > 1) {
                    result.add(stack.pop());
                }
                break;
            }

            switch (tokens.get(i).getCategory()) {
                case Category.IDENTIFIER:
                case Category.INT:
                case Category.REAL:
                case Category.BOOL:
                case Category.CHAR:
                    //遇到操作数直接输入到结果数组中
                    result.add(tokens.get(i));
                    break;
                case Category.BIN_AR_OP_1:
                case Category.BIN_AR_OP_2:
                case Category.RELATION_OP:
                case Category.LOGIC_OP:
                case Category.UN_LOGIC_OP:
                    //遇到其他运算符，弹出所有优先级（非左括号，左括号只有右括号能控制）
                    // 大于等于该运算符的栈中的运算符，然后加入该运算符
                    if(!stack.peek().getValue().equals("(")){
                        while (priority.get(tokens.get(i).getValue()) <= priority.get(stack.peek().getValue())) {
                            result.add(stack.pop());
                            if(stack.peek().getValue().equals("(")){
                                break;
                            }
                        }
                    }
                    stack.push(tokens.get(i));
                    break;
                case "(":
                    //遇到左括号直接进栈，在本例中优先级最好
                    stack.push(tokens.get(i));
                    break;
                case ")":
                    //遇到右括号，执行弹出，直到遇到右括号，注意左右括号不加入结果中
                    while (!stack.peek().getCategory().equals("(")) {
                        result.add(stack.pop());
                    }
                    stack.pop();
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    //计算后缀表达式，将结果放入到声明的stack中
    public static double calc(List<Token> tokens, SymbolTable table) {
        Stack<Double> stack = new Stack<>();
        for (Token token : tokens) {
            switch (token.getCategory()) {
                case Category.CHAR:
                    double charAscii = (int)token.getValue().charAt(0);
                    stack.push(charAscii);
                    break;
                case Category.INT:
                case Category.REAL:
                    stack.push(Double.parseDouble(token.getValue()));
                    break;
                case Category.BOOL:
                    stack.push(token.getValue().equals("true") ? 1.0 : 0.0);
                    break;
                case Category.IDENTIFIER:
                    //从符号表中获取标识符的值，放入当前栈中
                    stack.push(table.query(token.getValue()).getValue());
                    break;
                case Category.UN_LOGIC_OP:
                    stack.push(stack.pop() > 0 ? 0.0 : 1.0);
                    break;
                default:
                    //每遇到运算符，就将前两个操作数进行计算
                    double b = stack.pop();
                    double a = stack.pop();
                    switch (token.getValue()) {
                        case "+":
                            stack.push(a + b);
                            break;
                        case "-":
                            stack.push(a - b);
                            break;
                        case "*":
                            stack.push(a * b);
                            break;
                        case "/":
                            stack.push(a / b);
                            break;
                        case "%":
                            stack.push(a % b);
                            break;
                        case ">":
                            stack.push(a > b ? 1.0 : 0.0);
                            break;
                        case ">=":
                            stack.push(a >= b ? 1.0 : 0.0);
                            break;
                        case "<":
                            stack.push(a < b ? 1.0 : 0.0);
                            break;
                        case "<=":
                            stack.push(a <= b ? 1.0 : 0.0);
                            break;
                        case "==":
                            stack.push(a == b ? 1.0 : 0.0);
                            break;
                        case "<>":
                            stack.push(a != b ? 1.0 : 0.0);
                            break;
                        case "&&":
                            stack.push(a > 0 && b > 0 ? 1.0 : 0.0);
                            break;
                        case "||":
                            stack.push(a > 0 || b > 0 ? 1.0 : 0.0);
                            break;
                        default:
                            break;
                    }
                    break;

            }
        }
        return stack.pop();
    }
}
