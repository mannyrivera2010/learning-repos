package com.earasoft.beam.pubmed.bean;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@XmlRootElement(name = "PubmedArticleSet")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class PubmedArticleSet implements Serializable{
    private static Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();
    
    private static final long serialVersionUID = -1555351422828583738L;
    
    private List<PubmedArticle> pubmedArticle;
  
    public List<PubmedArticle> getPubmedArticle() {
        return pubmedArticle;
    }
    
    @XmlElement(name = "PubmedArticle")
    public void setPubmedArticle(List<PubmedArticle> pubmedArticle) {
        this.pubmedArticle = pubmedArticle;
    }
    
    @Override
    public String toString() {
        return gson.toJson(pubmedArticle);
    }
    
}
