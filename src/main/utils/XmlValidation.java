package main.utils;

import org.xml.sax.SAXException;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class XmlValidation {
    public String exceptionMessage;

    public void validateFile(String path)  {
        try {
            SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            URL url = getClass().getResource("/task3.xsd");
            Schema schema = sf.newSchema( new File( url.getPath()) );
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(path));
        } catch (SAXException e) {
            e.printStackTrace();
            exceptionMessage =" sax exception :" + e.getMessage();
        } catch (IOException ex) {
            ex.printStackTrace();
            exceptionMessage ="exception :" + ex.getMessage();
        }
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }
}
