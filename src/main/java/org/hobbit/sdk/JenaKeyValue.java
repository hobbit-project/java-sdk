//package org.hobbit.sdk;
//import org.apache.jena.datatypes.RDFDatatype;
//import org.apache.jena.rdf.model.*;
//import org.hobbit.core.rabbit.RabbitMQUtils;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//
///**
// * @author Roman Katerinenko
// */
//public class JenaKeyValue extends KeyValue {
//    public static String DEFAULT_URI = "http://jenaKeyValue.com/URI";
//
//    private final String URI;
//
//    public JenaKeyValue(String experimentUri) {
//        this.URI = experimentUri;
//    }
//
//    public JenaKeyValue() {
//        this.URI = DEFAULT_URI;
//    }
//
//    public JenaKeyValue(JenaKeyValue source) {
//        super(new HashMap(source.getMap()));
//        this.URI = source.URI;
//    }
//
//    public String getURI(){
//        return URI;
//    }
//
//    public String encodeToString() {
//        return RabbitMQUtils.writeModel2String(toModel());
//    }
//
//
//    public byte[] toBytes() {
//        return RabbitMQUtils.writeModel(toModel());
//    }
//
//    public Model toModel() {
//        Model model = ModelFactory.createDefaultModel();
//        Resource subject = model.createResource(URI);
//        for (Map.Entry entry : getEntries()) {
//            Literal literal = model.createTypedLiteral(entry.getValue());
//            Property property = model.createProperty(String.valueOf(entry.getKey()));
//            model.add(subject, property, literal);
//        }
//        return model;
//    }
//
//    public static class Builder {
//
//        public JenaKeyValue buildFrom(File file) {
//            Model model = ModelFactory.createDefaultModel();
//            model.read(file.getAbsolutePath());
//            return buildFrom(model);
//        }
//
//        public JenaKeyValue buildFrom(String string) {
//            Model model = RabbitMQUtils.readModel(string);
//            return buildFrom(model);
//        }
//
//        public JenaKeyValue buildFrom(byte[] bytes) {
//            Model model = RabbitMQUtils.readModel(bytes);
//            return buildFrom(model);
//        }
//
//        public JenaKeyValue buildFrom(Model model){
//            JenaKeyValue keyValue = null;
//            StmtIterator iterator = model.listStatements(null, null, (RDFNode) null);
//            while (iterator.hasNext()){
//
//                Statement statement = iterator.nextStatement();
//                if(keyValue==null)
//                    keyValue = new JenaKeyValue(statement.getSubject().getURI());
//                String propertyUri = statement.getPredicate().getURI();
//                RDFNode object = statement.getObject();
//                if (object.isLiteral()) {
//                    Literal literal = object.asLiteral();
//                    RDFDatatype datatype = literal.getDatatype();
//                    Object value = datatype.parse(literal.getLexicalForm());
//                    keyValue.setValue(propertyUri, value);
//                }
//            }
//            if(keyValue==null)
//                keyValue = new JenaKeyValue();
//            return keyValue;
//        }
//    }
//}