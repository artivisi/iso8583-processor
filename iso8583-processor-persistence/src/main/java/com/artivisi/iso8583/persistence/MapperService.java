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

import com.artivisi.iso8583.Mapper;
import com.artivisi.iso8583.SubElement;
import java.util.List;

public interface MapperService {
    void save(Mapper mapper);
    Mapper findMapperById(String id);
    Mapper findMapperByName(String name);
    List<Mapper> findAllMapper(Integer start, Integer rows);
    Long countAllMapper();
    void delete(Mapper m);
    List<SubElement> findSubElementByElementNumber(Integer elementNumber, String name);
}
