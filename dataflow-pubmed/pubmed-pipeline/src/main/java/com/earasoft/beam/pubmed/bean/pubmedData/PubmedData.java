package com.earasoft.beam.pubmed.bean.pubmedData;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

/**
 * PubmedData
 * 
 * DTD:
 * <!ELEMENT    PubmedData (History?, PublicationStatus, ArticleIdList, ObjectList?, ReferenceList*) >
 * 
 * <!ELEMENT    History (PubMedPubDate+) >
 * 
 * <!ELEMENT   PubMedPubDate (Year, Month, Day, (Hour, (Minute, Second?)?)?)>
 * <!ATTLIST   PubMedPubDate
 *     PubStatus (received | accepted | epublish | ppublish | revised | aheadofprint | 
 *                retracted | ecollection | pmc | pmcr | pubmed | pubmedr | 
 *                premedline | medline | medliner | entrez | pmc-release) #REQUIRED >
 * 
 * <!ELEMENT    PublicationStatus (#PCDATA) >
 * 
 * <!ELEMENT    ArticleId (#PCDATA) >
 * <!ATTLIST   ArticleId
 *     IdType (doi | pii | pmcpid | pmpid | pmc | mid 
 *             |sici | pubmed | medline | pmcid | pmcbook | bookaccession) "pubmed" >
 *
 * <!ELEMENT  ArticleIdList (ArticleId+)>
 *
 * @author erivera
 *
 */
public class PubmedData implements Serializable{
    private static final long serialVersionUID = -8836393084511494247L;
    
    private History history;
    private String publicationStatus;
    private ArticleIdList articleIdList;
    
    public History getHistory() {
        return history;
    }
    
    @XmlElement(name = "History")
    public void setHistory(History history) {
        this.history = history;
    }

    public String getPublicationStatus() {
        return publicationStatus;
    }

    @XmlElement(name = "PublicationStatus")
    public void setPublicationStatus(String publicationStatus) {
        this.publicationStatus = publicationStatus;
    }

    public ArticleIdList getArticleIdList() {
        return articleIdList;
    }
    
    @XmlElement(name = "ArticleIdList")
    public void setArticleIdList(ArticleIdList articleIdList) {
        this.articleIdList = articleIdList;
    }
    
}