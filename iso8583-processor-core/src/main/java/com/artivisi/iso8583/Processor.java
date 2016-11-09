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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoField;

public class Processor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class);
    private static final Integer MTI_LENGTH = 4;
    private static final Integer BITMAP_LENGTH = 16;
    private static final Integer NUMBER_OF_DATA_ELEMENT = 128;
    private static Mapper mapper;
    private static Processor instance;

    public static Processor getInstance(Mapper mapper) {
        if (instance == null) {
            instance = new Processor();
        }
        instance.mapper = mapper;
        return instance;
    }
    
    public static Message stringToMessage(String stream){
        if(stream == null || stream.trim().length() < MTI_LENGTH + BITMAP_LENGTH) {
            throw new IllegalArgumentException("Invalid Message : ["+stream+"]");
        }

        int currentPosition = 0;

        Message m = new Message();
        m.setMti(stream.substring(0,MTI_LENGTH));

        currentPosition += MTI_LENGTH;

        String primaryBitmapStream = stream
                .substring(currentPosition, currentPosition + BITMAP_LENGTH);
        m.setPrimaryBitmapStream(primaryBitmapStream);
        currentPosition += BITMAP_LENGTH;

        LOGGER.debug("[STRING2MESSAGE] : Primary Bitmap Hex : [{}]", primaryBitmapStream);

        if(m.isDataElementPresent(1)) {
            String secondaryBitmapStream = stream
                    .substring(currentPosition,
                            currentPosition + BITMAP_LENGTH);
            LOGGER.debug("[STRING2MESSAGE] : Secondary Bitmap Hex : [{}]", secondaryBitmapStream);
            m.setSecondaryBitmapStream(secondaryBitmapStream);
            currentPosition += BITMAP_LENGTH;
        }


        // mulai dari 2, karena bitmap tidak diparsing
        for(int i=2; i <= NUMBER_OF_DATA_ELEMENT; i++){
            if(!m.isDataElementPresent(i)){
                continue;
            }

            DataElement de = mapper.getDataElement().get(i);
            if(de == null){
                LOGGER.error("[STRING2MESSAGE] - [DATA ELEMENT {}] : Not configured", i);
                throw new IllegalStateException("Invalid Mapper, Data Element [" + i + "] not configured");
            }

            if(DataElementLength.FIXED.equals(de.getLengthType())){
                if(de.getLength() == null || de.getLength() < 1){
                    LOGGER.error("[STRING2MESSAGE] - [DATA ELEMENT {}] : Length not configured for fixed length element", i);
                    throw new IllegalStateException("Invalid Mapper, Data Element [" + i + "] length not configured for fixed length element");
                }

                String data = stream.substring(currentPosition, currentPosition + de.getLength());
                m.getDataElementContent().put(i, data);
                currentPosition += de.getLength();
                continue;
            }

            if(DataElementLength.VARIABLE.equals(de.getLengthType())){
                if(de.getLengthPrefix() == null || de.getLengthPrefix() < 1){
                    LOGGER.error("[STRING2MESSAGE] - [DATA ELEMENT {}] : Length prefix not configured for variable length element", i);
                    throw new IllegalStateException("Invalid Mapper, Data Element [" + i + "] length prefix not configured for variable length element");
                }

                String strLength = stream.substring(currentPosition, currentPosition+de.getLengthPrefix());
                currentPosition += de.getLengthPrefix();
                try {
                    Integer length = Integer.parseInt(strLength);
                    String data = stream.substring(currentPosition, currentPosition + length);
                    m.getDataElementContent().put(i, data);
                    currentPosition += length;
                    continue;
                } catch (NumberFormatException err) {
                    LOGGER.error("[STRING2MESSAGE] - [DATA ELEMENT {}] : Length prefix [{}] cannot be parsed", new Object[]{i, strLength});
                    throw err;
                }
            }

            LOGGER.error("[STRING2MESSAGE] - [DATA ELEMENT {}] : Length type [{}] not fixed nor variable", new Object[]{i, de.getLengthType()});
            throw new IllegalStateException("Invalid Mapper, Data Element [" + i + "] length type ["+de.getLengthType()+"] not fixed nor variable");
        }

        return m;
    }

    public static String messageToString(Message message) {
        message.calculateBitmap();

        LOGGER.debug("[MESSAGE2STRING] - [MTI] : [{}]", message.getMti());
        LOGGER.debug("[MESSAGE2STRING] - [Primary Bitmap] : [{}]", message.getPrimaryBitmapStream());
        LOGGER.debug("[MESSAGE2STRING] - [Secondary Bitmap] : [{}]", message.getSecondaryBitmapStream());
        LOGGER.debug("[MESSAGE2STRING] - [Data Element Content] : [{}]", message.getDataElementContent().size());

        StringBuilder builder = new StringBuilder();
        builder.append(message.getMti());
        builder.append(message.getPrimaryBitmapStream());
        builder.append(message.getSecondaryBitmapStream());

        for(int i=2; i <= NUMBER_OF_DATA_ELEMENT; i++){
            if(!message.isDataElementPresent(i)) {
                continue;
            }
            
            LOGGER.debug("[PROCESSING] - [DATA ELEMENT {}]", i);
            DataElement de = mapper.getDataElement().get(i);
            if(de == null){
                LOGGER.error("[PROCESSING] - [DATA ELEMENT {}] : Not configured", i);
                throw new IllegalStateException("Invalid Mapper, Data Element [" + i + "] not configured");
            }

            LOGGER.debug("[PROCESSING] - [DATA ELEMENT {}] : Length : {}", i, de.getLengthType().name());
            if(DataElementLength.FIXED.equals(de.getLengthType())){
                if(de.getLength() == null || de.getLength() < 1){
                    LOGGER.error("[PROCESSING] - [DATA ELEMENT {}] : Length not configured for fixed length element", i);
                    throw new IllegalStateException("Invalid Mapper, Data Element [" + i + "] length not configured for fixed length element");
                }

                builder.append(message.getDataElementContent().get(i));
                continue;
            }

            if(DataElementLength.VARIABLE.equals(de.getLengthType())){
                if(de.getLengthPrefix() == null || de.getLengthPrefix() < 1){
                    LOGGER.error("[PROCESSING] - [DATA ELEMENT {}] : Length prefix not configured for variable length element", i);
                    throw new IllegalStateException("Invalid Mapper, Data Element [" + i + "] length prefix not configured for variable length element");
                }

                String data = message.getDataElementContent().get(i);
                if(data == null){
                    data = "";
                }
                Integer dataLength = data.length();

                builder.append(StringUtils.leftPad(dataLength.toString(), de.getLengthPrefix(), "0"));
                builder.append(data);
                continue;
            }

            LOGGER.error("[PROCESSING] - [DATA ELEMENT {}] : Length type [{}] not fixed nor variable", new Object[]{i, de.getLengthType()});
            throw new IllegalStateException("Invalid Mapper, Data Element [" + i + "] length type ["+de.getLengthType()+"] not fixed nor variable");
        }
        return builder.toString();
    }
    
    private static Map<String, Object> convertToMap(
            List<SubElement> subElements, String stream, 
            Integer[] repeatedNumber, Map<Integer, String> repeatedRange){
        
        if(subElements.isEmpty()) return null;
            
        Collections.sort(subElements, new SubElementsComparator());
        
//        Integer requiredLength = calculateLengthSubElement(subElements);
//        if(stream.length() < requiredLength) {
//            LOGGER.error("[CONVERT2MAP] - [SUBELEMENT] : Invalid stream length. expected:{} actual:{}", requiredLength, stream.length());
//            throw new IllegalStateException("Invalid Stream Length. Required Length is : " + requiredLength + " on DataElement:" + subElements.get(0).getDataElement().getNumber());
//        }
        
        int currentPosition = 0;
        Map<String, Object> result = new HashMap<>();
        
        for (SubElement sub : subElements) {
            try {
                String strData = stream.substring(currentPosition, currentPosition + sub.getLength());
                strData = removePadding(strData, sub.getPadding(), sub.getPaddingPosition());
                
                //Check if column is index for repeated
                //Build List Object from repeated value and skip next step
                if(repeatedNumber != null){
                    if(Arrays.asList(repeatedNumber).contains(sub.getNumber())){
                        LOGGER.info("DataElement [{}] has repeated data in index [{}]", 
                                sub.getDataElement().getElementName(), 
                                repeatedRange.get(sub.getNumber()));
                        currentPosition += inputToMap(sub, result, strData);
                        currentPosition += buildListObject(
                                result, subElements, sub.getNumber(), 
                                Integer.parseInt(strData), repeatedRange.get(sub.getNumber()), 
                                stream, currentPosition);
                        continue;
                    }

                    if(isRepeatedElement(sub.getNumber(), repeatedRange)){
                        LOGGER.debug("SubElement index[{}] is repeated column, skip this", sub.getNumber());
                        continue;
                    }
                }
                
                currentPosition += inputToMap(sub, result, strData);
            } catch (Exception ex) {
                LOGGER.error("{} : SubElement:[{}]", ex.getMessage(), sub.getNumber());
                throw new IllegalStateException(ex.getMessage(), ex);
            }
        }
        return result;
    }
    
    public static String getKeyMessage(Message message){
        String bitKey = mapper.getKeyMessage();
        if(bitKey==null || bitKey.length()<1){
            throw new IllegalStateException("Mapper " + mapper.getName() + " dont have Key Message");
        }
        
        String[] arrKey = bitKey.split("-");
        Integer bitRC = Integer.parseInt(arrKey[0]);
        String keymsg = "REQ";
        if(message.getDataElementContent().containsKey(bitRC)){
            keymsg = "RESP";
        }
        
        for(int i=1; i<arrKey.length; i++){
            if(arrKey[i].equalsIgnoreCase("MTI")){
                keymsg = keymsg.concat("-").concat(message.getMti());
            } else {
                keymsg = keymsg.concat("-").concat(message.getDataElementContent().get(Integer.parseInt(arrKey[i])));
            }
        }
        
        return keymsg;
    }
    
    private static class SubElementsComparator implements Comparator<SubElement> {

        @Override
        public int compare(SubElement o1, SubElement o2) {
            return o1.getNumber().compareTo(o2.getNumber());
        }
        
    }
    
    private static Integer calculateLengthSubElement(List<SubElement> subElements) {
        Integer resultLength = 0;
        for (SubElement sub : subElements) {
            resultLength += sub.getLength();
        }
        return resultLength;
    }
    
    private static String removePadding(String text, String padding, PaddingPosition position) throws Exception {
        if (text == null || text.length() < 1) {
            throw new Exception("Invalid text for remove padding");
        }

        switch (position) {
            case RIGHT:
                for (int i = text.length() - 1; i > -1; i--) {
                    if (text.charAt(i) == padding.charAt(0)) {
                        continue;
                    }
                    return text.substring(0, i + 1);
                }
                //Jika semua karakter sama seperti padding, maka return huruf pertama saja
                return text.substring(0, 1);
            case LEFT:
                for (int i = 0; i < text.length(); i++) {
                    if (text.charAt(i) == padding.charAt(0)) {
                        continue;
                    }
                    return text.substring(i);
                }
                //Jika semua karakter sama seperti padding, maka return huruf terakhir saja
                return text.substring(text.length()-1);
            case NOPAD:
                return text;
            default:
                break;
        }
        
        throw new Exception("Invalid Padding Position");
    }
    
    private static Integer inputToMap(SubElement sub, Map<String, Object> result, String strData) {
        switch (sub.getType()) {
            case ALPHANUMERIC:
                result.put(sub.getElementName(), strData);
                break;
            case NUMERIC:
                result.put(sub.getElementName(), Integer.parseInt(strData));
                break;
            case DECIMAL:
                {
                    if (sub.getTypeFormat() == null || sub.getTypeFormat().length() < 1) {
                        throw new IllegalStateException("Type Format not configured");
                    }       int idxDot = sub.getTypeFormat().indexOf(".");
                    int scale = 0;
                    if (idxDot > -1) {
                        scale = sub.getTypeFormat().substring(idxDot + 1).length();
                    }       BigDecimal value = new BigDecimal(strData).divide(BigDecimal.TEN.pow(scale), scale, RoundingMode.HALF_EVEN);
                    result.put(sub.getElementName(), value);
                    break;
                }
            case DATE:
                {
                    if (sub.getTypeFormat() == null || sub.getTypeFormat().length() < 1) {
                        throw new IllegalStateException("Type Format not configured");
                    }       DateTimeFormatter formatter = DateTimeFormatter.ofPattern(sub.getTypeFormat()).withZone(ZoneId.systemDefault());
                    Date value = new Date(formatter.parse(strData).getLong(ChronoField.MILLI_OF_SECOND));
                    result.put(sub.getElementName(), value);
                    break;
                }
            default:
                throw new IllegalStateException("Invalid Data Element Type : " + sub.getType().name());
        }
        
        return sub.getLength();
    }
    
    private static boolean isRepeatedElement(Integer number, Map<Integer, String> repeatedRange){
        if(repeatedRange==null){
            LOGGER.debug("SubElement Number {} is not repeated");
            return false;
        }
        
        for(String val : repeatedRange.values()){
            String[] arrValue = val.split("-");
            
            if(Integer.parseInt(arrValue[0]) >= number && number <= Integer.parseInt(arrValue[1])){
                return true;
            }
        }
        return false;
    }
    
    private static Integer buildListObject(
            Map<String, Object> result, List<SubElement> subElements, 
            Integer indexRepeat, Integer totalRepeat, String repeatRange, 
            String stream, Integer currentPosition) throws Exception{
        //Initial Object
        Integer lengthUsed = 0;
        List<Map<String, Object>> listRepeat = new ArrayList<>();
        
        //Loop by totalRepeat value
        for(int i=0; i<totalRepeat; i++){
            //Create Object for save, 1 record repeated part
            Map<String, Object> dataRepeat = new HashMap<>();
            String[] ranges = repeatRange.split("-");
            Integer minRange = Integer.parseInt(ranges[0]);
            Integer maxRange = Integer.parseInt(ranges[1]);
            
            for(int r=minRange; r<=maxRange; r++){
                SubElement se = subElements.get(r);
                String strData = stream.substring(currentPosition, currentPosition + se.getLength());
                strData = removePadding(strData, se.getPadding(), se.getPaddingPosition());
                
                //put key and value, for dataRepeat with method inputToMap
                lengthUsed += inputToMap(se, dataRepeat, strData);
            }
            
            listRepeat.add(dataRepeat);
        }
        
        result.put(subElements.get(indexRepeat).getElementName(), listRepeat);
        return lengthUsed;
    }
    
    public static Map<String, Object> stream2Map(List<SubElement> subElements, String stream) {
        DataElement de = subElements.get(0).getDataElement();
        String[] strRepeatedNumber = null;
        Integer[] repeatedNumber = null;
        Map<Integer, String> repeatedRange = null;
        
        if(de.getRepeatedColumnIndex() != null && de.getRepeatedColumnIndex().length() > 0){
            strRepeatedNumber = de.getRepeatedColumnIndex().split(",");
            
            if(de.getRepeatedColumnRange()==null){
                throw new IllegalStateException("Invalid NULL value on column repeated_range in data element : " + de.getElementName());
            }
            
            String[] strRepeatedRange = de.getRepeatedColumnRange().split(",");
            repeatedNumber = new Integer[strRepeatedNumber.length];
            repeatedRange = new HashMap<>();
            for(int i=0; i<strRepeatedNumber.length; i++){
                repeatedNumber[i] = Integer.parseInt(strRepeatedNumber[i]);
                repeatedRange.put(i, strRepeatedRange[i]);
            }
        }
        
        return convertToMap(subElements, stream, repeatedNumber, repeatedRange);
    }
}
