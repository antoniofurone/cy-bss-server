package org.cysoft.bss.server;

import org.cysoft.bss.core.model.ServerQueueItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurchaseExecutor extends ItemExecutor {
	
	private static final Logger logger = LoggerFactory.getLogger(PurchaseExecutor.class);
	
	public PurchaseExecutor(ServerProcess parent, ServerQueueItem item) {
		super(parent, item);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected boolean exec() {
		// TODO Auto-generated method stub
		logger.info("exec() >>");
		
		lock();
		ServerQueueItem item=reloadItem();
		if (item.getServerId()!=getParent().server.getId())
			return false;
		
		startExecution();
		
		
		endExecution(ItemExecutor.RESULT_ITEM_OK);
		
		logger.info("exec() <<");
		return true;
	}

	@Override
	public Void getRawResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void setRawResult(Void arg0) {
		// TODO Auto-generated method stub

	}

}
