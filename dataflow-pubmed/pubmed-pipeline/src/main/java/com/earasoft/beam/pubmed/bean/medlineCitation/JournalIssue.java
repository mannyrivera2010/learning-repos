package com.earasoft.beam.pubmed.bean.medlineCitation;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

// <JournalIssue CitedMedium="Print">
//<Volume>158</Volume>
//<Issue>5</Issue>
//<PubDate>
//<Year>1971</Year>
//<Month>May</Month>
//</PubDate>
//</JournalIssue>

class JournalIssue implements Serializable{
    private static final long serialVersionUID = -4749336027469359928L;
    
    private String volume;
    private String issue;
    
    private DateCompleted pubDate;

    public String getVolume() {
        return volume;
    }
    
    @XmlElement(name = "Volume")
    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getIssue() {
        return issue;
    }

    @XmlElement(name = "Issue")
    public void setIssue(String issue) {
        this.issue = issue;
    }

    public DateCompleted getPubDate() {
        return pubDate;
    }

    @XmlElement(name = "PubDate")
    public void setPubDate(DateCompleted pubDate) {
        this.pubDate = pubDate;
    }
    
}
