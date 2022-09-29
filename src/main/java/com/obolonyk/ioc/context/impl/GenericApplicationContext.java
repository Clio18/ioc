package com.obolonyk.ioc.context.impl;

import com.obolonyk.ioc.context.ApplicationContext;
import com.obolonyk.ioc.entity.Bean;
import com.obolonyk.ioc.entity.BeanDefinition;
import com.obolonyk.ioc.exception.BeanInstantiationException;
import com.obolonyk.ioc.exception.NoSuchBeanDefinitionException;
import com.obolonyk.ioc.exception.NoUniqueBeanOfTypeException;
import com.obolonyk.ioc.reader.BeanDefinitionReader;
import com.obolonyk.ioc.reader.sax.XmlBeanDefinitionReader;
import com.obolonyk.ioc.util.ObjectRetriever;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

public class GenericApplicationContext implements ApplicationContext {

    private Map<String, Bean> beans;

    GenericApplicationContext() {
    }

    public GenericApplicationContext(String... paths) {
        this(new XmlBeanDefinitionReader(paths));
    }

    public GenericApplicationContext(BeanDefinitionReader definitionReader) {
        Map<String, BeanDefinition> beanDefinitions = definitionReader.getBeanDefinition();
        beans = createBeans(beanDefinitions);
        injectValueDependencies(beanDefinitions, beans);
        injectRefDependencies(beanDefinitions, beans);
    }

    @Override
    public Object getBean(String beanId) {
        return beans.get(beanId).getValue();
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        Collection<Bean> values = beans.values();
        List<Object> objects = new ArrayList<>();
        for (Bean bean : values) {
            Object beanValue = bean.getValue();
            if (beanValue.getClass().equals(clazz)) {
                objects.add(beanValue);
            }
        }
        if (objects.size() > 1) {
            throw new NoUniqueBeanOfTypeException("There is no unique bean for " + clazz);
        }
        return (T) objects.get(0);
    }

    @Override
    public <T> T getBean(String id, Class<T> clazz) {
        Object bean = getBean(id);
        Class<?> beanClass = bean.getClass();
        if (!beanClass.isAssignableFrom(clazz)) {
            throw new NoSuchBeanDefinitionException(id, clazz.getName(), beanClass.getName());
        }
        return (T) bean;
    }

    @Override
    public List<String> getBeanNames() {
        return beans.keySet().stream().collect(Collectors.toList());
    }

    Map<String, Bean> createBeans(Map<String, BeanDefinition> beanDefinitionMap) {
        Map<String, Bean> beansMap = new HashMap<>();
        Set<String> keySet = beanDefinitionMap.keySet();
        for (String key : keySet) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(key);
            String className = beanDefinition.getClassName();
            String id = beanDefinition.getId();
            try {
                Object instance = Class.forName(className).getConstructor().newInstance();
                Bean bean = new Bean(id, instance);
                beansMap.put(id, bean);
            } catch (ReflectiveOperationException e) {
                throw new BeanInstantiationException("Bean" + id + "was not instantiated: ", e);
            }
        }
        return beansMap;
    }

    void injectValueDependencies(Map<String, BeanDefinition> beanDefinitions, Map<String, Bean> beans) {
        Set<String> keys = beanDefinitions.keySet();
        for (String key : keys) {
            BeanDefinition beanDefinition = beanDefinitions.get(key);
            Map<String, String> valueDependencies = beanDefinition.getValueDependencies();
            Bean bean = beans.get(key);
            Object object = bean.getValue();
            Class<?> aClass = object.getClass();

            Set<String> fieldNames = valueDependencies.keySet();

            for (String fieldName : fieldNames) {
                String setterName = getSetterName(fieldName);
                try {
                    Field field = aClass.getDeclaredField(fieldName);
                    Class<?> type = field.getType();
                    String value = valueDependencies.get(fieldName);

                    Method method = aClass.getMethod(setterName, type);

                    injectValue(object, method, value);

                } catch (ReflectiveOperationException e) {
                    throw new BeanInstantiationException("Value dependency " + fieldName +
                            "was not injected to " + bean.getId(), e);
                }
            }
        }
    }

    void injectRefDependencies(Map<String, BeanDefinition> beanDefinitions, Map<String, Bean> beans) {
        Set<String> keys = beanDefinitions.keySet();
        for (String key : keys) {
            BeanDefinition beanDefinition = beanDefinitions.get(key);
            Map<String, String> refDependencies = beanDefinition.getRefDependencies();

            Bean bean = beans.get(key);
            Object object = bean.getValue();
            Class<?> aClass = object.getClass();

            Set<String> fieldNames = refDependencies.keySet();

            for (String fieldName : fieldNames) {
                String setterName = getSetterName(fieldName);
                try {
                    Field field = aClass.getDeclaredField(fieldName);
                    Class<?> type = field.getType();
                    String value = refDependencies.get(fieldName);
                    Method method = aClass.getMethod(setterName, type);

                    Bean beanForInjection = beans.get(value);
                    method.invoke(object, beanForInjection.getValue());
                } catch (ReflectiveOperationException e) {
                    throw new BeanInstantiationException("Ref dependency " + fieldName +
                            "was not injected to " + bean.getId(), e);
                }
            }
        }

    }

    private String getSetterName(String fieldName) {
        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    void injectValue(Object object, Method classMethod, String propertyValue) throws ReflectiveOperationException {
        Parameter[] parameters = classMethod.getParameters();
        Parameter parameter = parameters[0];
        Class<?> type = parameter.getType();
        Object setValue = ObjectRetriever.getObjectForSetter(propertyValue, type);
        classMethod.invoke(object, setValue);
    }

    void setBeans(Map<String, Bean> beans) {
        this.beans = beans;
    }
}
