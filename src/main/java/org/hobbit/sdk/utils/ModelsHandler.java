package org.hobbit.sdk.utils;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import org.hobbit.vocab.HOBBIT;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pavel Smirnov. (psmirnov@agtinternational.com / smirnp@gmail.com)
 */
public class ModelsHandler {

    public static Model byteArrayToModel(byte data[], String lang) {
        Model m = ModelFactory.createDefaultModel();
        m.read(new ByteArrayInputStream(data), null, lang);
        return m;
    }

    public static void fillTheInstanceWithDefaultModelValues(Model model, Resource benchmarkInstanceResource, String namespaceUri){
        Property parameter;
        NodeIterator objIterator;
        ResIterator iterator = model.listResourcesWithProperty(RDF.type, HOBBIT.Parameter);
        Property defaultValProperty = model.getProperty("http://w3id.org/hobbit/vocab#defaultValue");
        while (iterator.hasNext()) {
            try{
                parameter = model.getProperty(iterator.next().getURI());
                if(benchmarkInstanceResource.getProperty(parameter)==null){
                    objIterator = model.listObjectsOfProperty(parameter, defaultValProperty);
                    while (objIterator.hasNext()) {
                        Literal valueLiteral = (Literal) objIterator.next();//.asLiteral().getString();
                        model.add(benchmarkInstanceResource, parameter, valueLiteral.getString(), valueLiteral.getDatatype());
                        //model.add(benchmarkInstanceResource, parameter, model.createTypedLiteral(valueL));
                        //parameters.put(namespaceUri + "#" + parameter.getLocalName(), value);
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    public static Model addParameters(Model model, Resource benchmarkInstanceResource, Map<String, Object> params) throws Exception {
        //Property valueProperty = model.getProperty("http://w3id.org/hobbit/vocab#defaultValue");
//        Property valueProperty = model.getProperty("hobbit:defaultValue");
//        for(String key: params.keySet()){
//            ResIterator iterator = model.listResourcesWithProperty(RDF.type, HOBBIT.Parameter);
//            while (iterator.hasNext()) {
//                Resource resource = iterator.nextResource();
//                if(resource.getURI().equals(key)){
//                    String abc="123";
//                }
//
//            }
//            Resource resource = model.getResource(key);
//            Statement st = resource.getProperty(valueProperty);
//            if(st!=null){
//                RDFNode obj = st.getObject();
//                String ac ="123";
//                //st.changeLiteralObject();
//            }
//        }

        for(String uri: params.keySet()){
//            String uri = paramValue.getId();
//            String datatype = Datatype.getValue(paramValue.getDatatype());
            Object value = params.get(uri);
//            String range = paramValue.getRange();
            String datatype=null;
            if(value instanceof String)
                datatype = "xsd:string";
            if(value instanceof Integer)
                datatype = "xsd:unsignedInt";
            if(value instanceof Boolean)
                datatype = "xsd:boolean";
            if(datatype==null)
                throw new Exception("Cannot define datatype for "+uri);
            model.add(benchmarkInstanceResource, model.createProperty(uri), model.createTypedLiteral(value, expandedXsdId(datatype)));

//            if (range == null) {
//                model.add(benchmarkInstanceResource, model.createProperty(uri),
//                        model.createTypedLiteral(value, expandedXsdId(datatype)));
//            } else {
//                if (range.startsWith(XSD.NS)) {
//                    model.add(benchmarkInstanceResource, model.createProperty(uri),
//                            model.createTypedLiteral(value, range));
//                } else {
//                    model.add(benchmarkInstanceResource, model.createProperty(uri), model.createResource(value));
//                }
//            }
        }

        StringWriter writer = new StringWriter();
        model.write(writer, "Turtle");

        return model;
    }

    private static String expandedXsdId(String id) {
        if (!id.startsWith("http:")){
            String prefix = id.substring(0, id.indexOf(":"));
            return id.replace(prefix + ":", XSD.NS);
        } else {
            return id;
        }
    }
}
