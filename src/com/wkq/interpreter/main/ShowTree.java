package com.wkq.interpreter.main;

import com.wkq.interpreter.parser.TreeNode;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

/**
 * @author: wkq
 * @date: 2019/11/4 18:17
 */
public class ShowTree
{
    private JFrame jf = null;
    private JPanel panel = null;

    private  JLabel pathLabel = new JLabel("path: ");
    public ShowTree(){
        jf = new JFrame("树");
        jf.setSize(300, 300);
        jf.setLocationRelativeTo(null);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        panel = new JPanel(new BorderLayout());
    }

    private String getPathStr(String path){
        return path;
    }

    public boolean create(TreeNode header){
        DefaultMutableTreeNode rootNode = createTree(header);

        if(rootNode == null){
            return false;
        }

        // 使用根节点创建树组件
        JTree tree = new JTree(rootNode);

        // 设置树显示根节点句柄
        tree.setShowsRootHandles(true);

        // 设置树节点可编辑
        tree.setEditable(true);

        // 设置节点选中监听器
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
//                System.out.println("当前被选中的节点: " + e.getPath());
                pathLabel.setText("path: " + e.getPath().toString());
            }
        });

        // 创建滚动面板，包裹树（因为树节点展开后可能需要很大的空间来显示，所以需要用一个滚动面板来包裹）
        JScrollPane scrollPane = new JScrollPane(tree);

        // 添加滚动面板到那内容面板

        pathLabel.setSize(300, 30);
        panel.setBackground(Color.decode("#e3e3e3"));
        scrollPane.setBackground(Color.decode("#e3e3e3"));
        panel.add(pathLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        // 设置窗口内容面板并显示
        jf.setContentPane(panel);
        jf.setVisible(true);


        return true;
    }

//    private void expandAll(JTree tree, TreePath parent, boolean expand)
//    {
//        TreeNode node = (TreeNode) parent.getLastPathComponent();
//        if (node.getChildCount() >= 0)
//        {
//            for (Enumeration e = node.children(); e.hasMoreElements();)
//            {
//                TreeNode n = (TreeNode) e.nextElement();
//                TreePath path = parent.pathByAddingChild(n);
//                expandAll(tree, path, expand);
//            }
//        }
//        if (expand)
//        {
//            tree.expandPath(parent);
//        } else
//        {
//            tree.collapsePath(parent);
//        }
//    }

    public DefaultMutableTreeNode createTree(TreeNode header){
        if(header.next[0].cate == "program"){
            // 创建根节点
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(header.next[0].cate);
            createChild(header.next[0].next, rootNode);
            return rootNode;
        }
        else {
            return null;
        }
    }

    //递归
    private void createChild(TreeNode [] childList, DefaultMutableTreeNode parentNode){
        for(TreeNode child: childList){
            DefaultMutableTreeNode childTree = new DefaultMutableTreeNode(child.cate);
            if(child.next == null && child.token != null && child.cate != "&"){
                childTree.add(new DefaultMutableTreeNode(child.token.getValue()));
                parentNode.add(childTree);
                continue;
            }
            //加入当前子节点到父节点中
            parentNode.add(childTree);
            if(child.next != null){
                //子节点有孩子，递归
                createChild(child.next, childTree);
            }
        }
    }
}
