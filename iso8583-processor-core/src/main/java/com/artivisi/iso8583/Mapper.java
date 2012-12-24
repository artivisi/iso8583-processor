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

import java.util.HashMap;
import java.util.Map;

public class Mapper {
    private String name;
    private Map<Integer, DataElement> dataElement = new HashMap<Integer, DataElement>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Integer, DataElement> getDataElement() {
        return dataElement;
    }

    public void setDataElement(Map<Integer, DataElement> dataElement) {
        this.dataElement = dataElement;
    }
}
