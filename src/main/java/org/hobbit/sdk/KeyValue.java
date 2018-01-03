package org.hobbit.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.util.JSON;
import jdk.nashorn.internal.objects.Global;
import jdk.nashorn.internal.parser.JSONParser;
import jdk.nashorn.internal.runtime.Context;
import org.json.simple.JSONObject;

import java.util.*;

/**
 * @author Roman Katerinenko
 */
public class KeyValue {
    private final Map<String, Object> keyValueMap = new LinkedHashMap<>();

    public String getStringValueFor(String propertyName) throws Exception {
        checkExistence(propertyName);
        return (String) keyValueMap.get(propertyName);
    }

    public Object getValue(String propertyName) throws Exception {
        checkExistence(propertyName);
        return keyValueMap.get(propertyName);
    }

    public int getIntValueFor(String propertyName) throws Exception {
        checkExistence(propertyName);
        return (Integer) keyValueMap.get(propertyName);
    }

    public Double getDoubleValueFor(String propertyName) throws Exception {
        checkExistence(propertyName);
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

//    public static KeyValue parseFromString(String input) throws JsonProcessingException {
//        KeyValue ret = new KeyValue();
//        //JSONParser jsonParser = new JSONParser();
//
////        ObjectMapper mapper = new ObjectMapper();
////        String jsonString = mapper.writeValueAsString(input);
////        Object obj = JSON.parse(jsonString);
//
//        JsonParser jsonParser = new JsonParser();
//        JsonElement jsonElement = jsonParser.parse(input);
//        for(Map.Entry entry : jsonElement.getAsJsonObject().entrySet()){
//            Object value = entry.getValue();
//            ret.setValue((String)entry.getKey(), value);
//        }
//
//
//        return ret;
//    }
}
