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

	private ServerProcess parent=null;
	private ServerQueueItem item=null;
	
	public ItemExecutor(ServerProcess parent, ServerQueueItem item){
		super();
		this.parent=parent;
		this.item=item;
	}
	
	protected void startExecution(){
		parent.serverService.startRunQueueItem(item.getId());
	}
		
	protected void lock(){
		parent.serverService.lockQueueItem(item.getId(), parent.server.getId());
	}
	
	protected void endExecution(String result){
		parent.serverService.endRunQueueItem(item.getId(), result);
	}

	protected ServerProcess getParent() {
		return parent;
	}

	protected ServerQueueItem getItem() {
		return item;
	}
	
	protected ServerQueueItem reloadItem(){
		item=parent.serverService.getQueueItem(item.getId());
		return item;
	}
	
}
