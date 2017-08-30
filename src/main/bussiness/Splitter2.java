package main.bussiness;

import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import javafx.concurrent.Task;
import main.domain.Record;
import main.domain.RecordTable;
import main.utils.XmlValidation;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.io.*;

public class Splitter2 {
    public Task split(final String fileName, final String pathToFile, final String dir, final Long userBytes) {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                long filePartNumber = 0;
                long recordCounter = 0;
                int numberOfRows = 0;
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

                ByteArrayOutputStream nextRecord;
                XMLOutputFactory xmlOutputFactory =  XMLOutputFactory.newFactory();
                XMLStreamWriter sw;
                sw = saveHeader(file, xmlOutputFactory);

                while (streamReader.hasNext()) {
                    updateMessage("Working...");
                    if (isCancelled()) {
                        updateMessage("Canceled!");
                        break; }
                        streamReader.next();
                    if (streamReader.getEventType() == XMLEvent.START_ELEMENT && streamReader.getLocalName().equals("record")) {
                        JAXBElement<Record> recordObj = unmarshaller.unmarshal(streamReader, Record.class);
                        Record record = recordObj.getValue();
                        nextRecord = saveNextRecordToMemory(xmlOutputFactory, record);
                        if (nextRecord.size() + 225 > userBytes) { // 225 bytes - approximately footer with header tags
                            while (!isCancelled()) {
                                updateMessage("Looks like some record is too big. Hit Cancel and increase file size");
                            }
                        }else if (file.length() + nextRecord.size() <= userBytes) {
                            numberOfRows = addRecord(numberOfRows, sw, record);
                            recordCounter++;
                        } else {
                            saveFooter(recordCounter, numberOfRows, sw);
                            updateProgress(filePartNumber+1, maxProgress);
                            filePartNumber++;
                            newFilePath = dir +"\\"+fileName+"_"+ filePartNumber + ".xml";
                            file = new File(newFilePath);
                            recordCounter=1;
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

    private ByteArrayOutputStream saveNextRecordToMemory(XMLOutputFactory xmlOutputFactory, Record record) throws XMLStreamException, IOException {
        ByteArrayOutputStream nextRecord;
        nextRecord = new ByteArrayOutputStream();
        XMLStreamWriter nr =  xmlOutputFactory.createXMLStreamWriter(nextRecord, "UTF-8");
        nr.writeStartElement("record");
        nr.writeStartElement("record_id");
        nr.writeCharacters(record.getRecordId().toString());
        nr.writeEndElement();
        nr.writeStartElement("record_rows");
        int i=0;//record rows in each record
        while(true) {
            try {
                if(record.getRecordRow().getString().get(i) !=null) {
                    nr.writeStartElement("record_row");
                    nr.writeCharacters(record.getRecordRow().getString().get(i));
                    nr.writeEndElement();
                    nr.flush();
                    i++;
                }
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }
        nr.writeEndElement();//record rows
        nr.writeEndElement();//record
        nr.flush();
        nextRecord.flush();
        nextRecord.close();
        return nextRecord;
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
