package com.wkq.interpreter.semantic;

import com.wkq.interpreter.lexer.Lexer;
import com.wkq.interpreter.lexer.entity.Token;
import com.wkq.interpreter.lexer.utils.Category;
import com.wkq.interpreter.parser.AnalyseTable;
import com.wkq.interpreter.parser.TreeNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 语义分析
 * 进行类型判定
 *
 * @author: wkq
 * @date: 2019/11/6 19:10
 */
public class Semantic
{
    //符号表栈
    private SymbolTable symbolTable = new SymbolTable();

    //报错信息
    private String errorMsg = "";

    /* 语义分析标识符作用域 */
    private int level = 0;

    public Semantic()
    {
    }

    public void run(TreeNode curr)
    {
        semantic(curr);
    }

    /**
     * 开始解析
     */
    private void semantic(TreeNode curr)
    {
        double value;

        switch (curr.cate)
        {
            case "scan_stm":
                Scanner in = new Scanner(System.in);
                String line = in.nextLine();
                return;
            case "print_stm":
                if (curr.next[2].next.length > 1)
                {
                    Symbol symbol = symbolTable.query(curr.next[2].next[0].token.getValue());
                    Token id = curr.next[2].next[0].token;
                    if (symbol == null)
                    {
                        errorMsg = String.format("%d,%d: 没有声明的标识符", id.getRow(), id.getCol());
                        throw new RuntimeException(errorMsg);
                    }
                    else
                    {
                        // 输出变量
                        if (curr.next[2].next[1].next[0].cate.equals("$"))
                        {
                            System.out.println(symbol.getLength() < 0 ? symbol.getValueString() : symbol.getArray());
                        }
                        else
                        {
                            // 输出数组元素
                            if (symbol.getLength() < 0)
                            {
                                errorMsg = String.format("%d,%d: 标识符不是数组类型", id.getRow(), id.getCol());
                                throw new RuntimeException(errorMsg);
                            }
                            else
                            {
                                System.out.println(symbol.getArray(Integer.parseInt(curr.next[2].next[2].token.getValue())));
                            }
                        }
                    }
                }
                else
                {
                    System.out.println(curr.next[2].next[0].next[0].token.getValue());
                }
                return;
            case "dec":
                declare(curr, null);
                return;
            case "assign_func":
                assign(curr);
                return;
            case "while_loop":
                //while_stm -> while ( logic_exp ) { statements }
                // 进入while循环语句，改变作用域
                level++;
                value = logicExpression(curr.next[2], curr.next[1].token.getRow(), curr.next[1].token.getCol());
                if (Double.isNaN(value))
                {
                    break;
                }
                while (value > 0)
                {
                    semantic(curr.next[5]);
                    value = logicExpression(curr.next[2], curr.next[1].token.getRow(), curr.next[1].token.getCol());
                }
                level--;
                symbolTable.update(level);
                return;
            case "do_stm":
                // 进入do循环语句，改变作用域
                level++;
                //do_stm -> do { statements } while ( logic_exp ) ;
                value = logicExpression(curr.next[6], curr.next[5].token.getRow(), curr.next[5].token.getCol());
                if (Double.isNaN(value))
                {
                    break;
                }

                do
                {
                    semantic(curr.next[2]);
                    value = logicExpression(curr.next[6], curr.next[5].token.getRow(), curr.next[5].token.getCol());
                } while (value > 0);
                level--;
                symbolTable.update(level);
                return;
            case "for_loop":
                //for_stm -> for ( declare_stm ; logic_exp ; assign_stm ) { statements }
                // 进入for循环语句，改变作用域
                level++;
                value = logicExpression(curr.next[1].next[2], curr.next[1].next[3].token.getRow(), curr.next[1].next[3].token.getCol());
                if (Double.isNaN(value))
                {
                    break;
                }

                declare(curr.next[2], null);

                while (value > 0)
                {
                    semantic(curr.next[9]);
                    assign(curr.next[6]);
                    value = logicExpression(curr.next[4], curr.next[3].token.getRow(), curr.next[3].token.getCol());
                }
                level--;
                symbolTable.update(level);
                return;
            case "conditional_stat":
                // 进入if循环语句，改变作用域
                level++;
                value = logicExpression(curr.next[2], curr.next[1].token.getRow(), curr.next[1].token.getCol());
                if (Double.isNaN(value))
                {
                    break;
                }

                //判断是否正确，正确进入
                if (value > 0)
                {
                    semantic(curr.next[5]);
                }
                level--;
                symbolTable.update(level);
                return;
            default:
                break;
        }

        if (curr.next != null && curr.next.length > 0)
        {
            for (TreeNode node : curr.next)
            {
                semantic(node);
            }
        }
    }

