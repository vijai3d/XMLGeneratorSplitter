package main.domain;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "footer")
public class Footer {
    private long recordCount;
    private long recordRowCount;

    public Footer() {
    }

    public long getRecordCount() {
        return recordCount;
    }
    @XmlElement(name = "record_count")
    public void setRecordCount(long recordCount) {
        this.recordCount = recordCount;
    }

    public long getRecordRowCount() {
        return recordRowCount;
    }
    @XmlElement(name = "record_row_count")
    public void setRecordRowCount(long recordRowCount) {
        this.recordRowCount = recordRowCount;
    }
}
