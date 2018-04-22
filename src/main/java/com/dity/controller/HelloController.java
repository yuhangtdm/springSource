package com.dity.controller;

import com.dity.annotation.Autowired;
import com.dity.annotation.Controller;
import com.dity.annotation.RequestMapping;
import com.dity.service.HelloService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author:yuhang
 * @Date:2018/4/22
 */
@Controller
@RequestMapping("/hello")
public class HelloController {

    @Autowired
    private HelloService helloServiceImpl;

    @RequestMapping("/say")
    public void say(HttpServletRequest request, HttpServletResponse response){
        response.setCharacterEncoding("utf8");
        try {
            request.setCharacterEncoding("utf8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String name = request.getParameter("name");
        String str=helloServiceImpl.hello();
        try {
            response.getWriter().println(name+str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