    /**
     * 分析声明语句
     * declare_stm -> TYPE IDENTIFIER declare_val declare_item
     * declare_item -> $
     * declare_item -> , IDENTIFIER declare_val declare_item
     * declare_val -> $
     * declare_val -> ASSIGNMENT_OP arithmetic_exp
     * declare_val -> [ INT ] ASSIGNMENT_OP declare_arr
     * declare_arr -> { value arr_item }
     * arr_item -> $
     * arr_item -> , value arr_item
     */
    private void declare(TreeNode curr, String type)
    {
        //declare_stm -> TYPE ID declare_val declare_item
        //dec -> TYPE var assign_init_value dec_one_line
        if (curr.cate.equals("dec"))
        {
            declare(curr.next[1], curr.next[0].token.getValue());
            declare(curr.next[3], curr.next[0].token.getValue());
            return;
        }

        //declare_item -> , IDENTIFIER declare_val declare_item
        //dec_one_line -> , var assign_init_value dec_one_line
        if (curr.cate.equals("dec_one_line") && !curr.next[0].cate.equals("$"))
        {
            declare(curr.next[1], type);
            declare(curr.next[3], type);
            return;
        }

        //var -> IDENTIFIER arr_sub
        if (curr.cate.equals("var"))
        {
            declare(curr.next[0], type);
            return;
        }

        if (!curr.cate.equals("IDENTIFIER"))
        {
            return;
        }

        Token id = curr.token;
        if (symbolTable.query(id.getValue()) != null)
        {
            errorMsg = String.format("%d,%d: 重复定义的标识符", id.getRow(), id.getCol());
            throw new RuntimeException(errorMsg);
        }
        else
        {
            symbolTable.insert(new Symbol(type, id.getValue(), level));
        }

        //declare_val -> $
        // arr_sub -> $
        TreeNode val = curr.findRight();
        if (val.next[0].cate.equals("$"))
        {
            //说明后面直接是赋值或是结束；
            return;
        }
        //arr_sub -> [ exp ]
        else if (val.next[0].token.getValue().equals("["))
        {
            // arr_sub -> [ exp ]
            String [] result = mathExpression(val.next[1], val.next[0].token.getRow(), val.next[1].token.getCol());
            symbolTable.query(id.getValue()).setLength(Integer.parseInt(result[1]));

            //assign_init_value -> ASS_INIT_OP right_value
            TreeNode temp = val.pre.pre.next[2].next[1];

            //right_value -> { mult_data }
            //mult_data -> value number_closure
            //number_closure -> , value number_closure
            //number_closure -> $
            if(temp.next[0].cate != "{"){
                errorMsg = String.format("%d,%d: 数组赋值错误", temp.findLeft().token.getRow(), temp.findLeft().token.getCol());
                throw new RuntimeException(errorMsg);
            }
            temp = temp.next[1].next[0];
            int index = 0;
            while (temp != null)
            {
                /**
                 * value -> INT
                 * value -> REAL
                 * value -> CHAR
                 * value -> STRING
                 */
                if (temp.cate.equals("value"))
                {
                    //类型不对，报错
                    Token value = temp.next[0].token;
                    if (!value.getCategory().equals(Utils.type2Cate(type)))
                    {
                        errorMsg = String.format("%d,%d: 类型不对", value.getRow(), value.getCol());
                        throw new RuntimeException(errorMsg);
                    }
                    else
                    {
                        symbolTable.query(id.getValue()).setArrayValue(index, value.getValue());
                    }
                    index++;
                    temp = temp.findRight();
                }

                if (temp.next == null || temp.next[0].cate.equals("$"))
                {
                    temp = temp.findRight();
                }
                else
                {
                    temp = temp.next[0];
                }
            }
        }
//        else
//        {
//            //declare_val -> ASSIGNMENT_OP arithmetic_exp
//            String[] exp = mathExpression(val.next[1], val.next[0].token.getRow(), val.next[0].token.getCol());
//
//            if (exp == null)
//            {
//                return;
//            }
//
//            if (!Utils.cateConvert(type, exp[0]))
//            {
//                errorMsg = String.format("%d,%d: 类型不一致", val.next[0].token.getRow(), val.next[0].token.getCol());
//                throw new RuntimeException(errorMsg);
//            }
//            else
//            {
//                symbolTable.query(id.getValue()).setValue(exp[1]);
//            }
//        }
    }

