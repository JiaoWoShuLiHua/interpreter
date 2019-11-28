package com.wkq.interpreter.parser;

import com.wkq.interpreter.lexer.entity.Token;

/**
 * @author: wkq
 * @date: 2019/10/31 10:33
 */
public class TreeNode
{
    public String cate = null;

    public TreeNode[] next = null;

    public TreeNode pre = null;

    //叶子结点会有一个token属性
    public Token token = null;

    public TreeNode(TreeNode pre, String cate)
    {
        this.pre = pre;
        this.cate = cate;
    }

    /**
     * 寻找与当前结点相邻的下一个结点
     * 如果没有相邻，则返回父节点的相邻结点
     *
     * @return
     */
    public TreeNode findNext()
    {
        int index = 0;
        //父节点
        TreeNode parent = this.pre;

        for (int i = 0; i < parent.next.length; i++)
        {
            if (parent.next[i] == this)
            {
                index = i + 1;
                break;
            }
        }

        if (index >= parent.next.length)
        {
            //如果 parent 是根节点
            if (parent.pre == null)
            {
                return parent;
            }
            else
            {
                return parent.findNext();
            }
        }
        else
        {
            return parent.next[index];
        }
    }

    /**
     * 寻找右兄弟结点，没有则为空
     *
     * @return
     */
    public TreeNode findRight()
    {
        int index = 0;
        //父节点
        TreeNode parent = this.pre;

        for (int i = 0; i < parent.next.length; i++)
        {
            if (parent.next[i] == this)
            {
                index = i + 1;
                break;
            }
        }

        if (index >= parent.next.length)
        {
            return null;
        }
        else
        {
            return parent.next[index];
        }
    }

    /**
     * 寻找左兄弟结点，没有则为空
     *
     * @return
     */
    public TreeNode findLeft()
    {
        int index = 0;
        //父节点
        TreeNode parent = this.pre;

        for (int i = 0; i < parent.next.length; i++)
        {
            if (parent.next[i] == this)
            {
                index = i - 1;
                break;
            }
        }

        if (index < 0)
        {
            return null;
        }
        else
        {
            return parent.next[index];
        }
    }
}
