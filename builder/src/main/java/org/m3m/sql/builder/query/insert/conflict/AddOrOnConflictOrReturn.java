package org.m3m.sql.builder.query.insert.conflict;

import org.m3m.sql.builder.query.Query;
import org.m3m.sql.builder.query.returning.Returning;

public interface AddOrOnConflictOrReturn extends OnConflictOrReturn {

	AddOrOnConflictOrReturn add(Object...values);
}