/**
 * Copyright (C) 2012 ArtiVisi Intermedia <info@artivisi.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.artivisi.iso8583;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class ProcessorTest {

    @Test
    public void testBitshiftOperation() {
        String bitmapStream = "1010001000100000000000000000000000000000100000000000000000010000";
        BigInteger bitmap = new BigInteger(bitmapStream, 2);
        System.out.println(bitmap.toString(2));
        assertTrue("Bit 1 enabled", bitmap.testBit(64 - 1));
        assertFalse("Bit 2 enabled", bitmap.testBit(64 - 2));
        assertTrue("Bit 3 enabled", bitmap.testBit(64 - 3));
        assertFalse("Bit 64 enabled", bitmap.testBit(64 - 64));
    }

    @Test
    public void stringToIsoMessage() {
        String echoRequestStream = "0800A22000000080001004000000000000001010102012122408470800000110101010        019ArtiVisi Intermedia301";
        String echoResponseStream = "0810A2200000028000100400000000000000101010201212240850000000010010101010        019ArtiVisi Intermedia301";

        Message echoRequest = Processor.getInstance(configureMapper()).stringToMessage(echoRequestStream);
        assertNotNull(echoRequest.getPrimaryBitmap());
        assertEquals("A220000000800010", echoRequest.getPrimaryBitmapStream());

        assertNotNull(echoRequest.getSecondaryBitmap());
        assertTrue(echoRequest.isDataElementPresent(3));
        assertTrue(echoRequest.isDataElementPresent(7));
        assertTrue(echoRequest.isDataElementPresent(11));
        assertFalse(echoRequest.isDataElementPresent(39));
        assertTrue(echoRequest.isDataElementPresent(41));
        assertTrue(echoRequest.isDataElementPresent(60));
        assertTrue(echoRequest.isDataElementPresent(70));

        assertEquals("0800", echoRequest.getMti());
        assertEquals("101010", echoRequest.getDataElementContent().get(3));
        assertEquals("20121224084708", echoRequest.getDataElementContent().get(7));
        assertEquals("000001", echoRequest.getDataElementContent().get(11));
        assertEquals("10101010        ", echoRequest.getDataElementContent().get(41));
        assertEquals("ArtiVisi Intermedia", echoRequest.getDataElementContent().get(60));
        assertEquals("301", echoRequest.getDataElementContent().get(70));
        assertNull(echoRequest.getDataElementContent().get(39));

        Message echoResponse = Processor.getInstance(configureMapper()).stringToMessage(echoResponseStream);
        assertNotNull(echoResponse.getPrimaryBitmap());
        assertNotNull(echoResponse.getSecondaryBitmap());
        assertTrue(echoResponse.isDataElementPresent(3));
        assertTrue(echoResponse.isDataElementPresent(7));
        assertTrue(echoResponse.isDataElementPresent(11));
        assertTrue(echoResponse.isDataElementPresent(39));
        assertTrue(echoResponse.isDataElementPresent(41));
        assertFalse(echoResponse.isDataElementPresent(48));
        assertTrue(echoResponse.isDataElementPresent(60));
        assertTrue(echoResponse.isDataElementPresent(70));

        assertEquals("0810", echoResponse.getMti());
        assertEquals("101010", echoResponse.getDataElementContent().get(3));
        assertEquals("20121224085000", echoResponse.getDataElementContent().get(7));
        assertEquals("000001", echoResponse.getDataElementContent().get(11));
        assertEquals("00", echoResponse.getDataElementContent().get(39));
        assertEquals("10101010        ", echoResponse.getDataElementContent().get(41));
        assertEquals("ArtiVisi Intermedia", echoResponse.getDataElementContent().get(60));
        assertEquals("301", echoResponse.getDataElementContent().get(70));
        assertNull(echoResponse.getDataElementContent().get(48));
    }

    @Test
    public void isoMessageToString() {
        String echoRequestStream = "0800A22000000080001004000000000000001010102012122408470800000110101010        019ArtiVisi Intermedia301";
        String echoResponseStream = "0810A2200000028000100400000000000000101010201212240850000000010010101010        019ArtiVisi Intermedia301";

        Message echoRequest = new Message();
        echoRequest.setMti("0800");
        echoRequest.getDataElementContent().put(3, "101010");
        echoRequest.getDataElementContent().put(7, "20121224084708");
        echoRequest.getDataElementContent().put(11, "000001");
        echoRequest.getDataElementContent().put(41, "10101010        ");
        echoRequest.getDataElementContent().put(60, "ArtiVisi Intermedia");
        echoRequest.getDataElementContent().put(70, "301");

        assertEquals(echoRequestStream, Processor.getInstance(configureMapper()).messageToString(echoRequest));

        Message echoResponse = new Message();
        echoResponse.setMti("0810");
        echoResponse.getDataElementContent().put(3, "101010");
        echoResponse.getDataElementContent().put(7, "20121224085000");
        echoResponse.getDataElementContent().put(11, "000001");
        echoResponse.getDataElementContent().put(39, "00");
        echoResponse.getDataElementContent().put(41, "10101010        ");
        echoResponse.getDataElementContent().put(60, "ArtiVisi Intermedia");
        echoResponse.getDataElementContent().put(70, "301");

        assertEquals(echoResponseStream, Processor.getInstance(configureMapper()).messageToString(echoResponse));
    }

    private Mapper configureMapper() {
        Mapper m = new Mapper();
        m.getDataElement()
                .put(3,
                        new DataElement()
                        .setNumber(3)
                        .setLength(6)
                        .setLengthType(DataElementLength.FIXED)
                        .setType(DataElementType.NUMERIC)
                );

        m.getDataElement()
                .put(7,
                        new DataElement()
                        .setNumber(7)
                        .setLength(14)
                        .setLengthType(DataElementLength.FIXED)
                        .setType(DataElementType.NUMERIC)
                );

        m.getDataElement()
                .put(11,
                        new DataElement()
                        .setNumber(11)
                        .setLength(6)
                        .setLengthType(DataElementLength.FIXED)
                        .setType(DataElementType.NUMERIC)
                );

        m.getDataElement()
                .put(39,
                        new DataElement()
                        .setNumber(39)
                        .setLength(2)
                        .setLengthType(DataElementLength.FIXED)
                        .setType(DataElementType.NUMERIC)
                );

        m.getDataElement()
                .put(41,
                        new DataElement()
                        .setNumber(41)
                        .setLength(16)
                        .setLengthType(DataElementLength.FIXED)
                        .setType(DataElementType.ALPHANUMERIC)
                );

        m.getDataElement()
                .put(60,
                        new DataElement()
                        .setNumber(60)
                        .setLengthPrefix(3)
                        .setLengthType(DataElementLength.VARIABLE)
                        .setType(DataElementType.ALPHANUMERIC)
                );

        m.getDataElement()
                .put(70,
                        new DataElement()
                        .setNumber(70)
                        .setLength(3)
                        .setLengthType(DataElementLength.FIXED)
                        .setType(DataElementType.ALPHANUMERIC)
                );
        return m;
    }

}
