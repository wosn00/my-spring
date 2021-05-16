package com.hs.my.spring.context;

import com.hs.my.spring.annotation.Autowired;
import com.hs.my.spring.annotation.Component;
import com.hs.my.spring.annotation.ComponentScan;
import com.hs.my.spring.annotation.Scope;
import com.hs.my.spring.ext.BeanDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author hs
 */
public class AnnotationConfigApplicationContext implements Context {
    private static final Logger logger = LoggerFactory.getLogger(AnnotationConfigApplicationContext.class);

    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private Map<String, Object> earlyObjects = new ConcurrentHashMap<>();
    private Set<String> creatingBean = new CopyOnWriteArraySet<>();
    private static final String SINGLETON = "singleton";

    public AnnotationConfigApplicationContext(Class<?> config) {
        // 扫描并加载类，注册到beanDefinition
        scanAndLoad(config);
        // 根据beanDefinition创建bean，注入依赖属性
        refresh();
        // BeanPostProcess
        // initMethod
        // ...
    }

    @Override
    public synchronized Object getBean(String beanName) {
        // 首字母转大写
        beanName = beanName.substring(0, 1).toUpperCase() + beanName.substring(1);

        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new NullPointerException("No BeanDefinition found");
        }
        Object object = getSingleton(beanName);
        if (object == null) {
            throw new NullPointerException("No Bean found");
        }
        return object;

    }

    @Override
    public Map<String, BeanDefinition> getBeanDefinitionMap() {
        return beanDefinitionMap;
    }

    /**
     * 扫描->加载->注册到BeanDefinition
     */
    @Override
    public synchronized void scanAndLoad(Class<?> config) {

        if (config.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScan = config.getDeclaredAnnotation(ComponentScan.class);
            String[] scanPath = componentScan.value();
            if (scanPath.length == 0) {
                return;
            }
            for (String path : scanPath) {
                ClassLoader classLoader = AnnotationConfigApplicationContext.class.getClassLoader();
                path = path.replace(".", "/");
                URL resource = classLoader.getResource(path);
                File file = new File(resource.getFile());
                if (!file.isDirectory()) {
                    logger.warn("The component scanPath is not a dir!");
                }
                File[] files = file.listFiles();
                if (files == null) {
                    logger.warn("There is no class exist!");
                    return;
                }
                for (File classFile : files) {
                    String absolutePath = classFile.getAbsolutePath();
                    String className = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.lastIndexOf(".class"));
                    className = className.replace("\\", ".");
                    Class<?> loadClass;
                    try {
                        loadClass = classLoader.loadClass(className);
                    } catch (ClassNotFoundException e) {
                        logger.error("Failed to load the scanned class", e);
                        continue;
                    }
                    // 注册到beanDefinition
                    registry(loadClass);

                }
            }
        }
    }

    /**
     * 注册到BeanDefinition
     */
    private void registry(Class<?> loadClass) {
        if (loadClass.isAnnotationPresent(Component.class)) {
            Component annotation = loadClass.getAnnotation(Component.class);
            String value = annotation.value();
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setaClass(loadClass);
            Scope scopeAnnotation = loadClass.getAnnotation(Scope.class);
            String scope = null;
            if (scopeAnnotation != null) {
                scope = scopeAnnotation.value();
            } else {
                scope = SINGLETON;
            }
            beanDefinition.setScope(scope);
            if (value.length() != 0) {
                beanDefinitionMap.put(value, beanDefinition);
            } else {
                beanDefinitionMap.put(loadClass.getSimpleName(), beanDefinition);
            }
        }

    }

    /**
     * 创建Bean到单例池中
     */
    private synchronized void refresh() {
        if (beanDefinitionMap.isEmpty()) {
            logger.warn("No beanDefinition found!");
        }
        Set<String> beanNames = beanDefinitionMap.keySet();
        for (String beanName : beanNames) {
            createBean(beanName);
        }

    }

    /**
     * 创建bean，注入属性
     */
    private Object createBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        Class<?> aClass = beanDefinition.getaClass();
        String scope = beanDefinition.getScope();
        try {
            if (scope.equals(SINGLETON) && singletonObjects.get(beanName) != null) {
                return singletonObjects.get(beanName);
            }
            Object instance = aClass.getDeclaredConstructor().newInstance();
            earlyObjects.put(beanName, instance);
            creatingBean.add(beanName);

            Field[] declaredFields = aClass.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                if (declaredField.isAnnotationPresent(Autowired.class)) {
                    String fieldName = declaredField.getType().getSimpleName();
                    Object fieldInstance = getSingleton(fieldName);
                    declaredField.setAccessible(true);
                    declaredField.set(instance, fieldInstance);

                }
            }
            if (SINGLETON.equals(scope)) {
                singletonObjects.put(beanName, instance);
            }
            creatingBean.remove(beanName);
            return instance;

        } catch (Exception e) {
            logger.error("Failed to create Bean , beanName:{}", beanName, e);
        }
        return null;
    }

    /**
     * 从单例池获取bean，或创建bean，解决循环依赖（不包括有AOP的循环依赖）
     */
    private Object getSingleton(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        String scope = beanDefinition.getScope();
        if (SINGLETON.equals(scope)) {
            // 单例模式
            Object o = singletonObjects.get(beanName);
            if (o != null) {
                return o;
            }
            if (creatingBean.contains(beanName)) {
                //出现循环依赖，则先赋予初始引用
                return earlyObjects.get(beanName);
            }
            //还未创建，则递归创建bean
            return createBean(beanName);
        } else {
            // 原型模式
            return createBean(beanName);
        }
    }
}
