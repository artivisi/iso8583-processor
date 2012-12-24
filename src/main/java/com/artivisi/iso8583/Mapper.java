package com.artivisi.iso8583;

import java.util.HashMap;
import java.util.Map;

public class Mapper {
    private String name;
    private Map<Integer, DataElement> dataElement = new HashMap<Integer, DataElement>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Integer, DataElement> getDataElement() {
        return dataElement;
    }

    public void setDataElement(Map<Integer, DataElement> dataElement) {
        this.dataElement = dataElement;
    }
}
