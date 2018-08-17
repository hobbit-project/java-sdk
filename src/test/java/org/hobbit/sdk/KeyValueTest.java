package org.hobbit.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Pavel Smirnov
 */

public class KeyValueTest {


    @Test
    public void checkParsing() throws Exception {
        KeyValue kv = new KeyValue();
        kv.setValue("property1", 1);
        kv.setValue("property2", "value2");
        kv.setValue("property3", Arrays.asList(new String[]{ "value3", "value4" }));

        String jsonStr = kv.toJSONString();
        KeyValue parsedKv = KeyValue.parseFromString(jsonStr);
        Assert.assertEquals(kv.getIntValueFor("property1"), parsedKv.getIntValueFor("property1"));
        Assert.assertEquals(kv.getStringValueFor("property2"), parsedKv.getStringValueFor("property2"));
        Assert.assertEquals(kv.getValue("property3"), parsedKv.getValue("property3"));
    }

    @Test
    @Ignore
    public void checkRandomStringParsing() throws JsonProcessingException {
        String json = "{\"tasksLists\":[[task_0, task_1, task_2, task_3, task_4, task_5, task_6, task_7, task_8, task_9, task_10, task_11, task_12, task_13, task_14, task_15, task_16, task_17, task_18, task_19, task_20, task_21, task_22, task_23, task_24, task_25, task_26, task_27, task_28, task_29]]}";
        KeyValue parsedKv = KeyValue.parseFromString(json);
        String test="123";
    }
}
