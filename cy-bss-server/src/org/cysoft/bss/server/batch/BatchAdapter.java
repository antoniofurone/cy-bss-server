package org.cysoft.bss.server.batch;

import org.cysoft.bss.server.ServerProcess;

public abstract class BatchAdapter implements Batch{

	protected ServerProcess parentProcess=null;
	
	public BatchAdapter(ServerProcess parentProcess){
		this.parentProcess=parentProcess;
	}
	
	
}
