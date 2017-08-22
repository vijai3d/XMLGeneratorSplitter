package main.bussiness;

import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import javafx.concurrent.Task;
import main.utils.RandomString;
import main.utils.XmlValidation;
import org.xml.sax.SAXException;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class Generator2 {
    public Task runGenerator(final String fileName, final String dirName, final Long recordsCount) throws JAXBException, IOException, SAXException {
        final String pathToFile = dirName + "\\" +fileName+ ".xml";
        return new Task() {
            @Override
            protected Object call() throws Exception {

                File file = new File(pathToFile);
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream( file, true));
                XMLOutputFactory xmlOutputFactory =  XMLOutputFactory.newFactory();
                XMLStreamWriter sw =  xmlOutputFactory.createXMLStreamWriter(stream);

                long recordRowCount = 0;
                sw = new IndentingXMLStreamWriter(sw);
                sw.writeStartDocument("utf-8", "1.0");
                sw.writeStartElement("record-table");
                sw.flush();
                for (long i = 0; i < recordsCount; i++) {
                    if (isCancelled()) {
                        break;
                    }
                    sw.writeStartElement("record");
                    sw.writeStartElement("record_id");
                    sw.writeCharacters(String.valueOf(i+1));
                    sw.writeEndElement();
                    sw.writeStartElement("record_rows");
                    // random number of random string rows
                    Random random = new Random();
                    int randomNumber = random.nextInt(30) + 1;
                    recordRowCount = recordRowCount + randomNumber;
                    for (int r = 0; r < randomNumber; r++) { //how many rows to add
                        RandomString string = new RandomString();
                        String randomString = string.getRandomString();
                        sw.writeStartElement("record_row");
                        sw.writeCharacters(randomString);
                        sw.writeEndElement();
                    }
                    sw.writeEndElement();
                    sw.writeEndElement();
                    sw.flush();
                    updateProgress(i + 1, recordsCount);
                }
                sw.writeStartElement("footer");
                sw.writeStartElement("record_count");
                sw.writeCharacters(String.valueOf(recordsCount));
                sw.writeEndElement();
                sw.writeStartElement("record_row_count");
                sw.writeCharacters(String.valueOf(recordRowCount));
                sw.writeEndElement();
                sw.writeEndElement();
                sw.writeEndElement();
                sw.writeEndDocument();
                sw.flush();
                sw.close();
                validateWithSchema();
                return true;
            }

            private void validateWithSchema() {
                XmlValidation xmlValidation = new XmlValidation();
                try {
                    xmlValidation.validateFile(pathToFile);
                } catch (Exception e) {
                    while (!isCancelled()) {
                        updateMessage("Could not validate file");
                    }
                }
            }
        };
    }
}
