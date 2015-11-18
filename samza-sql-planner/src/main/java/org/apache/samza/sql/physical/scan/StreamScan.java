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
package org.apache.samza.sql.physical.scan;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.samza.SamzaException;
import org.apache.samza.config.Config;
import org.apache.samza.sql.api.data.Relation;
import org.apache.samza.sql.api.data.Schema;
import org.apache.samza.sql.api.data.Tuple;
import org.apache.samza.sql.data.DataUtils;
import org.apache.samza.sql.data.IntermediateMessageTuple;
import org.apache.samza.sql.data.TupleConverter;
import org.apache.samza.sql.operators.SimpleOperatorImpl;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
import org.apache.samza.task.sql.SimpleMessageCollector;

public class StreamScan extends SimpleOperatorImpl {

  /**
   * Type of this operator. Operator's type is its output type.
   */
  private final RelDataType type;

  private final StreamScanSpec spec;

  public StreamScan(StreamScanSpec spec, RelDataType type) {
    super(spec);
    this.type = type;
    this.spec = spec;
  }

  @Override
  protected void realRefresh(long timeNano, SimpleMessageCollector collector, TaskCoordinator coordinator) throws Exception {
  }

  @Override
  protected void realProcess(Relation rel, SimpleMessageCollector collector, TaskCoordinator coordinator) throws Exception {
    throw new UnsupportedOperationException("Doesn't process relations.");
  }

  @Override
  protected void realProcess(Tuple tuple, SimpleMessageCollector collector, TaskCoordinator coordinator) throws Exception {
    if(!DataUtils.isStruct(tuple.getMessage())) {
      // TODO: Log to error topic
      throw new SamzaException(String.format("Unsupported tuple type: %s expected: %s", tuple.getMessage().schema().getType(), Schema.Type.STRUCT));
    }

    collector.send(IntermediateMessageTuple.fromData(TupleConverter.samzaDataToObjectArray(tuple.getMessage(), type),
        tuple.getKey(), tuple.getCreateTimeNano(), tuple.getOffset(), false, spec.getOutputName()));
  }

  @Override
  public void init(Config config, TaskContext context) throws Exception {

  }
}
