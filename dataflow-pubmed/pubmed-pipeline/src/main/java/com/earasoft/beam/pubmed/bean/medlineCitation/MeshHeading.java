package com.earasoft.beam.pubmed.bean.medlineCitation;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

class MeshHeading {
    List<DescriptorName> descriptorNames;
    List<QualifierName> qualifierNames;
    
    
    public List<DescriptorName> getDescriptorNames() {
        return descriptorNames;
    }
    
    @XmlElements(value = @XmlElement(name="DescriptorName") )
    public void setDescriptorNames(List<DescriptorName> descriptorNames) {
        this.descriptorNames = descriptorNames;
    }
    public List<QualifierName> getQualifierNames() {
        return qualifierNames;
    }
    
    @XmlElements(value = @XmlElement(name="QualifierName") )
    public void setQualifierNames(List<QualifierName> qualifierNames) {
        this.qualifierNames = qualifierNames;
    }
    
    
}
