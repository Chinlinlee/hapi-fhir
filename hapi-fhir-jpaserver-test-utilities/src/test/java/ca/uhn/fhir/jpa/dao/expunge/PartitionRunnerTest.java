package ca.uhn.fhir.jpa.dao.expunge;

import ca.uhn.fhir.interceptor.api.HookParams;
import ca.uhn.fhir.jpa.api.config.JpaStorageSettings;
import ca.uhn.fhir.jpa.model.dao.JpaPid;
import ca.uhn.fhir.rest.api.server.storage.IResourcePersistentId;
import ca.uhn.test.concurrency.PointcutLatch;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
<<<<<<< HEAD
import static org.hamcrest.Matchers.isOneOf;
=======
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.oneOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
>>>>>>> master

public class PartitionRunnerTest {
	private static final Logger ourLog = LoggerFactory.getLogger(PartitionRunnerTest.class);
	private static final String TEST_THREADNAME_1 = "test-1";
	private static final String TEST_THREADNAME_2 = "test-2";

	private final PointcutLatch myLatch = new PointcutLatch("partition call");

	@AfterEach
	public void before() {
		myLatch.clear();
	}

	@Test
	public void emptyList() {
		List<IResourcePersistentId> resourceIds = buildPidList(0);
		Consumer<List<IResourcePersistentId>> partitionConsumer = buildPartitionConsumer(myLatch);
		myLatch.setExpectedCount(0);

		getPartitionRunner().runInPartitionedThreads(resourceIds, partitionConsumer);
		myLatch.clear();
	}

	private PartitionRunner getPartitionRunner() {
		return getPartitionRunner(JpaStorageSettings.DEFAULT_EXPUNGE_BATCH_SIZE);
	}

	private PartitionRunner getPartitionRunner(int theBatchSize) {
		return getPartitionRunner(theBatchSize, Runtime.getRuntime().availableProcessors());
	}

	private PartitionRunner getPartitionRunner(int theBatchSize, int theThreadCount) {
		return new PartitionRunner("TEST", "test", theBatchSize, theThreadCount);
	}

	private List<IResourcePersistentId> buildPidList(int size) {
		List<IResourcePersistentId> list = new ArrayList<>();
		for (long i = 0; i < size; ++i) {
			list.add(JpaPid.fromId(i + 1));
		}
		return list;
	}

	@Test
	public void oneItem() throws InterruptedException {
		List<IResourcePersistentId> resourceIds = buildPidList(1);

		Consumer<List<IResourcePersistentId>> partitionConsumer = buildPartitionConsumer(myLatch);
		myLatch.setExpectedCount(1);
		getPartitionRunner().runInPartitionedThreads(resourceIds, partitionConsumer);
		PartitionCall partitionCall = (PartitionCall) PointcutLatch.getLatchInvocationParameter(myLatch.awaitExpected());
		assertThat(partitionCall.threadName).isEqualTo("main");
		assertThat(partitionCall.size).isEqualTo(1);
	}


	@Test
	public void twoItems() throws InterruptedException {
		List<IResourcePersistentId> resourceIds = buildPidList(2);

		Consumer<List<IResourcePersistentId>> partitionConsumer = buildPartitionConsumer(myLatch);
		myLatch.setExpectedCount(1);
		getPartitionRunner().runInPartitionedThreads(resourceIds, partitionConsumer);
		PartitionCall partitionCall = (PartitionCall) PointcutLatch.getLatchInvocationParameter(myLatch.awaitExpected());
		assertThat(partitionCall.threadName).isEqualTo("main");
		assertThat(partitionCall.size).isEqualTo(2);
	}

	@Test
	public void tenItemsBatch5() throws InterruptedException {
		List<IResourcePersistentId> resourceIds = buildPidList(10);

		Consumer<List<IResourcePersistentId>> partitionConsumer = buildPartitionConsumer(myLatch);
		myLatch.setExpectedCount(2);
		getPartitionRunner(5).runInPartitionedThreads(resourceIds, partitionConsumer);
		List<HookParams> calls = myLatch.awaitExpected();
		PartitionCall partitionCall1 = (PartitionCall) PointcutLatch.getLatchInvocationParameter(calls, 0);
<<<<<<< HEAD
		assertThat(partitionCall1.threadName, isOneOf(TEST_THREADNAME_1, TEST_THREADNAME_2));
		assertThat(partitionCall1.size).isEqualTo(5);
		PartitionCall partitionCall2 = (PartitionCall) PointcutLatch.getLatchInvocationParameter(calls, 1);
		assertThat(partitionCall2.threadName, isOneOf(TEST_THREADNAME_1, TEST_THREADNAME_2));
		assertThat(partitionCall2.size).isEqualTo(5);
		assertThat(partitionCall2.threadName).isNotEqualTo(partitionCall1.threadName);
=======
		assertThat(partitionCall1.threadName, is(oneOf(TEST_THREADNAME_1, TEST_THREADNAME_2)));
		assertEquals(5, partitionCall1.size);
		PartitionCall partitionCall2 = (PartitionCall) PointcutLatch.getLatchInvocationParameter(calls, 1);
		assertThat(partitionCall2.threadName, is(oneOf(TEST_THREADNAME_1, TEST_THREADNAME_2)));
		assertEquals(5, partitionCall2.size);
		assertNotEquals(partitionCall1.threadName, partitionCall2.threadName);
>>>>>>> master
	}

