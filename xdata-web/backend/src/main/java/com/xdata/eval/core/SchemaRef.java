package com.xdata.eval.core;

import com.xdata.model.DbConnection;

/**
 * The schema context a rung needs: the schema id (for metadata / Derby) and the
 * assigned DB connection (for the last-resort compare). Either may be null.
 */
public record SchemaRef(Integer schemaId, DbConnection assignedConnection) {

    public static SchemaRef ofSchema(Integer schemaId) {
        return new SchemaRef(schemaId, null);
    }
}
