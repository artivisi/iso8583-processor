package com.artivisi.iso8583;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Message {
    private String mti;
    private BigInteger primaryBitmap;
    private BigInteger secondaryBitmap;
    private Map<Integer, String> dataElementContent = new HashMap<Integer, String>();

    public Boolean isDataElementPresent(int bit){
        if(bit < 1) {
            return false;
        }

        if(bit < 65) {
            return primaryBitmap.testBit(64 - bit);
        }

        if(secondaryBitmap == null) {
            return false;
        }

        return secondaryBitmap.testBit(128 - bit);
    }

    public String getPrimaryBitmapStream(){
        if(primaryBitmap == null){
            return null;
        }
        return primaryBitmap.toString(16).toUpperCase();
    }

    public String getSecondaryBitmapStream(){
        if(secondaryBitmap == null){
            return null;
        }
        return secondaryBitmap.toString(16).toUpperCase();
    }

    public void setPrimaryBitmapStream(String bitmap){
        this.primaryBitmap = new BigInteger(bitmap, 16);
    }

    public void setSecondaryBitmapStream(String bitmap){
        this.secondaryBitmap = new BigInteger(bitmap, 16);
    }

    public String getMti() {
        return mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    public BigInteger getPrimaryBitmap() {
        return primaryBitmap;
    }

    public void setPrimaryBitmap(BigInteger primaryBitmap) {
        this.primaryBitmap = primaryBitmap;
    }

    public BigInteger getSecondaryBitmap() {
        return secondaryBitmap;
    }

    public void setSecondaryBitmap(BigInteger secondaryBitmap) {
        this.secondaryBitmap = secondaryBitmap;
    }

    public Map<Integer, String> getDataElementContent() {
        return dataElementContent;
    }

    public void setDataElementContent(Map<Integer, String> dataElementContent) {
        this.dataElementContent = dataElementContent;
    }
}
