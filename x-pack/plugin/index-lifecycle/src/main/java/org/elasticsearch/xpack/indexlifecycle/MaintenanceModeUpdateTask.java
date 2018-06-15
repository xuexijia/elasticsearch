/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.indexlifecycle;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateUpdateTask;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.xpack.core.indexlifecycle.IndexLifecycleMetadata;
import org.elasticsearch.xpack.core.indexlifecycle.OperationMode;

public class MaintenanceModeUpdateTask extends ClusterStateUpdateTask {
    private static final Logger logger = ESLoggerFactory.getLogger(MaintenanceModeUpdateTask.class);
    private final OperationMode mode;

    public MaintenanceModeUpdateTask(OperationMode mode) {
        this.mode = mode;
    }

    OperationMode getOperationMode() {
        return mode;
    }

    @Override
    public ClusterState execute(ClusterState currentState) {
        IndexLifecycleMetadata currentMetadata = currentState.metaData().custom(IndexLifecycleMetadata.TYPE);


        if (currentMetadata.getMaintenanceMode().isValidChange(mode) == false) {
            return currentState;
        }

        ClusterState.Builder builder = new ClusterState.Builder(currentState);
        MetaData.Builder metadataBuilder = MetaData.builder(currentState.metaData());
        metadataBuilder.putCustom(IndexLifecycleMetadata.TYPE,
            new IndexLifecycleMetadata(currentMetadata.getPolicyMetadatas(), mode));
        builder.metaData(metadataBuilder.build());
        return builder.build();
    }

    @Override
    public void onFailure(String source, Exception e) {
        logger.error("unable to update lifecycle metadata with new mode [" + mode + "]", e);
    }
}