package org.cysoft.bss.server;

import java.util.concurrent.ForkJoinTask;

import org.cysoft.bss.core.model.ServerQueueItem;

public abstract class ItemExecutor extends ForkJoinTask<Void>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected static final String RESULT_ITEM_OK="OK";
	protected static final String RESULT_ITEM_NOK="NOK";

	private ServerProcess parentProcess=null;
	private ServerQueueItem item=null;
	
	public ItemExecutor(ServerProcess parentProcess, ServerQueueItem item){
		super();
		this.parentProcess=parentProcess;
		this.item=item;
	}
	
	protected void startExecution(){
		parentProcess.getServerService().startRunQueueItem(item.getId());
	}
		
	protected void lock(){
		parentProcess.getServerService().lockQueueItem(item.getId(), 
				parentProcess.getServer().getId());
	}
	
	protected void endExecution(String result){
		parentProcess.getServerService().endRunQueueItem(item.getId(), result);
	}

	protected ServerProcess getParentProcess() {
		return parentProcess;
	}

	protected ServerQueueItem getItem() {
		return item;
	}
	
	protected ServerQueueItem reloadItem(){
		item=parentProcess.getServerService().getQueueItem(item.getId());
		return item;
	}
	
}
