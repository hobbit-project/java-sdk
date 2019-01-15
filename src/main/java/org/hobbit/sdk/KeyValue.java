//package org.hobbit.sdk;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.google.gson.*;
//import org.json.simple.JSONObject;
//
//import java.util.*;
//
///**
// * @author Roman Katerinenko
// */
//public class KeyValue implements Map{
//    private Map map = new LinkedHashMap<>();
//
//    public KeyValue(){
//
//    }
//
//    public KeyValue(Map from){
//        map = from;
//    }
//
//    public String getStringValueFor(String propertyName) throws Exception {
//        checkExistence(propertyName);
//        Object value = map.get(propertyName);
//        if (JsonPrimitive.class.isInstance(value))
//            return ((JsonPrimitive)value).getAsString();
//
//        return (String) map.get(propertyName);
//    }
//
//    public Object getValue(String propertyName) throws Exception {
//        checkExistence(propertyName);
//        return map.get(propertyName);
//    }
//
//    public int getIntValueFor(String propertyName) throws Exception {
//        checkExistence(propertyName);
//
//        Object value = map.get(propertyName);
//        if (JsonPrimitive.class.isInstance(value))
//            return ((JsonPrimitive)value).getAsInt();
//
//        return (Integer) map.get(propertyName);
//    }
//
//    public long getLongValueFor(String propertyName) throws Exception {
//        checkExistence(propertyName);
//
//        Object value = map.get(propertyName);
//        if (JsonPrimitive.class.isInstance(value))
//            return ((JsonPrimitive)value).getAsLong();
//
//        return (long) map.get(propertyName);
//    }
//
//    public Double getDoubleValueFor(String propertyName) throws Exception {
//        checkExistence(propertyName);
//
//        Object value = map.get(propertyName);
//        if (JsonPrimitive.class.isInstance(value))
//            return ((JsonPrimitive)value).getAsDouble();
//
//        return (Double) map.get(propertyName);
//    }
//
//    public String[] mapToArray(){
//        List<String> ret= new ArrayList<String>();
//        Map map = getMap();
//        for(Object key: map.keySet()){
//            ret.add(key+"="+map.get(key).toString());
//        }
//        return ret.toArray(new String[0]);
//    }
//
//    public void setValue(String propertyName, String value){
//        map.put(propertyName, value);
//    }
//
//    public void setValue(String propertyName, int value){
//        map.put(propertyName, value);
//    }
//
//    public void setValue(String propertyName, double value) {
//        map.put(propertyName, value);
//    }
//
//    public void setValue(String propertyName, long value) {
//        map.put(propertyName, value);
//    }
//
//    public void setValue(String propertyName, Object value){
//        map.put(propertyName, value);
//    }
//
//    public void add(KeyValue keyValue) {
//        map.putAll(keyValue.map);
//    }
//
//    public Set<Map.Entry<Object, Object>> getEntries() {
//        return map.entrySet();
//    }
//
//    public Map<Object, Object> getMap(){ return map; }
//
//    private void checkExistence(String propertyName) throws Exception {
//        if(!map.containsKey(propertyName))
//            throw new Exception(propertyName+" not exists in data");
//    }
//
//    @Override
//    public String toString() {
//        return map.toString();
//    }
//
//    public String toJSONString() throws Exception {
//        for(Object value : map.values())
//            if (value.getClass().isArray())
//                throw new Exception("Arrays are not serializable by KeyValue object. Please use list instead");
//
//        JSONObject obj = new JSONObject(map);
//        return obj.toJSONString();
//    }
//
//    @Override
//    public int size() {
//        return map.size();
//    }
//
//    @Override
//    public boolean isEmpty() {
//        return map.isEmpty();
//    }
//
//    @Override
//    public boolean containsKey(Object key) {
//        return map.containsKey(key);
//    }
//
//    @Override
//    public boolean containsValue(Object value) {
//        return map.containsValue(value);
//    }
//
//    @Override
//    public Object get(Object key) {
//        return map.get(key);
//    }
//
//    @Override
//    public Object put(Object key, Object value) {
//        return map.put(key, value);
//    }
//
//    @Override
//    public Object remove(Object key) {
//        return map.remove(key);
//    }
//
//    @Override
//    public void putAll(Map m) {
//        map.putAll(m);
//    }
//
//    @Override
//    public void clear() {
//        map.clear();
//    }
//
//    @Override
//    public Set keySet() {
//        return map.keySet();
//    }
//
//    @Override
//    public Collection values() {
//        return map.values();
//    }
//
//    @Override
//    public Set<Entry> entrySet() {
//        return map.entrySet();
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        KeyValue keyValue = (KeyValue) o;
//        return map.equals(keyValue.map);
//
//    }
//
//    @Override
//    public int hashCode() {
//        return map.hashCode();
//    }
//
//    public static KeyValue parseFromString(String input) throws JsonProcessingException {
//        JsonParser jsonParser = new JsonParser();
//        JsonElement jsonElement = jsonParser.parse(input);
//        return parseFromObject(jsonElement.getAsJsonObject());
//    }
//
//    private static KeyValue parseFromObject(JsonObject input) throws JsonProcessingException {
//        KeyValue ret = new KeyValue();
//
//        for(Map.Entry entry : input.entrySet()){
//            Object value = entry.getValue();
//
//            if(JsonArray.class.isInstance(value))
//                value = parseFromJsonArray((JsonArray)value);
//
//            if(JsonObject.class.isInstance(value))
//                ret.setValue((String)entry.getKey(), parseFromObject((JsonObject)value));
//            else
//                ret.setValue((String)entry.getKey(), value);
//        }
//
//
//        return ret;
//    }
//
//    private static Object parseFromJsonArray(JsonArray array) throws JsonProcessingException {
//        List<Object> ret = new ArrayList<>();
//        for (Object obj : array){
//            Object value = obj;
//            if(JsonArray.class.isInstance(value))
//                value = parseFromJsonArray((JsonArray)obj);
//
//            if(JsonObject.class.isInstance(value))
//                    ret.add(parseFromObject((JsonObject)value));
//            else if(JsonPrimitive.class.isInstance(value))
//                ret.add(((JsonPrimitive)value).getAsString());
//            else
//                ret.add(value);
//        }
//        return ret;
//    }
//}
