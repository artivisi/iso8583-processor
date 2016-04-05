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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Message {
    private static final Logger LOGGER = LoggerFactory.getLogger(Message.class);
    private String mti;
    private BigInteger primaryBitmap;
    private BigInteger secondaryBitmap;
    private Map<Integer, String> dataElementContent = new HashMap<Integer, String>();

    public Message createCopy(){
        Message hasil = new Message();
        hasil.setMti(this.mti);
        hasil.getDataElementContent().putAll(this.getDataElementContent());
        hasil.calculateBitmap();
        return hasil;
    }

    public void calculateBitmap(){
        LOGGER.debug("Number of active Data Element [{}]", dataElementContent.size());
        BigInteger bitmap = BigInteger.ZERO.setBit(128);
        for(Integer de : dataElementContent.keySet()){
            if(de > 64){
                bitmap = bitmap.setBit(128 - 1);
            }
            bitmap = bitmap.setBit(128 - de);
        }
        LOGGER.debug("Final bitmap bin : [{}]", bitmap.toString(2).substring(1));
        LOGGER.debug("Final bitmap hex : [{}]", bitmap.toString(16).substring(1));
        setPrimaryBitmapStream(StringUtils.rightPad(bitmap.toString(16).substring(1,17), 16, "0"));
        if(bitmap.testBit(128 - 1)) {
            setSecondaryBitmapStream(StringUtils.rightPad(bitmap.toString(16).substring(17), 16, "0"));
        }
    }

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
        return StringUtils.leftPad(primaryBitmap.toString(16).toUpperCase(), 16, "0");
    }

    public String getSecondaryBitmapStream(){
        if(secondaryBitmap == null || BigInteger.ZERO.equals(secondaryBitmap)){
            return "";
        }
        return StringUtils.leftPad(secondaryBitmap.toString(16).toUpperCase(), 16, "0");
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
