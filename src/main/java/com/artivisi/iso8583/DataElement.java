package com.artivisi.iso8583;

public class DataElement {
    private Integer number;
    private DataElementType type;
    private DataElementLength lengthType;
    private Integer length = 0;
    private Integer lengthPrefix = 0;

    public DataElement() {
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
}
