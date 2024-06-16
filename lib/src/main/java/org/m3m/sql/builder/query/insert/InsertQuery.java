package org.m3m.sql.builder.query.insert;

import lombok.Getter;
import lombok.Setter;
import org.m3m.sql.builder.Sql;
import org.m3m.sql.builder.query.Query;
import org.m3m.sql.builder.query.from.SimpleFromAliasInto;
import org.m3m.sql.builder.query.from.TableDataSource;

import java.util.*;
import java.util.stream.Collectors;

public class InsertQuery implements SimpleFromAliasInto<InsertValuesOpts>,
                                    InsertValuesOpts, InsertOpts {

	@Setter @Getter
	private Query parent;

	private TableDataSource dataSource;

	private String valuesExpression;

	@Setter
	private String returningExpression;

	private List<String> values = new ArrayList<>();

	@Override
	public String build() {
		if (dataSource == null) {
			throw new IllegalStateException("Can't insert without target");
		}

		if (valuesExpression == null) {
			throw new IllegalStateException("Nothing to insert");
		}

		StringBuilder builder = new StringBuilder("INSERT INTO ")
				.append(dataSource.buildExpression()).append(' ')
				.append(valuesExpression)
				.append(String.join(",", values));

		// TODO on conflict

		if (returningExpression != null) {
			builder.append(' ').append(returningExpression);
		}

		return builder.toString();
	}

	@Override
	public InsertValuesOpts from(TableDataSource dataSource) {
		this.dataSource = dataSource;
		return this;
	}

	@Override
	public AddOrOnConflictOrReturn setValuesExpression(String expression) {
		this.valuesExpression = expression;
		return this;
	}

	@Override
	public String buildExpression() {
		return build();
	}

	@Override
	public AddOrOnConflictOrReturn add(Object...values) {
		Iterable<String> valuesIterable = Arrays.stream(values)
				.map(Sql::getObjectStringValue).collect(Collectors.toList());
		this.values.add("(" + String.join(",", valuesIterable) + ")");

		return this;
	}
}