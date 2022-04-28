package com.earasoft.beam.pubmed.bean.pubmedData;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

/**
 * History
 * @author erivera
 *
 */
class History implements Serializable{
    private static final long serialVersionUID = 6289730344605983884L;
    
    private List<PubMedPubDate> pubMedPubDates;

    public List<PubMedPubDate> getPubMedPubDates() {
        return this.pubMedPubDates;
    }
    
    @XmlElement(name = "PubMedPubDate")
    public void setPubMedPubDates(List<PubMedPubDate> pubMedPubDates) {
        this.pubMedPubDates = pubMedPubDates;
    }
    
}
