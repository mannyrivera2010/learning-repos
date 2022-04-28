package com.earasoft.beam.pubmed.bean.medlineCitation;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * 
 * <!ELEMENT   AffiliationInfo (Affiliation, Identifier*)>
 * <!ELEMENT    Affiliation  (%text;)*>
 * 
 * <!ELEMENT    Identifier (#PCDATA) >
 * <!ATTLIST   Identifier 
 * 
 * @author erivera
 *
 */
public class AffiliationInfo implements Serializable{
    private static final long serialVersionUID = -3797901982768650226L;
    
    private String affiliation;

    public String getAffiliation() {
        return affiliation;
    }
    
    @XmlElement(name = "Affiliation")
    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }
    
}
