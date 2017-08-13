package main.domain;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlType(propOrder = {"record", "footer"})
@XmlRootElement( name="record-table" )
public class RecordTable {

    private List<Record> record;
    private Footer footer;

    public List<Record> getRecord() {
        return record;
    }

    public void setRecord(List<Record> record) {
        this.record = record;
    }

    public Footer getFooter() {
        return footer;
    }

    public void setFooter(Footer footer) {
        this.footer = footer;
    }

    @Override
    public String toString() {
        return "RecordTable{" +
                "record=" + record +
                ", footer=" + footer +
                '}';
    }
}
