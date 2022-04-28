package com.earasoft.beam.pubmed.bean.medlineCitation;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;


public class MedlineCitation implements Serializable{
    private static final long serialVersionUID = -7234044235249732956L;
    
    private String PMID;
    private DateCompleted dateCompleted;
    private DateRevised dateRevised;
    private Article article;
    
    private MedlineJournalInfo medlineJournalInfo;
    private String citationSubset;
    private MeshHeadingList meshHeadingList;
    
    private ChemicalList chemicalList;
    
    
    
    public String getPMID() {
        return PMID;
    }
    
    @XmlElement(name = "PMID")
    public void setPMID(String PMID) {
        this.PMID = PMID;
    }
    
    public DateCompleted getDateCompleted() {
        return dateCompleted;
    }
    
    @XmlElement(name = "DateCompleted")
    public void setDateCompleted(DateCompleted dateCompleted) {
        this.dateCompleted = dateCompleted;
    }
    
    public DateRevised getDateRevised() {
        return dateRevised;
    }
    @XmlElement(name = "DateRevised")
    public void setDateRevised(DateRevised dateRevised) {
        this.dateRevised = dateRevised;
    }

    public Article getArticle() {
        return article;
    }
    
    @XmlElement(name = "Article")
    public void setArticle(Article article) {
        this.article = article;
    }

    public MedlineJournalInfo getMedlineJournalInfo() {
        return medlineJournalInfo;
    }
    
    @XmlElement(name = "MedlineJournalInfo")
    public void setMedlineJournalInfo(MedlineJournalInfo medlineJournalInfo) {
        this.medlineJournalInfo = medlineJournalInfo;
    }

    public String getCitationSubset() {
        return citationSubset;
    }
    
    @XmlElement(name = "CitationSubset")
    public void setCitationSubset(String citationSubset) {
        this.citationSubset = citationSubset;
    }

    public MeshHeadingList getMeshHeadingList() {
        return meshHeadingList;
    }
    
    @XmlElement(name = "MeshHeadingList")
    public void setMeshHeadingList(MeshHeadingList meshHeadingList) {
        this.meshHeadingList = meshHeadingList;
    }

    public ChemicalList getChemicalList() {
        return chemicalList;
    }
    
    @XmlElement(name = "ChemicalList")
    public void setChemicalList(ChemicalList chemicalList) {
        this.chemicalList = chemicalList;
    }
    
}