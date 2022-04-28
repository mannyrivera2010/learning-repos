package com.earasoft.beam.pubmed.bean.medlineCitation;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

//<Journal>
//<ISSN IssnType="Print">0023-2165</ISSN>
//<JournalIssue CitedMedium="Print">
//    ...
//</JournalIssue>
//<Title>Klinische Monatsblatter fur Augenheilkunde</Title>
//<ISOAbbreviation>Klin Monbl Augenheilkd</ISOAbbreviation>
//</Journal>

class Journal implements Serializable{
    private static final long serialVersionUID = -1944076659907928404L;

    private JournalIssue journalIssue;
    
    private String issn;
    private String title;
    private String isoAbbreviation;
    
    public JournalIssue getJournalIssue() {
        return journalIssue;
    }
    
    @XmlElement(name = "JournalIssue")
    public void setJournalIssue(JournalIssue journalIssue) {
        this.journalIssue = journalIssue;
    }
    
    public String getIssn() {
        return issn;
    }
    
    @XmlElement(name = "ISSN")
    public void setIssn(String issn) {
        this.issn = issn;
    }
    
    public String getTitle() {
        return title;
    }
    
    @XmlElement(name = "Title")
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getIsoAbbreviation() {
        return isoAbbreviation;
    }
    
    @XmlElement(name = "ISOAbbreviation")
    public void setIsoAbbreviation(String isoAbbreviation) {
        this.isoAbbreviation = isoAbbreviation;
    }
    
    
}
