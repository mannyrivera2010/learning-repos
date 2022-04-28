package com.earasoft.beam.pubmed.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.earasoft.beam.pubmed.bean.medlineCitation.MedlineCitation;
import com.earasoft.beam.pubmed.bean.pubmedData.PubmedData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * PubmedArticle
 * https://docs.oracle.com/cd/E19575-01/819-3669/6n5sg7bj5/index.html
 * https://www.baeldung.com/jaxb
 * 
 * DTD:
 * <!ELEMENT    PubmedArticle (MedlineCitation, PubmedData?)>
 * <!ATTLIST       PubmedArticle
 * 
 * 
 * <AbstractText Label="OBJECTIVE" NlmCategory="OBJECTIVE">To report a case of successful pregnancy after treatment for endometrial stromal sarcoma.</AbstractText>
 * 
 * @author erivera
 *
 */
@XmlRootElement(name = "PubmedArticle")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class PubmedArticle implements Serializable{
    private static final long serialVersionUID = 2713119930319372105L;
    static Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
    static Gson prettyGson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    
    private MedlineCitation medlineCitation;
    private PubmedData pubmedData;
    
    public MedlineCitation getMedlineCitation() {
        return medlineCitation;
    }
    
    @XmlElement(name = "MedlineCitation")
    public void setMedlineCitation(MedlineCitation medlineCitation) {
        this.medlineCitation = medlineCitation;
    }
    
    public PubmedData getPubmedData() {
        return pubmedData;
    }
    
    @XmlElement(name = "PubmedData")
    public void setPubmedData(PubmedData pubmedData) {
        this.pubmedData = pubmedData;
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }
    
    public String toPrettyString() {
        return prettyGson.toJson(this);
    }
    
}
