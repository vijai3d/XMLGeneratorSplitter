package main.bussiness;

import javafx.concurrent.Task;
import main.domain.Footer;
import main.domain.Record;
import main.domain.RecordTable;
import main.utils.XmlValidation;
import javax.xml.bind.*;
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
                Record recordToNextFile = null;
                ByteArrayOutputStream tempFile = new ByteArrayOutputStream();
                ByteArrayOutputStream tempRecord = new ByteArrayOutputStream();
                List<Record> recordList = new ArrayList<Record>();
                while (streamReader.hasNext()) {
                    if (isCancelled()) {
                        updateMessage("Canceled!");
                        break; }
                    if (allowNextTag) { streamReader.next(); }
                    if (streamReader.getEventType() == XMLEvent.START_ELEMENT && streamReader.getLocalName().equals("record")) {
                        if (tempRecord.size() + tempFile.size() <= userBytes) {
                            JAXBElement<Record> recordObj = unmarshaller.unmarshal(streamReader, Record.class);
                            Record record = recordObj.getValue();
                            tempFile = new ByteArrayOutputStream();
                            marshaller.marshal(record, tempFile);
                            tempFile.flush();
                            tempFile.close();
                            if (tempFile.size() + 225 > userBytes) { // 225 bytes - approximately footer with header tags
                                updateMessage("Looks like one some record is too big. Hit Cancel and increase file size");
                            }
                            if (tempRecord.size() + tempFile.size() <= userBytes) {
                                recordCounter++;
                                recordList.add(record);
                                allowNextTag = true;
                            } else {
                                recordToNextFile = recordList.get(recordList.size() - 1); //take last record from list
                            }
                            numberOfRows = getNumberOfRows(numberOfRows, record);
                            saveTempRecord(recordCounter, numberOfRows, tempRecord, marshaller, recordList);
                            updateProgress(filePartNumber+1, maxProgress );
                        } else {
                            saveXml(recordCounter, numberOfRows, file, marshaller, recordList);
                            tempRecord = new ByteArrayOutputStream();
                            recordList.clear();
                            recordList.add(recordToNextFile);
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

    private void saveTempRecord(long recordCounter, long numberOfRows, ByteArrayOutputStream tempRecord,
                                Marshaller marshaller, List<Record> recordList) throws JAXBException, IOException {
        RecordTable recordTable = getRecordTable(recordCounter, numberOfRows, recordList);
        marshaller.marshal(recordTable, tempRecord);
        tempRecord.flush();
        tempRecord.close();
    }
    private void saveXml(long recordCounter, long numberOfRows, File file, Marshaller marshaller, List<Record> recordList) throws JAXBException {
        RecordTable recordTable = getRecordTable(recordCounter, numberOfRows, recordList);
        marshaller.marshal(recordTable, file);
    }

    private RecordTable getRecordTable(long recordCounter, long numberOfRows, List<Record> recordList) {
        RecordTable recordTable = new RecordTable();
        recordTable.setRecord(recordList);
        Footer footer = new Footer();
        footer.setRecordCount(recordCounter);
        footer.setRecordRowCount(numberOfRows);
        recordTable.setFooter(footer);
        return recordTable;
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