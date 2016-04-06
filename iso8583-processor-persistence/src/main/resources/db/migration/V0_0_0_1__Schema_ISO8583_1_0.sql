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

CREATE TABLE `iso8583_mapper` (
  `id` varchar(36) NOT NULL,
  `name` varchar(35) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_mapper_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `iso8583_dataelement` (
  `id` varchar(36) NOT NULL,
  `id_mapper` varchar(36) NOT NULL,
  `dataelement_number` int(11) NOT NULL,
  `dataelement_name` varchar(50) NOT NULL,
  `dataelement_type` varchar(12) NOT NULL,
  `dataelement_length_type` varchar(8) NOT NULL,
  `dataelement_length` int(11) DEFAULT NULL,
  `dataelement_length_prefix` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`,`dataelement_number`),
  UNIQUE KEY `unique_mapper_number` (`id_mapper`,`dataelement_number`),
  UNIQUE KEY `unique_mapper_name` (`id_mapper`,`dataelement_name`),
  CONSTRAINT `fk_dataelement_mapper` FOREIGN KEY (`id_mapper`) REFERENCES `iso8583_mapper` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE `iso8583_subelement` (
  `id` varchar(36) NOT NULL,
  `id_data_element` varchar(36) NOT NULL,
  `subelement_number` int(11) NOT NULL,
  `subelement_type` varchar(12) NOT NULL,
  `subelement_name` varchar(50) NOT NULL,
  `subelement_length` int(11) NOT NULL,
  `subelement_padding` varchar(1) DEFAULT NULL,
  `subelement_padding_pos` varchar(5) DEFAULT NULL,
  `subelement_separator` varchar(1) DEFAULT NULL,
  `subelement_repeated` bit(1) NOT NULL DEFAULT FALSE,
  PRIMARY KEY (`id`,`subelement_number`),
  UNIQUE KEY `unique_element_number` (`id_data_element`,`subelement_number`),
  UNIQUE KEY `unique_element_name` (`id_data_element`,`subelement_name`),
  CONSTRAINT `fk_subelement_dataelement` FOREIGN KEY (`id_data_element`) REFERENCES `iso8583_dataelement` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;