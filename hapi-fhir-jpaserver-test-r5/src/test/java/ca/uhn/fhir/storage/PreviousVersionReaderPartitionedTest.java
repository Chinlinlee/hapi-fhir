package ca.uhn.fhir.storage;

import ca.uhn.fhir.interceptor.model.RequestPartitionId;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.dao.SimplePartitionTestHelper;
import ca.uhn.fhir.jpa.dao.r5.BaseJpaR5Test;
import ca.uhn.fhir.rest.api.server.SystemRequestDetails;
import org.hl7.fhir.r5.model.Enumerations;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Patient;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class PreviousVersionReaderPartitionedTest extends BaseJpaR5Test {
	PreviousVersionReader<Patient> mySvc;
	SystemRequestDetails mySrd;
	@Autowired
	DaoRegistry myDaoRegistry;
	SimplePartitionTestHelper mySimplePartitionTestHelper;

	@BeforeEach
	public void before() throws Exception {
		super.before();

		mySimplePartitionTestHelper = new SimplePartitionTestHelper(myPartitionSettings, myPartitionConfigSvc, myInterceptorRegistry);
		mySimplePartitionTestHelper.beforeEach(null);

		mySvc = new PreviousVersionReader<>(myPatientDao);
		mySrd = new SystemRequestDetails();
		RequestPartitionId part1 = RequestPartitionId.fromPartitionId(SimplePartitionTestHelper.TEST_PARTITION_ID);
		mySrd.setRequestPartitionId(part1);
	}

	@AfterEach
	public void after() throws Exception {
		mySimplePartitionTestHelper.afterEach(null);
	}

	@Test
	void readPreviousVersion() {
		// setup
		Patient patient = createMale();
		patient.setGender(Enumerations.AdministrativeGender.FEMALE);
		myPatientDao.update(patient, mySrd);
		assertThat(myPatientDao.read(patient.getIdElement(), mySrd).getGenderElement().getValue()).isEqualTo(Enumerations.AdministrativeGender.FEMALE);

		// execute
		Optional<Patient> oPreviousPatient = mySvc.readPreviousVersion(patient);

		// verify
		assertThat(oPreviousPatient.isPresent()).isTrue();
		Patient previousPatient = oPreviousPatient.get();
		assertThat(previousPatient.getGenderElement().getValue()).isEqualTo(Enumerations.AdministrativeGender.MALE);
	}

	private Patient createMale() {
		Patient male = new Patient();
		male.setGender(Enumerations.AdministrativeGender.MALE);
		return (Patient) myPatientDao.create(male, mySrd).getResource();
	}

	@Test
	void noPrevious() {
		// setup
		Patient patient = createMale();

		// execute
		Optional<Patient> oPreviousPatient = mySvc.readPreviousVersion(patient);

		// verify
		assertThat(oPreviousPatient.isPresent()).isFalse();
	}

	@Test
	void currentDeleted() {
		// setup
		Patient patient = createMale();
		IdType patientId = patient.getIdElement().toVersionless();
		myPatientDao.delete(patientId, mySrd);

		Patient currentDeletedVersion = myPatientDao.read(patientId, mySrd, true);

		// execute
		Optional<Patient> oPreviousPatient = mySvc.readPreviousVersion(currentDeletedVersion);

		// verify
		assertThat(oPreviousPatient.isPresent()).isTrue();
		Patient previousPatient = oPreviousPatient.get();
		assertThat(previousPatient.getGenderElement().getValue()).isEqualTo(Enumerations.AdministrativeGender.MALE);
	}

	@Test
	void previousDeleted() {
		// setup
		Patient latestUndeletedVersion = setupPreviousDeletedResource();

		// execute
		Optional<Patient> oDeletedPatient = mySvc.readPreviousVersion(latestUndeletedVersion);
		assertThat(oDeletedPatient.isPresent()).isFalse();
	}

	@Test
	void previousDeletedDeletedOk() {
		// setup
		Patient latestUndeletedVersion = setupPreviousDeletedResource();

		// execute
		Optional<Patient> oPreviousPatient = mySvc.readPreviousVersion(latestUndeletedVersion, true);

		// verify
		assertThat(oPreviousPatient.isPresent()).isTrue();
		Patient previousPatient = oPreviousPatient.get();
		assertThat(previousPatient.isDeleted()).isTrue();
	}

	@NotNull
	private Patient setupPreviousDeletedResource() {
		Patient patient = createMale();
		assertThat(patient.getIdElement().getVersionIdPartAsLong()).isEqualTo(1L);
		IdType patientId = patient.getIdElement().toVersionless();
		myPatientDao.delete(patientId, mySrd);

		Patient currentDeletedVersion = myPatientDao.read(patientId, mySrd, true);
		assertThat(currentDeletedVersion.getIdElement().getVersionIdPartAsLong()).isEqualTo(2L);

		currentDeletedVersion.setGender(Enumerations.AdministrativeGender.FEMALE);
		currentDeletedVersion.setId(currentDeletedVersion.getIdElement().toVersionless());
		myPatientDao.update(currentDeletedVersion, mySrd);

		Patient latestUndeletedVersion = myPatientDao.read(patientId, mySrd);
		assertThat(latestUndeletedVersion.getIdElement().getVersionIdPartAsLong()).isEqualTo(3L);

		assertThat(latestUndeletedVersion.isDeleted()).isFalse();
		assertThat(latestUndeletedVersion.getGenderElement().getValue()).isEqualTo(Enumerations.AdministrativeGender.FEMALE);
		return latestUndeletedVersion;
	}

}
