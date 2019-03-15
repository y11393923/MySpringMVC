package com.zyy.controller;

import com.zyy.annotation.Controller;
import com.zyy.annotation.RequestMapping;
import com.zyy.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Administrator
 * @create 2019/3/14
 * @since 1.0.0
 */
@Controller
@RequestMapping("/zyy")
public class TestController {
    @RequestMapping("/test1")
    public void test1(HttpServletResponse response, @RequestParam("param")String param){
        try {
            response.getWriter().write("method success! param:"+param);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/test2")
    public void test2(HttpServletResponse response){
        try {
            response.getWriter().write("method success! ");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
