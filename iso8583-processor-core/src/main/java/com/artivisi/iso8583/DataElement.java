/**
 * Copyright (C) 2012 ArtiVisi Intermedia <info@artivisi.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.artivisi.iso8583;

import java.util.ArrayList;
import java.util.List;

public class DataElement {
    private String id;
    private Mapper mapper;
    private Integer number;
    private String elementName;
    private DataElementType type;
    private DataElementLength lengthType;
    private Integer length = 0;
    private Integer lengthPrefix = 0;
    private String repeatedColumnIndex;
    private String repeatedColumnRange;
    private String subElementSeparator;
    private List<SubElement> subElements = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Mapper getMapper() {
        return mapper;
    }

    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public Integer getNumber() {
        return number;
    }

    public DataElement setNumber(Integer number) {
        this.number = number;
        return this;
    }

    public DataElementType getType() {
        return type;
    }

    public DataElement setType(DataElementType type) {
        this.type = type;
        return this;
    }

    public Integer getLength() {
        return length;
    }

    public DataElement setLength(Integer length) {
        this.length = length;
        return this;
    }

    public Integer getLengthPrefix() {
        return lengthPrefix;
    }

    public DataElement setLengthPrefix(Integer lengthPrefix) {
        this.lengthPrefix = lengthPrefix;
        return this;
    }

    public DataElementLength getLengthType() {
        return lengthType;
    }

    public DataElement setLengthType(DataElementLength lengthType) {
        this.lengthType = lengthType;
        return this;
    }

    public List<SubElement> getSubElements() {
        return subElements;
    }

    public DataElement setSubElements(List<SubElement> subElements) {
        this.subElements = subElements;
        return this;
    }

    public String getElementName() {
        return elementName;
    }

    public DataElement setElementName(String elementName) {
        this.elementName = elementName;
        return this;
    }

    public String getRepeatedColumnIndex() {
        return repeatedColumnIndex;
    }

    public void setRepeatedColumnIndex(String repeatedColumnIndex) {
        this.repeatedColumnIndex = repeatedColumnIndex;
    }

    public String getRepeatedColumnRange() {
        return repeatedColumnRange;
    }

    public void setRepeatedColumnRange(String repeatedColumnRange) {
        this.repeatedColumnRange = repeatedColumnRange;
    }

    public String getSubElementSeparator() {
        return subElementSeparator;
    }

    public void setSubElementSeparator(String subElementSeparator) {
        this.subElementSeparator = subElementSeparator;
    }
    
}
