package com.hs.my.spring.ext;

/**
 * @author hs
 */
public class BeanDefinition {
    private Class<?> aClass;
    private String scope;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Class<?> getaClass() {
        return aClass;
    }

    public void setaClass(Class<?> aClass) {
        this.aClass = aClass;
    }
}
