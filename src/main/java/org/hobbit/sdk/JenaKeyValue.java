package org.hobbit.sdk;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.*;
import org.hobbit.core.rabbit.RabbitMQUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author Roman Katerinenko
 */
public class JenaKeyValue extends KeyValue {
    public static String DEFAULT_URI = "http://jenaKeyValue.com/URI";

    private final String URI;

    public JenaKeyValue(String experimentUri) {
        this.URI = experimentUri;
    }

    public JenaKeyValue() {
        this.URI = DEFAULT_URI;
    }

    public String getURI(){
        return URI;
    }

    public String encodeToString() {
        return RabbitMQUtils.writeModel2String(toModel());
    }

    public String[] mapToArray(){
        List<String> ret= new ArrayList<String>();
        Map map = toMap();
        for(Object key: map.keySet()){
            ret.add(key+"="+map.get(key).toString());
        }
        return ret.toArray(new String[0]);
    }

    public byte[] toBytes() {
        return RabbitMQUtils.writeModel(toModel());
    }

    public Model toModel() {
        Model model = ModelFactory.createDefaultModel();
        Resource subject = model.createResource(URI);
        for (Map.Entry entry : getEntries()) {
            Literal literal = model.createTypedLiteral(entry.getValue());
            Property property = model.createProperty(String.valueOf(entry.getKey()));
            model.add(subject, property, literal);
        }
        return model;
    }

    public static class Builder {
        public JenaKeyValue buildFrom(String string) {
            Model model = RabbitMQUtils.readModel(string);
            return buildFrom(model);
        }

        public JenaKeyValue buildFrom(byte[] bytes) {
            Model model = RabbitMQUtils.readModel(bytes);
            return buildFrom(model);
        }

        public JenaKeyValue buildFrom(Model model) {
            JenaKeyValue keyValue = null;
            StmtIterator iterator = model.listStatements(null, null, (RDFNode) null);
            while (iterator.hasNext()){

                Statement statement = iterator.nextStatement();
                if(keyValue==null)
                    keyValue = new JenaKeyValue(statement.getSubject().getURI());
                String propertyUri = statement.getPredicate().getURI();
                RDFNode object = statement.getObject();
                if (object.isLiteral()) {
                    Literal literal = object.asLiteral();
                    RDFDatatype datatype = literal.getDatatype();
                    Object value = datatype.parse(literal.getLexicalForm());
                    keyValue.setValue(propertyUri, value);
                }
            }
            return keyValue;
        }
    }
}