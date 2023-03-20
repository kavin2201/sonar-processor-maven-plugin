package com.sonarprocessor.models;

import java.util.HashMap;
import java.util.Map;

/** S109ProcessModel */
public class S109ProcessModel {

    private static Map<Object, String> singletonMap = new HashMap<>();

    private static String constantFileName;

    /**
     * getConstantFileName
     *
     * @return String
     */
    public static String getConstantFileName() {
        return constantFileName;
    }

    /**
     * setConstantFileName
     *
     * @param fileName {String}
     */
    public static void setConstantFileName(String fileName) {
        constantFileName = fileName;
    }

    /**
     * getMap
     *
     * @return Map<Object, String>
     */
    public static Map<Object, String> getMap() {
        return new HashMap<>(singletonMap);
    }

    /**
     * put
     *
     * @param key {Object}
     * @param constantName {String}
     */
    public static void put(Object key, String constantName) {
        singletonMap.put(key, constantName);
    }

    /**
     * get
     *
     * @param key {Object}
     * @return String
     */
    public static String get(Object key) {
        return singletonMap.get(key);
    }

    /**
     * containsKey
     *
     * @param key {Object}
     * @return boolean
     */
    public static boolean containsKey(Object key) {
        return singletonMap.containsKey(key);
    }
}
