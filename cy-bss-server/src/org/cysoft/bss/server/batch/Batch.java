package org.cysoft.bss.server.batch;
import org.cysoft.bss.core.common.CyBssException;

public interface Batch {

	public void exec() throws CyBssException;
}
