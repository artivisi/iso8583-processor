--
-- Copyright (C) 2012 ArtiVisi Intermedia <info@artivisi.com>
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--         http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--         http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.

-- TABLE iso8583_mapper
CREATE TABLE iso8583_mapper (
  id character varying(36) NOT NULL,
  name character varying(35) NOT NULL,
  description character varying(255) DEFAULT NULL
);
ALTER TABLE ONLY iso8583_mapper
    ADD CONSTRAINT iso8583_mapper_pkey PRIMARY KEY (id);
ALTER TABLE ONLY iso8583_mapper
    ADD CONSTRAINT unique_iso8583_mapper UNIQUE (name);

-- TABLE iso8583_dataelement
CREATE TABLE iso8583_dataelement (
  id character varying(36) NOT NULL,
  id_mapper character varying(36) NOT NULL,
  dataelement_number integer NOT NULL,
  dataelement_name character varying(50) NOT NULL,
  dataelement_type character varying(12) NOT NULL,
  dataelement_length_type character varying(8) NOT NULL,
  dataelement_length integer DEFAULT NULL,
  dataelement_length_prefix integer DEFAULT NULL, 
  dataelement_repeated_colidx character varying (30) DEFAULT NULL, 
  dataelement_repeated_colrange character varying (30) DEFAULT NULL, 
  dataelement_subelement_separator character varying (1) DEFAULT NULL
);
ALTER TABLE ONLY iso8583_dataelement
    ADD CONSTRAINT iso8583_dataelement_pkey PRIMARY KEY (id);
ALTER TABLE ONLY iso8583_dataelement
    ADD CONSTRAINT unique_mapper_number UNIQUE (id_mapper,dataelement_number);
ALTER TABLE ONLY iso8583_dataelement
    ADD CONSTRAINT unique_mapper_name UNIQUE (id_mapper,dataelement_name);
ALTER TABLE ONLY iso8583_dataelement
    ADD CONSTRAINT fk_dataelement_mapper FOREIGN KEY (id_mapper) REFERENCES iso8583_mapper (id);

-- TABLE iso8583_subelement
CREATE TABLE iso8583_subelement (
  id character varying(36) NOT NULL,
  id_data_element character varying(36) NOT NULL,
  subelement_number integer NOT NULL,
  subelement_name character varying(50) NOT NULL,
  subelement_type character varying(12) NOT NULL,
  subelement_type_format character varying(20),
  subelement_length integer NOT NULL,
  subelement_padding character varying(1) DEFAULT NULL,
  subelement_padding_pos character varying(5) DEFAULT NULL,
  subelement_separator character varying(1) DEFAULT NULL
);
ALTER TABLE ONLY iso8583_subelement
    ADD CONSTRAINT iso8583_subelement_pkey PRIMARY KEY (id);
ALTER TABLE ONLY iso8583_subelement
    ADD CONSTRAINT unique_element_number UNIQUE (id_data_element,subelement_number);
ALTER TABLE ONLY iso8583_subelement
    ADD CONSTRAINT unique_element_name UNIQUE (id_data_element,subelement_name);
ALTER TABLE ONLY iso8583_subelement
    ADD CONSTRAINT fk_subelement_dataelement FOREIGN KEY (id_data_element) REFERENCES iso8583_dataelement (id);