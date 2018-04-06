package org.hobbit.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

/**
 * @author Roman Katerinenko
 */
public class KeyValue implements Map{
    private Map keyValueMap = new LinkedHashMap<>();

    public KeyValue(){

    }

    public KeyValue(Map from){
        keyValueMap = from;
    }

    public String getStringValueFor(String propertyName) throws Exception {
        checkExistence(propertyName);
        Object value = keyValueMap.get(propertyName);
        if (JsonPrimitive.class.isInstance(value))
            return ((JsonPrimitive)value).getAsString();

        return (String) keyValueMap.get(propertyName);
    }

    public Object getValue(String propertyName) throws Exception {
        checkExistence(propertyName);
        return keyValueMap.get(propertyName);
    }

    public int getIntValueFor(String propertyName) throws Exception {
        checkExistence(propertyName);

        Object value = keyValueMap.get(propertyName);
        if (JsonPrimitive.class.isInstance(value))
            return ((JsonPrimitive)value).getAsInt();

        return (Integer) keyValueMap.get(propertyName);
    }

    public long getLongValueFor(String propertyName) throws Exception {
        checkExistence(propertyName);

        Object value = keyValueMap.get(propertyName);
        if (JsonPrimitive.class.isInstance(value))
            return ((JsonPrimitive)value).getAsLong();

        return (long) keyValueMap.get(propertyName);
    }

    public Double getDoubleValueFor(String propertyName) throws Exception {
        checkExistence(propertyName);

        Object value = keyValueMap.get(propertyName);
        if (JsonPrimitive.class.isInstance(value))
            return ((JsonPrimitive)value).getAsDouble();

        return (Double) keyValueMap.get(propertyName);
    }

    public void setValue(String propertyName, String value){
        keyValueMap.put(propertyName, value);
    }

    public void setValue(String propertyName, int value){
        keyValueMap.put(propertyName, value);
    }

    public void setValue(String propertyName, double value) {
        keyValueMap.put(propertyName, value);
    }

    public void setValue(String propertyName, long value) {
        keyValueMap.put(propertyName, value);
    }

    public void setValue(String propertyName, Object value) {
        keyValueMap.put(propertyName, value);
    }

    public void add(KeyValue keyValue) {
        keyValueMap.putAll(keyValue.keyValueMap);
    }

    public Set<Map.Entry<Object, Object>> getEntries() {
        return keyValueMap.entrySet();
    }

    public Map<Object, Object> toMap(){ return keyValueMap; }

    private void checkExistence(String propertyName) throws Exception {
        if(!keyValueMap.containsKey(propertyName))
            throw new Exception(propertyName+" not exists in data");
    }

    @Override
    public String toString() {
        return keyValueMap.toString();
    }

    public String toJSONString(){
        JSONObject obj = new JSONObject(keyValueMap);
        return obj.toJSONString();
    }

    @Override
    public int size() {
        return keyValueMap.size();
    }

    @Override
    public boolean isEmpty() {
        return keyValueMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return keyValueMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return keyValueMap.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return keyValueMap.get(key);
    }

    @Override
    public Object put(Object key, Object value) {
        return keyValueMap.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return keyValueMap.remove(key);
    }

    @Override
    public void putAll(Map m) {
        keyValueMap.putAll(m);
    }

    @Override
    public void clear() {
        keyValueMap.clear();
    }

    @Override
    public Set keySet() {
        return keyValueMap.keySet();
    }

    @Override
    public Collection values() {
        return keyValueMap.values();
    }

    @Override
    public Set<Entry> entrySet() {
        return keyValueMap.entrySet();
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

    public static KeyValue parseFromString(String input) throws JsonProcessingException {
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(input);
        return parseFromObject(jsonElement.getAsJsonObject());
    }

    private static KeyValue parseFromObject(JsonObject input) throws JsonProcessingException {
        KeyValue ret = new KeyValue();

        for(Map.Entry entry : input.entrySet()){
            Object value = entry.getValue();

            if(JsonArray.class.isInstance(value))
                value = parseFromJsonArray((JsonArray)value);

            if(JsonObject.class.isInstance(value))
                ret.setValue((String)entry.getKey(), parseFromObject((JsonObject)value));
            else
                ret.setValue((String)entry.getKey(), value);
        }


        return ret;
    }

    private static Object parseFromJsonArray(JsonArray array) throws JsonProcessingException {
        List<Object> ret = new ArrayList<>();
        for (Object obj : array) {
            Object value = obj;
            if(JsonArray.class.isInstance(value))
                value = parseFromJsonArray((JsonArray)obj);

            if(JsonObject.class.isInstance(value))
                    ret.add(parseFromObject((JsonObject)value));
            else
                ret.add(value);
        }
        return ret.toArray();
    }
}
