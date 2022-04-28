package com.earasoft.beam.pubmed.bean.medlineCitation;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

/**
 * 
 * <!ELEMENT    Abstract (AbstractText+, CopyrightInformation?)>

<!ELEMENT   AbstractText   (%text; | mml:math | DispFormula)* >
<!ATTLIST   AbstractText
            Label CDATA #IMPLIED
            NlmCategory (BACKGROUND | OBJECTIVE | METHODS | RESULTS | CONCLUSIONS | UNASSIGNED) #IMPLIED >
            
 * @author erivera
 *
 */
public class Abstract implements Serializable {
    private static final long serialVersionUID = -9208683177478106902L;
    // <AbstractText Label="OBJECTIVES" NlmCategory="OBJECTIVE">
    public List<String> abstractTextList;
    public String copyrightInformation;
    
    public List<String> getAbstractText() {
        return abstractTextList;
    }
    
    @XmlElement(name="AbstractText")
    public void setAbstractText(List<String> abstractText) {
        this.abstractTextList = abstractText;
    }
//
    public String getCopyrightInformation() {
        return copyrightInformation;
    }

//    @XmlElement(name="CopyrightInformation")
//    public void setCopyrightInformation(String copyrightInformation) {
//        this.copyrightInformation = copyrightInformation;
//    }
    
    
}
