package com.hs.example.service;

import com.hs.my.spring.annotation.Autowired;
import com.hs.my.spring.annotation.Component;
import com.hs.my.spring.annotation.Scope;

/**
 * @author hs
 */
@Component
//@Scope("prototype")
public class TestServiceA {

    @Autowired
    private TestServiceC testServiceC;

    public void testC() {
        testServiceC.testC();
    }
}
