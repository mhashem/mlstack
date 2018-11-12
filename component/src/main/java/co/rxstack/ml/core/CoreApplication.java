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
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@EnableDiscoveryClient
@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = {"co.rxstack.ml.core.jobs.model", "co.rxstack.ml.faces.model"})
@EnableJpaRepositories(basePackages = {"co.rxstack.ml.core.jobs.dao", "co.rxstack.ml.faces.dao"})
@ComponentScan(basePackages = {"co.rxstack.ml.core", "co.rxstack.ml.tensorflow.service",
	"co.rxstack.ml.faces.service"})
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
		
		@Scheduled(fixedDelay = 15000, initialDelay = 10000)
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
	
	@Component
	public class ScheduledTrainingTask {

		private final Logger logger = getLogger(ScheduledTrainingTask.class);
		
		@Scheduled(fixedDelay = 60000, initialDelay = 10000)
		public void doTask() {
			logger.info("============== ScheduledTrainingTask  fired ==============");
			Ticket ticket = new Ticket(UUID.randomUUID().toString());
			ticket.setType(Ticket.Type.TRAINING);
			jobService.startJob(ticket);
		}
		
	}
}
