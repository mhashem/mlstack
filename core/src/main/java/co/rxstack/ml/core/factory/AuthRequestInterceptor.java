package co.rxstack.ml.core.factory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class AuthRequestInterceptor implements ClientHttpRequestInterceptor {

	private static final Logger log = LoggerFactory.getLogger(AuthRequestInterceptor.class);

	private String username;
	private String password;
	private String serviceUrl;
	private ObjectMapper objectMapper = new ObjectMapper();

	private LoadingCache<String, String> tokenCache;

	public AuthRequestInterceptor(String serviceUrl, String username, String password) {
		this.username = username;
		this.password = password;
		this.serviceUrl = serviceUrl;
		buildCache();
	}

	private void buildCache() {
		tokenCache = CacheBuilder.newBuilder()
			.refreshAfterWrite(120, TimeUnit.MINUTES)
			.maximumSize(10)
			.build(new CacheLoader<String, String>() {
				@Override
				public String load(String key) throws Exception {
					try {
						Map<String, Object> params = new HashMap<>();
						params.put("username", username);
						params.put("password", password);
						params.put("rememberMe", true);
						HttpResponse<JsonNode> response = Unirest
							.post(serviceUrl + "/api/authenticate")
							.header("Content-Type", "application/json")
							.body(objectMapper.writeValueAsString(params))
							.asJson();
						if (response.getStatus() == 200) {
							return response.getBody().getObject().getString(key);
						}
					} catch (UnirestException | JSONException e) {
						log.error(e.getMessage(), e);
					}
					throw new IllegalStateException("Failed to obtain id_token from authorization endpoint");
				}
			});
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
		throws IOException {
		try {
			request.getHeaders().add("Authorization", "Bearer " + tokenCache.get("id_token"));
		} catch (ExecutionException e) {
			log.error(e.getMessage(), e);
		}
		return execution.execute(request, body);
	}
}
