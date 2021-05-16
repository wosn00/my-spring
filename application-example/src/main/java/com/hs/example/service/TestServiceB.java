package com.hs.example.service;

import com.hs.my.spring.annotation.Autowired;
import com.hs.my.spring.annotation.Component;

@Component
public class TestServiceB {

    @Autowired
    private TestServiceC testServiceC;

    public void testC() {
        testServiceC.testC();
    }
}
