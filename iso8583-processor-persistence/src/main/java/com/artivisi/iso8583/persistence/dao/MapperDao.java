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
package com.artivisi.iso8583.persistence.dao;

import com.artivisi.iso8583.DataElement;
import com.artivisi.iso8583.DataElementLength;
import com.artivisi.iso8583.DataElementType;
import com.artivisi.iso8583.Mapper;
import com.artivisi.iso8583.PaddingPosition;
import com.artivisi.iso8583.SubElement;
import com.artivisi.iso8583.helper.DatabaseHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class MapperDao {

    //Query Table iso8583_mapper
    private static final String SQL_INSERT_MAPPER = 
            "INSERT INTO " +
            "       iso8583_mapper (id, name, description) " +
            "VALUES (:id, :name,:description)";
    private static final String SQL_COUNT_MAPPER = 
            "SELECT COUNT(m.id) FROM iso8583_mapper m";
    private static final String SQL_FIND_ALL_MAPPER = 
            "SELECT m.* FROM iso8583_mapper m limit ?,?";
    private static final String SQL_FIND_MAPPER_BY_ID = 
            "SELECT m.* FROM iso8583_mapper m where m.id=?";
    private static final String SQL_FIND_MAPPER_BY_NAME = 
            "SELECT m.* FROM iso8583_mapper m WHERE m.name=?";
    private static final String SQL_DELETE_MAPPER_BY_ID = 
            "DELETE FROM iso8583_mapper m WHERE m.id=?";

    //Query Table iso8583_dataelement
    private static final String SQL_FIND_ONE_DATAELEMENT = 
            "SELECT de.* FROM iso8583_dataelement de WHERE de.id=?";
    private static final String SQL_FIND_DATAELEMENT_BY_ID_MAPPER = 
            "SELECT de.* FROM iso8583_dataelement de WHERE de.id_mapper=?";
    private static final String SQL_INSERT_DATA_ELEMENT = 
            "INSERT INTO " + 
            "       iso8583_dataelement (" + 
            "           id, " + 
            "           id_mapper, " + 
            "           dataelement_number, " + 
            "           dataelement_name, " + 
            "           dataelement_type, " + 
            "           dataelement_length_type, " + 
            "           dataelement_length, " + 
            "           dataelement_length_prefix, " + 
            "           dataelement_repeated_colidx, " + 
            "           dataelement_repeated_colrange, " + 
            "           dataelement_subelement_separator" + 
            "       ) VALUES (:id, " + 
            "           :id_mapper, " + 
            "           :dataelement_number, " + 
            "           :dataelement_name, " + 
            "           :dataelement_type, " + 
            "           :dataelement_length_type, " + 
            "           :dataelement_length, " + 
            "           :dataelement_length_prefix, " + 
            "           :dataelement_repeated_colidx, " + 
            "           :dataelement_repeated_colrange, " + 
            "           :dataelement_subelement_separator " +
            "       )";
    private static final String SQL_DELETE_DATA_ELEMENT_BY_MAPPER = 
            "DELETE FROM iso8583_dataelement de WHERE de.id_mapper = ?";
    
    private static final String SQL_FIND_SUBELEMENT_BY_ID_ELEMENT = 
            "SELECT sub.id, " +
            "	    sub.subelement_number, " +
            "	    sub.subelement_name, " +
            "	    sub.subelement_type, " +
            "	    sub.subelement_type_format, " +
            "	    sub.subelement_length, " +
            "	    sub.subelement_padding, " +
            "	    sub.subelement_padding_pos, " + 
            "	    sub.subelement_keygroup " + 
            "FROM   iso8583_subelement sub " + 
            "WHERE  sub.id_data_element=?";
    private static final String SQL_FIND_SUBELEMENT_BY_ELEMENT_NUMBER_AND_MAPPER = 
                                "select	sub.id, " +
                                "	sub.subelement_number, " +
                                "	sub.subelement_name, " +
                                "	sub.subelement_type, " +
                                "	sub.subelement_type_format, " +
                                "	sub.subelement_length, " +
                                "	sub.subelement_padding, " +
                                "	sub.subelement_padding_pos,   " +
                                "	sub.subelement_keygroup,   " +
                                "	sub.id_data_element  " +
                                "FROM	iso8583_subelement sub " +
                                "LEFT JOIN iso8583_dataelement data " +
                                "     ON sub.id_data_element=data.id " +
                                "LEFT JOIN iso8583_mapper mapper " +
                                "     ON data.id_mapper = mapper.id " +
                                "WHERE	sub.subelement_keygroup = ? " +
                                "     AND data.dataelement_number = ? " +
                                "     AND mapper.name=? " +
                                "ORDER BY sub.subelement_number";
    private static final String SQL_INSERT_SUB_ELEMENT = 
            "INSERT INTO " +
            "       iso8583_subelement ( " + 
            "            id, " + 
            "            id_data_element, " + 
            "            subelement_number, " +
            "            subelement_name, " + 
            "            subelement_type, " + 
            "            subelement_type_format, " + 
            "            subelement_length, " + 
            "            subelement_padding, " +
            "            subelement_padding_pos, " + 
            "            subelement_keygroup" + 
            "       ) VALUES (" + 
            "            :id, " + 
            "            :id_data_element, " + 
            "            :subelement_number, " + 
            "            :subelement_name, " + 
            "            :subelement_type, " + 
            "            :subelement_type_format, " + 
            "            :subelement_length, " + 
            "            :subelement_padding, " + 
            "            :subelement_padding_pos, " +
            "            :subelement_keygroup " +
            "       )";
    private static final String SQL_DELETE_SUB_ELEMENT_BY_ID_DATA_ELEMENT = 
            "DELETE FROM iso8583_subelement se WHERE se.id_data_element = ?";

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void save(Mapper mapper) {
        mapper.setId(UUID.randomUUID().toString());
        SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(mapper);
        namedParameterJdbcTemplate.update(SQL_INSERT_MAPPER, namedParameters);

        for(Integer dataElement : mapper.getDataElement().keySet()) {
            DataElement de = mapper.getDataElement().get(dataElement);

            Map<String, Object> params = new HashMap<>();
            params.put("id", UUID.randomUUID().toString());
            params.put("id_mapper", mapper.getId());
            params.put("dataelement_number", dataElement);
            params.put("dataelement_name", de.getElementName());
            params.put("dataelement_type", de.getType().name());
            params.put("dataelement_length_type", de.getLengthType().name());
            params.put("dataelement_length", de.getLength());
            params.put("dataelement_length_prefix", de.getLengthPrefix());
            params.put("dataelement_repeated_colidx", de.getRepeatedColumnIndex());
            params.put("dataelement_repeated_colrange", de.getRepeatedColumnRange());
            params.put("dataelement_subelement_separator", de.getSubElementSeparator());

            namedParameterJdbcTemplate.update(SQL_INSERT_DATA_ELEMENT, params);
            
            for (SubElement sub : de.getSubElements()) {
                Map<String, Object> subParams = new HashMap<>();
                subParams.put("id", UUID.randomUUID().toString());
                subParams.put("id_data_element", params.get("id"));
                subParams.put("subelement_number", sub.getNumber());
                subParams.put("subelement_name", sub.getElementName());
                subParams.put("subelement_type", sub.getType().name());
                subParams.put("subelement_type_format", sub.getTypeFormat());
                subParams.put("subelement_length", sub.getLength());
                subParams.put("subelement_padding", sub.getPadding());
                subParams.put("subelement_padding_pos", sub.getPaddingPosition().name());
                subParams.put("subelement_keygroup", sub.getKeyGroup());
                
                namedParameterJdbcTemplate.update(SQL_INSERT_SUB_ELEMENT, subParams);
            }
        }
    }

    public void delete(Mapper m) {
        for(DataElement de : m.getDataElement().values()){
            jdbcTemplate.update(SQL_DELETE_SUB_ELEMENT_BY_ID_DATA_ELEMENT, de.getId());
        }
        
        jdbcTemplate.update(SQL_DELETE_DATA_ELEMENT_BY_MAPPER,m.getId());
        jdbcTemplate.update(SQL_DELETE_MAPPER_BY_ID,m.getId());
    }

    public Mapper findOne(String id) {
        if(!StringUtils.hasText(id)) {
            return null;
        }
        Mapper m = jdbcTemplate.queryForObject(SQL_FIND_MAPPER_BY_ID, new MapperFromResultSet(), id);
        loadDataElement(m);
        return m;
    }

    public Mapper findByName(String name) {
        if(!StringUtils.hasText(name)) {
            return null;
        }
        Mapper m = jdbcTemplate.queryForObject(SQL_FIND_MAPPER_BY_NAME, new MapperFromResultSet(), name);
        loadDataElement(m);
        return m;
    }

    private void loadDataElement(Mapper m) {
        if(m == null){
            return;
        }
        List<DataElement> deList = jdbcTemplate
                .query(SQL_FIND_DATAELEMENT_BY_ID_MAPPER, new DataElementFromResultSet(), m.getId());

        for (DataElement d : deList) {
            List<SubElement> subList = jdbcTemplate.query(SQL_FIND_SUBELEMENT_BY_ID_ELEMENT, new SubElementFromResultSet(), d.getId());
            for (SubElement se : subList) {
                se.setDataElement(d);
                d.getSubElements().add(se);
            }
            
            d.setMapper(m);
            m.getDataElement().put(d.getNumber(), d);
        }
    }

    public Long count() {
        return jdbcTemplate.queryForObject(SQL_COUNT_MAPPER, Long.class);
    }

    public List<Mapper> findAll(Integer start, Integer rows) {
        if(start == null | start < 0){
            start = 0;
        }

        if(rows == null || rows < 0){
            rows = 20;
        }

        return jdbcTemplate.query(SQL_FIND_ALL_MAPPER, new MapperFromResultSet(), start, rows);
    }
    
    public List<SubElement> findSubElementByElementNumber(Integer elementNumber, String mapperName, String keygroup) {
        if(!StringUtils.hasText(mapperName) || elementNumber==null || !StringUtils.hasText(keygroup)) {
            return null;
        }
        List<SubElement> subElements = jdbcTemplate.query(
                SQL_FIND_SUBELEMENT_BY_ELEMENT_NUMBER_AND_MAPPER, new SubElementFromResultSet(), 
                keygroup, elementNumber, mapperName);
        return subElements;
    }

    private class MapperFromResultSet implements RowMapper<Mapper>{

        @Override
        public Mapper mapRow(ResultSet resultSet, int i) throws SQLException {
            Mapper m = new Mapper();
            m.setId(resultSet.getString("id"));
            m.setName(resultSet.getString("name"));
            m.setDescription(resultSet.getString("description"));
            m.setKeyMessage(resultSet.getString("keymsg"));
            return m;
        }
    }

    private class DataElementFromResultSet implements RowMapper<DataElement>{

        @Override
        public DataElement mapRow(ResultSet resultSet, int i) throws SQLException {
            DataElement de = new DataElement();
            de.setId(resultSet.getString("id"));
            de.setLength((Integer)resultSet.getObject("dataelement_length"));
            de.setLengthPrefix((Integer)resultSet.getObject("dataelement_length_prefix"));
            de.setLengthType(DataElementLength.valueOf(resultSet.getString("dataelement_length_type")));
            de.setNumber((Integer)resultSet.getObject("dataelement_number"));
            de.setElementName((String) resultSet.getString("dataelement_name"));
            de.setType(DataElementType.valueOf(resultSet.getString("dataelement_type")));
            de.setRepeatedColumnIndex(resultSet.getString("dataelement_repeated_colidx"));
            de.setRepeatedColumnRange(resultSet.getString("dataelement_repeated_colrange"));
            de.setSubElementSeparator(resultSet.getString("dataelement_subelement_separator"));
            return de;
        }
    }
    
    private class SubElementFromResultSet implements RowMapper<SubElement>{

        @Override
        public SubElement mapRow(ResultSet resultSet, int i) throws SQLException {
            SubElement se = new SubElement();
            se.setId(resultSet.getString("id"));
            se.setNumber((Integer)resultSet.getObject("subelement_number"));
            se.setElementName((String) resultSet.getString("subelement_name"));
            se.setType(DataElementType.valueOf(resultSet.getString("subelement_type")));
            se.setTypeFormat(resultSet.getString("subelement_type_format"));
            se.setLength((Integer)resultSet.getObject("subelement_length"));
            se.setPadding(resultSet.getString("subelement_padding"));
            se.setPaddingPosition(PaddingPosition.valueOf(resultSet.getString("subelement_padding_pos")));
            se.setKeyGroup(resultSet.getString("subelement_keygroup"));
            if(DatabaseHelper.hasColumn(resultSet, "id_data_element")){
                if(resultSet.getString("id_data_element") != null) {
                    se.setDataElement(jdbcTemplate.queryForObject(SQL_FIND_ONE_DATAELEMENT, new DataElementFromResultSet(), 
                            resultSet.getString("id_data_element")));
                }
            }
            
            return se;
        }
    }
}
