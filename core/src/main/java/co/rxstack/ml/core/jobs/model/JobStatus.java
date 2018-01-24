package co.rxstack.ml.core.jobs.model;

public enum JobStatus {
	
	IDLE("idle"), RUNNING("running"), SUCCESS("success"), FAILED("failed"), STOPPED("stopped");
	
	private String status;
	
	JobStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return status;
	}
}