    /**
     * arithmetic_exp -> arithmetic_item item_op arithmetic_alt
     * arithmetic_alt -> $
     * arithmetic_alt -> BIN_AR_OP_2 arithmetic_item arithmetic_alt
     * item_op -> BIN_AR_OP_1 arithmetic_item
     * item_op -> $
     * arithmetic_item -> ( arithmetic_exp )
     * arithmetic_item -> ID
     * arithmetic_item -> value
     * <p>
     * 算术表达式
     *
     * @param curr
     * @return
     */
    private String[] mathExpression(TreeNode curr, int row, int column)
    {
        List<Token> tokens = Expression.getTokens(curr);

        //先进行类型检查，cate表示整个表达式的类型
        String cate = "";
        String tempCate = "";
        for (Token token : tokens)
        {
            //仅支持int、 real进行算术运算
            switch (token.getCategory())
            {
                case Category.CHAR:
                    if (cate.isEmpty())
                    {
                        cate = Category.CHAR;
                        if (tokens.size() != 1)
                        {
                            errorMsg = String.format("%d,%d: 不支持CHAR的运算", token.getRow(), token.getCol());
                            throw new RuntimeException(errorMsg);
                        }
                    }
                    break;
                case Category.INT:
                case Category.REAL:
                    tempCate = Utils.calcCate(cate, token.getCategory());
                    if (tempCate.isEmpty())
                    {
                        errorMsg = String.format("%d,%d: 表达式类型 %s 与变量类型 %s 不一致",
                            token.getRow(),
                            token.getCol(),
                            cate,
                            token.getCategory());

                        throw new RuntimeException(errorMsg);
                    }
                    cate = tempCate;
                    break;
                case Category.IDENTIFIER:
                    Symbol symbol = symbolTable.query(token.getValue());
                    if (symbol == null)
                    {
                        errorMsg = String.format("%d,%d: 没有声明的标识符", token.getRow(), token.getCol());

                        throw new RuntimeException(errorMsg);
                    }
                    else
                    {
                        tempCate = Utils.calcCate(cate, Utils.type2Cate(symbol.getType()));
                        if (tempCate.isEmpty())
                        {
                            errorMsg = String.format("%d,%d: 表达式类型 %s 与变量类型 %s 不一致",
                                token.getRow(),
                                token.getCol(),
                                cate,
                                Utils.type2Cate(symbol.getType()));

                            throw new RuntimeException(errorMsg);
                        }
                        cate = tempCate;
                    }
                    break;
                default:
                    break;
            }
        }

        if (cate.equals("CHAR"))
        {
            return new String[] {cate, tokens.get(0).getValue()};
        }

        //计算表达式
        //中缀转后缀
        List<Token> postfix = Expression.infix2Postfix(tokens);
        //计算后缀表达式
        double value = Double.valueOf(Expression.calc(postfix, symbolTable).toString());

        if (Double.isInfinite(value))
        {
            errorMsg = String.format("%d,%d: 表达式中含有除数为0的项", row, column);
            throw new RuntimeException(errorMsg);
        }

        return new String[] {cate, Double.toString(value)};
    }

