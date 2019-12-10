package com.wkq.interpreter.semantic;

import com.wkq.interpreter.lexer.entity.Token;
import com.wkq.interpreter.lexer.utils.Category;
import com.wkq.interpreter.parser.TreeNode;

import java.util.*;

/**
 * @author: wkq
 * @date: 2019/11/13 18:47
 */
public class Semantic_v1 {
    //符号表栈
    private SymbolTable symbolTable = new SymbolTable();

    //报错信息
    private String errorMsg = "";

    /* 语义分析标识符作用域 */
    private int level = 0;

    private boolean hasBreak = false;

    private boolean hasContinue = false;

    public void run(TreeNode curr) {
        semantic(curr);
    }

    private int number = 1;

    private TreeNode findNode(TreeNode curr, String idName) {

        if (curr.next == null) {
            if (curr.token != null && curr.token.getValue().equals(idName)) {
                //找到数组
                if (number == 1) {
                    return curr;
                }
                number--;
            }
            return null;
        }

        TreeNode target = null;
        for (int i = 0; i < curr.next.length; i++) {
            target = findNode(curr.next[i], idName);
            if (target != null) {
                break;
            }
        }
        return target;
    }

    private Object getValue(TreeNode var) {
        TreeNode arr_sub = var.next[1];
        String idName = var.next[0].token.getValue();
        TreeNode arrExp = null;
        try {
            arrExp = arr_sub.next[1];
        } catch (NullPointerException e) {
            errorMsg = String.format("%d,%d: 数组运算错误", var.next[0].token.getRow(), var.next[0].token.getCol());
            throw new RuntimeException(errorMsg);
        }

        Object arrIndex = expression(arrExp, var.next[0].token.getRow(), var.next[0].token.getCol());

        if (!arrIndex.getClass().equals(Integer.class)) {
            errorMsg = String.format("%d,%d: 数组下标错误", var.next[0].token.getRow(), var.next[0].token.getCol());
            throw new RuntimeException(errorMsg);
        }

        //如果下标小于0，直接返回0
        if (Integer.parseInt(arrIndex.toString()) < 0) {
            return 0;
        }

        return symbolTable.query(idName).getArray(Integer.parseInt(arrIndex.toString()));
    }

    private List<Token> getArrayValue_V1(TreeNode exp){
        List<Token> tokens = Expression.getTokens(exp);
        List<String> zKH = new LinkedList<>();
        List<String> idName = new LinkedList<>();
        List<Token> zKHNToken = new LinkedList<>();


        return null;
    }

    private List<Token> getArrayValue(TreeNode exp) {
        List<Token> tokens = Expression.getTokens(exp);
        List<String> valueType = new LinkedList<>();
        List<Integer> arrIdIndexList = new LinkedList<>();
        List<Integer> leftIndexList = new LinkedList<>();
        List<Integer> rightIndexList = new LinkedList<>();



        boolean hasArray = false;
        List<String> arrIdSet = new LinkedList<>();

        for (int i = 0; i < tokens.size(); i++) {
            if (hasArray) {
                if (tokens.get(i).getCategory().equals("]")) {
                    rightIndexList.add(i - rightIndexList.size() * 3);
                    continue;
                }
            }
            Token token = tokens.get(i);
            if (token.getCategory().equals("IDENTIFIER")) {
                String idValue = token.getValue();
                Symbol symbol = symbolTable.query(idValue);
                if (symbol == null) {
                    errorMsg = String.format("%d,%d: 未声明标识符", token.getRow(), token.getCol());
                    throw new RuntimeException(errorMsg);
                }
                //如果是数组，转为数组数据
                if (symbol.isArray()) {
                    hasArray = true;
                    arrIdSet.add(symbol.getName());
                    valueType.add(Utils.type2Cate(symbol.getType()));
                    arrIdIndexList.add(i - arrIdIndexList.size() * 3);
                    leftIndexList.add(i + 1 - leftIndexList.size() * 3);
                } else {
                    continue;
                }
            }
        }

        if (hasArray) {
            for (int j = 0; j < arrIdSet.size(); j++) {
                int index = j;
                if (j > 0) {
                    while (index > 0 && arrIdSet.get(index).equals(arrIdSet.get(index - 1))) {
                        number++;
                        index--;
                    }
                }
                TreeNode result = findNode(exp, arrIdSet.get(j));
                if (result != null) {
                    Object arrValue = getValue(result.pre);
                    int initIndex = arrIdIndexList.get(j);
                    int row = tokens.get(initIndex).getRow();
                    int col = tokens.get(initIndex).getCol();
                    for (int i = initIndex; arrIdIndexList.get(j) <= i && i <= rightIndexList.get(j); i++) {
                        tokens.remove(initIndex);
                    }
                    tokens.add(initIndex, new Token(valueType.get(j), arrValue.toString(), row, col));
                }
            }
            return tokens;
        }

        return tokens;
    }

