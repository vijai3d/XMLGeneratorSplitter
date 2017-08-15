package main.utils;

import javafx.scene.control.Label;
import main.domain.RecordTable;
import org.xml.sax.SAXException;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;

public class XmlValidation {
    public Label errorLabel;
    public void validate(Object o) throws SAXException, JAXBException, IOException {
        SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
        Schema schema = sf.newSchema( new File( "src/main/resources/task3.xsd" ) );
        JAXBContext jaxbContext = JAXBContext.newInstance( RecordTable.class );
        JAXBSource sourceRecordTable = new JAXBSource( jaxbContext, o );
        Validator validator = schema.newValidator();
        validator.setErrorHandler( new ValidationErrorHandler());
        try
        {
            validator.validate( sourceRecordTable );
            errorLabel.setText( o.getClass() + " has no problems" );
        }
        catch( SAXException ex )
        {
            ex.printStackTrace();
            errorLabel.setText( o.getClass() + " has problems" );
        }
    }

    public void validateFile(String path)  {
        try {
            SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            Schema schema = sf.newSchema( new File( "src/main/resources/task3.xsd" ) );
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(path));
        } catch (SAXException e) {
            e.printStackTrace();
            errorLabel.setText(" sax exception :" + e.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
            errorLabel.setText("exception :" + ex.getMessage());
        }
    }
}
