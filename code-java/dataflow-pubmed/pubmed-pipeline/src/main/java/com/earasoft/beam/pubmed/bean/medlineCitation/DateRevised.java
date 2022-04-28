package com.earasoft.beam.pubmed.bean.medlineCitation;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

class DateRevised implements Serializable{
    private static final long serialVersionUID = -2998777978218745120L;
    private String year;
    private String month;
    private String day;
    
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
    
}