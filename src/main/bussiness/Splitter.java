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
                long recordCounter = 0;
                long numberOfRows = 0;
                long maxProgress = getMaxProgress(pathToFile, userBytes);

                makeValidation();

                String newFilePath = dir + "\\" +fileName+"_"+ filePartNumber + ".xml";
                InputStream xmlInputStream = new FileInputStream(pathToFile);
                XMLInputFactory xmlInputFactory =  XMLInputFactory.newInstance();
                XMLStreamReader streamReader =  xmlInputFactory.createXMLStreamReader(xmlInputStream);
                JAXBContext context = JAXBContext.newInstance(RecordTable.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();

                File file = new File(newFilePath);
                Marshaller marshaller = context.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                ByteArrayOutputStream nextRecord;
                ByteArrayOutputStream tempFile = new ByteArrayOutputStream();
                List<Record> recordList = new ArrayList<Record>();
                while (streamReader.hasNext()) {
                    if (isCancelled()) {
                        updateMessage("Canceled!");
                        break; }
                            streamReader.next();
                    if (streamReader.getEventType() == XMLEvent.START_ELEMENT && streamReader.getLocalName().equals("record")) {
                        JAXBElement<Record> recordObj = unmarshaller.unmarshal(streamReader, Record.class);
                        Record record = recordObj.getValue();
                        nextRecord = saveNextRecord(marshaller, record);
                        if (nextRecord.size() + 225 > userBytes) { // 225 bytes - approximately footer with header tags
                            while (!isCancelled()) {
                                updateMessage("Looks like some record is too big. Hit Cancel and increase file size");
                            }
                        } else if (tempFile.size() + nextRecord.size() <= userBytes) {
                            tempFile = new ByteArrayOutputStream();
                            recordCounter = recordList.size()+1;
                            recordList.add(record);
                            numberOfRows = getNumberOfRows(numberOfRows, record);
                            saveTempRecord(recordCounter, numberOfRows, tempFile, marshaller, recordList);
                            updateProgress(filePartNumber+1, maxProgress);
                        } else {
                            saveXmlFile(recordCounter, numberOfRows, file, marshaller, recordList);
                            recordList.clear();
                            numberOfRows =0;
                            recordList.add(record);
                            numberOfRows = getNumberOfRows(numberOfRows, record);
                            tempFile = new ByteArrayOutputStream();
                            saveTempRecord(recordCounter, numberOfRows, tempFile, marshaller, recordList);
                            filePartNumber++;
                            newFilePath = dir +"\\"+fileName+"_"+ filePartNumber + ".xml";
                            file = new File(newFilePath);
                        }
                    }
                }
                recordCounter = recordList.size();
                saveXmlFile(recordCounter, numberOfRows, file, marshaller, recordList);
                streamReader.close();
                return true;
            }

            private void makeValidation() {
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

    private ByteArrayOutputStream saveNextRecord(Marshaller marshaller, Record record) throws JAXBException, IOException {
        ByteArrayOutputStream nextRecord;
        nextRecord = new ByteArrayOutputStream();
        marshaller.marshal(record, nextRecord);
        nextRecord.flush();
        nextRecord.close();
        return nextRecord;
    }

    private void saveTempRecord(long recordCounter, long numberOfRows, ByteArrayOutputStream tempRecord,
                                Marshaller marshaller, List<Record> recordList) throws JAXBException, IOException {
        RecordTable recordTable = new RecordTable();
        recordTable.setRecord(recordList);
        Footer footer = new Footer();
        footer.setRecordCount(recordCounter);
        footer.setRecordRowCount(numberOfRows);
        recordTable.setFooter(footer);
        marshaller.marshal(recordTable, tempRecord);
        tempRecord.flush();
        tempRecord.close();
    }
    private void saveXmlFile(long recordCounter, long numberOfRows, File file, Marshaller marshaller, List<Record> recordList) throws JAXBException {
        RecordTable recordTable = new RecordTable();
        recordTable.setRecord(recordList);
        Footer footer = new Footer();
        footer.setRecordCount(recordCounter);
        footer.setRecordRowCount(numberOfRows);
        recordTable.setFooter(footer);
        marshaller.marshal(recordTable, file);
    }

    private long getNumberOfRows(long numberOfRows, Record record) {
        long recRowCounter = record.getRecordRow().getString().size();
        numberOfRows = numberOfRows + recRowCounter;
        return numberOfRows;
    }

    private long getMaxProgress(String pathToFile, Long userBytes) {
        File fileToParse = new File(pathToFile); // to count number of files should be created for progress indicator
        long maxProgress;
        if (userBytes>7500) {
            maxProgress = fileToParse.length() / userBytes;
        } else {
            maxProgress = fileToParse.length() / 3750; //quick way to get progress
        }
        return maxProgress;
    }
}
