package com.earasoft.beam.pubmed.bean.pubmedData;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

class PubMedPubDate implements Serializable{
    private static final long serialVersionUID = 3367628858178635110L;
    
    private String pubStatus;
    
    private String year;
    private String month;
    private String day;
    
    private String hour;
    private String minute;
    
    public String getPubStatus() {
        return pubStatus;
    }
    
    @XmlAttribute(name="PubStatus")
    public void setPubStatus(String pubStatus) {
        this.pubStatus = pubStatus;
    }
    
    public String getYear() {
        return year;
    }
    @XmlElement(name = "Year")
    public void setYear(String year) {
        this.year = year;
    }
    public String getMonth() {
        return month;
    }
    @XmlElement(name = "Month")
    public void setMonth(String month) {
        this.month = month;
    }
    public String getDay() {
        return day;
    }
    @XmlElement(name = "Day")
    public void setDay(String day) {
        this.day = day;
    }
    
    public String getHour() {
        return hour;
    }
    
    @XmlElement(name = "Hour")
    public void setHour(String hour) {
        this.hour = hour;
    }
    
    public String getMinute() {
        return minute;
    }
    
    @XmlElement(name = "Minute")
    public void setMinute(String minute) {
        this.minute = minute;
    }
    
}
