package com.hs.example.service;

import com.hs.my.spring.annotation.Autowired;
import com.hs.my.spring.annotation.Component;
import com.hs.my.spring.annotation.Scope;

@Component
//@Scope("prototype")
public class TestServiceC {

    @Autowired
    private TestServiceA testServiceA;

    public void testC() {
        System.out.println("This is C ,  " + this.toString());
    }

    public void testA(){
        testServiceA.testC();
    }
}
