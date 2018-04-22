package com.dity.controller;

import java.lang.reflect.Field;

/**
 * @author:yuhang
 * @Date:2018/4/22
 */
public class Test {
    public static void main(String[] args) {
        HelloController helloController=new HelloController();
        Field[] fields = helloController.getClass().getDeclaredFields();
        for(Field field:fields){
            System.out.println(field.getName());
        }


    }
}
