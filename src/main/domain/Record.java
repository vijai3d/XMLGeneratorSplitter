package main.domain;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "record")
public class Record {
    private Long recordId;
    private Row recordRow;

    public Record() {
    }

    public Long getRecordId() {
        return recordId;
    }
    @XmlElement(name = "record_id")
    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Row getRecordRow() {
        return recordRow;
    }
    @XmlElement(name = "record_rows")
    public void setRecordRow(Row recordRow) {
        this.recordRow = recordRow;
    }
}
