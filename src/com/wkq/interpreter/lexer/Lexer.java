package com.wkq.interpreter.lexer;

import com.wkq.interpreter.lexer.entity.ErrorToken;
import com.wkq.interpreter.lexer.entity.Token;
import com.wkq.interpreter.lexer.utils.Category;

import java.io.*;
import java.util.ArrayList;

import com.google.gson.*;

import static com.wkq.interpreter.lexer.utils.Judgement.*;

public class Lexer
{
    //token列表
    private ArrayList<Token> tokenList = null;

    //error列表
    private ArrayList<ErrorToken> errorTokens = null;

    //结果
    private String resultJson = null;

    private BufferedReader br = null;

    //行号和列号
    private int rowNum = 1, colNum = 1;

    //读取的字符
    private char ch;

    public Lexer()
    {
        tokenList = new ArrayList<>();
        errorTokens = new ArrayList<>();
    }

    /**
     * 词法分析
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public ArrayList<Token> lexing(String filePath)
        throws IOException
    {
        br = new BufferedReader(new FileReader(filePath));

        ch = (char)br.read();

        while (!isEOF(ch))
        {
            //判断开头的字符
            if (isLETTER(ch))
            {
                startWithLetter();
            }
            else if (isNum(ch))
            {
                startWithNumber();
            }
            else
            {
                startWithOther();
            }
        }

        saveJson();
        return tokenList;
    }

    //以字母开头
    private void startWithLetter()
        throws IOException
    {
        int i = 0;
        StringBuilder sb = new StringBuilder();
        sb.append(ch);

        ch = (char)br.read();
        i++;

        while (ch == '_' || isLETTER(ch) || isNum(ch))
        {
            sb.append(ch);
            ch = (char)br.read();
            i++;
        }

        String word = sb.toString();

        if (isKeyword(word))
        {
            //是关键字
            Token token = new Token(word, word, rowNum, colNum);
            if (isType(word))
            {
                token.setCategory(Category.TYPE);
            }
            tokenList.add(token);
        }
        else if (word.equals("true") || word.equals("false"))
        {
            //是布尔变量
            Token token = new Token(Category.BOOL, word, rowNum, colNum);
            tokenList.add(token);
        }
        //判断是否以下划线结尾
        else if (word.endsWith("_"))
        {
            //非法标识符
            ErrorToken errorToken = new ErrorToken("标识符下划线结尾", word, rowNum, colNum);
            errorTokens.add(errorToken);
        }
        else
        {
            //是标识符
            Token token = new Token(Category.IDENTIFIER, word, rowNum, colNum);
            tokenList.add(token);
        }

        colNum += i;
    }

    //数字开头
    private void startWithNumber()
        throws IOException
    {
        int i = 0;

        StringBuilder sb = new StringBuilder();
        sb.append(ch);

        ch = (char)br.read();
        i++;

        int pointNum = 0;
        //判断数字及数字开头的非法标识符
        while (isNum(ch) || ch == '.' || isLETTER(ch) || ch == '_')
        {
            if (ch == '.' && pointNum == 0)
            {
                pointNum++;
                sb.append(ch);
                ch = (char)br.read();
                i++;
            }
            else if (ch != '.')
            {
                sb.append(ch);
                ch = (char)br.read();
                i++;
            }

            if (ch == '.' && pointNum == 1)
            {
                break;
            }
        }

        String word = sb.toString();

        if (isInt(word))
        {
            Token token = new Token(Category.INT, word, rowNum, colNum);
            tokenList.add(token);
        }
        else if (isReal(word))
        {
            Token token = new Token(Category.REAL, word, rowNum, colNum);
            tokenList.add(token);
        }
        else
        {
            errorTokens.add(new ErrorToken("非法标识符", sb.toString(), rowNum, colNum));
            //非法标识符
        }

        colNum += i;
    }

    //以小数点开头
    private void startWithPoint()
        throws IOException
    {
        int i = 1;

        StringBuilder sb = new StringBuilder();
        sb.append('.');
        sb.append(ch);

        ch = (char)br.read();
        i++;
        int pointNum = 0;
        //判断是否为数字或小数点，是则继续
        while (isNum(ch) || ch == '.')
        {
            if (ch == '.' && pointNum != 0)
            {
                break;
            }
            pointNum++;
            sb.append(ch);
            ch = (char)br.read();
            i++;
        }

        String word = sb.toString();

        if (isReal(word))
        {
            Token token = new Token(Category.REAL, word, rowNum, colNum);
            tokenList.add(token);
        }
        else
        {
            //非法
            errorTokens.add(new ErrorToken("非法标识符", sb.toString(), rowNum, colNum));
        }
        colNum += i;
    }

    //以其他字符开头
    private void startWithOther()
        throws IOException
    {
        Token token;

        switch (ch)
        {
            //空格
            case ' ':
                ch = (char)br.read();
                colNum++;
                break;
            //制表符
            case '\t':
                ch = (char)br.read();
                colNum += 5;
                break;
            //回车键
            case '\r':
                ch = (char)br.read();
                if (ch == '\n')
                {
                    ch = (char)br.read();
                }
                colNum = 1;
                rowNum++;
                break;
            //换行符
            case '\n':
                ch = (char)br.read();
                colNum = 1;
                rowNum++;
                break;
            //分界符
            case '[':
            case ']':
            case '(':
            case ')':
            case '{':
            case '}':
                //双分界符
                token = new Token(ch + "", ch + "", rowNum, colNum);
                tokenList.add(token);
                ch = (char)br.read();
                colNum++;
                break;
            case ':':
            case ',':
            case ';':
                token = new Token(ch + "", ch + "", rowNum, colNum);
                tokenList.add(token);
                ch = (char)br.read();
                colNum++;
                break;
            //小数点
            case '.':
                ch = (char)br.read();
                if (isNum(ch))
                {
                    startWithPoint();
                }
                break;
            //单引号
            case '\'':
                handleChar();
                break;
            //双引号
            case '\"':
                handleString();
                break;
            //运算符
            case '+':
                handlePlus();
                break;
            case '-':
                handleMinus();
                break;
            case '*':
            case '%':
            case '=':
                handleOperator();
                break;
            case '!':
                errorTokens.add(new ErrorToken("无法识别的符号", "" + ch, rowNum, colNum));
                break;
            case '/':
                handleDiv();
                break;
            case '>':
                handleGreater();
                break;
            case '<':
                handleLess();
                break;
            case '&':
                ch = (char)br.read();
                if (ch == '&')
                {
                    token = new Token(Category.LOGIC_OP, "&&", rowNum, colNum);
                    tokenList.add(token);
                    ch = (char)br.read();
                    colNum += 2;
                }
                else
                {
                    //错误处理
                    errorTokens.add(new ErrorToken("无法识别的符号", "&" + ch, rowNum, colNum));
                    colNum++;
                }
                break;
            case '|':
                ch = (char)br.read();
                if (ch == '|')
                {
                    token = new Token(Category.LOGIC_OP, "||", rowNum, colNum);
                    tokenList.add(token);
                    ch = (char)br.read();
                    colNum += 2;
                }
                else
                {
                    //错误处理
                    errorTokens.add(new ErrorToken("无法识别的符号", "|" + ch, rowNum, colNum));
                    colNum++;
                }
                break;
            default:
                //错误处理
                errorTokens.add(new ErrorToken("无法识别的符号", ch + "", rowNum, colNum));
                ch = (char)br.read();
                colNum++;
        }
    }

    //处理字符
    private void handleChar()
        throws IOException
    {
        int i = 0;

        StringBuilder sb = new StringBuilder();

        ch = (char)br.read();
        i++;

        while (ch != '\'')
        {
            if (isEnd(ch))
            {
                //错误处理
                errorTokens.add(new ErrorToken("字符没有结束", sb.toString(), rowNum, colNum));
                break;
            }
            else
            {
                sb.append(ch);
            }

            ch = (char)br.read();
            i++;
        }

        if (ch == '\'')
        {
            ch = (char)br.read();
            i++;
            Token token = new Token(Category.CHAR, sb.toString(), rowNum, colNum);
            tokenList.add(token);
        }

        colNum += i;
    }

    //处理字符串
    private void handleString()
        throws IOException
    {
        int i = 0;
        StringBuilder sb = new StringBuilder();

        ch = (char)br.read();
        i++;

        while (ch != '\"')
        {
            if (isEnd(ch))
            {
                //错误处理
                errorTokens.add(new ErrorToken("字符串没有结束", sb.toString(), rowNum, colNum));
                break;
            }
            else //继续读取字符
            {
                sb.append(ch);
            }

            ch = (char)br.read();
            i++;
        }

        if (ch == '\"')
        {
            ch = (char)br.read();
            i++;

            Token token = new Token(Category.STRING, sb.toString(), rowNum, colNum);

            tokenList.add(token);
        }

        colNum += i;
    }

    //处理加号
    private void handlePlus()
        throws IOException
    {
        //默认 +
        Token token = new Token(Category.BIN_AR_OP_2, ch + "", rowNum, colNum);
        ;
        StringBuilder sb = new StringBuilder();
        int i = 1;

        sb.append(ch);

        ch = (char)br.read();

        //++
        if (ch == '+')
        {
            sb.append(ch);
            token = new Token(Category.SELF_OP, sb.toString(), rowNum, colNum);
            ch = (char)br.read();
            i++;
        }
        else if (ch == '=')
        {
            //+=
            sb.append(ch);
            token = new Token(Category.ASSIGNMENT_OP, sb.toString(), rowNum, colNum);
            ch = (char)br.read();
            i++;
        }
        else if (!tokenList.get(tokenList.size() - 1).getCategory().equals(Category.INT) &&
            !tokenList.get(tokenList.size() - 1).getCategory().equals(Category.REAL) &&
            !tokenList.get(tokenList.size() - 1).getCategory().equals(")") &&
            !tokenList.get(tokenList.size() - 1).getCategory().equals("]") &&
            !tokenList.get(tokenList.size() - 1).getCategory().equals(Category.IDENTIFIER))
        {
            //在加号前不会为数、），添加一个0
            tokenList.add(new Token(Category.INT, "0", rowNum, colNum));
        }

        boolean illegal = false;

        //如果后面还有运算符，则为非法
        while (isOperator(ch))
        {
            illegal = true;
            sb.append(ch);
            ch = (char)br.read();
            i++;
        }

        //如果后面还有运算符，则为非法
        if (illegal)
        {
            errorTokens.add(new ErrorToken("非法运算符", sb.toString(), rowNum, colNum));
        }
        else
        {
            tokenList.add(token);
        }

        colNum += i;
    }

    //处理减号
    private void handleMinus()
        throws IOException
    {
        //默认 -
        Token token = new Token(Category.BIN_AR_OP_2, ch + "", rowNum, colNum);

        StringBuilder sb = new StringBuilder();
        int i = 1;

        sb.append(ch);

        ch = (char)br.read();

        // --
        if (ch == '-')
        {
            sb.append(ch);
            token = new Token(Category.SELF_OP, sb.toString(), rowNum, colNum);
            ch = (char)br.read();
            i++;
        }
        else if (ch == '=')
        {
            //-=
            sb.append(ch);
            token = new Token(Category.ASSIGNMENT_OP, sb.toString(), rowNum, colNum);
            ch = (char)br.read();
            i++;
        }
        else if (!tokenList.get(tokenList.size() - 1).getCategory().equals(Category.INT) &&
            !tokenList.get(tokenList.size() - 1).getCategory().equals(Category.REAL) &&
            !tokenList.get(tokenList.size() - 1).getCategory().equals(")") &&
            !tokenList.get(tokenList.size() - 1).getCategory().equals("]") &&
            !tokenList.get(tokenList.size() - 1).getCategory().equals(Category.IDENTIFIER))
        {
            //在减号前不会为数，添加一个0
            tokenList.add(new Token(Category.INT, "0", rowNum, colNum));
        }

        boolean illegal = false;
        //如果后面还有运算符，则为非法
        while (isOperator(ch))
        {
            illegal = true;
            sb.append(ch);
            ch = (char)br.read();
            i++;
        }

        //如果后面还有运算符，则为非法
        if (illegal)
        {
            errorTokens.add(new ErrorToken("非法运算符", "", rowNum, colNum));
        }
        else
        {
            tokenList.add(token);
        }

        colNum += i;
    }

    //处理 * % =
    private void handleOperator()
        throws IOException
    {
        Token token = null;
        StringBuilder sb = new StringBuilder();
        sb.append(ch);

        int i = 1;

        switch (ch)
        {
            case '*':
            case '%':
                token = new Token(Category.BIN_AR_OP_1, sb.toString(), rowNum, colNum);
                break;
            case '=':
                token = new Token(Category.ASS_INIT_OP, sb.toString(), rowNum, colNum);
                break;
            default:
                break;
        }
        char oldChar = ch;
        ch = (char)br.read();
        if(oldChar == '='){
            if(ch == '-' || ch == '+'){
                tokenList.add(token);
                colNum += i;
                return;
            }
            else if (ch == '='){
                token.setCategory(Category.LOGIC_OP);
                token.setValue("==");
                ch = (char)br.read();
                tokenList.add(token);
                colNum += i;
                return;
            }
        }

        //默认赋值
        if (ch == '=')
        {
            sb.append(ch);
            ch = (char)br.read();
            i++;
            token = new Token(Category.ASSIGNMENT_OP, sb.toString(), rowNum, colNum);
        }

        boolean illegal = false;

        //如果后面还有运算符，则为非法
        while (isOperator(ch))
        {
            illegal = true;
            sb.append(ch);
            ch = (char)br.read();
            i++;
        }

        //如果后面还有运算符，则为非法
        if (illegal)
        {
            //添加错误的token
            errorTokens.add(new ErrorToken("非法运算符", sb.toString(), rowNum, colNum));
        }
        else
        {
            tokenList.add(token);
        }

        colNum += i;
    }

    //处理除法
    private void handleDiv()
        throws IOException
    {

        Token token = new Token(Category.BIN_AR_OP_1, ch + "", rowNum, colNum);

        ch = (char)br.read();

        if (ch == '=')
        {
            //赋值
            token = new Token(Category.ASSIGNMENT_OP, "/=", rowNum, colNum);

            tokenList.add(token);

            ch = (char)br.read();
            colNum += 2;
        }
        else if (ch == '*')
        {
            StringBuilder sbNotes = new StringBuilder();
            //处理多行注释
            token = new Token(Category.NOTES, "/*", rowNum, colNum);

            sbNotes.append("/*");

            int i = 1;

            do
            {
                ch = (char)br.read();
                i++;
                sbNotes.append(ch);
                if (ch == '*')
                {
                    ch = (char)br.read();
                    sbNotes.append(ch);
                    i++;
                    if (ch == '/')
                    {
                        //多行注释
                        colNum += i;
                        token = new Token(Category.NOTES, sbNotes.toString(), rowNum, colNum);
                        tokenList.add(token);
                        ch = (char)br.read();
                        return;
                    }
                }
                //回车符
                if (isEnter(ch))
                {
                    ch = (char)br.read();
                    rowNum++;
                    colNum = 0;
                    i = 0;
                }
            } while (!isEOF(ch));

            //错误处理
            errorTokens.add(new ErrorToken("多行注释没有结束", sbNotes.toString(), rowNum, colNum));
        }
        else if (ch == '/')
        {
            //单行注释
            token = new Token(Category.NOTES, "//", rowNum, colNum);

            br.readLine();
            ch = (char)br.read();
            rowNum++;
            colNum = 1;
        }
        else
        {
            //除法
            tokenList.add(token);
            colNum++;
        }
    }

    //处理大于 >
    private void handleGreater()
        throws IOException
    {
        //默认 >
        Token token = new Token(Category.LOGIC_OP, ch + "", rowNum, colNum);

        ch = (char)br.read();

        switch (ch)
        {
            case '>':
                token.setCategory(Category.BIT_OP);
                token.setValue(">>");
                tokenList.add(token);
                ch = (char)br.read();
                colNum += 2;
                break;
            case '=':
                String s = ">" + ch;
                token.setValue(s);
                tokenList.add(token);
                ch = (char)br.read();
                colNum += 2;
                break;
            default:
                tokenList.add(token);
                colNum ++;
                break;
        }
    }

    //处理小于 <
    private void handleLess()
        throws IOException
    {
        //默认 <
        Token token = new Token(Category.LOGIC_OP, ch + "", rowNum, colNum);

        ch = (char)br.read();

        switch (ch)
        {
            case '<':
                token.setCategory(Category.BIT_OP);
                token.setValue("<<");
                tokenList.add(token);
                ch = (char)br.read();
                colNum += 2;
                break;
            case '=':
                String s = "<" + ch;
                token.setValue(s);
                tokenList.add(token);
                ch = (char)br.read();
                colNum += 2;
                break;
            case '>':
                String s1 = "<" + ch;
                token.setValue(s1);
                tokenList.add(token);
                ch = (char)br.read();
                colNum += 2;
                break;
            default:
                tokenList.add(token);
                colNum ++;
                break;
        }
    }

    //保存结果
    public boolean saveJson()
    {
        Gson gson = new Gson();
        resultJson = gson.toJson(tokenList);

        BufferedWriter writer = null;
        File file = new File("result.json");
        //如果文件不存在，则新建一个
        if (!file.exists())
        {
            try
            {
                file.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        //写入
        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8"));
            writer.write(resultJson);
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (writer != null)
                {
                    writer.close();
                }
                return false;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return false;
    }

    public ArrayList<ErrorToken> getErrorTokens()
    {
        return errorTokens;
    }

    public static void main(String[] args)
        throws IOException
    {
        Lexer lexer = new Lexer();
        lexer.lexing("test.c");
    }
}
