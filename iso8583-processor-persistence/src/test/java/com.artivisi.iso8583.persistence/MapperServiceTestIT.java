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
package com.artivisi.iso8583.persistence;

import com.artivisi.iso8583.DataElement;
import com.artivisi.iso8583.DataElementLength;
import com.artivisi.iso8583.DataElementType;
import com.artivisi.iso8583.Mapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:com/artivisi/**/applicationContext.xml")
public class MapperServiceTestIT {
    @Autowired private MapperService mapperService;
    @Autowired private DataSource dataSource;

    @Test
    public void testFindByName(){
        Mapper m = mapperService.findMapperByName("demo");
        assertNotNull(m);
        assertEquals("demo", m.getName());
        assertNotNull(m.getDataElement());
        assertEquals(new Integer(7), new Integer(m.getDataElement().size()));
        assertNull(m.getDataElement().get(2));
        DataElement de = m.getDataElement().get(3);
        System.out.println("ID : "+de.getId());
        assertNotNull(de);
        assertEquals(new Integer(3), new Integer(de.getNumber()));
        assertEquals(DataElementType.NUMERIC, de.getType());
        assertEquals(DataElementLength.FIXED, de.getLengthType());
        assertEquals(new Integer(6), new Integer(de.getLength()));
        assertNull(de.getLengthPrefix());
    }

    @Test
    public void testFindAll(){
        List<Mapper> result = mapperService.findAllMapper(0,10);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testCrud() throws Exception {
        Mapper m = new Mapper();
        m.setName("tester");
        m.setDescription("xx");

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

        mapperService.save(m);
        assertNotNull(m.getId());

        Connection conn = dataSource.getConnection();
        verifyCountMapper(m, conn, 1);
        verifyMapper(m, conn, "tester", "xx");
        verifyCountDataelement(m, conn, 7);

        m.setName("tester2");
        m.setDescription("test edit");

        m.getDataElement()
                .put(48,
                        new DataElement()
                                .setNumber(48)
                                .setLengthPrefix(3)
                                .setLengthType(DataElementLength.VARIABLE)
                                .setType(DataElementType.ALPHANUMERIC)
                );

        mapperService.save(m);

        verifyCountMapper(m, conn, 1);
        verifyMapper(m, conn, "tester2", "test edit");
        verifyCountDataelement(m, conn, 8);

        mapperService.delete(m);

        verifyCountMapper(m, conn, 0);
        verifyCountDataelement(m, conn, 0);

        conn.close();
    }

    private void verifyCountDataelement(Mapper m, Connection conn, int count) throws SQLException {
        String sqlSelectDataelement = "select count(*) from iso8583_dataelement where id_mapper = ?";
        PreparedStatement psSelectDataelement = conn.prepareStatement(sqlSelectDataelement);
        psSelectDataelement.setString(1,m.getId());
        ResultSet rsSelectDataelement = psSelectDataelement.executeQuery();
        assertTrue(rsSelectDataelement.next());
        assertEquals(new Integer(count), new Integer(rsSelectDataelement.getInt(1)));
    }

    private void verifyMapper(Mapper m, Connection conn, String name, String description) throws SQLException {
        String sqlSelectMapper = "select * from iso8583_mapper where id = ?";
        PreparedStatement psSelectMapper = conn.prepareStatement(sqlSelectMapper);
        psSelectMapper.setString(1,m.getId());
        ResultSet rsSelectMapper = psSelectMapper.executeQuery();
        assertTrue(rsSelectMapper.next());
        assertEquals(name, rsSelectMapper.getString("name"));
        assertEquals(description, rsSelectMapper.getString("description"));
    }

    private void verifyCountMapper(Mapper m, Connection conn, int count) throws SQLException {
        String sqlSelectMapper = "select count(*) from iso8583_mapper where name = ?";
        PreparedStatement psSelectMapper = conn.prepareStatement(sqlSelectMapper);
        psSelectMapper.setString(1,m.getName());
        ResultSet rsSelectMapper = psSelectMapper.executeQuery();
        assertTrue(rsSelectMapper.next());
        assertEquals(new Integer(count), new Integer(rsSelectMapper.getInt(1)));
    }
}
