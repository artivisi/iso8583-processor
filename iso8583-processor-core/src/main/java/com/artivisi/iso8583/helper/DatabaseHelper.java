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
package com.artivisi.iso8583.helper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author adi
 */
public class DatabaseHelper {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHelper.class);

    public static boolean hasColumn(ResultSet rs, String columnName) {
        try {
            ResultSetMetaData meta = rs.getMetaData();
            int numCol = meta.getColumnCount();
            
            for (int i = 1; i < numCol + 1; i++) {
                if (meta.getColumnName(i).equals(columnName)) {
                    return true;
                }
            }
            return false;
        } catch (SQLException ex) {
            LOGGER.error("method hasColumn has error ["+ex.getMessage()+"]", ex);
            return false;
        }
    }

}
