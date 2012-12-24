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

public class Processor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class);
    private static final Integer HEXADECIMAL = 16;
    private static final Integer BINARY = 2;
    private static final Integer MTI_LENGTH = 4;
    private static final Integer BITMAP_LENGTH = 16;
    private static final Integer NUMBER_OF_DATA_ELEMENT = 128;
    private Mapper mapper;

    public Mapper getMapper() {
        return mapper;
    }

    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public Message stringToMessage(String stream){
        LOGGER.debug("[STRING2MESSAGE] : [{}]", stream);
        if(stream == null || stream.trim().length() < MTI_LENGTH + BITMAP_LENGTH) {
            LOGGER.error("[STRING2MESSAGE] : Invalid Message [{}]", stream);
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

        String primaryBitmapBinary = new BigInteger(primaryBitmapStream, HEXADECIMAL).toString(BINARY);
        LOGGER.debug("[STRING2MESSAGE] : Primary Bitmap Bin : [{}]", primaryBitmapBinary);

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

    public String messageToString(Message message) {
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
            LOGGER.info("[PROCESSING] - [DATA ELEMENT {}]", i);
            DataElement de = mapper.getDataElement().get(i);
            if(de == null){
                LOGGER.error("[PROCESSING] - [DATA ELEMENT {}] : Not configured", i);
                throw new IllegalStateException("Invalid Mapper, Data Element [" + i + "] not configured");
            }

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
}
