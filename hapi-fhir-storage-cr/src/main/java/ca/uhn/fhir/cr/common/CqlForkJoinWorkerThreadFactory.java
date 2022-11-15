package ca.uhn.fhir.cr.common;

/*-
 * #%L
 * HAPI FHIR - Clinical Reasoning
 * %%
 * Copyright (C) 2014 - 2022 Smile CDR, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;

/**
 * This class resolves issues with loading JAXB in a server environment and using CompleteableFutures
 * https://stackoverflow.com/questions/49113207/completablefuture-forkjoinpool-set-class-loader
 **/
public class CqlForkJoinWorkerThreadFactory implements ForkJoinWorkerThreadFactory {

	@Override
	public final ForkJoinWorkerThread newThread(ForkJoinPool pool) {
		return new CqlForkJoinWorkerThread(pool);
	}

	private static class CqlForkJoinWorkerThread extends ForkJoinWorkerThread {

		private CqlForkJoinWorkerThread(final ForkJoinPool pool) {
			super(pool);
			// set the correct classloader here
			setContextClassLoader(Thread.currentThread().getContextClassLoader());
		}
	}
}
