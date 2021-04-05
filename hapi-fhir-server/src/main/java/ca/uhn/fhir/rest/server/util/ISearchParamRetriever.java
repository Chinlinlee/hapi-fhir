package ca.uhn.fhir.rest.server.util;

/*-
 * #%L
 * HAPI FHIR - Server Framework
 * %%
 * Copyright (C) 2014 - 2021 Smile CDR, Inc.
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

import ca.uhn.fhir.context.RuntimeSearchParam;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public interface ISearchParamRetriever {
	/**
	 * @return Returns {@literal null} if no match
	 */
	@Nullable
	RuntimeSearchParam getActiveSearchParam(String theResourceName, String theParamName);

	/**
	 * @return Returns all active search params for the given resource
	 */
	@Nonnull
	Map<String, RuntimeSearchParam> getActiveSearchParams(String theResourceName);

}
