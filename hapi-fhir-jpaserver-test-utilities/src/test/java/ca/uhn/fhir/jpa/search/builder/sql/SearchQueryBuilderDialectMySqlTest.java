package ca.uhn.fhir.jpa.search.builder.sql;

import ca.uhn.fhir.jpa.model.dialect.HapiFhirMySQLDialect;
import ca.uhn.fhir.jpa.search.builder.predicate.BaseJoiningPredicateBuilder;
import ca.uhn.fhir.jpa.search.builder.predicate.DatePredicateBuilder;
import ca.uhn.fhir.jpa.search.builder.predicate.ResourceTablePredicateBuilder;
import ca.uhn.fhir.jpa.search.builder.predicate.StringPredicateBuilder;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.OrderObject;
import org.hibernate.dialect.Dialect;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.annotation.Nonnull;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SearchQueryBuilderDialectMySqlTest extends BaseSearchQueryBuilderDialectTest {

	@Test
	public void testAddSortNumericNoNullOrder() {
		GeneratedSql generatedSql = buildSqlWithNumericSort(true,null);
		assertThat(generatedSql.getSql().endsWith("ORDER BY -t1.SP_VALUE_LOW DESC limit ?")).isTrue();

		generatedSql =  buildSqlWithNumericSort(false,null);
		assertThat(generatedSql.getSql().endsWith("ORDER BY t1.SP_VALUE_LOW DESC limit ?")).isTrue();

	}

	@Test
	public void testAddSortNumericWithNullOrder() {
		GeneratedSql generatedSql =  buildSqlWithNumericSort(true, OrderObject.NullOrder.FIRST);
		assertThat(generatedSql.getSql().endsWith("ORDER BY t1.SP_VALUE_LOW ASC limit ?")).isTrue();

		generatedSql = buildSqlWithNumericSort(false, OrderObject.NullOrder.FIRST);
		assertThat(generatedSql.getSql().endsWith("ORDER BY -t1.SP_VALUE_LOW ASC limit ?")).isTrue();

		generatedSql = buildSqlWithNumericSort(true, OrderObject.NullOrder.LAST);
		assertThat(generatedSql.getSql().endsWith("ORDER BY -t1.SP_VALUE_LOW DESC limit ?")).isTrue();

		generatedSql = buildSqlWithNumericSort(false, OrderObject.NullOrder.LAST);
		assertThat(generatedSql.getSql().endsWith("ORDER BY t1.SP_VALUE_LOW DESC limit ?")).isTrue();

	}

	@Test
	public void testAddSortStringNoNullOrder() {
		GeneratedSql generatedSql = buildSqlWithStringSort(true,null);
//		assertTrue(generatedSql.getSql().endsWith("ORDER BY CASE WHEN t1.SP_VALUE_NORMALIZED IS NULL THEN 1 ELSE 0 END ASC, t1.SP_VALUE_NORMALIZED ASC limit ?"));
		assertThat(generatedSql.getSql().endsWith("ORDER BY t1.SP_VALUE_NORMALIZED ASC limit ?")).isTrue();

		generatedSql = buildSqlWithStringSort(false,null);
		assertThat(generatedSql.getSql().endsWith("ORDER BY t1.SP_VALUE_NORMALIZED DESC limit ?")).isTrue();

	}

	private GeneratedSql buildSqlWithStringSort(Boolean theAscending, OrderObject.NullOrder theNullOrder) {
		SearchQueryBuilder searchQueryBuilder = createSearchQueryBuilder();
		when(mySqlObjectFactory.resourceTable(any())).thenReturn(new ResourceTablePredicateBuilder(searchQueryBuilder));
		when(mySqlObjectFactory.stringIndexTable(any())).thenReturn(new StringPredicateBuilder(searchQueryBuilder));

		BaseJoiningPredicateBuilder firstPredicateBuilder = searchQueryBuilder.getOrCreateFirstPredicateBuilder();
		StringPredicateBuilder sortPredicateBuilder = searchQueryBuilder.addStringPredicateBuilder(firstPredicateBuilder.getResourceIdColumn());

		Condition hashIdentityPredicate = sortPredicateBuilder.createHashIdentityPredicate("patient", "family");
		searchQueryBuilder.addPredicate(hashIdentityPredicate);
		if (theNullOrder == null) {
			searchQueryBuilder.addSortString(sortPredicateBuilder.getColumnValueNormalized(), theAscending);
		} else {
			searchQueryBuilder.addSortString(sortPredicateBuilder.getColumnValueNormalized(), theAscending, theNullOrder, false);
		}

		return searchQueryBuilder.generate(0,500);

	}

	@Test
	public void testAddSortStringWithNullOrder() {
		GeneratedSql generatedSql =  buildSqlWithStringSort(true, OrderObject.NullOrder.FIRST);
		assertThat(generatedSql.getSql().endsWith("ORDER BY t1.SP_VALUE_NORMALIZED ASC limit ?")).isTrue();

		generatedSql = buildSqlWithStringSort(false, OrderObject.NullOrder.FIRST);
//		assertTrue(generatedSql.getSql().endsWith("ORDER BY CASE WHEN t1.SP_VALUE_NORMALIZED IS NULL THEN 1 ELSE 0 END DESC, t1.SP_VALUE_NORMALIZED DESC limit ?"));
		assertThat(generatedSql.getSql().endsWith("ORDER BY t1.SP_VALUE_NORMALIZED DESC limit ?")).isTrue();

		generatedSql = buildSqlWithStringSort(true, OrderObject.NullOrder.LAST);
//		assertTrue(generatedSql.getSql().endsWith("ORDER BY CASE WHEN t1.SP_VALUE_NORMALIZED IS NULL THEN 1 ELSE 0 END ASC, t1.SP_VALUE_NORMALIZED ASC limit ?"));
		assertThat(generatedSql.getSql().endsWith("ORDER BY t1.SP_VALUE_NORMALIZED ASC limit ?")).isTrue();

		generatedSql = buildSqlWithStringSort(false, OrderObject.NullOrder.LAST);
		assertThat(generatedSql.getSql().endsWith("ORDER BY t1.SP_VALUE_NORMALIZED DESC limit ?")).isTrue();

	}

	@Test
	public void testAddSortDateNoNullOrder() {
		GeneratedSql generatedSql = buildSqlWithDateSort(true,null);
//		assertTrue(generatedSql.getSql().endsWith("ORDER BY CASE WHEN t1.SP_VALUE_LOW IS NULL THEN 1 ELSE 0 END ASC, t1.SP_VALUE_LOW ASC limit ?"));
		assertThat(generatedSql.getSql().endsWith("ORDER BY t1.SP_VALUE_LOW ASC limit ?")).isTrue();

		generatedSql = buildSqlWithDateSort(false,null);
		assertThat(generatedSql.getSql().endsWith("ORDER BY t1.SP_VALUE_LOW DESC limit ?")).isTrue();

	}

	private GeneratedSql buildSqlWithDateSort(Boolean theAscending, OrderObject.NullOrder theNullOrder) {
		SearchQueryBuilder searchQueryBuilder = createSearchQueryBuilder();
		when(mySqlObjectFactory.resourceTable(any())).thenReturn(new ResourceTablePredicateBuilder(searchQueryBuilder));
		when(mySqlObjectFactory.dateIndexTable(any())).thenReturn(new DatePredicateBuilder(searchQueryBuilder));

		BaseJoiningPredicateBuilder firstPredicateBuilder = searchQueryBuilder.getOrCreateFirstPredicateBuilder();
		DatePredicateBuilder sortPredicateBuilder = searchQueryBuilder.addDatePredicateBuilder(firstPredicateBuilder.getResourceIdColumn());

		Condition hashIdentityPredicate = sortPredicateBuilder.createHashIdentityPredicate("patient", "birthdate");
		searchQueryBuilder.addPredicate(hashIdentityPredicate);
		if (theNullOrder == null) {
			searchQueryBuilder.addSortDate(sortPredicateBuilder.getColumnValueLow(), theAscending);
		} else {
			searchQueryBuilder.addSortDate(sortPredicateBuilder.getColumnValueLow(), theAscending, theNullOrder, false);
		}

		return searchQueryBuilder.generate(0,500);

	}

	@Test
	public void testAddSortDateWithNullOrder() {
		GeneratedSql generatedSql =  buildSqlWithDateSort(true, OrderObject.NullOrder.FIRST);
		assertThat(generatedSql.getSql().endsWith("ORDER BY t1.SP_VALUE_LOW ASC limit ?")).isTrue();

		generatedSql = buildSqlWithDateSort(false, OrderObject.NullOrder.FIRST);
//		assertTrue(generatedSql.getSql().endsWith("ORDER BY CASE WHEN t1.SP_VALUE_LOW IS NULL THEN 1 ELSE 0 END DESC, t1.SP_VALUE_LOW DESC limit ?"));
		assertThat(generatedSql.getSql().endsWith("ORDER BY t1.SP_VALUE_LOW DESC limit ?")).isTrue();

		generatedSql = buildSqlWithDateSort(true, OrderObject.NullOrder.LAST);
//		assertTrue(generatedSql.getSql().endsWith("ORDER BY CASE WHEN t1.SP_VALUE_LOW IS NULL THEN 1 ELSE 0 END ASC, t1.SP_VALUE_LOW ASC limit ?"));
		assertThat(generatedSql.getSql().endsWith("ORDER BY t1.SP_VALUE_LOW ASC limit ?")).isTrue();

		generatedSql = buildSqlWithDateSort(false, OrderObject.NullOrder.LAST);
		assertThat(generatedSql.getSql().endsWith("ORDER BY t1.SP_VALUE_LOW DESC limit ?")).isTrue();

	}

	@Nonnull
	@Override
	protected Dialect createDialect() {
		return new HapiFhirMySQLDialect();
	}
}
