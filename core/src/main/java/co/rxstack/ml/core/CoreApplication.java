package co.rxstack.ml.core;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.UUID;

import co.rxstack.ml.common.model.Ticket;
import co.rxstack.ml.core.jobs.service.impl.JobService;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableDiscoveryClient
@SpringBootApplication
@EnableScheduling
public class CoreApplication implements CommandLineRunner {

	private JobService jobService;

	@Autowired
	public CoreApplication(JobService jobService) {
		this.jobService = jobService;
	}

	public static void main(String[] args) {
		new SpringApplicationBuilder()
			.bannerMode(Banner.Mode.CONSOLE)
			.sources(CoreApplication.class)
			.run(args);
	}

	@Override
	public void run(String... strings) throws Exception {
	}
	
	@Component
	public class ScheduledTask {

		private final Logger logger = getLogger(ScheduledTask.class);
		
		@Scheduled(fixedRate = 15000, initialDelay = 10000)
		public void doTask() {
			execute();
		}

		private void execute() {
			logger.info("ScheduledTask.execute() fired");
			Ticket ticket = new Ticket(UUID.randomUUID().toString());
			ticket.setType(Ticket.Type.INDEXING);
			jobService.startJob(ticket);
		}
	}
}
