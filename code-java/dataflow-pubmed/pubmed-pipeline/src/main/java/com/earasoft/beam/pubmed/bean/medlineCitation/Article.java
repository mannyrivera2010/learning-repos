package com.earasoft.beam.pubmed.bean.medlineCitation;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Article
 * 
 * DTD
 * <!ELEMENT   Article 
 *     (Journal, ArticleTitle, ((Pagination, ELocationID*) | ELocationID+),
 *      Abstract?,
 *      AuthorList?, 
 *      Language+, 
 *      DataBankList?, 
 *      GrantList?,
 *      PublicationTypeList, 
 *      VernacularTitle?, 
 *      ArticleDate*) >
 *      
 * <!ATTLIST   Article 
 *     PubModel (Print | Print-Electronic | Electronic | Electronic-Print | Electronic-eCollection) #REQUIRED >
 * 
 * <!ELEMENT    AuthorList (Author+) >
 * <!ATTLIST   AuthorList CompleteYN (Y | N) "Y" Type ( authors | editors )  #IMPLIED >
            
 * @author erivera
 *
 */
public class Article{
    // XmlAttribute
    private String PubModel;
    // Elements
    private String articleTitle;
    private AuthorList authorList;
    
    private Journal journal;
    
    private List<String> languages;
    private PublicationTypeList publicationTypeList;
    private String vernacularTitle;
    
    private Abstract abstractClass;
    
    public Abstract getAbstractClass() {
        return abstractClass;
    }
    
    @XmlElement(name="Abstract")
    public void setAbstractClass(Abstract abstractClass) {
        this.abstractClass = abstractClass;
    }

    public String getPubModel() {
        return PubModel;
    }
    
    @XmlAttribute(name="PubModel")
    public void setPubModel(String pubModel) {
        PubModel = pubModel;
    }
    
    public AuthorList getAuthorList() {
        return authorList;
    }
    
    @XmlElement(name="AuthorList")
    public void setAuthorList(AuthorList authorList) {
        this.authorList = authorList;
    }
    
    public String getArticleTitle() {
        return articleTitle;
    }
    
    @XmlElement(name = "ArticleTitle")
    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }

    public Journal getJournal() {
        return journal;
    }
    
    @XmlElement(name = "Journal")
    public void setJournal(Journal journal) {
        this.journal = journal;
    }

    public List<String> getLanguages() {
        return languages;
    }
    
    @XmlElement(name = "Language")
    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public PublicationTypeList getPublicationTypeList() {
        return publicationTypeList;
    }
    
    @XmlElement(name = "PublicationTypeList")
    public void setPublicationTypeList(PublicationTypeList publicationTypeList) {
        this.publicationTypeList = publicationTypeList;
    }

    public String getVernacularTitle() {
        return vernacularTitle;
    }
    
    @XmlElement(name = "VernacularTitle")
    public void setVernacularTitle(String vernacularTitle) {
        this.vernacularTitle = vernacularTitle;
    }
    
}