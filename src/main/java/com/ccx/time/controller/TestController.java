package com.ccx.time.controller;

import com.ccx.time.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class TestController {

    @Autowired
    private UserMapper mapper;

    @RequestMapping("/hello")
    public String hello() {
        return "hello world";
    }


    @RequestMapping("/select")
    public List doSome() {
        return mapper.selectByExample(null);
    }
}
