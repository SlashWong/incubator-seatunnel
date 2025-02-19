/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.seatunnel.api.sink;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The sink writer use to write data to third party data receiver. This class will run on taskManger/Worker.
 *
 * @param <T>           The data class by sink accept. Only support
 *                      {@link org.apache.seatunnel.api.table.type.SeaTunnelRow} at now.
 * @param <CommitInfoT> The type of commit message.
 * @param <StateT>      The type of state.
 */
public interface SinkWriter<T, CommitInfoT, StateT> extends Serializable {

    /**
     * write data to third party data receiver.
     *
     * @param element the date need be written.
     * @throws IOException throw IOException when write data failed.
     */
    void write(T element) throws IOException;

    /**
     * prepare the commit, will be called before {@link #snapshotState(long checkpointId)}.
     * If you need to use 2pc, you can return the commit info in this method, and receive the commit info in {@link SinkCommitter#commit(List)}.
     * If this method failed (by throw exception), **Only** Spark engine will call {@link #abortPrepare()}
     *
     * @return the commit info need to commit
     */
    Optional<CommitInfoT> prepareCommit() throws IOException;

    /**
     * @return The writer's state.
     * @throws IOException if fail to snapshot writer's state.
     */
    default List<StateT> snapshotState(long checkpointId) throws IOException {
        return Collections.emptyList();
    }

    /**
     * Used to abort the {@link #prepareCommit()}, if the prepareCommit failed,
     * there is no CommitInfoT, so the rollback work cannot be done by {@link SinkCommitter}. But we can
     * use this method to rollback side effects of {@link #prepareCommit()}. Only use it in Spark engine at
     * now.
     */
    void abortPrepare();

    /**
     * call it when SinkWriter close
     *
     * @throws IOException if close failed
     */
    void close() throws IOException;

    interface Context extends Serializable{

        /**
         * Gets the configuration with which Job was started.
         */
        Map<String, String> getConfiguration();

        /**
         * @return The index of this subtask.
         */
        int getIndexOfSubtask();

        /**
         * @return The number of parallel Sink tasks.
         */
        int getNumberOfParallelSubtasks();

    }
}
