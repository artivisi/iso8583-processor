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
package com.artivisi.iso8583.persistence.service;

import com.artivisi.iso8583.DataElement;
import com.artivisi.iso8583.Mapper;
import com.artivisi.iso8583.SubElement;
import com.artivisi.iso8583.persistence.MapperService;
import com.artivisi.iso8583.persistence.dao.MapperDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service("mapperService") @Transactional
public class MapperServiceImpl implements MapperService {

    @Autowired private MapperDao mapperDao;

    @Override
    public void save(Mapper mapper) {
        if(mapper.getId() == null){
            mapper.setId(UUID.randomUUID().toString());
        }
        for (Integer key : mapper.getDataElement().keySet()){
            DataElement de = mapper.getDataElement().get(key);
            if(de.getId() == null){
                de.setId(UUID.randomUUID().toString());
            }
            de.setMapper(mapper);
        }
        mapperDao.save(mapper);
    }

    @Override
    public void delete(Mapper m) {
        mapperDao.delete(m);
    }

    @Override
    public Mapper findMapperById(String id) {
        if(!StringUtils.hasText(id)) {
            return null;
        }
        Mapper m =  mapperDao.findOne(id);
        if(m != null){
            m.getDataElement().size();
        }
        return m;
    }

    @Override
    public Mapper findMapperByName(String name) {
        if(!StringUtils.hasText(name)) {
            return null;
        }
        Mapper m =  mapperDao.findByName(name);
        if(m != null){
            m.getDataElement().size();
        }
        return m;
    }

    @Override
    public List<Mapper> findAllMapper(Integer start, Integer rows) {
        return mapperDao.findAll(start, rows);
    }

    @Override
    public Long countAllMapper() {
        return mapperDao.count();
    }

    @Override
    public List<SubElement> findSubElementByElementNumber(Integer elementNumber, String name, String keygroup) {
        return mapperDao.findSubElementByElementNumber(elementNumber, name, keygroup);
    }
}
