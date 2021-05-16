package com.hs.my.spring.context;

import com.hs.my.spring.ext.BeanDefinition;

import java.util.Map;

/**
 * @author hs
 */
public interface Context {

    Object getBean(String beanName);

    Map<String, BeanDefinition> getBeanDefinitionMap();

    void scanAndLoad(Class<?> config);
}
