package ca.uhn.fhir.rest.server;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.TokenParamModifier;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import ca.uhn.fhir.test.utilities.HttpClientExtension;
import ca.uhn.fhir.test.utilities.server.RestfulServerExtension;
import ca.uhn.fhir.util.TestUtil;
import ca.uhn.fhir.util.UrlUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.hl7.fhir.dstu3.hapi.rest.server.ServerCapabilityStatementProvider;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationDefinition;
import org.hl7.fhir.dstu3.model.Parameters;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OperationServerWithSearchParamTypesDstu3Test {
	private static final FhirContext ourCtx = FhirContext.forDstu3Cached();

	private static String ourLastMethod;
	private static List<StringOrListParam> ourLastParamValStr;
	private static List<TokenOrListParam> ourLastParamValTok;
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(OperationServerWithSearchParamTypesDstu3Test.class);


	@RegisterExtension
	private RestfulServerExtension ourServer  = new RestfulServerExtension(ourCtx)
		 .setDefaultResponseEncoding(EncodingEnum.XML)
		 .registerProvider(new PatientProvider())
		 .withPagingProvider(new FifoMemoryPagingProvider(10).setDefaultPageSize(2))
		 .setDefaultPrettyPrint(false);

	@RegisterExtension
	private HttpClientExtension ourClient = new HttpClientExtension();

	@BeforeEach
	public void before() {
		ourLastMethod = "";
		ourLastParamValStr = null;
		ourLastParamValTok = null;
	}

	private HttpServletRequest createHttpServletRequest() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getRequestURI()).thenReturn("/FhirStorm/fhir/Patient/_search");
		when(req.getServletPath()).thenReturn("/fhir");
		when(req.getRequestURL()).thenReturn(new StringBuffer().append("http://fhirstorm.dyndns.org:8080/FhirStorm/fhir/Patient/_search"));
		when(req.getContextPath()).thenReturn("/FhirStorm");
		return req;
	}

	private ServletConfig createServletConfig() {
		ServletConfig sc = mock(ServletConfig.class);
		when(sc.getServletContext()).thenReturn(null);
		return sc;
	}

	@Test
	public void testAndListWithParameters() throws Exception {
		Parameters p = new Parameters();
		p.addParameter().setName("valstr").setValue(new StringType("VALSTR1A,VALSTR1B"));
		p.addParameter().setName("valstr").setValue(new StringType("VALSTR2A,VALSTR2B"));
		p.addParameter().setName("valtok").setValue(new StringType("VALTOK1A|VALTOK1B"));
		p.addParameter().setName("valtok").setValue(new StringType("VALTOK2A|VALTOK2B"));
		String inParamsStr = ourCtx.newXmlParser().encodeResourceToString(p);

		HttpPost httpPost = new HttpPost(ourServer.getBaseUrl() + "/Patient/$andlist");
		httpPost.setEntity(new StringEntity(inParamsStr, ContentType.create(Constants.CT_FHIR_XML, "UTF-8")));
		HttpResponse status = ourClient.execute(httpPost);

		assertThat(status.getStatusLine().getStatusCode()).isEqualTo(200);
		String response = IOUtils.toString(status.getEntity().getContent());
		ourLog.info(response);
		IOUtils.closeQuietly(status.getEntity().getContent());

		assertThat(ourLastParamValStr).hasSize(2);
		assertThat(ourLastParamValStr.get(0).getValuesAsQueryTokens()).hasSize(2);
		assertThat(ourLastParamValStr.get(0).getValuesAsQueryTokens().get(0).getValue()).isEqualTo("VALSTR1A");
		assertThat(ourLastParamValStr.get(0).getValuesAsQueryTokens().get(1).getValue()).isEqualTo("VALSTR1B");
		assertThat(ourLastParamValStr.get(1).getValuesAsQueryTokens().get(0).getValue()).isEqualTo("VALSTR2A");
		assertThat(ourLastParamValStr.get(1).getValuesAsQueryTokens().get(1).getValue()).isEqualTo("VALSTR2B");
		assertThat(ourLastParamValTok).hasSize(2);
		assertThat(ourLastParamValTok.get(0).getValuesAsQueryTokens()).hasSize(1);
		assertThat(ourLastParamValTok.get(0).getValuesAsQueryTokens().get(0).getSystem()).isEqualTo("VALTOK1A");
		assertThat(ourLastParamValTok.get(0).getValuesAsQueryTokens().get(0).getValue()).isEqualTo("VALTOK1B");
		assertThat(ourLastParamValTok.get(1).getValuesAsQueryTokens().get(0).getSystem()).isEqualTo("VALTOK2A");
		assertThat(ourLastParamValTok.get(1).getValuesAsQueryTokens().get(0).getValue()).isEqualTo("VALTOK2B");
		assertThat(ourLastMethod).isEqualTo("type $orlist");
	}

	@Test
	public void testEscapedOperationName() throws Exception {
		HttpGet httpGet = new HttpGet(ourServer.getBaseUrl() + "/Patient/%24andlist?valstr=VALSTR1A,VALSTR1B&valstr=VALSTR2A,VALSTR2B&valtok=" + UrlUtil.escapeUrlParam("VALTOK1A|VALTOK1B") + "&valtok=" + UrlUtil.escapeUrlParam("VALTOK2A|VALTOK2B"));
		HttpResponse status = ourClient.execute(httpGet);

		assertThat(status.getStatusLine().getStatusCode()).isEqualTo(200);
		String response = IOUtils.toString(status.getEntity().getContent());
		ourLog.info(response);
		IOUtils.closeQuietly(status.getEntity().getContent());

		assertThat(ourLastParamValStr).hasSize(2);
	}
	
	@Test
	public void testAndListWithUrl() throws Exception {
		HttpGet httpGet = new HttpGet(ourServer.getBaseUrl() + "/Patient/$andlist?valstr=VALSTR1A,VALSTR1B&valstr=VALSTR2A,VALSTR2B&valtok=" + UrlUtil.escapeUrlParam("VALTOK1A|VALTOK1B") + "&valtok=" + UrlUtil.escapeUrlParam("VALTOK2A|VALTOK2B"));
		HttpResponse status = ourClient.execute(httpGet);

		assertThat(status.getStatusLine().getStatusCode()).isEqualTo(200);
		String response = IOUtils.toString(status.getEntity().getContent());
		ourLog.info(response);
		IOUtils.closeQuietly(status.getEntity().getContent());

		assertThat(ourLastParamValStr).hasSize(2);
		assertThat(ourLastParamValStr.get(0).getValuesAsQueryTokens()).hasSize(2);
		assertThat(ourLastParamValStr.get(0).getValuesAsQueryTokens().get(0).getValue()).isEqualTo("VALSTR1A");
		assertThat(ourLastParamValStr.get(0).getValuesAsQueryTokens().get(1).getValue()).isEqualTo("VALSTR1B");
		assertThat(ourLastParamValStr.get(1).getValuesAsQueryTokens().get(0).getValue()).isEqualTo("VALSTR2A");
		assertThat(ourLastParamValStr.get(1).getValuesAsQueryTokens().get(1).getValue()).isEqualTo("VALSTR2B");
		assertThat(ourLastParamValTok).hasSize(2);
		assertThat(ourLastParamValTok.get(0).getValuesAsQueryTokens()).hasSize(1);
		assertThat(ourLastParamValTok.get(0).getValuesAsQueryTokens().get(0).getSystem()).isEqualTo("VALTOK1A");
		assertThat(ourLastParamValTok.get(0).getValuesAsQueryTokens().get(0).getValue()).isEqualTo("VALTOK1B");
		assertThat(ourLastParamValTok.get(1).getValuesAsQueryTokens().get(0).getSystem()).isEqualTo("VALTOK2A");
		assertThat(ourLastParamValTok.get(1).getValuesAsQueryTokens().get(0).getValue()).isEqualTo("VALTOK2B");
		assertThat(ourLastMethod).isEqualTo("type $orlist");
	}

	@Test
	public void testGenerateCapabilityStatement() throws Exception {
		RestfulServer rs = new RestfulServer(ourCtx);
		rs.setProviders(new PatientProvider());

		ServerCapabilityStatementProvider sc = new ServerCapabilityStatementProvider(rs);
		rs.setServerConformanceProvider(sc);

		rs.init(createServletConfig());

		CapabilityStatement conformance = sc.getServerConformance(createHttpServletRequest(), createRequestDetails(rs));

		String conf = ourCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(conformance);
		ourLog.info(conf);
		//@formatter:off
		assertThat(conf).contains(
			"<type value=\"Patient\"/>",
			"<operation>",
			"<name value=\"andlist\"/>",
			"</operation>"
		);
		assertThat(conf).contains(
			"<type value=\"Patient\"/>",
			"<operation>",
			"<name value=\"nonrepeating\"/>"
		);
		assertThat(conf).contains(
			"<type value=\"Patient\"/>",
			"<operation>",
			"<name value=\"orlist\"/>"
		);
		//@formatter:on
		
		/*
		 * Check the operation definitions themselves
		 */
		OperationDefinition andListDef = sc.readOperationDefinition(new IdType("OperationDefinition/Patient-t-andlist"), createRequestDetails(rs));
		String def = ourCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(andListDef);
		ourLog.info(def);
		//@formatter:off
		assertThat(def).contains(
			"<parameter>",
			"<name value=\"valtok\"/>",
			"<use value=\"in\"/>",
			"<min value=\"0\"/>",
			"<max value=\"10\"/>",
			"<type value=\"string\"/>",
			"<searchType value=\"token\"/>",
			"</parameter>"
		);
		//@formatter:on
		
		andListDef = sc.readOperationDefinition(new IdType("OperationDefinition/Patient-t-andlist-withnomax"), createRequestDetails(rs));
		def = ourCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(andListDef);
		ourLog.info(def);
		//@formatter:off
		assertThat(def).contains(
			"<parameter>",
			"<name value=\"valtok\"/>",
			"<use value=\"in\"/>",
			"<min value=\"0\"/>",
			"<max value=\"*\"/>",
			"<type value=\"string\"/>",
			"<searchType value=\"token\"/>",
			"</parameter>"
		);
		//@formatter:on

		OperationDefinition orListDef = sc.readOperationDefinition(new IdType("OperationDefinition/Patient-t-orlist"), createRequestDetails(rs));
		def = ourCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(orListDef);
		ourLog.info(def);
		//@formatter:off
		assertThat(def).contains(
			"<parameter>",
			"<name value=\"valtok\"/>",
			"<use value=\"in\"/>",
			"<min value=\"0\"/>",
			"<max value=\"10\"/>",
			"<type value=\"string\"/>",
			"<searchType value=\"token\"/>",
			"</parameter>"
		);
		//@formatter:on
		
		orListDef = sc.readOperationDefinition(new IdType("OperationDefinition/Patient-t-orlist-withnomax"), createRequestDetails(rs));
		def = ourCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(orListDef);
		ourLog.info(def);
		//@formatter:off
		assertThat(def).contains(
			"<parameter>",
			"<name value=\"valtok\"/>",
			"<use value=\"in\"/>",
			"<min value=\"0\"/>",
			"<max value=\"*\"/>",
			"<type value=\"string\"/>",
			"<searchType value=\"token\"/>",
			"</parameter>"
		);
		//@formatter:on

	}

	@Test
	public void testNonRepeatingWithParams() throws Exception {
		Parameters p = new Parameters();
		p.addParameter().setName("valstr").setValue(new StringType("VALSTR"));
		p.addParameter().setName("valtok").setValue(new StringType("VALTOKA|VALTOKB"));
		String inParamsStr = ourCtx.newXmlParser().encodeResourceToString(p);

		HttpPost httpPost = new HttpPost(ourServer.getBaseUrl() + "/Patient/$nonrepeating");
		httpPost.setEntity(new StringEntity(inParamsStr, ContentType.create(Constants.CT_FHIR_XML, "UTF-8")));
		HttpResponse status = ourClient.execute(httpPost);

		assertThat(status.getStatusLine().getStatusCode()).isEqualTo(200);
		String response = IOUtils.toString(status.getEntity().getContent());
		ourLog.info(response);
		IOUtils.closeQuietly(status.getEntity().getContent());

		assertThat(ourLastParamValStr).hasSize(1);
		assertThat(ourLastParamValStr.get(0).getValuesAsQueryTokens()).hasSize(1);
		assertThat(ourLastParamValStr.get(0).getValuesAsQueryTokens().get(0).getValue()).isEqualTo("VALSTR");
		assertThat(ourLastParamValTok).hasSize(1);
		assertThat(ourLastParamValTok.get(0).getValuesAsQueryTokens()).hasSize(1);
		assertThat(ourLastParamValTok.get(0).getValuesAsQueryTokens().get(0).getSystem()).isEqualTo("VALTOKA");
		assertThat(ourLastParamValTok.get(0).getValuesAsQueryTokens().get(0).getValue()).isEqualTo("VALTOKB");
		assertThat(ourLastMethod).isEqualTo("type $nonrepeating");
	}
	@Test
	public void testNonRepeatingWithUrl() throws Exception {
		HttpGet httpGet = new HttpGet(ourServer.getBaseUrl() + "/Patient/$nonrepeating?valstr=VALSTR&valtok=" + UrlUtil.escapeUrlParam("VALTOKA|VALTOKB"));
		HttpResponse status = ourClient.execute(httpGet);

		assertThat(status.getStatusLine().getStatusCode()).isEqualTo(200);
		String response = IOUtils.toString(status.getEntity().getContent());
		ourLog.info(response);
		IOUtils.closeQuietly(status.getEntity().getContent());

		assertThat(ourLastParamValStr).hasSize(1);
		assertThat(ourLastParamValStr.get(0).getValuesAsQueryTokens()).hasSize(1);
		assertThat(ourLastParamValStr.get(0).getValuesAsQueryTokens().get(0).getValue()).isEqualTo("VALSTR");
		assertThat(ourLastParamValTok).hasSize(1);
		assertThat(ourLastParamValTok.get(0).getValuesAsQueryTokens()).hasSize(1);
		assertThat(ourLastParamValTok.get(0).getValuesAsQueryTokens().get(0).getSystem()).isEqualTo("VALTOKA");
		assertThat(ourLastParamValTok.get(0).getValuesAsQueryTokens().get(0).getValue()).isEqualTo("VALTOKB");
		assertThat(ourLastMethod).isEqualTo("type $nonrepeating");
	}

	@Test
	public void testNonRepeatingWithUrlQualified() throws Exception {
		HttpGet httpGet = new HttpGet(ourServer.getBaseUrl() + "/Patient/$nonrepeating?valstr:exact=VALSTR&valtok:not=" + UrlUtil.escapeUrlParam("VALTOKA|VALTOKB"));
		HttpResponse status = ourClient.execute(httpGet);

		assertThat(status.getStatusLine().getStatusCode()).isEqualTo(200);
		String response = IOUtils.toString(status.getEntity().getContent());
		ourLog.info(response);
		IOUtils.closeQuietly(status.getEntity().getContent());

		assertThat(ourLastParamValStr).hasSize(1);
		assertThat(ourLastParamValStr.get(0).getValuesAsQueryTokens()).hasSize(1);
		assertThat(ourLastParamValStr.get(0).getValuesAsQueryTokens().get(0).getValue()).isEqualTo("VALSTR");
		assertThat(ourLastParamValStr.get(0).getValuesAsQueryTokens().get(0).isExact()).isTrue();
		assertThat(ourLastParamValTok).hasSize(1);
		assertThat(ourLastParamValTok.get(0).getValuesAsQueryTokens()).hasSize(1);
		assertThat(ourLastParamValTok.get(0).getValuesAsQueryTokens().get(0).getSystem()).isEqualTo("VALTOKA");
		assertThat(ourLastParamValTok.get(0).getValuesAsQueryTokens().get(0).getValue()).isEqualTo("VALTOKB");
		assertThat(ourLastParamValTok.get(0).getValuesAsQueryTokens().get(0).getModifier()).isEqualTo(TokenParamModifier.NOT);
		assertThat(ourLastMethod).isEqualTo("type $nonrepeating");
	}
	
	@Test
	public void testOrListWithParameters() throws Exception {
		Parameters p = new Parameters();
		p.addParameter().setName("valstr").setValue(new StringType("VALSTR1A,VALSTR1B"));
		p.addParameter().setName("valstr").setValue(new StringType("VALSTR2A,VALSTR2B"));
		p.addParameter().setName("valtok").setValue(new StringType("VALTOK1A|VALTOK1B"));
		p.addParameter().setName("valtok").setValue(new StringType("VALTOK2A|VALTOK2B"));
		String inParamsStr = ourCtx.newXmlParser().encodeResourceToString(p);

		HttpPost httpPost = new HttpPost(ourServer.getBaseUrl() + "/Patient/$orlist");
		httpPost.setEntity(new StringEntity(inParamsStr, ContentType.create(Constants.CT_FHIR_XML, "UTF-8")));
		HttpResponse status = ourClient.execute(httpPost);

		assertThat(status.getStatusLine().getStatusCode()).isEqualTo(200);
		String response = IOUtils.toString(status.getEntity().getContent());
		ourLog.info(response);
		IOUtils.closeQuietly(status.getEntity().getContent());

		assertThat(ourLastParamValStr).hasSize(2);
		assertThat(ourLastParamValStr.get(0).getValuesAsQueryTokens()).hasSize(2);
		assertThat(ourLastParamValStr.get(0).getValuesAsQueryTokens().get(0).getValue()).isEqualTo("VALSTR1A");
		assertThat(ourLastParamValStr.get(0).getValuesAsQueryTokens().get(1).getValue()).isEqualTo("VALSTR1B");
		assertThat(ourLastParamValStr.get(1).getValuesAsQueryTokens().get(0).getValue()).isEqualTo("VALSTR2A");
		assertThat(ourLastParamValStr.get(1).getValuesAsQueryTokens().get(1).getValue()).isEqualTo("VALSTR2B");
		assertThat(ourLastParamValTok).hasSize(2);
		assertThat(ourLastParamValTok.get(0).getValuesAsQueryTokens()).hasSize(1);
		assertThat(ourLastParamValTok.get(0).getValuesAsQueryTokens().get(0).getSystem()).isEqualTo("VALTOK1A");
		assertThat(ourLastParamValTok.get(0).getValuesAsQueryTokens().get(0).getValue()).isEqualTo("VALTOK1B");
		assertThat(ourLastParamValTok.get(1).getValuesAsQueryTokens().get(0).getSystem()).isEqualTo("VALTOK2A");
		assertThat(ourLastParamValTok.get(1).getValuesAsQueryTokens().get(0).getValue()).isEqualTo("VALTOK2B");
		assertThat(ourLastMethod).isEqualTo("type $orlist");
	}

	@Test
	public void testOrListWithUrl() throws Exception {
		HttpGet httpGet = new HttpGet(ourServer.getBaseUrl() + "/Patient/$orlist?valstr=VALSTR1A,VALSTR1B&valstr=VALSTR2A,VALSTR2B&valtok=" + UrlUtil.escapeUrlParam("VALTOK1A|VALTOK1B") + "&valtok=" + UrlUtil.escapeUrlParam("VALTOK2A|VALTOK2B"));
		HttpResponse status = ourClient.execute(httpGet);

		assertThat(status.getStatusLine().getStatusCode()).isEqualTo(200);
		String response = IOUtils.toString(status.getEntity().getContent());
		ourLog.info(response);
		IOUtils.closeQuietly(status.getEntity().getContent());

		assertThat(ourLastParamValStr).hasSize(2);
		assertThat(ourLastParamValStr.get(0).getValuesAsQueryTokens()).hasSize(2);
		assertThat(ourLastParamValStr.get(0).getValuesAsQueryTokens().get(0).getValue()).isEqualTo("VALSTR1A");
		assertThat(ourLastParamValStr.get(0).getValuesAsQueryTokens().get(1).getValue()).isEqualTo("VALSTR1B");
		assertThat(ourLastParamValStr.get(1).getValuesAsQueryTokens().get(0).getValue()).isEqualTo("VALSTR2A");
		assertThat(ourLastParamValStr.get(1).getValuesAsQueryTokens().get(1).getValue()).isEqualTo("VALSTR2B");
		assertThat(ourLastParamValTok).hasSize(2);
		assertThat(ourLastParamValTok.get(0).getValuesAsQueryTokens()).hasSize(1);
		assertThat(ourLastParamValTok.get(0).getValuesAsQueryTokens().get(0).getSystem()).isEqualTo("VALTOK1A");
		assertThat(ourLastParamValTok.get(0).getValuesAsQueryTokens().get(0).getValue()).isEqualTo("VALTOK1B");
		assertThat(ourLastParamValTok.get(1).getValuesAsQueryTokens().get(0).getSystem()).isEqualTo("VALTOK2A");
		assertThat(ourLastParamValTok.get(1).getValuesAsQueryTokens().get(0).getValue()).isEqualTo("VALTOK2B");
		assertThat(ourLastMethod).isEqualTo("type $orlist");
	}

	@AfterAll
	public static void afterClassClearContext() throws Exception {
		TestUtil.randomizeLocaleAndTimezone();
	}


	public static class PatientProvider implements IResourceProvider {


		@Operation(name = "$andlist", idempotent = true)
		public Parameters andlist(
				//@formatter:off
				@OperationParam(name="valstr", max=10) StringAndListParam theValStr,
				@OperationParam(name="valtok", max=10) TokenAndListParam theValTok
				//@formatter:on
		) {
			ourLastMethod = "type $orlist";
			ourLastParamValStr = theValStr.getValuesAsQueryTokens();
			ourLastParamValTok = theValTok.getValuesAsQueryTokens();
			
			return createEmptyParams();
		}

		@Operation(name = "$andlist-withnomax", idempotent = true)
		public Parameters andlistWithNoMax(
				//@formatter:off
				@OperationParam(name="valstr") StringAndListParam theValStr,
				@OperationParam(name="valtok") TokenAndListParam theValTok
				//@formatter:on
		) {
			ourLastMethod = "type $orlist";
			ourLastParamValStr = theValStr.getValuesAsQueryTokens();
			ourLastParamValTok = theValTok.getValuesAsQueryTokens();
			
			return createEmptyParams();
		}

		/**
		 * Just so we have something to return
		 */
		private Parameters createEmptyParams() {
			Parameters retVal = new Parameters();
			retVal.setId("100");
			return retVal;
		}

		@Override
		public Class<? extends IBaseResource> getResourceType() {
			return Patient.class;
		}

		@Operation(name = "$nonrepeating", idempotent = true)
		public Parameters nonrepeating(
				//@formatter:off
				@OperationParam(name="valstr") StringParam theValStr,
				@OperationParam(name="valtok") TokenParam theValTok
				//@formatter:on
		) {
			ourLastMethod = "type $nonrepeating";
			ourLastParamValStr = Collections.singletonList(new StringOrListParam().add(theValStr));
			ourLastParamValTok = Collections.singletonList(new TokenOrListParam().add(theValTok));
			
			return createEmptyParams();
		}

		@Operation(name = "$orlist", idempotent = true)
		public Parameters orlist(
				//@formatter:off
				@OperationParam(name="valstr", max=10) List<StringOrListParam> theValStr,
				@OperationParam(name="valtok", max=10) List<TokenOrListParam> theValTok
				//@formatter:on
		) {
			ourLastMethod = "type $orlist";
			ourLastParamValStr = theValStr;
			ourLastParamValTok = theValTok;
			
			return createEmptyParams();
		}

		@Operation(name = "$orlist-withnomax", idempotent = true)
		public Parameters orlistWithNoMax(
				//@formatter:off
				@OperationParam(name="valstr"
				) List<StringOrListParam> theValStr,
				@OperationParam(name="valtok") List<TokenOrListParam> theValTok
				//@formatter:on
		) {
			ourLastMethod = "type $orlist";
			ourLastParamValStr = theValStr;
			ourLastParamValTok = theValTok;
			
			return createEmptyParams();
		}

	}

	private RequestDetails createRequestDetails(RestfulServer theServer) {
		ServletRequestDetails retVal = new ServletRequestDetails();
		retVal.setServer(theServer);
		return retVal;
	}


}
