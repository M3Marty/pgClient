package org.m3m.sql.builder;

import org.junit.jupiter.api.Test;

import static org.m3m.sql.builder.Sql.*;
import static org.m3m.sql.builder.SqlFunctions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.m3m.sql.builder.query.returning.Returning.all;
import static org.m3m.sql.builder.query.where.WhereOps.*;

public class DeleteTest {

	@Test
	public void deleteUsingTest() {
		String query = delete().from("films").using(table("producers"))
				.where("producer_id", eq(field("producers.id"))).and("producers.name", eq("foo"))
				.build();

		assertEquals("DELETE FROM films USING producers WHERE producer_id = producers.id AND producers.name = 'foo'", query);
	}

	@Test
	public void deleteWithInnerQueryTest() {
		String query = delete().from("films")
				.where("producer_id", in(select("id").from("producers").where("name", eq("foo"))))
				.build();

		assertEquals("DELETE FROM films WHERE producer_id IN (SELECT id FROM producers WHERE name = 'foo')", query);
	}

	@Test
	public void deleteNotEqFilterTest() {
		String query = delete().from("films").where("kind", notEq("Musical")).build();

		assertEquals("DELETE FROM films WHERE kind <> 'Musical'", query);
	}

	@Test
	public void deleteAllTest() {
		String query = delete().from("films").build();

		assertEquals("DELETE FROM films", query);
	}

	@Test
	public void deleteWithFilterAndReturningTest() {
		String query = delete().from("tasks").where("status", eq("DONE")).returning(all());

		assertEquals("DELETE FROM tasks WHERE status = 'DONE' RETURNING *", query);
	}

	@Test
	public void deleteWhereCursorTest() {
		String query = delete().from("tasks").whereCurrentOf("c_tasks").build();
		assertEquals("DELETE FROM tasks WHERE CURRENT OF c_tasks", query);
	}

	@Test
	public void complexDeleteTest() {
		String query = delete().from(table("orders").as("o")).using(table("customers").as("c"))
				.where("o.customer_id", eq(field("c.id"))).and("c.country", eq("USA"))
				.and("o.order_date", lsThan(raw("NOW() - INTERVAL '1 year'")))
				.and("o.quantity", lsThan(5))
				.returning(all());

		assertEquals("DELETE FROM orders AS o USING customers AS c WHERE o.customer_id = c.id AND c.country = 'USA' AND o.order_date < NOW() - INTERVAL '1 year' AND o.quantity < 5 RETURNING *", query);
	}

	@Test
	public void complexDeleteWithSelectTest() {
		String query = delete().from(table("orders").as("o")).using(table("customers").as("c"))
				.where("o.customer_id", eq(field("c.id"))).and("c.country", eq("USA"))
				.and("o.id", in(
						select("o2.id").from(table("orders").as("o2"))
						.where("o2.order_date", lsThan(raw("NOW() - INTERVAL '1 year'")))
						.and("o2.quantity", lsThan(
								select(avg("o3.quantity")).from(table("orders").as("o3"))
										.where("o3.customer_id", eq(field("o2.customer_id"))))
						)
				)).returning("o.id", "o.customer_id", "o.product_id", "o.quantity", "o.order_date");

		assertEquals("DELETE FROM orders AS o USING customers AS c WHERE o.customer_id = c.id AND c.country = 'USA' AND o.id IN (SELECT o2.id FROM orders AS o2 WHERE o2.order_date < NOW() - INTERVAL '1 year' AND o2.quantity < (SELECT AVG(o3.quantity) FROM orders AS o3 WHERE o3.customer_id = o2.customer_id)) RETURNING o.id,o.customer_id,o.product_id,o.quantity,o.order_date", query);
	}
}