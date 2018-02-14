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

	private LoadingCache<String, String> tokenLoadingCache;

	public AuthRequestInterceptor(String serviceUrl, String username, String password) {
		this.username = username;
		this.password = password;
		this.serviceUrl = serviceUrl;
		buildCache();
	}

	private void buildCache() {
		log.debug("Building token cache");
		tokenLoadingCache = CacheBuilder.newBuilder()
			.refreshAfterWrite(120, TimeUnit.MINUTES) // every 2 hours
			.maximumSize(2)
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
							String token = response.getBody().getObject().getString(key);
							log.debug("Caching {} with value {}", key, token);
							return token;
						}
					} catch (UnirestException | JSONException e) {
						log.error(e.getMessage(), e);
					}
					throw new IllegalStateException("Failed to obtain " + key + " from authorization endpoint");
				}
			});
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
		throws IOException {
		try {
			log.debug("Retrieving id_token from cache");
			request.getHeaders().add("Authorization", "Bearer " + tokenLoadingCache.get("id_token"));
		} catch (ExecutionException e) {
			log.error(e.getMessage(), e);
		}
		return execution.execute(request, body);
	}
}
