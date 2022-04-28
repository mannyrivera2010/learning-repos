package com.earasoft.beam.pubmed.bean.medlineCitation;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * AuthorList
 * 
 * DTD:
 * <!ELEMENT    AuthorList (Author+) >
 * <!ATTLIST   AuthorList 
 *     CompleteYN (Y | N) "Y"
 *     Type ( authors | editors )  #IMPLIED >
 *     
 * @author erivera
 *
 */
public class AuthorList implements Serializable{
    private static final long serialVersionUID = -3097458048816785578L;
    
    private String completeYN;
    private List<Author> authors;
    
    public String getCompleteYN() {
        return completeYN;
    }
    
    @XmlAttribute(name="CompleteYN")
    public void setCompleteYN(String completeYN) {
        this.completeYN = completeYN;
    }

    public List<Author> getAuthors() {
        return authors;
    }
    
    @XmlElement(name = "Author")
    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }
    
}