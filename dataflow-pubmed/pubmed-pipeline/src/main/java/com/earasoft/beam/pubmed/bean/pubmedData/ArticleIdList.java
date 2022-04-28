package com.earasoft.beam.pubmed.bean.pubmedData;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

class ArticleIdList implements Serializable{
    private static final long serialVersionUID = -3131547502184369730L;
    private List<ArticleId> articleIds;

    public List<ArticleId> getArticleIds() {
        return articleIds;
    }
    
    @XmlElements(value = @XmlElement(name="ArticleId") )
    public void setArticleIds(List<ArticleId> articleIds) {
        this.articleIds = articleIds;
    }
    
}
