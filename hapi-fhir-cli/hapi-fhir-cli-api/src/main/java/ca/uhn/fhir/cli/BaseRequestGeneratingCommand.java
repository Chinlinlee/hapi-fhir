package ca.uhn.fhir.cli;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.AdditionalRequestHeadersInterceptor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class BaseRequestGeneratingCommand extends BaseCommand {

	public enum BaseRequestGeneratingCommandOptions {
		VERSION,
		BASE_URL,
		BASIC_AUTH,
		VERBOSE_LOGGING,
		HEADER_PASSTHROUGH
	}


	protected static final String HEADER_PASSTHROUGH = "hp";
	protected static final String HEADER_PASSTHROUGH_NAME = "header";
	protected static final String HEADER_PASSTHROUGH_LONGOPT = "header-passthrough";


	@Override
	public Options getOptions() {
		return getSomeOptions(Collections.emptySet());
	}

	/**
	 * Allows child classes to obtain a subset of the parent-defined options
	 */
	protected Options getSomeOptions(Collection<BaseRequestGeneratingCommandOptions> theExcludeOptions) {
		Options options = new Options();

		if (! theExcludeOptions.contains(BaseRequestGeneratingCommandOptions.VERSION)) {
			addFhirVersionOption(options);
		}

		if (! theExcludeOptions.contains(BaseRequestGeneratingCommandOptions.BASE_URL)) {
			addBaseUrlOption(options);
		}

		if (! theExcludeOptions.contains(BaseRequestGeneratingCommandOptions.BASIC_AUTH)) {
			addBasicAuthOption(options);
		}

		if (! theExcludeOptions.contains(BaseRequestGeneratingCommandOptions.VERBOSE_LOGGING)) {
			addVerboseLoggingOption(options);
		}

		if (! theExcludeOptions.contains(BaseRequestGeneratingCommandOptions.HEADER_PASSTHROUGH)) {
			addHeaderPassthroughOption(options);
		}

		return options;
	}


	@Override
	protected IGenericClient newClient(CommandLine theCommandLine) throws ParseException {
		IGenericClient client = super.newClient(theCommandLine);

		if (theCommandLine.hasOption(HEADER_PASSTHROUGH)) {
			client.registerInterceptor(
				new AdditionalRequestHeadersInterceptor(
					getAndParseOptionHeadersPassthrough(theCommandLine, HEADER_PASSTHROUGH)));
		}

		return client;
	}

	private void addHeaderPassthroughOption(Options theOptions) {
		addOptionalOption(theOptions, HEADER_PASSTHROUGH, HEADER_PASSTHROUGH_LONGOPT, HEADER_PASSTHROUGH_NAME,
			"If specified, this argument specifies headers to include in the generated request");
	}

	/**
	 * @return Returns the optional pass-through header name and value
	 */
	protected Map<String, List<String>> getAndParseOptionHeadersPassthrough(
		CommandLine theCommandLine, String theOptionName) throws ParseException {

		if (! theCommandLine.hasOption(theOptionName)) {
			return Collections.emptyMap();
		}

		Map<String, List<String>> headersMap = new HashMap<>();
		for (String nextOptionValue: theCommandLine.getOptionValues(theOptionName)) {
			Pair<String, String> nextHeader = parseNameValueParameter(":", theOptionName, nextOptionValue);
			headersMap.compute(nextHeader.getKey(), (k, v) -> v == null ? new ArrayList<>() : v).add(nextHeader.getValue());
		}

		return headersMap;
	}

}
