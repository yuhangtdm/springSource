package com.dity.service.impl;

import com.dity.annotation.Service;
import com.dity.service.HelloService;

/**
 * @author:yuhang
 * @Date:2018/4/22
 */
@Service
public class HelloServiceImpl implements HelloService {

    public String hello(){
        return "hello";
    }
}
