package co.rxstack.ml.common.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

public class TrainingStatus {
	
	public enum Status {
		SUCCEEDED, // Training is succeeded.
		FAILED, // Training is failed.
		RUNNING; // Training is in progress.

		private static Map<String, Status> namesMap = new HashMap<>(3);

		static {
			namesMap.put("succeeded", SUCCEEDED);
			namesMap.put("failed", FAILED);
			namesMap.put("running", RUNNING);
		}

		@JsonCreator
		public static Status forValue(String value) {
			return namesMap.get(value.toLowerCase());
		}

		@JsonValue
		public String toValue() {
			for (Map.Entry<String, Status> entry : namesMap.entrySet()) {
				if (entry.getValue() == this)
					return entry.getKey();
			}

			return null; // or fail
		}
	}

	/**
	 * Training status.
	 */
	private Status status;

	/**
	 * Creation date time.
	 */
	@JsonFormat(pattern = "MM/dd/yyyy HH:mm:ss a")
	private Date createdDateTime;

	/**
	 * Last action date time.
	 */
	@JsonFormat(pattern = "MM/dd/yyyy HH:mm:ss a")
	private Date lastActionDateTime;

	/**
	 * Message. Only when failed
	 */
	private String message;

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Date getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(Date createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

	public Date getLastActionDateTime() {
		return lastActionDateTime;
	}

	public void setLastActionDateTime(Date lastActionDateTime) {
		this.lastActionDateTime = lastActionDateTime;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
