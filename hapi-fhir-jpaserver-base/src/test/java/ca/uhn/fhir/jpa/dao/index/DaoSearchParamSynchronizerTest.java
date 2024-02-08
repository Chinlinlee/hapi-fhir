package ca.uhn.fhir.jpa.dao.index;

import ca.uhn.fhir.jpa.model.config.PartitionSettings;
import ca.uhn.fhir.jpa.model.entity.BaseResourceIndex;
import ca.uhn.fhir.jpa.model.entity.ResourceIndexedSearchParamNumber;
import ca.uhn.fhir.jpa.model.entity.ResourceTable;
import ca.uhn.fhir.jpa.searchparam.extractor.ResourceIndexedSearchParams;
import ca.uhn.fhir.jpa.util.AddRemoveCount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DaoSearchParamSynchronizerTest {
	private static final String GRITTSCORE = "grittscore";

	private static final ResourceIndexedSearchParamNumber EXISTING_SEARCH_PARAM_NUMBER = new ResourceIndexedSearchParamNumber(new PartitionSettings(), "Patient", GRITTSCORE, BigDecimal.valueOf(10));
	private static final ResourceIndexedSearchParamNumber THE_SEARCH_PARAM_NUMBER = new ResourceIndexedSearchParamNumber(new PartitionSettings(), "Patient", GRITTSCORE, BigDecimal.valueOf(12));

	private final DaoSearchParamSynchronizer subject = new DaoSearchParamSynchronizer();

	private ResourceIndexedSearchParams theParams;

	@Mock
	private ResourceTable theEntity;

	@Mock
	private ResourceTable existingEntity;

	@Mock
	private EntityManager entityManager;

	private ResourceIndexedSearchParams existingParams;

	@BeforeEach
	void setUp() {
		when(theEntity.isParamsNumberPopulated()).thenReturn(true);
		when(theEntity.getParamsNumber()).thenReturn(List.of(THE_SEARCH_PARAM_NUMBER));
		when(existingEntity.isParamsNumberPopulated()).thenReturn(true);
		when(existingEntity.getParamsNumber()).thenReturn(List.of(EXISTING_SEARCH_PARAM_NUMBER));

		theParams = ResourceIndexedSearchParams.withLists(theEntity);
		existingParams = ResourceIndexedSearchParams.withLists(existingEntity);

		final ResourceTable resourceTable = new ResourceTable();
		resourceTable.setId(1L);
		EXISTING_SEARCH_PARAM_NUMBER.setResource(resourceTable);
		THE_SEARCH_PARAM_NUMBER.setResource(resourceTable);

		subject.setEntityManager(entityManager);
	}

	@Test
	void synchronizeSearchParamsNumberOnlyValuesDifferent() {
		final AddRemoveCount addRemoveCount = subject.synchronizeSearchParamsToDatabase(theParams, theEntity, existingParams);

		assertThat(addRemoveCount.getRemoveCount()).isEqualTo(0);
		assertThat(addRemoveCount.getAddCount()).isEqualTo(1);

		verify(entityManager, never()).remove(any(BaseResourceIndex.class));
		verify(entityManager, times(1)).merge(THE_SEARCH_PARAM_NUMBER);
	}
}