	@Test
	public void nineItemsBatch5() throws InterruptedException {
		List<IResourcePersistentId> resourceIds = buildPidList(9);

		// We don't care in which order, but one partition size should be
		// 5 and one should be 4
		Set<Integer> nums = Sets.newHashSet(5, 4);

		Consumer<List<IResourcePersistentId>> partitionConsumer = buildPartitionConsumer(myLatch);
		myLatch.setExpectedCount(2);
		getPartitionRunner(5).runInPartitionedThreads(resourceIds, partitionConsumer);
		List<HookParams> calls = myLatch.awaitExpected();
		PartitionCall partitionCall1 = (PartitionCall) PointcutLatch.getLatchInvocationParameter(calls, 0);
<<<<<<< HEAD
		assertThat(partitionCall1.threadName, isOneOf(TEST_THREADNAME_1, TEST_THREADNAME_2));
		assertThat(nums.remove(partitionCall1.size)).isEqualTo(true);
		PartitionCall partitionCall2 = (PartitionCall) PointcutLatch.getLatchInvocationParameter(calls, 1);
		assertThat(partitionCall2.threadName, isOneOf(TEST_THREADNAME_1, TEST_THREADNAME_2));
		assertThat(nums.remove(partitionCall2.size)).isEqualTo(true);
		assertThat(partitionCall2.threadName).isNotEqualTo(partitionCall1.threadName);
=======
		assertThat(partitionCall1.threadName, is(oneOf(TEST_THREADNAME_1, TEST_THREADNAME_2)));
		assertEquals(true, nums.remove(partitionCall1.size));
		PartitionCall partitionCall2 = (PartitionCall) PointcutLatch.getLatchInvocationParameter(calls, 1);
		assertThat(partitionCall2.threadName, is(oneOf(TEST_THREADNAME_1, TEST_THREADNAME_2)));
		assertEquals(true, nums.remove(partitionCall2.size));
		assertNotEquals(partitionCall1.threadName, partitionCall2.threadName);
>>>>>>> master
	}



	/**
	 * See #5636 $expunge operation ignoring ExpungeThreadCount setting in certain cases
	 */
	@Test
	public void testExpunge_withTasksSizeBiggerThanExecutorQueue_usesConfiguredNumberOfThreads() throws InterruptedException {
		// setup
		List<IResourcePersistentId> resourceIds = buildPidList(2500);
		Consumer<List<IResourcePersistentId>> partitionConsumer = buildPartitionConsumer(myLatch);
		// with batch size = 2 we expect 2500/2 runnableTasks to be created
		myLatch.setExpectedCount(1250);

		// execute
		getPartitionRunner(2, 2).runInPartitionedThreads(resourceIds, partitionConsumer);
		List<HookParams> calls = myLatch.awaitExpected();

		// validate - only two threads should be used for execution
		for (int i = 0; i < 1250; i++) {
			PartitionCall partitionCall = (PartitionCall) PointcutLatch.getLatchInvocationParameter(calls, i);
			assertThat(partitionCall.threadName, is(oneOf(TEST_THREADNAME_1, TEST_THREADNAME_2)));
		}
	}

	@Test
	public void tenItemsOneThread() throws InterruptedException {
		List<IResourcePersistentId> resourceIds = buildPidList(10);

		Consumer<List<IResourcePersistentId>> partitionConsumer = buildPartitionConsumer(myLatch);
		myLatch.setExpectedCount(2);
		getPartitionRunner(5, 1).runInPartitionedThreads(resourceIds, partitionConsumer);
		List<HookParams> calls = myLatch.awaitExpected();
		{
			PartitionCall partitionCall = (PartitionCall) PointcutLatch.getLatchInvocationParameter(calls, 0);
			assertThat(partitionCall.threadName).isEqualTo(TEST_THREADNAME_1);
			assertThat(partitionCall.size).isEqualTo(5);
		}
		{
			PartitionCall partitionCall = (PartitionCall) PointcutLatch.getLatchInvocationParameter(calls, 1);
			assertThat(partitionCall.threadName).isEqualTo(TEST_THREADNAME_1);
			assertThat(partitionCall.size).isEqualTo(5);
		}
	}

	private Consumer<List<IResourcePersistentId>> buildPartitionConsumer(PointcutLatch latch) {
		return list -> latch.call(new PartitionCall(Thread.currentThread().getName(), list.size()));
	}

	static class PartitionCall {
		private final String threadName;
		private final int size;

		PartitionCall(String theThreadName, int theSize) {
			threadName = theThreadName;
			size = theSize;
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
				.append("myThreadName", threadName)
				.append("mySize", size)
				.toString();
		}
	}
}
