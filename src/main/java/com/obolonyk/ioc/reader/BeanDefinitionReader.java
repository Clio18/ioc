package com.obolonyk.ioc.reader;


import com.obolonyk.ioc.entity.BeanDefinition;

import java.util.Map;

public interface BeanDefinitionReader {
    Map<String, BeanDefinition> getBeanDefinition();
}
