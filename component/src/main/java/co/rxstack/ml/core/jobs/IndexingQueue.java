package co.rxstack.ml.core.jobs;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import co.rxstack.ml.common.model.Ticket;

import com.google.common.collect.ImmutableList;
import org.springframework.stereotype.Component;

@Component
public class IndexingQueue {

	private List<Ticket> tickets = new CopyOnWriteArrayList<>();

	public void push(Ticket ticket) {
		tickets.add(ticket);
	}

	public List<Ticket> getTickets() {
		return ImmutableList.copyOf(tickets);
	}
	
	public synchronized void clear() {
		tickets.clear();
	}

}
