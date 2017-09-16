package org.cysoft.bss.core.dao;

import java.util.List;

import org.cysoft.bss.core.common.CyBssException;
import org.cysoft.bss.core.model.Ticket;
import org.cysoft.bss.core.model.TicketCategory;
import org.cysoft.bss.core.model.TicketStatus;
import org.cysoft.bss.core.model.TicketStatusTrace;

public interface TicketDao {
	public long add(Ticket ticket) throws CyBssException;
	public Ticket get(long id,long langId);
	public void update(long id,Ticket ticket) throws CyBssException;
	public void remove(long id) throws CyBssException;
	public void removeLocation(long id) throws CyBssException;
	public void removeTrace(long id) throws CyBssException;
	
	
	public List<Ticket> find(String text,long categoryId,long statusId,long personId,
			String fromDate,String toDate,long langId) throws CyBssException;
	
	public List<TicketCategory> getCategoryAll(long langId);
	public List<TicketStatus> getStatusAll(long langId);
	public List<TicketStatus> getNextStates(long stateId,long langId);
	public List<TicketStatusTrace> getStatusTrace(long id, long langId);
	public void changeStatus(long id,long newStatus) throws CyBssException;
	public void addTrace(long id,long oldStatus,long newStatus,long userId,String note) throws CyBssException;
	
}
