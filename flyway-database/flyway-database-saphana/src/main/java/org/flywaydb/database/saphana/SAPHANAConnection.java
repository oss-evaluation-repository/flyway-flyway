/*
 * Copyright (C) Red Gate Software Ltd 2010-2023
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
package org.flywaydb.database.saphana;

import org.flywaydb.core.internal.database.base.Connection;
import org.flywaydb.core.internal.database.base.Schema;
import org.flywaydb.core.internal.exception.FlywaySqlException;

import java.sql.SQLException;

public class SAPHANAConnection extends Connection<SAPHANADatabase> {
    private final boolean isCloud;

    SAPHANAConnection(SAPHANADatabase database, java.sql.Connection connection) {
        super(database, connection);
        try {
            String build = jdbcTemplate.queryForString("SELECT VALUE FROM M_HOST_INFORMATION WHERE KEY='build_branch'");
            // Cloud databases will be fa/CE<year>.<build> eg. fa/CE2020.48
            // On-premise will be fa/hana<version>sp<servicepack> eg. fa/hana2sp05
            isCloud = build.startsWith("fa/CE");
        } catch (SQLException e) {
            throw new FlywaySqlException("Unable to determine build edition", e);
        }
    }

    public boolean isCloudConnection() {
        return isCloud;
    }

    @Override
    protected String getCurrentSchemaNameOrSearchPath() throws SQLException {
        return jdbcTemplate.queryForString("SELECT CURRENT_SCHEMA FROM DUMMY");
    }

    @Override
    public void doChangeCurrentSchemaOrSearchPathTo(String schema) throws SQLException {
        jdbcTemplate.execute("SET SCHEMA " + database.doQuote(schema));
    }

    @Override
    public Schema getSchema(String name) {
        return new SAPHANASchema(jdbcTemplate, database, name);
    }
}