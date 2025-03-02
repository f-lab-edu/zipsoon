package com.zipsoon.batch.source.loader;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

public interface SourceFileLoader {
    int load(Reader reader, String tableName) throws IOException, SQLException;
}