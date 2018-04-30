package crm.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;

import cucumber.api.Scenario;
import cucumber.api.java8.En;

public class Steps implements En {
	HttpCrmCall call= new HttpCrmCall();
	static boolean debug;

	public Steps() {

		Before((Scenario scenario) -> {
			CurrentScenario.setCurrentScenario(scenario);
			// logger.setLevel(Level.INFO);
		});

		Given("debug is (.*)$", (String status) -> {
			System.out.println("DEBUG " + status);
			debug = "active".equals(status);
		});

		Given("protocol is (.*)$", (String protocol) -> {
			System.out.println("Protocol: " + protocol);
			call.protocol = protocol;
		});

		And("path is (.*)$", (String path) -> {
			System.out.println("Path: " + path);
			call.path = path;
		});

		Given("host is (.*)$", (String host) -> {
			System.out.println("Host: " + host);
			call.host = host;
		});

		And("UserId is (.*)$", (String userId) -> {
			System.out.println("userId: " + userId);
			call.userId = userId;
		});

		And("UserPwd is (.*)$", (String userPwd) -> {
			call.pwd = userPwd;
		});

		Given("query (.*)$", (String q) -> {
			reset();
			System.out.println("Query: " + q);
			call.query = q;
		});

		And("compagnia (.*)$", (String comp) -> {
			System.out.println("Compagnia: " + comp);
			call.compagnia = comp;
		});

		And("agenzia (.*)$", (String age) -> {
			System.out.println("Agenzia: " + age);
			call.agenzia = age;
		});

		And("pagina (\\d+)", (Integer page) -> {
			System.out.println("Pagina: " + page);
			call.pagina = page;
		});

		And("risultati in pagina (\\d+)", (Integer n) -> {
			System.out.println("Risultati in Pagina: " + n);
			call.nRisulati = n;
		});

		Then("response code (\\d+)", (Integer code) -> {
			System.out.println("ResponseCode: " + code);
			Assert.assertTrue(code == call.responseCode());
		});

		And("the response match (.*)$", (String req) -> {
			Assert.assertTrue((Boolean) HttpCrmCall.class
					.getMethod(req.replaceAll("-", "_").replaceAll(" ", "").toLowerCase()).invoke(call));
		});
	}

	private void reset() {
		call.agenzia = null;
		call.compagnia = null;
		call.query = null;
		call.pagina = 1;
		call.nRisulati = 10;
	}

	public class HttpCrmCall {
		String protocol;
		String host;
		String path;
		String userId;
		String pwd;
		String query;
		String agenzia;
		String compagnia;
		int pagina = 1;
		int nRisulati = 10;

		private HttpGet constructGet() throws IOException, URISyntaxException {

			String baseAuth = "Basic " + Base64.encodeBase64String((userId + ":" + pwd).getBytes());
			if (debug)
				System.out.println("Base64: " + baseAuth);
			String url = path;
			if (compagnia != null && !compagnia.equals("D")) {
				if (url.charAt(url.length() - 1) != '/') {
					url += "/";
				}
				url += compagnia;
				if (agenzia != null && !agenzia.equals("D")) {
					url += "/" + agenzia;
				}
			}
			URIBuilder builder = new URIBuilder().setScheme(protocol).setHost(host).setPath(url)
					.setParameter("q", query).setParameter("pagina", "" + pagina)
					.setParameter("risultatiPagina", "" + nRisulati);

			URI uri = builder.build();
			HttpGet httpget = new HttpGet(uri);
			httpget.setHeader("Authorization", baseAuth);
			if (debug)
				System.out.println("URI: " + httpget.getURI());

			return httpget;
		}

		public int responseCode() throws IOException, URISyntaxException {
			CloseableHttpResponse response = callRequest();
			return response.getStatusLine().getStatusCode();
		}

		public boolean req_f11() throws IOException, URISyntaxException {
			JSONObject responseObject = convertResponse();
			boolean match = true;
			List<String> tokens = Arrays.asList(query.split(" "));

			if (responseObject.has("risultati")) {
				JSONArray results = responseObject.getJSONArray("risultati");
				for (Object obj : results) {
					JSONObject res = (JSONObject) obj;
					JSONObject content = res.getJSONObject("content");
					match = match & content.getString("agenziaMadre").equals(agenzia) & allMatch(content, tokens);
				}
			}

			return match;
		}

		public boolean req_f12() throws IOException, URISyntaxException {
			JSONObject responseObject = convertResponse();
			boolean match = true;
			List<String> tokens = Arrays.asList(query.split(" "));

			if (responseObject.has("risultati")) {
				JSONArray results = responseObject.getJSONArray("risultati");
				for (Object obj : results) {
					JSONObject res = (JSONObject) obj;
					JSONObject content = res.getJSONObject("content");
					match = match & groupMatch(content, tokens);
				}
			}

			return match;
		}

		public boolean req_f14() throws IOException, URISyntaxException {
			JSONObject responseObject = convertResponse();
			boolean match = true;
			List<String> tokens = Arrays.asList(query.split(" "));

			if (responseObject.has("risultati")) {
				JSONArray results = responseObject.getJSONArray("risultati");
				if (this.compagnia == null || this.compagnia.equals("D")) {
					for (Object obj : results) {
						JSONObject res = (JSONObject) obj;
						JSONObject content = res.getJSONObject("content");
						match = match &groupMatch(content, tokens);
					}
				} else {
					for (Object obj : results) {
						JSONObject res = (JSONObject) obj;
						JSONObject content = res.getJSONObject("content");
						match = match & allMatch(content, tokens);
					}
				}
			}

			return match;
		}
		
