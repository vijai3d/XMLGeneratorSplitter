package main.bussiness;

import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import javafx.concurrent.Task;
import main.domain.Footer;
import main.domain.Record;
import main.domain.RecordTable;
import main.utils.RandomString;
import main.utils.XmlValidation;
import javax.xml.bind.*;
import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Splitter2 {


    public Task split(final String fileName, final String pathToFile, final String dir, final Long userBytes) {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                long filePartNumber = 0;
                long recordCounter = 0;
                int numberOfRows = 0;
                boolean newDocument = true;
                updateMessage("Validating your file...");
                validateWithSchema(pathToFile);

                long maxProgress = getMaxProgress(pathToFile, userBytes);
                updateProgress(filePartNumber+1, maxProgress);
                JAXBContext context = JAXBContext.newInstance(RecordTable.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                String newFilePath = dir + "\\" +fileName+"_"+ filePartNumber + ".xml";
                File file = new File(newFilePath);
                InputStream xmlInputStream = new FileInputStream(pathToFile);
                XMLInputFactory xmlInputFactory =  XMLInputFactory.newInstance();
                XMLStreamReader streamReader =  xmlInputFactory.createXMLStreamReader(xmlInputStream);
                FileOutputStream stream = new FileOutputStream( file, true);
                XMLOutputFactory xmlOutputFactory =  XMLOutputFactory.newFactory();
                XMLStreamWriter sw =  xmlOutputFactory.createXMLStreamWriter(stream, "UTF-8");
                sw = new IndentingXMLStreamWriter(sw);

                while (streamReader.hasNext()) {
                    updateMessage("Working...");
                    if (isCancelled()) {
                        updateMessage("Canceled!");
                        break; }

                        streamReader.next();
                    if (streamReader.getEventType() == XMLEvent.START_ELEMENT && streamReader.getLocalName().equals("record")) {
                        JAXBElement<Record> recordObj = unmarshaller.unmarshal(streamReader, Record.class);
                        Record record = recordObj.getValue();
                        if (newDocument) {
                            numberOfRows=0;
                            sw = saveHeader(file, xmlOutputFactory);
                        }
                        if (file.length() <= userBytes) {
                            numberOfRows = addRecord(numberOfRows, sw, record);
                            recordCounter++;
                            newDocument=false;
                        } else {
                            saveFooter(recordCounter, numberOfRows, sw);
                            updateProgress(filePartNumber+1, maxProgress);
                            filePartNumber++;
                            newFilePath = dir +"\\"+fileName+"_"+ filePartNumber + ".xml";
                            file = new File(newFilePath);
                            recordCounter=1;
                            newDocument=true;
                            sw = saveHeader(file, xmlOutputFactory);
                            numberOfRows = addRecord(numberOfRows, sw, record);
                        }

                    }
                }
                saveFooter(recordCounter, numberOfRows, sw);
                streamReader.close();
                updateMessage("");
                return true;
            }

            private void validateWithSchema(String pathToFile) {
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

    private void saveFooter(long recordCounter, int numberOfRows, XMLStreamWriter sw) throws XMLStreamException {
        sw.writeStartElement("footer");
        sw.writeStartElement("record_count");
        sw.writeCharacters(String.valueOf(recordCounter));
        sw.writeEndElement();//record_count
        sw.writeStartElement("record_row_count");
        sw.writeCharacters(String.valueOf(numberOfRows));
        sw.writeEndElement();//record_row_count
        sw.writeEndElement();
        sw.writeEndElement();//record-table
        sw.writeEndDocument();
        sw.flush();
        sw.close();
    }

    private XMLStreamWriter saveHeader(File file, XMLOutputFactory xmlOutputFactory) throws FileNotFoundException, XMLStreamException {
        FileOutputStream stream;
        XMLStreamWriter sw;
        stream = new FileOutputStream( file, true);
        sw =  xmlOutputFactory.createXMLStreamWriter(stream, "UTF-8");
        sw = new IndentingXMLStreamWriter(sw);
        sw.writeStartDocument("UTF-8", "1.0"); // caused runtime error if remove encoding parameter in create streamwriter
        sw.writeStartElement("record-table");
        sw.flush();
        return sw;
    }

    private int addRecord(int numberOfRows, XMLStreamWriter sw, Record record) throws XMLStreamException {
        sw.writeStartElement("record");
        sw.writeStartElement("record_id");
        sw.writeCharacters(record.getRecordId().toString());
        sw.writeEndElement();
        sw.writeStartElement("record_rows");
        int i=0;//record rows in each record
        while(true) {
            try {
                if(record.getRecordRow().getString().get(i) !=null) {
                    sw.writeStartElement("record_row");
                    sw.writeCharacters(record.getRecordRow().getString().get(i));
                    sw.writeEndElement();
                    sw.flush();
                    i++;
                    numberOfRows++;
                }
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }
        sw.writeEndElement();//record rows
        sw.writeEndElement();//record
        sw.flush();
        return numberOfRows;
    }

    private long getMaxProgress(String pathToFile, Long userBytes) {
        File fileToParse = new File(pathToFile); // to count number of files should be created for progress indicator
        long maxProgress;
        if (userBytes>7500) {
            maxProgress = fileToParse.length() / userBytes;
        } else {
            maxProgress = fileToParse.length() / 3200; //quick way to get progress
        }
        return maxProgress;
    }
}