    //表达式处理
    private Object expression(TreeNode curr, int row, int col) {

        Object resultValue = 0;
        //一元表达式
        if (curr.next[0].cate.equals("unary_exp")) {
            //左运算
            TreeNode prefixNode = curr.next[0].next[0];
            TreeNode varNode = prefixNode.next[1];
            Token idToken = varNode.next[0].token;
            Symbol symbol = symbolTable.query(idToken.getValue());
            //查表，是否声明
            if (symbol == null) {
                errorMsg = String.format("%d,%d: 未声明标识符", idToken.getRow(), idToken.getCol());
                throw new RuntimeException(errorMsg);
            }

            if (symbol.isArray()) {
                if (varNode.next[1].next[0].cate.equals("$")) {
                    errorMsg = String.format("%d,%d: 地址无法自运算", idToken.getRow(), idToken.getCol());
                    throw new RuntimeException(errorMsg);
                } else {
                    Object i = expression(varNode.next[1].next[1],
                            prefixNode.next[0].token.getRow(),
                            prefixNode.next[0].token.getCol());
                    int index = Integer.parseInt(i.toString());
                    //var -> IDENTIFIER arr_sub

                    if (prefixNode.next[0].cate.equals("++")) {
                        symbol.setArrayValue(index, String.valueOf(Integer.parseInt(symbol.getArray(index)) + 1));
                    } else if (prefixNode.next[0].cate.equals("--")) {
                        symbol.setArrayValue(index, String.valueOf(Integer.parseInt(symbol.getArray(index)) - 1));
                    }
                    resultValue = symbol.getArray(index);
                }
            } else {
                if (prefixNode.next[0].cate.equals("++")) {
                    symbol.selfAdd();
                    resultValue = symbol.getValue();
                } else if (prefixNode.next[0].cate.equals("--")) {
                    symbol.selfMin();
                    resultValue = symbol.getValue();
                }
            }

        } else {

            List<Token> tokens = getArrayValue(curr);
            //先进行类型检查，cate表示整个表达式的类型
            String cate = "";
            String tempCate = "";
            for (Token token : tokens) {
                //仅支持int、 real进行算术运算
                switch (token.getCategory()) {
                    case Category.INT:
                    case Category.CHAR:
                    case Category.REAL:
                        tempCate = Utils.calcCate(cate, token.getCategory());
                        if (tempCate.isEmpty()) {
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
                        if (symbol == null) {
                            errorMsg = String.format("%d,%d: 没有声明的标识符", token.getRow(), token.getCol());

                            throw new RuntimeException(errorMsg);
                        } else {
                            tempCate = Utils.calcCate(cate, Utils.type2Cate(symbol.getType()));
                            if (tempCate.isEmpty()) {
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

            if (tokens.size() == 1 && tokens.get(0).getCategory().equals(Category.STRING)) {
                return tokens.get(0).getValue();
            }
            //计算表达式
            //中缀转后缀
            List<Token> postfix = Expression.infix2Postfix(tokens);
            //计算后缀表达式
            resultValue = Expression.calc(postfix, symbolTable);

//            if (Double.isInfinite(Double.valueOf(resultValue))) {
//                errorMsg = String.format("%d,%d: 表达式中含有除数为0的项", row, col);
//                throw new RuntimeException(errorMsg);
//            }
//            if (cate != "REAL") {
//                resultValue = (new Double((double) resultValue)).intValue();
//            }
        }
        return resultValue;
    }

    //声明语句
    private void declare(TreeNode curr, String type) {
        //赋初值
        //assign_init_value -> ASS_INIT_OP right_value
        //assign_init_value -> $
        //参数声明  声明  声明闭包
        //param_dec -> $
        //dec -> TYPE var assign_init_value dec_one_line
        if (curr.cate.equals("dec")) {
            //变量
            declare(curr.next[1], curr.next[0].token.getValue());

            //初值
            declare(curr.next[3], curr.next[0].token.getValue());
            return;
        }

        //dec_one_line -> , var assign_init_value dec_one_line
        //dec_one_line -> $
        if (curr.cate.equals("dec_one_line")) {
            if (!curr.next[0].cate.equals("$")) {
                //有后续
                //变量
                declare(curr.next[1], type);

                //初值
                declare(curr.next[3], type);
            }
        }

        //var -> IDENTIFIER arr_sub
        if (curr.cate.equals("var")) {
            declare(curr.next[0], type);
            return;
        }

        if (!curr.cate.equals("IDENTIFIER")) {
            return;
        }

        Token id = curr.token;
        if (symbolTable.query(id.getValue()) != null) {
            errorMsg = String.format("%d,%d: 重复定义的标识符", id.getRow(), id.getCol());
            throw new RuntimeException(errorMsg);
        } else {
            symbolTable.insert(new Symbol(type, id.getValue(), level));
        }

        TreeNode val = curr.findRight();
        if (val.next[0].cate.equals("$")) {
            //说明后面直接是赋值或是结束；
            val = val.pre.findNext().next[0];
            if (val.cate.equals("ASS_INIT_OP")) {
                //有初值
                TreeNode rightValue = val.findNext();

                if (rightValue.next[0].cate.equals("{")) {
                    //报错
                    throw new RuntimeException("赋值错误，不应为数组");
                } else if (rightValue.next[0].cate.equals("exp")) {
                    //表达式
                    Object value = expression(rightValue.next[0], val.token.getRow(), val.token.getCol());
                    symbolTable.query(id.getValue()).setValue(value.toString());
                    return;
                } else {
                    //输入
                    Scanner in = new Scanner(System.in);
                    try {
                        switch (id.getCategory()) {
                            case Category.INT:
                                int lineInt = in.nextInt();
                                symbolTable.query(id.getValue()).setValue(lineInt + "");
                                break;
                            case Category.REAL:
                                float lineFloat = in.nextFloat();
                                symbolTable.query(id.getValue()).setValue(lineFloat + "");
                                break;
                            case Category.CHAR:
                                String lineChar = in.nextLine();
                                symbolTable.query(id.getValue()).setValue(lineChar.charAt(0) + "");
                                break;
                            default:
                                break;
                        }
                    } catch (Exception e) {
                        errorMsg =
                                String.format("%d,%d: 输入类型不一致", curr.next[0].token.getRow(), curr.next[0].token.getCol());
                        throw new RuntimeException(errorMsg);
                    }
                    return;
                }
            } else if (val.cate.equals("$")) {
                //无初值
                //自定义初值
                if (type.equals("int") || type.equals("real")) {
                    symbolTable.query(id.getValue()).setValue("0");
                } else if (type.equals("char")) {
                    symbolTable.query(id.getValue()).setValue("");
                }
                return;
            }
            return;
        } else if (val.next[0].token.getValue().equals("[")) {
            // arr_sub -> [ exp ]
            //数组大小
            Object result = expression(val.next[1], val.next[0].token.getRow(), val.next[0].token.getCol());
            symbolTable.query(id.getValue()).setLength((int) result);

            val = val.pre.findNext();
            if (val.next[0].cate.equals("ASS_INIT_OP")) {
                //有初值
                TreeNode rightValue = val.next[0].findNext();

                if (!rightValue.next[0].cate.equals("{")) {
                    //报错
                    errorMsg = String.format("%d,%d: 赋值错误，应为数组", rightValue.token.getRow(), rightValue.token.getCol());
                    throw new RuntimeException(errorMsg);
                } else {
                    //数组初值
                    TreeNode multValue = rightValue.next[1];

                    TreeNode temp = multValue.next[0];
                    int index = 0;
                    while (temp != null) {
                        /**
                         * value -> INT
                         * value -> REAL
                         * value -> CHAR
                         * value -> STRING
                         */
                        if (temp.cate.equals("exp")) {
                            Object expvalue = expression(temp, val.next[0].token.getRow(), val.next[0].token.getCol());
                            //类型不对，报错
                            if (Utils.classCheck(expvalue.getClass(), type)) {
                                symbolTable.query(id.getValue()).setArrayValue(index, expvalue.toString());
                            } else {
                                errorMsg = String.format("%d,%d: 数组赋值类型不匹配",
                                        val.next[0].token.getRow(),
                                        val.next[0].token.getCol());
                                throw new RuntimeException(errorMsg);
                            }
                            index++;
                            temp = temp.findRight();
                        }

                        if (temp.next == null || temp.next[0].cate.equals("$")) {
                            temp = temp.findRight();
                        } else {
                            temp = temp.next[0];
                        }
                    }
                    return;
                }
            } else if (val.next[0].cate.equals("$")) {
                //无初值
                //自定义初值
                int length = (int) result;
                for (int i = 0; i < length; i++) {
                    symbolTable.query(id.getValue()).setArrayValue(i, "0");
                }
                return;
            }
            return;
        }

    }

    private double assignRightValue(TreeNode rightValueNode) {
        Object rightValue = null;
        String inputValue = null;
        //等于{ mult}就报错
        if (rightValueNode.next[0].cate.equals("{")) {
            errorMsg = String.format("%d,%d: 右值错误",
                    rightValueNode.findLeft().next[0].token.getRow(),
                    rightValueNode.findLeft().next[0].token.getCol());
            throw new RuntimeException(errorMsg);
        } else if (rightValueNode.next[0].cate.equals("exp")) {
            rightValueNode.findLeft();
            //右值为表达式，直接计算
            rightValue = expression(rightValueNode.next[0],
                    rightValueNode.findLeft().token.getRow(),
                    rightValueNode.findLeft().token.getCol());
        } else {
            //scan_stm 输入
            if (rightValueNode.next[0].next[2].next[0].cate != "$") {
                inputValue = String.valueOf(expression(rightValueNode.next[0].next[2].next[0],
                        rightValueNode.next[0].next[1].token.getRow(),
                        rightValueNode.next[0].next[1].token.getCol()));
            } else {
                inputValue = scanStm();
            }
        }
        Object value = inputValue == null ? rightValue : inputValue;
        double newValue = -1;
        try {
            newValue = new Double(value.toString());
        } catch (Exception e) {
            throw new RuntimeException("输入的数据有误,可能是类型不符.");
        }

        return newValue;
    }

    //赋值
    private void assign(Symbol symbol, String op, boolean isArray, int index, double oldValue, double newValue) {
        switch (op) {
            case "=":
                if (isArray) {
                    symbol.setArrayValue(index, String.valueOf(newValue));
                } else {
                    symbol.setValue(String.valueOf(newValue));
                }
                break;
            case "+=":
                if (isArray) {
                    symbol.setArrayValue(index, String.valueOf(oldValue + newValue));
                } else {
                    symbol.setValue(String.valueOf(oldValue + newValue));
                }
                break;
            case "-=":
                if (isArray) {
                    symbol.setArrayValue(index, String.valueOf(oldValue - newValue));
                } else {
                    symbol.setValue(String.valueOf(oldValue - newValue));
                }
                break;
            case "*=":
                if (isArray) {
                    symbol.setArrayValue(index, String.valueOf(oldValue * newValue));
                } else {
                    symbol.setValue(String.valueOf(oldValue * newValue));
                }
                break;
            case "/=":
                if (isArray) {
                    symbol.setArrayValue(index, String.valueOf(oldValue / newValue));
                } else {
                    symbol.setValue(String.valueOf(oldValue / newValue));
                }
                break;
            case "%=":
                if (isArray) {
                    symbol.setArrayValue(index, String.valueOf(oldValue % newValue));
                } else {
                    symbol.setValue(String.valueOf(oldValue % newValue));
                }
                break;
            default:
                throw new RuntimeException("为识别的赋值符号");
        }
    }

    //赋值语句
    private void assignFunc(TreeNode assign_func) {
        //assign_func -> self_in_decrease_op var ;
        //assign_func -> var ass_funccall
        //var -> IDENTIFIER arr_sub
        //第一个是自增自减运算符 ++ --
        if (assign_func.next[0].cate.equals("self_in_decrease_op")) {
            Symbol symbol = symbolTable.query(assign_func.next[1].next[0].token.getValue());
            if (symbol == null) {
                errorMsg = String.format("%d,%d: 未定义的标识符",
                        assign_func.next[0].next[0].token.getRow(),
                        assign_func.next[0].next[0].token.getCol());
                throw new RuntimeException(errorMsg);
            }
            TreeNode var = assign_func.next[1];
            String op = assign_func.next[0].next[0].token.getValue();
            if (symbol.isArray()) {
                //是数组
                Object index = expression(var.next[1].next[1],
                        var.next[1].next[0].token.getRow(),
                        var.next[1].next[1].token.getCol());
                if (index.toString().contains(".")) {
                    errorMsg = String.format("%d,%d: 数组下标有误",
                            var.next[1].next[0].token.getRow(),
                            var.next[1].next[0].token.getCol());
                    throw new RuntimeException(errorMsg);
                }
                double oValue = Double.parseDouble(symbol.getArray(Integer.parseInt(index.toString())));
                symbol.setArrayValue(Integer.parseInt(index.toString()),
                        op.equals("++") ? String.valueOf(oValue + 1) : String.valueOf(oValue - 1));
            } else {
                if (op.equals("++")) {
                    symbol.selfAdd();
                } else {
                    symbol.selfMin();
                }
            }

            return;
        }

        //第一个为变量
        TreeNode var = assign_func.next[0];
        Symbol symbol = symbolTable.query(var.next[0].token.getValue());

        String assOp = assign_func.next[1].next[0].cate;
        //不等于这两个就直接返回，没意义
        if (!assOp.equals("ASSIGNMENT_OP") && !assOp.equals("ASS_INIT_OP")) {
            return;
        }

        //ass_funccall -> ASS_INIT_OP right_value ;
        TreeNode rightValueNode = assign_func.next[1].next[1];

        //for array
        Object index = null;
        double oValue = 0;
        double newValue = assignRightValue(rightValueNode);

        String op = assign_func.next[1].next[0].token.getValue();
        if (symbol.isArray()) {
            //数组赋值
            index =
                    expression(var.next[1].next[1], var.next[1].next[0].token.getRow(), var.next[1].next[0].token.getCol());
            if (index.toString().contains(".")) {
                errorMsg = String.format("%d,%d: 数组下标有误",
                        var.next[1].next[0].token.getRow(),
                        var.next[1].next[0].token.getCol());
                throw new RuntimeException(errorMsg);
            }
            oValue = Double.parseDouble(symbol.getArray(Integer.parseInt(index.toString())));

            //SELP_OP
            String selfOp = var.findNext().next[0].token.getValue();
            if (var.findNext().next[0].cate.equals("self_in_decrease_op")) {
                symbol.setArrayValue(Integer.parseInt(index.toString()),
                        selfOp.equals("++") ? String.valueOf(oValue + 1) : String.valueOf(oValue - 1));
                return;
            }

            assign(symbol, op, true, Integer.parseInt(index.toString()), oValue, newValue);
        } else {
            //变量赋值
            assign(symbol, op, false, -1, symbol.getValue(), newValue);
        }

    }

    //while循环
    private void whileLoop(TreeNode while_loop) {
        //while_loop -> while while_loop_block
        //while_loop_block -> ( exp ) loop_block
        TreeNode whileLoopBlock = while_loop.next[1];

        Object value = expression(whileLoopBlock.next[1],
                whileLoopBlock.next[0].token.getRow(),
                whileLoopBlock.next[0].token.getCol());

        if (Double.isNaN(Double.valueOf(value.toString()))) {
            return;
        }

        while (Double.valueOf(value.toString()) > 0) {
            level++;
            if (whileLoopBlock.next[3].next[0].cate.equals(";")) {
                return;
            } else if (!whileLoopBlock.next[3].next[0].cate.equals(";") &&
                    !whileLoopBlock.next[3].next[0].cate.equals("com_func_block")) {
                this.semantic(whileLoopBlock.next[3].next[0]);
            } else {
                //loop_block -> com_func_block
                //com_func_block -> { func_block }
                this.semantic(whileLoopBlock.next[3].next[0].next[1]);
            }
            value = expression(whileLoopBlock.next[1],
                    whileLoopBlock.next[0].token.getRow(),
                    whileLoopBlock.next[0].token.getCol());
            if (hasBreak) {
                hasBreak = false;
                break;
            }
            if (hasContinue) {
                hasContinue = false;
            }
            level--;
            symbolTable.update(level);
        }
    }

    //for循环
    private void forAfterAssign(TreeNode for_after) {
        //for_after -> var for_after_op
        //for_after_op -> self_in_decrease_op
        //for_after_op -> ASSIGNMENT_OP right_value
        TreeNode var = for_after.next[0];
        TreeNode forAfterOp = for_after.next[1];
        Symbol symbol = symbolTable.query(var.next[0].token.getValue());
        if (symbol == null) {
            errorMsg = String.format("%d,%d: 未声明标识符",
                    for_after.pre.next[3].token.getRow(),
                    for_after.pre.next[3].token.getCol());
            throw new RuntimeException(errorMsg);
        }

        Object index = null;
        double oValue = 0;
        if (symbol.isArray()) {
            index =
                    expression(var.next[1].next[1], var.next[1].next[0].token.getRow(), var.next[1].next[1].token.getCol());
            if (index.toString().contains(".")) {
                errorMsg = String.format("%d,%d: 数组下标有误",
                        var.next[1].next[0].token.getRow(),
                        var.next[1].next[0].token.getCol());
                throw new RuntimeException(errorMsg);
            }
            oValue = Double.parseDouble(symbol.getArray(Integer.parseInt(index.toString())));

            if (forAfterOp.next[0].cate.equals("self_in_decrease_op")) {
                //for_after_op -> self_in_decrease_op
                if (forAfterOp.next[0].next[0].token.getValue().equals("++")) {
                    symbol.setArrayValue(Integer.parseInt(index.toString()), String.valueOf(oValue + 1));
                } else {
                    symbol.setArrayValue(Integer.parseInt(index.toString()), String.valueOf(oValue - 1));
                }
            } else {
                //for_after_op -> ASSIGNMENT_OP right_value
                String op = forAfterOp.next[0].token.getValue();
                assign(symbol,
                        op,
                        true,
                        Integer.parseInt(index.toString()),
                        oValue,
                        assignRightValue(forAfterOp.next[1]));
            }
        } else {
            if (forAfterOp.next[0].cate.equals("self_in_decrease_op")) {
                //for_after_op -> self_in_decrease_op
                if (forAfterOp.next[0].next[0].token.getValue().equals("++")) {
                    symbol.selfAdd();
                } else {
                    symbol.selfMin();
                }
            } else {
                //for_after_op -> ASSIGNMENT_OP right_value
                String op = forAfterOp.next[0].token.getValue();
                assign(symbol, op, false, -1, oValue, assignRightValue(forAfterOp.next[1]));
            }
        }
    }

    private void forLoop(TreeNode for_loop) {
        //for_loop -> for for_loop_block
        //for_loop_block -> ( assign_func exp ; for_after ) loop_block

        TreeNode forLoopBlock = for_loop.next[1];
        assignFunc(forLoopBlock.next[1]);
        Object value =
                expression(forLoopBlock.next[2], forLoopBlock.next[0].token.getRow(), forLoopBlock.next[0].token.getCol());

        if (Double.isNaN(Double.valueOf(value.toString()))) {
            return;
        }

        while (Double.valueOf(value.toString()) > 0) {
            level++;
            if (forLoopBlock.next[6].next[0].cate.equals(";")) {
                return;
            } else if (!forLoopBlock.next[6].next[0].cate.equals(";") &&
                    !forLoopBlock.next[6].next[0].cate.equals("com_func_block")) {
                this.semantic(forLoopBlock.next[6].next[0]);
            } else {
                //loop_block -> com_func_block
                //com_func_block -> { func_block }
                this.semantic(forLoopBlock.next[6].next[0].next[1]);
            }

            //for_after
            forAfterAssign(forLoopBlock.next[4]);

            //exp
            value = expression(forLoopBlock.next[2],
                    forLoopBlock.next[0].token.getRow(),
                    forLoopBlock.next[0].token.getCol());
            if (hasBreak) {
                hasBreak = false;
                break;
            }
            if (hasContinue) {
                hasContinue = false;
            }
            level--;
            symbolTable.update(level);
        }

    }

    //条件语句
    private void conditionalStat(TreeNode conditional_stat) {
        //conditional_stat -> if conditional_stat_block
        //conditional_stat_block -> ( exp ) if_then_block
        Object value = expression(conditional_stat.next[1].next[1],
                conditional_stat.next[1].next[0].token.getRow(),
                conditional_stat.next[1].next[0].token.getCol());
        if (Double.isNaN(Double.valueOf(value.toString()))) {
            return;
        }

        TreeNode if_then_block = conditional_stat.next[1].next[3];
        //判断是否正确，正确进入
        if (Double.valueOf(value.toString()) > 0) {
            if (if_then_block.next[0].cate.equals(";")) {
                return;
            } else if (!if_then_block.next[0].cate.equals(";") && !if_then_block.next[0].cate.equals("com_func_block")) {
                this.semantic(if_then_block.next[0]);
            } else {
                this.semantic(if_then_block.next[0].next[1]);
            }
        } else {
            //有else进入else部分
            int nextLength = if_then_block.next.length;
            TreeNode lastNode = if_then_block.next[nextLength - 1];
            if (lastNode.cate.equals("else_stat")) {
                this.semantic(lastNode);
            }
        }
    }

    //打印
    private void printStm(TreeNode curr) {
        //print_stm -> ( print_val )
        //pint_val -> exp
        Object expResult = expression(curr.next[2].next[0], curr.next[0].token.getRow(), curr.next[0].token.getCol());
        System.out.println(expResult.toString());
    }

    //输入
    private String scanStm() {
        Scanner in = new Scanner(System.in);
        String line = in.nextLine();
        return line;
    }

    //开始分析
    private void semantic(TreeNode curr) {
        if (!hasBreak && !hasContinue) {
            switch (curr.cate) {
                case "scan_stm":
                    scanStm();
                    return;
                case "print_stm":
                    printStm(curr);
                    return;
                case "dec":
                    declare(curr, "");
                    return;
                case "assign_func":
                    assignFunc(curr);
                    return;
                case "while_loop":
                    whileLoop(curr);
                    return;
                case "for_loop":
                    forLoop(curr);
                    return;
                case "conditional_stat":
                    level++;
                    conditionalStat(curr);
                    level--;
                    symbolTable.update(level);
                    return;
                case "break":
                    hasBreak = true;
                    return;
                case "continue":
                    hasContinue = true;
                    return;
                default:
                    break;
            }
        }

        if (curr.next != null && curr.next.length > 0) {
            for (TreeNode node : curr.next) {
                semantic(node);
            }
        }
    }
}
