/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.samza.sql.bench;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.samza.SamzaException;
import org.apache.samza.config.Config;
import org.apache.samza.sql.bench.utils.DataVerifier;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.*;

public class ProjectTask implements StreamTask, InitableTask {

  private static final SystemStream OUTPUT_STREAM = new SystemStream("kafka", "nativeprojectoutput");

  private Schema schema;

  @Override
  public void init(Config config, TaskContext context) throws Exception {
    schema = new Schema.Parser().parse(DataVerifier.loadSchema(DataVerifier.SchemaType.PROJECT));
  }

  @Override
  public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator) throws Exception {
    if(!(envelope.getMessage() instanceof GenericRecord)) {
      throw new SamzaException("Unsupported message type: " + envelope.getMessage().getClass());
    }

    GenericRecord message = (GenericRecord)envelope.getMessage();

    GenericRecord output = new GenericRecordBuilder(schema)
        .set("productId", message.get("productId"))
        .set("units", message.get("units"))
        .set("rowtime", message.get("rowtime"))
        .build();

    collector.send(new OutgoingMessageEnvelope(OUTPUT_STREAM, envelope.getKey(), output));
  }
}