    /**
     * 赋值语句
     * assign_stm -> SELF_OP IDENTIFIER
     * assign_stm -> IDENTIFIER assign_item
     * assign_item -> ASSIGNMENT_OP assign_fac
     * assign_item -> SELF_OP
     * assign_item -> [ INT ] ASSIGNMENT_OP assign_fac
     * assign_fac -> arithmetic_exp
     * assign_fac -> read_stm
     *
     * @param curr
     */
    private void assign(TreeNode curr)
    {
        //第一个不是自增自减运算符 ++ --，就是标识符
        if (curr.next[0].cate.equals("SELF_OP"))
        {
            if (curr.next[0].token.getValue().equals("++"))
            {
                symbolTable.query(curr.next[1].token.getValue()).selfAdd();
            }
            else
            {
                symbolTable.query(curr.next[1].token.getValue()).selfMin();
            }
            return;
        }
        //属于标识符，进行查表
        Symbol symbol = symbolTable.query(curr.next[0].token.getValue());

        //查表符号为null，则为没有声明的标识符
        if (symbol == null)
        {
            errorMsg = String.format("%d,%d: 没有声明的标识符", curr.next[0].token.getRow(), curr.next[0].token.getCol());
            throw new RuntimeException(errorMsg);
        }
        //有则继续检查下一个
        if (curr.next[1].cate.equals("assign_item"))
        {
            //assign_item -> ASSIGNMENT_OP assign_fac
            if (curr.next[1].next[0].cate.equals("ASSIGNMENT_OP"))
            {
                TreeNode node = curr.next[1].next[1].next[0];
                //assign_fac -> arithmetic_exp
                if (node.cate.equals("arithmetic_exp"))
                {
                    String[] exp = mathExpression(curr.next[1],
                        curr.next[1].next[0].token.getRow(),
                        curr.next[1].next[0].token.getCol());
                    if (exp == null)
                    {
                        return;
                    }
                    if (Utils.calcCate(Utils.type2Cate(symbol.getType()), exp[0]).isEmpty())
                    {
                        errorMsg =
                            String.format("%d,%d: 类型不一致", curr.next[0].token.getRow(), curr.next[0].token.getCol());
                        throw new RuntimeException(errorMsg);
                    }
                    else
                    {
                        symbolTable.query(curr.next[0].token.getValue()).setValue(exp[1]);
                    }
                }
                else
                {
                    try
                    {
                        Scanner in = new Scanner(System.in);
                        switch (symbol.getType())
                        {
                            case Type.INT:
                                int lineInt = in.nextInt();
                                symbol.setValue(lineInt+ "");
                                break;
                            case Type.REAL:
                                float lineFloat = in.nextFloat();
                                symbol.setValue(lineFloat + "");
                                break;
                            case Type.CHAR:
                                String lineChar = in.nextLine();
                                symbol.setValue(lineChar.charAt(0) + "");
                                break;
                            default:
                                break;
                        }
                    }
                    catch (Exception e)
                    {
                        errorMsg =
                            String.format("%d,%d: 输入类型不一致", curr.next[0].token.getRow(), curr.next[0].token.getCol());
                        throw new RuntimeException(errorMsg);
                    }
                }
            }
            else if (curr.next[1].next[0].token.getCategory().equals("["))
            {
                int index = Integer.parseInt(curr.next[1].next[1].token.getValue());
                if (index >= symbol.getLength())
                {
                    errorMsg = String.format("%d,%d: 数组越界", curr.next[0].token.getRow(), curr.next[0].token.getCol());
                    throw new RuntimeException(errorMsg);
                }
                else
                {
                    String[] exp = mathExpression(curr.next[1].next[4],
                        curr.next[1].next[3].token.getRow(),
                        curr.next[1].next[3].token.getCol());

                    if (exp == null)
                    {
                        return;
                    }
                    if (Utils.calcCate(Utils.type2Cate(symbol.getType()), exp[0]).isEmpty())
                    {
                        errorMsg =
                            String.format("%d,%d: 类型不一致", curr.next[0].token.getRow(), curr.next[0].token.getCol());
                        throw new RuntimeException(errorMsg);
                    }
                    else
                    {
                        symbolTable.query(curr.next[0].token.getValue()).setValue(exp[1]);
                    }
                    symbolTable.query(curr.next[0].token.getValue()).setArrayValue(index, exp[1]);
                }
            }
            else
            {
                if (curr.next[0].token.getValue().equals("++"))
                {
                    symbolTable.query(curr.next[0].token.getValue()).selfAdd();
                }
                else
                {
                    symbolTable.query(curr.next[0].token.getValue()).selfMin();
                }
            }
        }
    }

