/*-
 * #%L
 * HAPI FHIR - Master Data Management
 * %%
 * Copyright (C) 2014 - 2023 Smile CDR, Inc.
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
package ca.uhn.fhir.mdm.api;

import ca.uhn.fhir.mdm.rules.json.MdmRulesJson;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public interface IMdmSettings {

	String EMPI_CHANNEL_NAME = "empi";

	// Parallel processing of MDM can result in missed matches.  Best to single-thread.
	int MDM_DEFAULT_CONCURRENT_CONSUMERS = 1;

	boolean isEnabled();

	int getConcurrentConsumers();

	MdmRulesJson getMdmRules();

	boolean isPreventEidUpdates();

	boolean isPreventMultipleEids();

	String getRuleVersion();

	String getSurvivorshipRules();

	default boolean isSupportedMdmType(String theResourceName) {
		return getMdmRules().getMdmTypes().contains(theResourceName);
	}

	default String getSupportedMdmTypes() {
		return getMdmRules().getMdmTypes().stream().collect(Collectors.joining(", "));
	}

	int getCandidateSearchLimit();

	String getGoldenResourcePartitionName();

	void setGoldenResourcePartitionName(String theGoldenResourcePartitionName);

	boolean getSearchAllPartitionForMatch();

	void setSearchAllPartitionForMatch(boolean theSearchAllPartitionForMatch);

	/**
	 * Returns a map of names -> list of related names
	 */
	Map<String, Collection<String>> nicknameMap();
}
