/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.phoenix.hbase.index.table;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.CoprocessorEnvironment;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.phoenix.hbase.index.IndexQosCompat;
import org.apache.phoenix.hbase.index.util.ImmutableBytesPtr;
import org.apache.phoenix.hbase.index.util.IndexManagementUtil;

public class CoprocessorHTableFactory implements HTableFactory {

    private static final Log LOG = LogFactory.getLog(CoprocessorHTableFactory.class);
    private CoprocessorEnvironment e;

    public CoprocessorHTableFactory(CoprocessorEnvironment e) {
        this.e = e;
    }

    @Override
    public HTableInterface getTable(ImmutableBytesPtr tablename) throws IOException {
        Configuration conf = e.getConfiguration();

        // make sure we use the index priority writer for our rpcs
        IndexQosCompat.setPhoenixIndexRpcController(conf);

        // make sure we include the index table in the tables we need to track
        String tableName = Bytes.toString(tablename.copyBytesIfNecessary());
        IndexQosCompat.enableIndexQosForTable(conf, tableName);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating new HTable: " + tableName);
        }
        return this.e.getTable(TableName.valueOf(tablename.copyBytesIfNecessary()));
    }

    @Override
    public void shutdown() {
        // noop
    }
}