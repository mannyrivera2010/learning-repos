package com.earasoft.beam.pubmed.bean.medlineCitation;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

class QualifierName {
    private String ui;
    private String majorTopicYN;
    private String qualifierName;
    
    public String getUi() {
        return ui;
    }
    
    @XmlAttribute(name="UI")
    public void setUi(String ui) {
        this.ui = ui;
    }
    
    public String getMajorTopicYN() {
        return majorTopicYN;
    }
    
    @XmlAttribute(name="MajorTopicYN")
    public void setMajorTopicYN(String majorTopicYN) {
        this.majorTopicYN = majorTopicYN;
    }

    public String getQualifierName() {
        return qualifierName;
    }
    
    @XmlValue
    public void setQualifierName(String qualifierName) {
        this.qualifierName = qualifierName;
    }
    
    
}
