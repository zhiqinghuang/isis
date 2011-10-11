/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.runtimes.dflt.objectstores.sql.jdbc;

import org.joda.time.LocalDate;

import org.apache.isis.applib.PersistFailedException;
import org.apache.isis.applib.value.Date;
import org.apache.isis.core.commons.exceptions.IsisApplicationException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.objectstores.sql.Defaults;
import org.apache.isis.runtimes.dflt.objectstores.sql.Results;
import org.apache.isis.runtimes.dflt.objectstores.sql.mapping.FieldMapping;
import org.apache.isis.runtimes.dflt.objectstores.sql.mapping.FieldMappingFactory;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;

/**
 * Handles reading and writing java.sql.Date and org.apache.isis.applib.value.Date values to and from the data store.
 * 
 * 
 * @version $Rev$ $Date$
 */
public class JdbcDateMapper extends AbstractJdbcFieldMapping {

    private final AdapterManager adapterManager;

    public static class Factory implements FieldMappingFactory {
        @Override
        public FieldMapping createFieldMapping(final ObjectAssociation field) {
            return new JdbcDateMapper(field);
        }
    }

    protected JdbcDateMapper(final ObjectAssociation field) {
        super(field);
        adapterManager = IsisContext.getPersistenceSession().getAdapterManager();
    }

    @Override
    protected Object preparedStatementObject(final ObjectAdapter value) {
        final Object o = value.getObject();
        if (o instanceof java.sql.Date) {
            final java.sql.Date javaSqlDate = (java.sql.Date) value.getObject();
            final long millisSinceEpoch = javaSqlDate.getTime();
            return new LocalDate(millisSinceEpoch);
        } else if (o instanceof Date) {
            final Date asDate = (Date) value.getObject();
            return new LocalDate(asDate.getMillisSinceEpoch());
        } else {
            throw new IsisApplicationException("Unimplemented JdbcDateMapper instance type: "
                + value.getClass().toString());
        }
    }

    @Override
    public ObjectAdapter setFromDBColumn(final Results results, final String columnName, final ObjectAssociation field) {
        ObjectAdapter restoredValue;
        final java.util.Date javaDateValue = results.getJavaDateOnly(columnName);
        final Class<?> correspondingClass = field.getSpecification().getCorrespondingClass();
        if (correspondingClass == java.util.Date.class || correspondingClass == java.sql.Date.class) {
            // 2011-04-08 = 1270684800000
            restoredValue = adapterManager.adapterFor(javaDateValue);
        } else if (correspondingClass == Date.class) {
            // 2010-03-05 = 1267747200000
            Date dateValue;
            dateValue = new Date(javaDateValue);
            restoredValue = adapterManager.adapterFor(dateValue);
        } else {
            throw new PersistFailedException("Unhandled date type: " + correspondingClass.getCanonicalName());
        }
        return restoredValue;
    }

    @Override
    public String columnType() {
        return Defaults.TYPE_DATE();
    }

}
