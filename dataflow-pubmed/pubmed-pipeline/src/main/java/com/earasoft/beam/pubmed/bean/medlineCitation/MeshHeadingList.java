package com.earasoft.beam.pubmed.bean.medlineCitation;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

/**
 * MeshHeadingList
 * @author erivera
 *
 */
public class MeshHeadingList {
    private List<MeshHeading> meshHeadings;

    public List<MeshHeading> getMeshHeadings() {
        return meshHeadings;
    }
    
    @XmlElement(name = "MeshHeading")
    public void setMeshHeadings(List<MeshHeading> meshHeadings) {
        this.meshHeadings = meshHeadings;
    }
    
}
