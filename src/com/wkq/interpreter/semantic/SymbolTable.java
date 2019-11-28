package com.wkq.interpreter.semantic;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author: wkq
 * @date: 2019/11/6 19:10
 */
public class SymbolTable {
    private Map<String, Symbol> symbolMap;

    public SymbolTable() {
        symbolMap = new HashMap<>();
    }

    //添加
    public void insert(Symbol symbol) {
        symbolMap.put(symbol.getName(), symbol);
    }

    //查找
    public Symbol query(String symbol) {
        return symbolMap.get(symbol);
    }

    //删除
    public void delete(String symbol) {
        symbolMap.remove(symbol);
    }

    //更新
    public void update(int level){
        List<String> neededSymbol = new LinkedList<>();
        //找出需要去除的符号
        for(String key : symbolMap.keySet()){
            if(symbolMap.get(key).getLevel() > level){
                neededSymbol.add(key);
            }
        }

        //去除
        for(int i = 0; i < neededSymbol.size(); i++){
            symbolMap.remove(neededSymbol.get(i));
        }
    }
}
