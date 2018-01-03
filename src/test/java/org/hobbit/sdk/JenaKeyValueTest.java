package org.hobbit.sdk;

import org.apache.jena.rdf.model.*;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Katerinenko
 */
public class JenaKeyValueTest {
    private static final String STRING_URI = "http://example.com/stringParam";
    private static final String STRING_EXPECTED = "paramValue";
    private static final String INT_URI = "http://example.com/intParam";
    private static final int INT_EXPECTED = 88;
    private static final String DOUBLE_URI = "http://example.com/doubleParam";
    private static final double DOUBLE_EXPECTED = 0.43;

    @Test
    public void checkEncodingDecoding() throws Exception {
        JenaKeyValue expectedKeyValue = new JenaKeyValue();
        expectedKeyValue.setValue(STRING_URI, STRING_EXPECTED);
        expectedKeyValue.setValue(INT_URI, INT_EXPECTED);
        expectedKeyValue.setValue(DOUBLE_URI, DOUBLE_EXPECTED);
        Model modelEncoding = expectedKeyValue.toModel();
        JenaKeyValue actualKeyValue = new JenaKeyValue.Builder().buildFrom(modelEncoding);
        checkParameters(actualKeyValue);
        Assert.assertEquals(expectedKeyValue, actualKeyValue);
        //
        String stringEnconding = expectedKeyValue.encodeToString();
        actualKeyValue = new JenaKeyValue.Builder().buildFrom(stringEnconding);
        checkParameters(actualKeyValue);
        Assert.assertEquals(expectedKeyValue, actualKeyValue);
        //
        byte[] bytesEncoding = expectedKeyValue.toBytes();
        actualKeyValue = new JenaKeyValue.Builder().buildFrom(bytesEncoding);
        checkParameters(actualKeyValue);
        Assert.assertEquals(expectedKeyValue, actualKeyValue);

    }

    private static void checkParameters(KeyValue actual) throws Exception {
        Assert.assertEquals(STRING_EXPECTED, actual.getStringValueFor(STRING_URI));
        Assert.assertTrue(INT_EXPECTED == actual.getIntValueFor(INT_URI));
        Double d = actual.getDoubleValueFor(DOUBLE_URI);
        Assert.assertTrue(Double.compare(DOUBLE_EXPECTED, d) == 0);
    }

    private static final String str = "{\n" +
            "  \"@id\" : \"exp1\",\n" +
            "  \"@type\" : \"http://w3id.org/hobbit/vocab#Experiment\",\n" +
            "  \"correctness\" : \"Executed on 10 messages:null\\n\",\n" +
            "  \"@context\" : {\n" +
            "    \"correctness\" : {\n" +
            "      \"@id\" : \"http://www.example.org/debs/correctness\"\n" +
            "    }\n" +
            "  }\n" +
            "}\n";

    @Test
    public void tempTest() {
        Model model = RabbitMQUtils.readModel(str);
        Assert.assertNotNull(model);
    }
}