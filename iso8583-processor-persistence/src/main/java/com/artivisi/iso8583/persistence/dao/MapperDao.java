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

    private static final String SQL_INSERT_MAPPER = "insert into iso8583_mapper (id, name, description) values (:id, :name,:description)";
    private static final String SQL_COUNT_MAPPER = "select count(*) from iso8583_mapper";
    private static final String SQL_FIND_ALL_MAPPER = "select * from iso8583_mapper limit ?,?";
    private static final String SQL_FIND_MAPPER_BY_ID = "select * from iso8583_mapper where id=?";
    private static final String SQL_FIND_MAPPER_BY_NAME = "select * from iso8583_mapper where name=?";
    private static final String SQL_DELETE_MAPPER_BY_ID = "delete from iso8583_mapper where id=?";

    private static final String SQL_FIND_DATAELEMENT_BY_ID_MAPPER = "select * from iso8583_dataelement where id_mapper=?";
    private static final String SQL_INSERT_DATA_ELEMENT = "insert into iso8583_dataelement " +
            "(id, id_mapper, dataelement_number, dataelement_name, dataelement_type, dataelement_length_type, dataelement_length, dataelement_length_prefix) " +
            "values (:id, :id_mapper, :dataelement_number, :dataelement_name, :dataelement_type, :dataelement_length_type, :dataelement_length, :dataelement_length_prefix)";
    private static final String SQL_DELETE_DATA_ELEMENT_BY_MAPPER = "delete from iso8583_dataelement where id_mapper = ?";
    
    private static final String SQL_FIND_SUBELEMENT_BY_ID_ELEMENT = "select * from iso8583_subelement where id_data_element=?";
    private static final String SQL_INSERT_SUB_ELEMENT = "insert into iso8583_subelement "
            + "(id, id_data_element, subelement_type, subelement_number, subelement_name, subelement_length, subelement_padding, subelement_padding_pos, subelement_separator, subelement_repeated) "
            + "values (:id, :id_data_element, :subelement_type, :subelement_number, :subelement_name, :subelement_length, :subelement_padding, :subelement_padding_pos, :subelement_separator, :subelement_repeated)";
    private static final String SQL_DELETE_SUB_ELEMENT_BY_ID_DATA_ELEMENT = "delete from iso8583_subelement where id_data_element = ?";

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

            namedParameterJdbcTemplate.update(SQL_INSERT_DATA_ELEMENT, params);
            
            for (SubElement sub : de.getSubElements()) {
                Map<String, Object> subParams = new HashMap<>();
                subParams.put("id", UUID.randomUUID().toString());
                subParams.put("id_data_element", params.get("id"));
                subParams.put("subelement_type", sub.getType().name());
                subParams.put("subelement_number", sub.getNumber());
                subParams.put("subelement_name", sub.getElementName());
                subParams.put("subelement_length", sub.getLength());
                subParams.put("subelement_padding", sub.getPadding());
                subParams.put("subelement_padding_pos", sub.getPaddingPosition().name());
                subParams.put("subelement_separator", sub.getSeparatorChar());
                subParams.put("subelement_repeated", sub.getRepeated());
                
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

    private class MapperFromResultSet implements RowMapper<Mapper>{

        @Override
        public Mapper mapRow(ResultSet resultSet, int i) throws SQLException {
            Mapper m = new Mapper();
            m.setId(resultSet.getString("id"));
            m.setName(resultSet.getString("name"));
            m.setDescription(resultSet.getString("description"));
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
            return de;
        }
    }
    
    private class SubElementFromResultSet implements RowMapper<SubElement>{

        @Override
        public SubElement mapRow(ResultSet resultSet, int i) throws SQLException {
            SubElement se = new SubElement();
            se.setId(resultSet.getString("id"));
            se.setLength((Integer)resultSet.getObject("subelement_length"));
            se.setNumber((Integer)resultSet.getObject("subelement_number"));
            se.setElementName((String) resultSet.getString("subelement_name"));
            se.setType(DataElementType.valueOf(resultSet.getString("subelement_type")));
            se.setPadding(resultSet.getString("subelement_padding"));
            se.setPaddingPosition(PaddingPosition.valueOf(resultSet.getString("subelement_padding_pos")));
            se.setRepeated(resultSet.getBoolean("subelement_repeated"));
            se.setSeparatorChar(resultSet.getString("subelement_separator"));
            return se;
        }
    }
}
