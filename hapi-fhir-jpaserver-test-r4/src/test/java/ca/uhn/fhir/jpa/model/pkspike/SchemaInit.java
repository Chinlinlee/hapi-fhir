package ca.uhn.fhir.jpa.model.pkspike;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

class SchemaInit {
	public static void initSchema(BasicDataSource theDataSource) {
		var t = new JdbcTemplate(theDataSource);

		t.execute("create table res_root (RES_ID IDENTITY NOT NULL PRIMARY KEY, PARTITION_ID numeric, STRING_COL varchar)");
		t.execute("create table res_join (PID IDENTITY NOT NULL PRIMARY KEY, RES_ID numeric, PARTITION_ID numeric, STRING_COL varchar)");

	}
}
