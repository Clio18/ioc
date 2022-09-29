package com.obolonyk.ioc.util;

public class ObjectRetriever {
    private static final String SHORT = "short";
    private static final String LONG = "long";
    private static final String FLOAT = "float";
    private static final String DOUBLE = "double";
    private static final String BYTE = "byte";
    private static final String BOOLEAN = "boolean";
    private static final String INT = "int";
    private static final String STRING = "java.lang.String";

    public static Object getObjectForSetter(String propertyValue, Class<?> type) {
        Object setValue = null;
        if (type.getName().equals(INT)) {
            setValue = Integer.valueOf(propertyValue);
        }
        if (type.getName().equals(BOOLEAN)) {
            setValue = Boolean.valueOf(propertyValue);
        }
        if (type.getName().equals(BYTE)) {
            setValue = Byte.valueOf(propertyValue);
        }
        if (type.getName().equals(DOUBLE)) {
            setValue = Double.valueOf(propertyValue);
        }
        if (type.getName().equals(FLOAT)) {
            setValue = Float.parseFloat(propertyValue);
        }
        if (type.getName().equals(LONG)) {
            setValue = Long.parseLong(propertyValue);
        }
        if (type.getName().equals(SHORT)) {
            setValue = Short.parseShort(propertyValue);
        }
        if (type.getName().equals(STRING)) {
            setValue = propertyValue;
        }
        return setValue;
    }
}