    /**
     * 逻辑运算
     * logic_exp -> arithmetic_exp logic_ari
     * logic_exp -> BOOL logic_bool
     * logic_ari -> $
     * logic_ari -> RELATION_OP arithmetic_exp
     * logic_bool -> $
     * logic_bool -> RELATION_OP logic_exp
     * <p>
     * logic_exp -> logic_item logic_term
     * logic_term -> LOGIC_OP logic_item logic_term
     * logic_term -> $
     * logic_item -> UN_LOGIC_OP logic_item
     * logic_item -> BOOL
     * logic_item -> arithmetic_exp logic_ari
     * logic_ari -> RELATION_OP arithmetic_exp
     * logic_ari -> $
     */
    private double logicExpression(TreeNode curr, int row, int column)
    {
        List<Token> tokens = Expression.getTokens(curr);

        //先进行类型检查
        String type = "";
        for (Token token : tokens)
        {
            //变量接受bool、int 、real、char
            switch (token.getCategory())
            {
                case Category.IDENTIFIER:
                    Symbol symbol = symbolTable.query(token.getValue());
                    if (symbol == null)
                    {
                        errorMsg = String.format("%d,%d: 没有声明的标识符", token.getRow(), token.getCol());
                        throw new RuntimeException(errorMsg);
                    }
                    else
                    {
                        if (!symbol.getType().equals(Type.INT) && !symbol.getType().equals(Type.REAL) && !symbol.getType().equals(Type.CHAR))
                        {
                            errorMsg = String.format("%d,%d: 变量类型与表达式不一致", token.getRow(), token.getCol());
                            throw new RuntimeException(errorMsg);
                        }
                    }
                    break;
//                case Category.CHAR:
                case Category.STRING:
                    errorMsg = String.format("%d,%d: 与表达式类型不一致", token.getRow(), token.getCol());
                    throw new RuntimeException(errorMsg);
                default:
                    break;
            }
        }

        //计算表达式
        //中缀转后缀
        List<Token> postfix = Expression.infix2Postfix(tokens);
        //计算后缀表达式
        double value = Double.valueOf(Expression.calc(postfix, symbolTable).toString());
//        System.out.println(value);

        return value;
    }
    public static void main(String[] args)
    {

        int a = 1;
        int b = 22;
        try
        {
            AnalyseTable at = null;
            Lexer lexer = new Lexer();
            ArrayList<Token> tokens = lexer.lexing("testAnalyse.c");

            if (lexer.getErrorTokens().size() > 0)
            {
                StringBuilder errorMsg = new StringBuilder();
                for (int i = 0; i < lexer.getErrorTokens().size(); i++)
                {
                    errorMsg.append(lexer.getErrorTokens().get(i).toString() + "\n");
                }
                throw new RuntimeException(errorMsg.toString());
            }

            at = new AnalyseTable("production_v1.txt", "program");
            at.analyze(tokens);
            if (at.isCorrect())
            {
//                ShowTree showTree = new ShowTree();
//                if (showTree.create(at.getHeader()))
//                {
//                    System.out.println("成功");
//                }
//                else
//                {
//                    System.out.println("失败");
//                }
                Semantic_v1 semantic = new Semantic_v1();
                semantic.run(at.getHeader());
            }
            else{
                for(String error : at.getErrors()){
                    System.out.println(error);
                }
            }
        }
        catch (RuntimeException e)
        {
            System.out.println(e.toString());
            System.out.println("遇到错误,终止进程，退出");
            System.exit(0);
        }
        catch (IOException ioe)
        {
            System.out.println(ioe.getMessage());
            System.out.println("IO操作出错,终止进程，退出");
            System.exit(0);
        }
        //MainUI mainUI = new MainUI();
    }
}
