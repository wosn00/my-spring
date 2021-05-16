package com.hs.example;

import com.hs.example.config.Config;
import com.hs.example.service.TestServiceA;
import com.hs.example.service.TestServiceC;
import com.hs.my.spring.context.AnnotationConfigApplicationContext;

public class ApplicationTest {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(Config.class);

        TestServiceA testServiceA = (TestServiceA) applicationContext.getBean("testServiceA");
//        TestServiceA testServiceA1 = (TestServiceA) applicationContext.getBean("testServiceA");
//        System.out.println(testServiceA);
//        System.out.println(testServiceA1);

//        TestServiceB testServiceB = (TestServiceB) applicationContext.getBean("testServiceB");

        testServiceA.testC();

        TestServiceC testServiceC = (TestServiceC) applicationContext.getBean("testServiceC");
        testServiceC.testA();

//        testServiceB.testB();
    }
}
