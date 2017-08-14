package main.bussiness;

import javafx.concurrent.Task;
import main.domain.Footer;
import main.domain.Record;
import main.domain.RecordTable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Splitter {
    public Task split(final String pathToFile, final String dir, final Long userBytes) {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                long filePartNumber = 0;
                boolean allowNextTag = true;
                long recordCounter = 0;
                long numberOfRows = 0;
                File fileToParse = new File(pathToFile); // to count number of files should be created
                long maxProgress = fileToParse.length()/userBytes;

                String newFilePath = dir + "\\" + filePartNumber + ".xml";
                InputStream xmlInputStream = new FileInputStream(pathToFile);
                XMLInputFactory xmlInputFactory =  XMLInputFactory.newInstance();
                XMLStreamReader streamReader =  xmlInputFactory.createXMLStreamReader(xmlInputStream);
                JAXBContext context = JAXBContext.newInstance(RecordTable.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                File file = new File(newFilePath);
                Marshaller marshaller = context.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                List<Record> recordList = new ArrayList<Record>();
                while (streamReader.hasNext()) {
                    if (allowNextTag) {
                        streamReader.next();
                    }
                    if (streamReader.getEventType() == XMLEvent.START_ELEMENT && streamReader.getLocalName().equals("record")) {

                        if (file.length() <= userBytes/2) {
                            recordCounter++;
                            FileOutputStream fos = new FileOutputStream(file);
                            JAXBElement<Record> recordObj = unmarshaller.unmarshal(streamReader, Record.class);
                            Record record = recordObj.getValue();
                            recordList.add(record);
                            RecordTable recordTable = new RecordTable();
                            recordTable.setRecord(recordList);
                            Footer footer = new Footer();
                            footer.setRecordCount(recordCounter);
                            long recRowCounter = record.getRecordRow().getString().size();
                            numberOfRows = numberOfRows + recRowCounter;
                            footer.setRecordRowCount(numberOfRows);
                            recordTable.setFooter(footer);
                            marshaller.marshal(recordTable, fos);
                            fos.close();
                            allowNextTag = true;
                            updateProgress(filePartNumber+1, maxProgress );
                        } else {
                            recordList.clear();
                            recordCounter = 0;
                            numberOfRows =0;
                            allowNextTag = false;
                            filePartNumber++;

                            newFilePath = dir +"\\" + filePartNumber + ".xml";
                            file = new File(newFilePath);
                        }
                    }
                }
                streamReader.close();
                return true;
            }
        };
    }
}
