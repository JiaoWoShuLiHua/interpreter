package com.wkq.interpreter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author: wkq
 * @date: 2019/11/2 13:44
 */
public class test
{
    public static void main(String[] args)
    {
        Map<Integer, Set<String>> testMap = new HashMap<>();

        for(int i = 0; i < 5; i++)
        {
            testMap.put(i, new HashSet<>());
        }

        Set<String> str =  testMap.get(4);
        str.add("test1");
        str.add("test2");

        System.out.println(testMap);
    }
}
