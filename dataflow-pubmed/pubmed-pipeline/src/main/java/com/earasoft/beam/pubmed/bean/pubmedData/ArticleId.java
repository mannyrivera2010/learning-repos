package com.earasoft.beam.pubmed.bean.pubmedData;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;

class ArticleId implements Serializable{
    private static final long serialVersionUID = -2505738353262258129L;
    
    private String idType;
    private String articleId;
    
    public String getIdType() {
        return this.idType;
    }   
    
    @XmlAttribute(name="IdType")
    public void setIdType(String idType) {
        this.idType = idType;
    }
    
    public String getArticleId() {
        return articleId;
    }
    
    @XmlValue
    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }
    
}
