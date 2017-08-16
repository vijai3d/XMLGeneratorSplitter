package main.bussiness;

import javafx.concurrent.Task;
import main.domain.Footer;
import main.domain.Record;
import main.domain.RecordTable;
import main.utils.XmlValidation;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Splitter {

    public Task split(final String fileName, final String pathToFile, final String dir, final Long userBytes) {
        return new Task() {
            @Override
            protected Object call() throws Exception {

                long filePartNumber = 0;
                boolean allowNextTag = true;
                long recordCounter = 0;
                long numberOfRows = 0;
                long maxProgress = getMaxProgress(pathToFile, userBytes);
                XmlValidation xmlValidation = new XmlValidation();
                xmlValidation.validateFile(pathToFile);

                String newFilePath = dir + "\\" +fileName+"_"+ filePartNumber + ".xml";
                InputStream xmlInputStream = new FileInputStream(pathToFile);

                XMLInputFactory xmlInputFactory =  XMLInputFactory.newInstance();
                XMLStreamReader streamReader =  xmlInputFactory.createXMLStreamReader(xmlInputStream);
                JAXBContext context = JAXBContext.newInstance(RecordTable.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                File file = new File(newFilePath);
                Marshaller marshaller = context.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                File tempFile = new File(dir+"\\" + "temp.xml");
                List<Record> recordList = new ArrayList<Record>();
                while (streamReader.hasNext()) {

                    if (isCancelled()) { break; }
                    if (allowNextTag) { streamReader.next(); }
                    if (streamReader.getEventType() == XMLEvent.START_ELEMENT && streamReader.getLocalName().equals("record")) {
                        if (file.length() + tempFile.length() <= userBytes || recordCounter == 0) {

                            //FileOutputStream fos = new FileOutputStream(file);
                            JAXBElement<Record> recordObj = unmarshaller.unmarshal(streamReader, Record.class);
                            Record record = recordObj.getValue();

                            //FileOutputStream tempRecord = new FileOutputStream(tempFile);
                            marshaller.marshal(record, tempFile);

                            if (file.length() + tempFile.length() <= userBytes) { //second check after tempFile creation
                                recordCounter++;
                                recordList.add(record);
                            }
                            numberOfRows = getNumberOfRows(numberOfRows, record);
                            RecordTable recordTable = new RecordTable();
                            recordTable.setRecord(recordList);

                            Footer footer = new Footer();
                            footer.setRecordCount(recordCounter);
                            footer.setRecordRowCount(numberOfRows);
                            recordTable.setFooter(footer);
                            marshaller.marshal(recordTable, file);
                            //fos.close();
                            allowNextTag = true;
                            updateProgress(filePartNumber+1, maxProgress );
                        } else {
                            recordList.clear();
                            recordCounter = 0;
                            numberOfRows =0;
                            allowNextTag = false;
                            filePartNumber++;
                            newFilePath = dir +"\\"+fileName+"_"+ filePartNumber + ".xml";
                            file = new File(newFilePath);
                        }
                    }
                }
                streamReader.close();
                return true;
            }
        };
    }

    private long getNumberOfRows(long numberOfRows, Record record) {
        long recRowCounter = record.getRecordRow().getString().size();
        numberOfRows = numberOfRows + recRowCounter;
        return numberOfRows;
    }

    private long getMaxProgress(String pathToFile, Long userBytes) {
        File fileToParse = new File(pathToFile); // to count number of files should be created for progress indicator
        long maxProgress;
        if (userBytes>4000) {
             maxProgress = fileToParse.length() / userBytes;
        } else {
             maxProgress = fileToParse.length() / 4000;
        }
        return maxProgress;
    }
}
