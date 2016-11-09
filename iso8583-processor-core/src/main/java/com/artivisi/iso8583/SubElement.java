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

/**
 *
 * @author adi
 */
public class SubElement {
    
    private String id;
    private DataElement dataElement;
    private Integer number;
    private String elementName;
    private DataElementType type;
    private String typeFormat;
    private Integer length = 0;
    private String padding;
    private PaddingPosition paddingPosition;
    private String keyGroup;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DataElement getDataElement() {
        return dataElement;
    }

    public void setDataElement(DataElement dataElement) {
        this.dataElement = dataElement;
    }

    public DataElementType getType() {
        return type;
    }

    public void setType(DataElementType type) {
        this.type = type;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public String getPadding() {
        return padding;
    }

    public void setPadding(String padding) {
        this.padding = padding;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public PaddingPosition getPaddingPosition() {
        return paddingPosition;
    }

    public void setPaddingPosition(PaddingPosition paddingPosition) {
        this.paddingPosition = paddingPosition;
    }

    public String getTypeFormat() {
        return typeFormat;
    }

    public void setTypeFormat(String typeFormat) {
        this.typeFormat = typeFormat;
    }

    public String getKeyGroup() {
        return keyGroup;
    }

    public void setKeyGroup(String keyGroup) {
        this.keyGroup = keyGroup;
    }
    
}
