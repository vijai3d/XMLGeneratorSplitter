package main.bussiness;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import main.domain.Footer;
import main.domain.Record;
import main.domain.RecordTable;
import main.domain.Row;
import main.utils.RandomString;
import main.utils.XmlValidation;
import org.xml.sax.SAXException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class XmlGenerator{

    public void generate(String fileName, String dirName, Long recordsCount) throws JAXBException, IOException, SAXException {
        RecordTable recordTable = new RecordTable();
        Footer footer = new Footer();
        footer.setRecordCount(recordsCount);
        int recordRowCount = 0;

        List<Record> recordList = new ArrayList();

            // record element
            for (long i = 0; i < recordsCount; i++) {
                List<String> stringList = new ArrayList(); // every record - new string list
                Record record = new Record();
                record.setRecordId((long) (i + 1));
                // random number of random string rows
                Random random = new Random();
                int randomNumber = random.nextInt(30) + 1;
                recordRowCount = recordRowCount + randomNumber;

                for (int r = 0; r < randomNumber; r++) { //how many rows to add
                    RandomString string = new RandomString();
                    String randomString = string.getRandomString();
                    stringList.add(randomString);
                    Row row = new Row();
                    row.setString(stringList);
                    record.setRecordRow(row);
                }
                recordList.add(record); //create record
            }

        recordTable.setRecord(recordList); // creates list of records
        footer.setRecordRowCount(recordRowCount);
        recordTable.setFooter(footer); // creates footer

        JAXBContext jaxbContext = JAXBContext.newInstance( RecordTable.class );
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
        jaxbMarshaller.marshal( recordTable, new File( dirName + "\\" +fileName+ ".xml" ) );
        jaxbMarshaller.marshal( recordTable, System.out );

        // validate with schema
        XmlValidation validation = new XmlValidation();
        Record record = new Record();
        Row row = new Row();
        validation.validate(recordTable);
        validation.validate(footer);
        validation.validate(record);
        validation.validate(row);
    }
}
