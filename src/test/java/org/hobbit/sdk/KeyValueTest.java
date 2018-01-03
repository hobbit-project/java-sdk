package org.hobbit.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Pavel Smirnov
 */

public class KeyValueTest {


    @Test
    public void checkParsing() throws JsonProcessingException {
        KeyValue kv = new KeyValue();
        kv.setValue("property1", 1);
        kv.setValue("property2", "value2");
        String jsonStr = kv.toJSONString();

//        KeyValue parsedKv = KeyValue.parseFromString(jsonStr);
//        Assert.assertEquals(kv.getIntValueFor("property1"), parsedKv.getIntValueFor("property1"));
//        Assert.assertEquals(kv.getStringValueFor("property2"), parsedKv.getStringValueFor("property2"));
    }
}
