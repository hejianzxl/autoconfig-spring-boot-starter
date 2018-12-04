package com.autoConfig.client.propertyeditor;


public class PropertyEditor {
    
    /**
     * 基本数据类型转化
     * @param clazz
     * @param value
     * @return
     */
    public static Object primitiveTypeConvert(Class<?> clazz, String value) {
        if (clazz == long.class) {
        	if("null".equalsIgnoreCase(value)) value="0";
            return Long.parseLong(value);
        }
        if (clazz == int.class) {
        	if("null".equalsIgnoreCase(value)) value="0";
            return Integer.parseInt(value);
        }
        if (clazz == double.class) {
        	if("null".equalsIgnoreCase(value)) value="0";
            return Double.parseDouble(value);
        }
        if (clazz == float.class) {
        	if("null".equalsIgnoreCase(value)) value="0";
            return Float.parseFloat(value);
        }
        if (clazz == byte.class) {
            return Byte.parseByte(value);
        }
        if (clazz == short.class) {
            return Short.parseShort(value);
        }
        if (clazz == boolean.class) {
            return Boolean.parseBoolean(value);
        }
        if (clazz == char.class) {
            return value.charAt(0);
        }
        return value;
    }
}
