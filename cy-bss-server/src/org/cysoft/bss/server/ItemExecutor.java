package org.cysoft.bss.server;

import java.util.concurrent.ForkJoinTask;

import org.cysoft.bss.core.model.ServerQueueItem;

public abstract class ItemExecutor extends ForkJoinTask<Void>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected ServerProcess parent=null;
	protected ServerQueueItem item=null;
	
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
	
	protected void endExecution(long itemId,String result){
		parent.serverService.endCommand(item.getId(), result);
	}
}
