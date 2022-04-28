package com.earasoft.beam.pubmed.bean.medlineCitation;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Author Object
 * 
 * DTD: 
 * <!ELEMENT   Author (((LastName, ForeName?, Initials?, Suffix?) | CollectiveName), Identifier*, AffiliationInfo*) >
 * <!ATTLIST   Author 
 *             ValidYN (Y | N) "Y" 
 *             EqualContrib    (Y | N)  #IMPLIED >
 * 
 * <!ELEMENT    LastName (#PCDATA) >
 * <!ELEMENT    ForeName (#PCDATA) >
 * <!ELEMENT    Initials (#PCDATA) >
 * <!ELEMENT    Suffix (%text;)*>
 * <!ELEMENT    CollectiveName (%text;)*>
 * 
 * <!ELEMENT   AffiliationInfo (Affiliation, Identifier*)>
 * <!ELEMENT    Affiliation  (%text;)*>
 * 
 * <!ELEMENT    Identifier (#PCDATA) >
 * <!ATTLIST   Identifier 
 *    Source CDATA #REQUIRED >
 * 
 * @author erivera
 *
 */
public class Author implements Serializable{
    private static final long serialVersionUID = 7628957342308836809L;
    private String lastName;
    private String foreName;
    private String initials;
    private String suffix;
    private String collectiveName;
    private String validYN;
    private AffiliationInfo affiliationInfo;
    
    public AffiliationInfo getAffiliationInfo() {
        return affiliationInfo;
    }
    
    @XmlElement(name = "AffiliationInfo")
    public void setAffiliationInfo(AffiliationInfo affiliationInfo) {
        this.affiliationInfo = affiliationInfo;
    }

    public String getValidYN() {
        return validYN;
    }
    
    @XmlAttribute(name="ValidYN")
    public void setValidYN(String validYN) {
        this.validYN = validYN;
    }

    public String getLastName() {
        return lastName;
    }
    
    @XmlElement(name = "LastName")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getForeName() {
        return foreName;
    }
    @XmlElement(name = "ForeName")
    public void setForeName(String foreName) {
        this.foreName = foreName;
    }
    
    public String getInitials() {
        return initials;
    }
    
    @XmlElement(name = "Initials")
    public void setInitials(String initials) {
        this.initials = initials;
    }
    
    public String getSuffix() {
        return suffix;
    }
    
    @XmlElement(name = "Suffix")
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    
    public String getCollectiveName() {
        return collectiveName;
    }
    
    @XmlElement(name = "CollectiveName")
    public void setCollectiveName(String collectiveName) {
        this.collectiveName = collectiveName;
    }

    @Override
    public String toString() {
        return "Author [lastName=" + lastName + ", foreName=" + foreName + ", initials=" + initials + ", suffix="
                + suffix + ", collectiveName=" + collectiveName + ", validYN=" + validYN + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((affiliationInfo == null) ? 0 : affiliationInfo.hashCode());
        result = prime * result + ((collectiveName == null) ? 0 : collectiveName.hashCode());
        result = prime * result + ((foreName == null) ? 0 : foreName.hashCode());
        result = prime * result + ((initials == null) ? 0 : initials.hashCode());
        result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
        result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
        result = prime * result + ((validYN == null) ? 0 : validYN.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Author other = (Author) obj;
        if (affiliationInfo == null) {
            if (other.affiliationInfo != null)
                return false;
        } else if (!affiliationInfo.equals(other.affiliationInfo))
            return false;
        if (collectiveName == null) {
            if (other.collectiveName != null)
                return false;
        } else if (!collectiveName.equals(other.collectiveName))
            return false;
        if (foreName == null) {
            if (other.foreName != null)
                return false;
        } else if (!foreName.equals(other.foreName))
            return false;
        if (initials == null) {
            if (other.initials != null)
                return false;
        } else if (!initials.equals(other.initials))
            return false;
        if (lastName == null) {
            if (other.lastName != null)
                return false;
        } else if (!lastName.equals(other.lastName))
            return false;
        if (suffix == null) {
            if (other.suffix != null)
                return false;
        } else if (!suffix.equals(other.suffix))
            return false;
        if (validYN == null) {
            if (other.validYN != null)
                return false;
        } else if (!validYN.equals(other.validYN))
            return false;
        return true;
    }

}