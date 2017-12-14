package org.hobbit.sdk;

import java.util.*;

/**
 * @author Roman Katerinenko
 */
public class KeyValue {
    private final Map<String, Object> keyValueMap = new LinkedHashMap<>();

    public String getStringValueFor(String propertyName) {
        return (String) keyValueMap.get(propertyName);
    }

    public Object getValue(String propertyName) {
        return keyValueMap.get(propertyName);
    }

    public int getIntValueFor(String propertyName) {
        return (Integer) keyValueMap.get(propertyName);
    }

    public Double getDoubleValueFor(String propertyName) {
        return (Double) keyValueMap.get(propertyName);
    }

    public void setValue(String propertyName, String value) {
        keyValueMap.put(propertyName, value);
    }

    public void setValue(String propertyName, int value) {
        keyValueMap.put(propertyName, value);
    }

    public void setValue(String propertyName, double value) {
        keyValueMap.put(propertyName, value);
    }

    public void setValue(String propertyName, Object value) {
        keyValueMap.put(propertyName, value);
    }

    public void add(KeyValue keyValue) {
        keyValueMap.putAll(keyValue.keyValueMap);
    }

    public Set<Map.Entry<String, Object>> getEntries() {
        return keyValueMap.entrySet();
    }

    public Map<String, Object> toMap(){ return keyValueMap; }

    @Override
    public String toString() {
        return keyValueMap.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyValue keyValue = (KeyValue) o;
        return keyValueMap.equals(keyValue.keyValueMap);

    }

    @Override
    public int hashCode() {
        return keyValueMap.hashCode();
    }


}
