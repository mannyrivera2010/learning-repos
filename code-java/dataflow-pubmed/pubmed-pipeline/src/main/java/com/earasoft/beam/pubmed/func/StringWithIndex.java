package com.earasoft.beam.pubmed.func;

import java.util.ArrayList;
import java.util.List;

/**
 * String with Index Value
 * @author erivera
 *
 */
public class StringWithIndex{
    private String value;
    private List<Integer> index;
    
    public StringWithIndex(String value, Integer index) {
        super();
        this.value = value;
        this.index = new ArrayList<Integer>();
        this.index.add(index);
    }
    
    public StringWithIndex(String value, List<Integer> index) {
        super();
        this.value = value;
        this.index = index;
    }

    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }

    public List<Integer> getIndex() {
        return index;
    }


    public void setIndex(List<Integer> index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return value + " " + index + "";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((index == null) ? 0 : index.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        StringWithIndex other = (StringWithIndex) obj;
        if (index == null) {
            if (other.index != null)
                return false;
        } else if (!index.equals(other.index))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
    
    
    
}