		public boolean req_f4() throws IOException, URISyntaxException {
			JSONObject responseObject = convertResponse();
			boolean result=false;
			if (responseObject.has("risultati")) {
				JSONArray results = responseObject.getJSONArray("risultati");
				result = results.length() == 0;
			}
			return result;
		}
		
		public boolean req_f6() throws IOException, URISyntaxException {
			/*
			 * TODO requisito su anagrafiche cessate 
			 * */
			return true;
		}
		
		public boolean req_f23() throws IOException, URISyntaxException {
			return univoche();
		}

		public boolean req_f24() throws IOException, URISyntaxException {
			return univoche();
		}

		private boolean univoche() throws UnsupportedOperationException, IOException, URISyntaxException {
			this.pagina = 1;
			JSONObject responseObject = convertResponse();

			List<String> ids = new ArrayList<>();

			while (responseObject.has("risultati") && responseObject.has("successivo")
					&& !responseObject.getString("successivo").equals("")) {
				JSONArray results = responseObject.getJSONArray("risultati");
				for (Object obj : results) {
					JSONObject res = (JSONObject) obj;
					JSONObject content = res.getJSONObject("content");
					String idUrn = content.getString("idUrn");
					ids.add(idUrn);
				}

				this.pagina++;
				responseObject = convertResponse();
			}

			return ids.size() == ids.stream().distinct().collect(Collectors.toList()).size();
		}

		private boolean allMatch(JSONObject content, List<String> tokens) {
			String nominativo = content.has("nominativo") ? content.getString("nominativo") : "";
			String cfIva = content.has("cfIva") ? content.getString("cfIva") : "";
			String indirizzo = content.has("indirizzo") ? content.getString("indirizzo") : "";
			indirizzo += " " + (content.has("localita") ? content.getString("localita") : "");
			indirizzo += " " + (content.has("comune") ? content.getString("comune") : "");
			indirizzo += " " + (content.has("cap") ? content.getString("cap") : "");
			indirizzo += " " + (content.has("provincia") ? content.getString("provincia") : "");
			indirizzo += " " + (content.has("nazione") ? content.getString("nazione") : "");
			String cdg = content.has("cdg") ? content.getString("cdg") : "";

			String text = (nominativo + " " + cfIva + " " + indirizzo + " " + cdg).toLowerCase();
			Set<String> textSet = new HashSet<String>((Collection<String>) Arrays.asList(text.split(" ")));

			tokens = tokens.stream().map(String::toLowerCase).map(token -> token.length() > 3 ? token + ".*" : token)
					.collect(Collectors.toList());

			if (debug)
				System.out.println("----TOKEN");
			if (debug)
				tokens.stream().forEach(System.out::println);
			if (debug)
				System.out.println("END TOKEN ----");

			if (debug)
				System.out.println("----TEXT in Content:");
			if (debug)
				textSet.stream().forEach(System.out::println);
			if (debug)
				System.out.println("END TEXT in Content----");

			List<String> matchTokens = tokens.stream()
					.filter(token -> textSet.stream().anyMatch(t -> ((String) t).matches(token)))
					.collect(Collectors.toList());

			if (debug)
				System.out.println("----TOKEN MATCH");
			if (debug)
				matchTokens.stream().forEach(System.out::println);
			if (debug)
				System.out.println("END TOKEN MATCH ----");

			return matchTokens.size() == tokens.size();
		}

		private boolean groupMatch(JSONObject content, List<String> tokens) {
			String nominativo = content.has("nominativo") ? content.getString("nominativo") : "";
			String cfIva = content.has("cfIva") ? content.getString("cfIva") : "";
			String cdg = content.has("cdg") ? content.getString("cdg") : "";

			String text = (nominativo + " " + cfIva + " " + cdg).toLowerCase();
			Set<String> textSet = new HashSet<String>((Collection<String>) Arrays.asList(text.split(" ")));

			tokens = tokens.stream().map(String::toLowerCase).map(token -> token.length() > 3 ? token + ".*" : token)
					.collect(Collectors.toList());

			if (debug)
				System.out.println("----TOKEN");
			if (debug)
				tokens.stream().forEach(System.out::println);
			if (debug)
				System.out.println("END TOKEN ----");

			if (debug)
				System.out.println("----TEXT in Content:");
			if (debug)
				textSet.stream().forEach(System.out::println);
			if (debug)
				System.out.println("END TEXT in Content----");

			List<String> matchTokens = tokens.stream()
					.filter(token -> textSet.stream().anyMatch(t -> ((String) t).matches(token)))
					.collect(Collectors.toList());

			if (debug)
				System.out.println("----TOKEN MATCH");
			if (debug)
				matchTokens.stream().forEach(System.out::println);
			if (debug)
				System.out.println("END TOKEN MATCH ----");

			return matchTokens.size() == tokens.size();
		}

		private CloseableHttpResponse callRequest() throws IOException, URISyntaxException {
			HttpGet call = constructGet();
			CloseableHttpClient httpclient = HttpClients.createDefault();
			CloseableHttpResponse response = httpclient.execute(call);
			if (debug)
				System.out.println(response);
			return response;
		}

		private JSONObject convertResponse() throws UnsupportedOperationException, IOException, URISyntaxException {
			CloseableHttpResponse response = callRequest();
			InputStream input = response.getEntity().getContent();
			BufferedReader buff = new BufferedReader(new InputStreamReader(input));
			String line = null;
			String obj = "";
			while ((line = buff.readLine()) != null) {
				obj += line;
			}
			JSONObject responseObject = new JSONObject(obj);
			return responseObject;
		}

	}

}
