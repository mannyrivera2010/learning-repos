package com.earasoft.beam.pubmed.bean.medlineCitation;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

/**
 * 
 * @author erivera
 *
 */
public class DateCompleted implements Serializable{
    private static final long serialVersionUID = -279200186808263346L;
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