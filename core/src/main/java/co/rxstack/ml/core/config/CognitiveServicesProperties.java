package co.rxstack.ml.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cognitive", ignoreInvalidFields = true)
public class CognitiveServicesProperties {
	
	private String serviceUrl;
	private String subscriptionKey;
	private String personGroupId;
	private String personGroupName;

	public String getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public String getSubscriptionKey() {
		return subscriptionKey;
	}

	public void setSubscriptionKey(String subscriptionKey) {
		this.subscriptionKey = subscriptionKey;
	}

	public String getPersonGroupId() {
		return personGroupId;
	}

	public void setPersonGroupId(String personGroupId) {
		this.personGroupId = personGroupId;
	}

	public String getPersonGroupName() {
		return personGroupName;
	}

	public void setPersonGroupName(String personGroupName) {
		this.personGroupName = personGroupName;
	}
}
